package com.jackyblackson.idunntemplates.core.store;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.store.dao.InstanceDao;
import com.jackyblackson.idunntemplates.manager.DatabaseManager;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseInstanceRepository implements InstanceRepository {

    private final DatabaseManager databaseManager;
    private final Logger logger;

    // 内存缓存: WorldUUID -> (ChunkKey -> List<Instance>)
    // 我们保留这个缓存是为了 getAllLoadedInstances() 能瞬间返回，而不是去查全库
    private final Map<UUID, Map<Long, List<Instance>>> cache = new ConcurrentHashMap<>();

    // 辅助锁对象，防止并发加载同一个 Chunk 时产生竞态条件
    private final Object lock = new Object();

    public DatabaseInstanceRepository(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    // 获取 DAO 的快捷方式
    private InstanceDao getDao() {
        return (InstanceDao) databaseManager.getInstanceDao();
    }

    /**
     * 将 Chunk 坐标转换为 long 类型的 Key (Minecraft 标准做法)
     */
    private long getChunkKey(int x, int z) {
        return (long) x & 0xffffffffL | ((long) z & 0xffffffffL) << 32;
    }

    /**
     * 根据 Block 坐标获取 Chunk Key
     */
    private long getChunkKeyFromBlock(int x, int z) {
        return getChunkKey(x >> 4, z >> 4);
    }

    @Override
    public CompletableFuture<Void> saveInstance(Instance instance) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 1. 写入数据库 (Create Or Update)
                getDao().createOrUpdate(instance);

                // 2. 更新缓存 (如果该实例所在的 Chunk 已经被加载了)
                updateCacheIfLoaded(instance);

            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to save instance " + instance.getId() + " to database", e);
                throw new RuntimeException(e);
            }
        }); // 默认使用 ForkJoinPool，如果你有专门的 ioExecutor 也可以传进去
    }

    /**
     * 辅助方法：仅当实例所在的 Chunk 在缓存中存在时，才更新缓存。
     * 避免“保存了一个远处的实例，结果意外地把它加载到了内存里”。
     */
    private void updateCacheIfLoaded(Instance instance) {
        UUID worldId = instance.getWorldId();
        long key = getChunkKeyFromBlock(instance.getX(), instance.getZ());

        Map<Long, List<Instance>> worldCache = cache.get(worldId);
        if (worldCache != null) {
            List<Instance> chunkInstances = worldCache.get(key);
            if (chunkInstances != null) {
                synchronized (chunkInstances) {
                    // 替换旧对象或添加新对象
                    boolean found = false;
                    for (int i = 0; i < chunkInstances.size(); i++) {
                        if (chunkInstances.get(i).getId().equals(instance.getId())) {
                            chunkInstances.set(i, instance);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        chunkInstances.add(instance);
                    }
                }
            }
        }
    }

    @Override
    public CompletableFuture<List<Instance>> loadInstancesForChunk(UUID worldId, int chunkX, int chunkZ) {
        return CompletableFuture.supplyAsync(() -> {
            long key = getChunkKey(chunkX, chunkZ);

            // 1. 检查缓存
            Map<Long, List<Instance>> worldCache = cache.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>());
            if (worldCache.containsKey(key)) {
                return worldCache.get(key);
            }

            // 2. 数据库查询
            // 计算 Chunk 的世界坐标边界
            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            int maxX = minX + 15;
            int maxZ = minZ + 15;

            try {
                // 使用 Dao 的空间查询
                List<Instance> loaded = getDao().findInRegion(worldId, minX, minZ, maxX, maxZ);

                // 3. 放入缓存
                // 使用 CopyOnWriteArrayList 或者同步列表，防止遍历时被修改
                worldCache.put(key, Collections.synchronizedList(new ArrayList<>(loaded)));

                return loaded;
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load instances for chunk " + chunkX + "," + chunkZ, e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public void unloadInstancesForChunk(UUID worldId, int chunkX, int chunkZ) {
        // 数据库模式下，卸载就是简单地从内存 Map 中移除。
        // 不需要 "flush" 操作，因为 saveInstance 已经实时写入数据库了。
        long key = getChunkKey(chunkX, chunkZ);
        Map<Long, List<Instance>> worldCache = cache.get(worldId);
        if (worldCache != null) {
            worldCache.remove(key);
        }
    }

    @Override
    public void flushPartition(UUID worldId, int partitionX, int partitionZ) {
        // Database 模式不需要手动 flush 分区
        // 因为所有 saveInstance 都是原子性写入数据库的
        // 留空即可，或者打印 debug 日志
    }

    @Override
    public List<Instance> getAllLoadedInstances() {
        // 这里的语义是：获取所有“缓存中”的实例
        // 也就是所有“当前已加载 Chunk”里的实例
        List<Instance> all = new ArrayList<>();
        for (Map<Long, List<Instance>> worldMap : cache.values()) {
            for (List<Instance> list : worldMap.values()) {
                if (list != null) {
                    synchronized (list) {
                        // 过滤掉软删除的 (虽然 DB 查询时已经过滤了，但内存中可能刚被标记)
                        for (Instance i : list) {
                            if (!i.isDeleted()) {
                                all.add(i);
                            }
                        }
                    }
                }
            }
        }
        return all;
    }

    @Override
    public CompletableFuture<List<Instance>> getActiveInstancesInWorld(UUID worldId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getDao().queryBuilder()
                        .where()
                        .eq("world_id", worldId)
                        .and()
                        .isNull("deleted_timestamp")
                        .query();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to query active instances in world " + worldId, e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<Void> hardDelete(Instance instance) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 1. 数据库物理删除
                getDao().delete(instance);

                // 2. 清理缓存
                removeFromCache(instance);

            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete instance " + instance.getId(), e);
                throw new RuntimeException(e);
            }
        });
    }

    private void removeFromCache(Instance instance) {
        UUID worldId = instance.getWorldId();
        long key = getChunkKeyFromBlock(instance.getX(), instance.getZ());

        Map<Long, List<Instance>> worldCache = cache.get(worldId);
        if (worldCache != null) {
            List<Instance> list = worldCache.get(key);
            if (list != null) {
                synchronized (list) {
                    list.removeIf(i -> i.getId().equals(instance.getId()));
                }
            }
        }
    }

    // 如果需要清理资源
    public void shutdown() {
        // 如果内部维护了线程池，可以在这里关闭
        // 数据库连接的关闭通常由 DatabaseManager.close() 处理
        cache.clear();
    }
}
