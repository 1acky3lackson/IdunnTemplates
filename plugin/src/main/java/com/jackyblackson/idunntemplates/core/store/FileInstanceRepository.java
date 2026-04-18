package com.jackyblackson.idunntemplates.core.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jackyblackson.idunntemplates.core.domain.Instance;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class FileInstanceRepository implements InstanceRepository {

    private final File rootDirectory;
    private final Gson gson;
    private final ExecutorService ioExecutor;
    private final Logger logger;
    
    // Cache: WorldUUID -> (PartitionKey -> List<Instance>)
    // PartitionKey string format: "x.z"
    // Using a simple in-memory cache for now. In production, consider memory management.
    private final Map<UUID, Map<String, List<Instance>>> cache = new ConcurrentHashMap<>();
    
    // Reference Counting: WorldUUID -> (PartitionKey -> Count)
    private final Map<UUID, Map<String, java.util.concurrent.atomic.AtomicInteger>> partitionRefCounts = new ConcurrentHashMap<>();

    private static final int PARTITION_SIZE = 8; // 8x8 chunks

    public FileInstanceRepository(File rootDirectory, Logger logger) {
        this.rootDirectory = rootDirectory;
        if (!this.rootDirectory.exists()) {
            this.rootDirectory.mkdirs();
        }
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.ioExecutor = Executors.newSingleThreadExecutor(); // Sequential IO to avoid file locking issues on same partition
        this.logger = logger;
    }

    private String getPartitionKey(int chunkX, int chunkZ) {
        int px = Math.floorDiv(chunkX, PARTITION_SIZE);
        int pz = Math.floorDiv(chunkZ, PARTITION_SIZE);
        return px + "." + pz;
    }
    
    private String getPartitionKeyFromBlock(int x, int z) {
        return getPartitionKey(x >> 4, z >> 4);
    }

    @Override
    public CompletableFuture<Void> saveInstance(Instance instance) {
        return CompletableFuture.runAsync(() -> {
            UUID worldId = instance.getWorldId();
            String key = getPartitionKeyFromBlock(instance.getX(), instance.getZ());
            
            // Update cache first
            cache.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>())
                 .computeIfAbsent(key, k -> new ArrayList<>());
            
            Map<String, List<Instance>> worldCache = cache.get(worldId);
            List<Instance> instances = worldCache.get(key);
            if (instances == null) {
                instances = loadPartitionFromDisk(worldId, key);
                worldCache.put(key, instances);
            }
            
            // Prevent duplication: Update if exists, otherwise add
            boolean found = false;
            for (int i = 0; i < instances.size(); i++) {
                if (instances.get(i).getId().equals(instance.getId())) {
                    instances.set(i, instance);
                    found = true;
                    break;
                }
            }
            if (!found) {
                instances.add(instance);
            }
            
            // Save to disk
            savePartitionToDisk(worldId, key, instances);
            
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<List<Instance>> loadInstancesForChunk(UUID worldId, int chunkX, int chunkZ) {
        return CompletableFuture.supplyAsync(() -> {
            String key = getPartitionKey(chunkX, chunkZ);
            
            // Ref Count Management
            Map<String, java.util.concurrent.atomic.AtomicInteger> worldCounts = partitionRefCounts.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>());
            worldCounts.computeIfAbsent(key, k -> new java.util.concurrent.atomic.AtomicInteger(0)).incrementAndGet();

            Map<String, List<Instance>> worldCache = cache.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>());
            
            if (worldCache.containsKey(key)) {
                return worldCache.get(key);
            }
            
            List<Instance> loaded = loadPartitionFromDisk(worldId, key);
            worldCache.put(key, loaded);
            return loaded;
        }, ioExecutor);
    }
    
    @Override
    public void unloadInstancesForChunk(UUID worldId, int chunkX, int chunkZ) {
        CompletableFuture.runAsync(() -> {
            String key = getPartitionKey(chunkX, chunkZ);
            Map<String, java.util.concurrent.atomic.AtomicInteger> worldCounts = partitionRefCounts.get(worldId);
            if (worldCounts != null) {
                java.util.concurrent.atomic.AtomicInteger count = worldCounts.get(key);
                if (count != null) {
                    int current = count.decrementAndGet();
                    if (current <= 0) {
                        // Unload from cache
                        Map<String, List<Instance>> worldCache = cache.get(worldId);
                        if (worldCache != null) {
                            worldCache.remove(key);
                        }
                        // Reset count to 0 to be safe
                        if (current < 0) count.set(0);
                    }
                }
            }
        }, ioExecutor);
    }
    
    @Override
    public void flushPartition(UUID worldId, int partitionX, int partitionZ) {
        String key = partitionX + "." + partitionZ;
        Map<String, List<Instance>> worldCache = cache.get(worldId);
        if (worldCache != null && worldCache.containsKey(key)) {
             // Submit to executor
             ioExecutor.submit(() -> savePartitionToDisk(worldId, key, worldCache.get(key)));
        }
    }

    private synchronized List<Instance> loadPartitionFromDisk(UUID worldId, String key) {
        File worldDir = new File(rootDirectory, worldId.toString());
        File partitionFile = new File(worldDir, "r." + key + ".json");
        
        if (!partitionFile.exists()) {
            return new ArrayList<>();
        }
        
        try (FileReader reader = new FileReader(partitionFile)) {
            Type listType = new TypeToken<ArrayList<Instance>>(){}.getType();
            List<Instance> list = gson.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            logger.severe("Failed to load instance partition " + partitionFile.getPath() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private synchronized void savePartitionToDisk(UUID worldId, String key, List<Instance> instances) {
        File worldDir = new File(rootDirectory, worldId.toString());
        if (!worldDir.exists()) worldDir.mkdirs();
        
        File partitionFile = new File(worldDir, "r." + key + ".json");
        
        try (FileWriter writer = new FileWriter(partitionFile)) {
            gson.toJson(instances, writer);
        } catch (IOException e) {
            logger.severe("Failed to save instance partition " + partitionFile.getPath() + ": " + e.getMessage());
        }
    }
    
    @Override
    public List<Instance> getAllLoadedInstances() {
        List<Instance> all = new ArrayList<>();
        for (Map<String, List<Instance>> worldMap : cache.values()) {
            for (List<Instance> list : worldMap.values()) {
                all.addAll(list.stream().filter(ins -> !ins.isDeleted()).toList());
            }
        }
        return all;
    }

    @Override
    public CompletableFuture<List<Instance>> getActiveInstancesInWorld(UUID worldId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Instance> all = new ArrayList<>();
            Map<String, List<Instance>> worldCache = cache.get(worldId);
            if (worldCache != null) {
                for (List<Instance> list : worldCache.values()) {
                    all.addAll(list.stream().filter(instance -> !instance.isDeleted()).toList());
                }
            }
            return all;
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<Void> hardDelete(Instance instance) {
        return CompletableFuture.runAsync(() -> {
            UUID worldId = instance.getWorldId();
            String key = getPartitionKeyFromBlock(instance.getX(), instance.getZ());
            
            // Update cache first
            cache.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>());
            
            Map<String, List<Instance>> worldCache = cache.get(worldId);
            List<Instance> instances = worldCache.get(key);
            if (instances == null) {
                instances = loadPartitionFromDisk(worldId, key);
                worldCache.put(key, instances);
            }
            
            // Remove
            boolean removed = instances.removeIf(i -> i.getId().equals(instance.getId()));
            
            if (removed) {
                // Save to disk
                savePartitionToDisk(worldId, key, instances);
            }
        }, ioExecutor);
    }
    
    public void shutdown() {
        ioExecutor.shutdown();
    }
}
