package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.jackyblackson.idunntemplates.core.util.PermissionUtil;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import com.jackyblackson.idunntemplates.util.EntityHelper;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.jackyblackson.idunntemplates.core.util.PermissionUtil.hasRecursivePermission;

public class TemplateManager {

    private final TemplateStorage storage;
//    private final PluginSnapshotManager snapshotManager;
    private TemplateUpdater updater;
    private final Map<UUID, Template> idCache = new ConcurrentHashMap<>();
    private final Map<String, Template> pathCache = new ConcurrentHashMap<>();

//    public PluginSnapshotManager getSnapshotManager() {
//        return snapshotManager;
//    }

    public TemplateManager(TemplateStorage storage) {
        this.storage = storage;
        // [新增] 初始化 SnapshotManager
//        this.snapshotManager = new PluginSnapshotManager(IdunnTemplates.getInstance());
        IdunnTemplates.getInstance().getLogger().info("Scanning for all templates...");
        reloadTemplates();
        IdunnTemplates.getInstance().getLogger().info("Finished, get " + idCache.size() + " unique templates.");
    }

    public void reloadTemplates() {
        idCache.clear();
        pathCache.clear();
        try {
            java.util.List<Template> loaded = storage.loadAllTemplates();
//            snapshotManager.clearTasks();
            // [新增] 异步批量检查所有模版的缩略图
            // 避免在服务器启动时阻塞主线程，等待所有请求完成
//            Bukkit.getScheduler().runTaskAsynchronously(IdunnTemplates.getInstance(), () -> {
//                int checkedCount = 0;
//                for (Template t : loaded) {
//                    // checkAndGenerateThumbnails 内部本身也是异步安全的，
//                    // 但这里我们在一个大的异步任务里循环，减少调度开销
////                    snapshotManager.checkAndGenerateThumbnails(t, false);
//                    checkedCount++;
//                }
//                IdunnTemplates.getInstance().getLogger().info("Thumbnail check scheduled for " + checkedCount + " templates.");
//            });

            for (Template t : loaded) {
                idCache.put(t.getId(), t);
                String normalized = normalizePath(t.getPath());
                pathCache.put(normalized, t);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String normalizePath(String rawPath) {
        // rawPath: users/jacky/_mytmpl
        // wanted: users/jacky/mytmpl
        int lastSlash = rawPath.lastIndexOf('/');
        if (lastSlash >= 0) {
            String parent = rawPath.substring(0, lastSlash);
            String name = rawPath.substring(lastSlash + 1);
            if (name.startsWith("_")) name = name.substring(1);
            return parent + "/" + name;
        } else {
            if (rawPath.startsWith("_")) return rawPath.substring(1);
            return rawPath;
        }
    }

    public java.util.Collection<Template> getTemplates() {
        return java.util.Collections.unmodifiableCollection(idCache.values());
    }

    /**
     * Finds templates whose master region intersects with the given world bounds.
     */
    public List<Template> getIntersectingTemplates(UUID worldId, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        List<Template> intersecting = new java.util.ArrayList<>();

        // 这里依然在内存中遍历，如果 Template 数量巨大（如 >1万），建议改为数据库的空间查询
        // 但考虑到 Template 通常是手工创建的，数量不会太多，内存遍历尚可接受。
        for (Template template : idCache.values()) {
            TemplateMetadata meta = template.getMetadata();

            // Check World
            if (!meta.getWorldId().equals(worldId)) {
                continue;
            }

            // Template Region (Master Region)
            int tMinX = meta.getAnchorX();
            int tMinY = meta.getAnchorY();
            int tMinZ = meta.getAnchorZ();
            int tMaxX = tMinX + meta.getWidth() - 1;
            int tMaxY = tMinY + meta.getHeight() - 1;
            int tMaxZ = tMinZ + meta.getLength() - 1;

            // Check Overlap
            boolean overlap = (minX <= tMaxX && maxX >= tMinX) &&
                    (minY <= tMaxY && maxY >= tMinY) &&
                    (minZ <= tMaxZ && maxZ >= tMinZ);

            if (overlap) {
                intersecting.add(template);
            }
        }
        return intersecting;
    }

    public void setUpdater(TemplateUpdater updater) {
        this.updater = updater;
    }

    /**
     * [DB Compatible] 从 childInstanceMap 中移除指定 IDs 的实例，并在数据库中执行软删除
     */
    public void removeInstancesFromMap(Map<UUID, List<Instance>> childInstanceMap, List<String> deleteIds) {
        if (childInstanceMap == null || deleteIds == null || deleteIds.isEmpty()) {
            return;
        }

        InstanceRepository instanceRepository = IdunnTemplates.getInstance().getInstanceRepository();
        Set<String> idSet = new HashSet<>(deleteIds);

        Iterator<Map.Entry<UUID, List<Instance>>> iterator = childInstanceMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, List<Instance>> entry = iterator.next();
            List<Instance> instances = entry.getValue();

            if (instances != null) {
                instances.removeIf(instance -> {
                    boolean removed = idSet.contains(instance.getId());
                    if (removed) {
                        // 1. 内存中移除 (当前操作)

                        // 2. [新增] 数据库持久化：标记为删除
                        // 在新架构中，childTemplateInstances 是通过 SQL 查询动态生成的
                        // 如果不更新数据库，下次加载时它们又会回来。
                        instance.setDeletedTimestamp(System.currentTimeMillis());
                        // 异步保存，不阻塞主线程
                        instanceRepository.saveInstance(instance);

                        // 3. 处理父级模板中的反向引用 (保持内存一致性)
                        // 注意：这部分逻辑可能需要根据实际情况优化，因为数据库外键会自动处理引用完整性，
                        // 但为了当前的内存缓存一致性，我们手动清理。
                        Template childTemplate = getTemplate(instance.getTemplateId()); // 这里获取的是 Instance 对应的那个 Template 定义
                        if (childTemplate != null) {
                            var parentTemplateMap = childTemplate.getMetadata().getParentTemplateInstances();
                            if (parentTemplateMap.containsKey(instance.getEmbeddedInTemplateId())) {
                                var parentTemplateInstanceList = parentTemplateMap.get(instance.getEmbeddedInTemplateId());
                                if (parentTemplateInstanceList != null) {
                                    parentTemplateInstanceList.removeIf(i -> i.getId().equals(instance.getId()));
                                }
                            }
                            // Metadata save is less critical for the instances list now, but good for other fields
                            this.saveTemplateMetadata(childTemplate);
                        }
                    }
                    return removed;
                });

                if (instances.isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Commits a new version to an existing template.
     */
    public void commitTemplate(Player player, Template template, String message) throws Exception {
        InstanceRepository instanceRepository = IdunnTemplates.getInstance().getInstanceRepository();
        TemplateUpdater templateUpdater = IdunnTemplates.getInstance().getTemplateUpdater();

        TemplateMetadata meta = template.getMetadata();
        org.bukkit.World sourceWorld = Bukkit.getWorld(meta.getWorldId());

        // 6. Create Version (Standard Flow)
        String versionId = generateVersionId();
        // [DB Compatible] 使用带 Template 参数的构造函数 (传入 null 或 this，Storage 层会修正)
        // 或者使用新的构造函数：TemplateVersion(Template template, String versionId, UUID submitterId, String message)
        TemplateVersion version = new TemplateVersion(template, versionId, player.getUniqueId(), message);

        // V2: Handle Staged Changes & Locking
        if (meta.getStagedChanges() != null && !meta.getStagedChanges().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Processing staged changes...");

            // 1. Merge Staged Data (Additions)
            java.util.List<Instance> added = meta.getStagedChanges().getAddedInstances();
            for (Instance inst : added) {
                // Add to transient map
                meta.getChildTemplateInstances()
                        .computeIfAbsent(inst.getTemplateId(), k -> new java.util.ArrayList<>())
                        .add(inst);

                // Ensure link
                inst.setEmbeddedInTemplateId(template.getId());

                // [DB Compatible] Ensure active status in DB
                // 虽然 placeInstance 时已经保存，但这里确认一下状态是个好习惯
                instanceRepository.saveInstance(inst);
            }

            // 2. Merge Staged Data (Removals)
            var deleteIds = meta.getStagedChanges().getRemovedInstanceIds();
            if (!deleteIds.isEmpty()) {
                var childInstanceMap = template.getMetadata().getChildTemplateInstances();
                // 这里的 removeInstancesFromMap 已经更新为会操作数据库了
                this.removeInstancesFromMap(childInstanceMap, deleteIds);
                // Metadata update happens below
            }

            // 3. Clear Staging
            meta.getStagedChanges().clear();

            // 4. Force Update Child Instances
            player.sendMessage(ChatColor.YELLOW + "Verifying child instance versions...");
            int updatedCount = 0;
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(meta.getWorldId());
            if (world != null) {
                // ... (Original logic for updating children visuals) ...
                try (com.sk89q.worldedit.EditSession session = com.sk89q.worldedit.WorldEdit.getInstance().newEditSession(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world))) {
                    for (java.util.List<Instance> childList : meta.getChildTemplateInstances().values()) {
                        for (Instance childInst : childList) {
                            Template childTemplate = getTemplate(childInst.getTemplateId());
                            if (childTemplate != null) {
                                TemplateVersion latestChildVer = childTemplate.getLatestVersion();
                                if (latestChildVer != null && !childInst.getCurrentVersionId().equals(latestChildVer.getVersionId())) {
                                    IdunnTemplates.getInstance().getLogger().info("[TemplateManager] Updating child instance " + childInst.getId());
                                    templateUpdater.updateSingleInstance(childTemplate, childInst, latestChildVer, session);
                                    updatedCount++;
                                }
                            }
                        }
                    }
                }
            }
            if (updatedCount > 0) {
                player.sendMessage(ChatColor.GREEN + "Forced update of " + updatedCount + " child instances.");
            }

            // 5. Unlock
            meta.setLocked(false);

            // 6. Save Metadata (Merged & Unlocked)
            // Storage.updateMetadata 会处理 stagedChangesJson 的清空
            saveTemplateMetadata(template);
        } else {
            if (meta.isLocked()) {
                meta.setLocked(false);
                saveTemplateMetadata(template);
            }
        }

        // [Capture Logic - No changes needed, using WE API]
        BlockVector3 min = BlockVector3.at(meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
        BlockVector3 max = min.add(meta.getWidth() - 1, meta.getHeight() - 1, meta.getLength() - 1);

        BlockVector3 originOffset = BlockVector3.ZERO;
        TemplateVersion latest = template.getLatestVersion();
        if (latest != null) {
            try {
                // 注意：这里 template.getDirectory() 已经适配了新的文件路径逻辑
                java.io.File schemFile = new java.io.File(EntityHelper.getDirectory(template), latest.getVersionId() + ".schem");
                if (schemFile.exists()) {
                    // ... (Recover origin logic) ...
                    com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat format = com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats.findByFile(schemFile);
                    if (format == null) format = com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats.findByAlias("sponge");
                    if (format != null) {
                        try (com.sk89q.worldedit.extent.clipboard.io.ClipboardReader reader = format.getReader(new java.io.FileInputStream(schemFile))) {
                            Clipboard oldClip = reader.read();
                            originOffset = oldClip.getOrigin().subtract(oldClip.getRegion().getMinimumPoint());
                        }
                    }
                }
            } catch (Exception e) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.warning_origin"));
                e.printStackTrace();
            }
        }

        BlockVector3 newOrigin = min.add(originOffset);

        com.sk89q.worldedit.regions.CuboidRegion region = new com.sk89q.worldedit.regions.CuboidRegion(
                BukkitAdapter.adapt(sourceWorld), min, max
        );

        com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard clipboard = new com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard(region);
        clipboard.setOrigin(newOrigin);

        try (com.sk89q.worldedit.EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(sourceWorld))) {
            com.sk89q.worldedit.function.operation.ForwardExtentCopy copy = new com.sk89q.worldedit.function.operation.ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            com.sk89q.worldedit.function.operation.Operations.completeLegacy(copy);
        } catch (Exception e) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.failed_capture", e.getMessage()));
            e.printStackTrace();
            return;
        }

        // 7. Save Version to DB & Disk
        storage.saveTemplateVersion(template, version, clipboard);

        // 8. Trigger Update
        TemplateVersion newVer = template.getLatestVersion();
        List<Instance> allLoaded = instanceRepository.getAllLoadedInstances();

        List<Instance> targets = allLoaded.stream()
                .filter(i -> i.getTemplateId().equals(template.getId()))
                .filter(i ->
                        i.isWild()
                                || ((EntityHelper.getEmbeddedTemplate(i) != null) && !(EntityHelper.getEmbeddedTemplate(i).isLocked()))
                )
                .collect(Collectors.toList());

        player.sendMessage(ChatColor.YELLOW + "Found " + targets.size() + " active instances. Updating...");
        templateUpdater.updateInstances(template, newVer, targets);
        player.sendMessage(ChatColor.GREEN + "Update process finished.");

        // 9. Trigger Cascading Update
        if (updater != null && updater.getCascadingUpdateManager() != null) {
            Map<UUID, List<Instance>> parents = meta.getParentTemplateInstances();
            if (!parents.isEmpty()) {
                player.sendMessage(ChatColor.AQUA + "Triggering cascading updates for " + parents.size() + " parent templates...");
                for (UUID parentId : parents.keySet()) {
                    updater.getCascadingUpdateManager().scheduleUpdate(parentId);
                }
            }
        }

        // 10. Generate Thumbnail
//        snapshotManager.checkAndGenerateThumbnails(template, true);
        player.sendMessage(ChatColor.GREEN + "Thumbnail generation queued.");
    }

    public void commitTemplateSystem(Template template, String message) throws Exception {
        // ... (System commit logic, similar update for TemplateVersion constructor) ...
        InstanceRepository instanceRepository = IdunnTemplates.getInstance().getInstanceRepository();
        TemplateUpdater templateUpdater = IdunnTemplates.getInstance().getTemplateUpdater();
        TemplateMetadata meta = template.getMetadata();
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(meta.getWorldId());

        // ... Capture Logic ...
        BlockVector3 min = BlockVector3.at(meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
        BlockVector3 max = min.add(meta.getWidth() - 1, meta.getHeight() - 1, meta.getLength() - 1);
        com.sk89q.worldedit.regions.CuboidRegion region = new com.sk89q.worldedit.regions.CuboidRegion(min, max);
        Clipboard clipboard;
        try (com.sk89q.worldedit.EditSession session = com.sk89q.worldedit.WorldEdit.getInstance().newEditSession(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world))) {
            clipboard = new com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard(region);
            com.sk89q.worldedit.function.operation.ForwardExtentCopy copy = new com.sk89q.worldedit.function.operation.ForwardExtentCopy(
                    session, region, clipboard, region.getMinimumPoint()
            );
            com.sk89q.worldedit.function.operation.Operations.completeLegacy(copy);
        }

        String versionId = generateVersionId();
        UUID systemUUID = new UUID(0, 0);
        // [DB Compatible]
        TemplateVersion version = new TemplateVersion(template, versionId, systemUUID, message);

        storage.saveTemplateVersion(template, version, clipboard);

        TemplateVersion newVer = template.getLatestVersion();
        List<Instance> allLoaded = instanceRepository.getAllLoadedInstances();
        List<Instance> targets = allLoaded.stream()
                .filter(i -> i.getTemplateId().equals(template.getId()))
                .collect(Collectors.toList());

        if (!targets.isEmpty()) {
            templateUpdater.updateInstances(template, newVer, targets);
        }

//        snapshotManager.checkAndGenerateThumbnails(template, true);
    }

    // Getters
    public Template getTemplate(String path) {
        return pathCache.get(path);
    }

    public Template getTemplate(UUID id) {
        return idCache.get(id);
    }

    public Clipboard getTemplateClipboard(Template template, TemplateVersion version) throws IOException {
        return storage.loadSchematic(template, version);
    }

    public Template createTemplate(Player player, String name, String subPath, Clipboard clipboard) throws Exception {
        // 1. Determine Path
        String finalPath;
        if (subPath == null || subPath.trim().isEmpty()) {
            if (!player.hasPermission(PermissionNames.Templates.createPersonal)) {
                throw new SecurityException("You do not have permission to save personal templates.");
            }
            finalPath = "users/" + player.getName();
        } else {
            String permNode = PermissionNames.Templates.createInPath + "." + subPath.replace("/", ".");
            if (!player.hasPermission(permNode)) {
                if (!hasRecursivePermission(player, PermissionNames.Templates.createInPath$R, subPath)) {
                    throw new SecurityException("You do not have permission to save to " + subPath);
                }
            }
            finalPath = subPath;
        }

        // 2. Prepare Metadata
        Region region = clipboard.getRegion();
        BlockVector3 min = region.getMinimumPoint();

        TemplateMetadata metadata = new TemplateMetadata(
                UUID.randomUUID(),
                player.getUniqueId(),
                System.currentTimeMillis(),
                player.getWorld().getUID(),
                min.x(), min.y(), min.z(),
                region.getWidth(), region.getHeight(), region.getLength()
        );

        // 3. Prepare Version
        String versionId = generateVersionId();
        // [DB Compatible] Template is null initially, Storage will set it
        TemplateVersion version = new TemplateVersion(null, versionId, player.getUniqueId(), "Initial creation");

        // 4. Save
        // storage.saveNewTemplate will handle transaction and foreign keys
        Template t = storage.saveNewTemplate(finalPath, name, metadata, clipboard, version);

        // 5. Update Cache
        idCache.put(t.getId(), t);
        pathCache.put(normalizePath(t.getPath()), t);

//        snapshotManager.checkAndGenerateThumbnails(t, true);

        return t;
    }

    public void saveTemplateMetadata(Template template) {
        try {
            storage.updateMetadata(template);
        } catch (IOException e) {
            IdunnTemplates.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Failed to save metadata for template " + template.getId(), e);
        }
    }

    private String generateVersionId() {
        return Long.toString(System.currentTimeMillis());
    }

    // ... (getNextPathsFor and getNextPathsWithPerm remain unchanged) ...
    public List<String> getNextPathsFor(String input) {
        if (input == null) return new ArrayList<>();
        return this.getTemplates().stream()
                .map(t -> {
                    String p = t.getPath();
                    return p.startsWith("_") ? p.substring(1) : p;
                })
                .filter(p -> {
                    if (input.endsWith("/")) {
                        return p.startsWith(input);
                    } else {
                        return p.startsWith(input);
                    }
                })
                .map(p -> {
                    int nextSlashIndex = p.indexOf("/", input.length());
                    if (nextSlashIndex != -1) {
                        return p.substring(0, nextSlashIndex + 1);
                    }
                    return p;
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getNextPathsWithPerm(Player p, String input, String basePerm$R) {
        return getNextPathsFor(input).stream().filter(path -> PermissionUtil.hasRecursivePermission(
                p,
                basePerm$R,
                path
        )).toList();
    }
}