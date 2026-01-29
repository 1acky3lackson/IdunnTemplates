package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.jackyblackson.idunntemplates.permission.PermissionNames;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.jackyblackson.idunntemplates.core.util.PermissionUtil.hasRecursivePermission;

public class TemplateManager {

    private final TemplateStorage storage;
    private TemplateUpdater updater;
    private final Map<UUID, Template> idCache = new ConcurrentHashMap<>();
    private final Map<String, Template> pathCache = new ConcurrentHashMap<>();

    public TemplateManager(TemplateStorage storage) {
        this.storage = storage;
        IdunnTemplates.getInstance().getLogger().info("Scanning for all templates...");
        reloadTemplates();
        IdunnTemplates.getInstance().getLogger().info("Finished, get " + idCache.size() + " unique templates.");
    }
    
    public void reloadTemplates() {
        idCache.clear();
        pathCache.clear();
        try {
            java.util.List<Template> loaded = storage.loadAllTemplates();
            for (Template t : loaded) {
                // Cache by ID
                idCache.put(t.getId(), t);
                
                // Cache by Normalized Path (User Friendly)
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
     * @param worldId The world UUID
     * @param minX Min X of the query box
     * @param minY Min Y of the query box
     * @param minZ Min Z of the query box
     * @param maxX Max X of the query box
     * @param maxY Max Y of the query box
     * @param maxZ Max Z of the query box
     * @return List of overlapping templates
     */
    public List<Template> getIntersectingTemplates(UUID worldId, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        List<Template> intersecting = new java.util.ArrayList<>();

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
     * Commits a new version to an existing template.
     */
    public void commitTemplate(Player player, Template template, String message) throws Exception {
        InstanceRepository instanceRepository = IdunnTemplates.getInstance().getInstanceRepository();
        TemplateUpdater templateUpdater = IdunnTemplates.getInstance().getTemplateUpdater();

        TemplateMetadata meta = template.getMetadata();
        org.bukkit.World sourceWorld = Bukkit.getWorld(meta.getWorldId());

        // 6. Create Version (Standard Flow)
        String versionId = generateVersionId();
        TemplateVersion version = new TemplateVersion(versionId, player.getUniqueId(), message);

        // V2: Handle Staged Changes & Locking
        if (meta.getStagedChanges() != null && !meta.getStagedChanges().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Processing staged changes...");
            
            // 1. Merge Staged Data
            java.util.List<Instance> added = meta.getStagedChanges().getAddedInstances();
            for (Instance inst : added) {
                // Add to childTemplateInstances
                meta.getChildTemplateInstances()
                    .computeIfAbsent(inst.getTemplateId(), k -> new java.util.ArrayList<>())
                    .add(inst);
                
                // Ensure the child instance knows it's embedded (redundant if set during place, but safe)
                inst.setEmbeddedInTemplateId(template.getId());
                // Note: We don't save instance here, it was saved during place.
            }
            
            // 2. Clear Staging
            meta.getStagedChanges().clear();
            
            // 3. Force Update Child Instances
            // Iterate ALL child instances (newly merged + existing) and update them in the world if needed
            // This ensures the captured schematic contains the latest version of children.
            player.sendMessage(ChatColor.YELLOW + "Verifying child instance versions...");
            int updatedCount = 0;
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(meta.getWorldId());
            if (world != null) {
                try (com.sk89q.worldedit.EditSession session = com.sk89q.worldedit.WorldEdit.getInstance().newEditSession(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world))) {
                    for (java.util.List<Instance> childList : meta.getChildTemplateInstances().values()) {
                        for (Instance childInst : childList) {
                                Template childTemplate = getTemplate(childInst.getTemplateId());
                                if (childTemplate != null) {
                                    TemplateVersion latestChildVer = childTemplate.getLatestVersion();
                                    if (latestChildVer != null && !childInst.getCurrentVersionId().equals(latestChildVer.getVersionId())) {
                                        // Force update this child instance in the world
                                        IdunnTemplates.getInstance().getLogger().info("[TemplateManager] [commitTemplate] updating instance " + childInst.getId() + " of template " + childTemplate.getPath() + " due to template " + template.getPath() + " committed by " + player.getName() + " with commit message " + message);
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
            
            // 4. Unlock
            meta.setLocked(false);


            
            // 5. Save Metadata (Merged & Unlocked)
            saveTemplateMetadata(template);
        } else {
            // Even if no staged changes, we should unlock if it was locked for some reason
            if (meta.isLocked()) {
                meta.setLocked(false);
                saveTemplateMetadata(template);
            }
        }

        // capture
        // 3. Calculate Region and Origin
        BlockVector3 min = BlockVector3.at(meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
        BlockVector3 max = min.add(meta.getWidth() - 1, meta.getHeight() - 1, meta.getLength() - 1);

        // Recover origin offset from previous version
        BlockVector3 originOffset = BlockVector3.ZERO;
        TemplateVersion latest = template.getLatestVersion();
        if (latest != null) {
            try {
                java.io.File schemFile = new java.io.File(template.getDirectory(), latest.getVersionId() + ".schem");
                if (schemFile.exists()) {
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

        // 4. Capture
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

         // 7. Save Version
        storage.saveTemplateVersion(template, version, clipboard);
        
        // Update cache (ensure metadata/versions are up to date in cache objects)
        // Since 'template' reference is what we modify, and it's in cache, we might be fine.
        // But if storage returned a new object or we want to be safe:
        // (TemplateStorage.saveTemplateVersion updates the passed template object in memory too? Yes, see code)
        
         // 8. Trigger Update
        TemplateVersion newVer = template.getLatestVersion();
        List<Instance> allLoaded = instanceRepository.getAllLoadedInstances();

        // Filter by template ID
        List<Instance> targets = allLoaded.stream()
                .filter(i -> i.getTemplateId().equals(template.getId()))
                .filter(i ->
                        i.isWild()
                        || ((i.getEmbeddedTemplate() != null) && !(i.getEmbeddedTemplate().isLocked()))
                )
                .collect(Collectors.toList());

        player.sendMessage(ChatColor.YELLOW + "Found " + targets.size() + " active instances. Updating...");

        templateUpdater.updateInstances(template, newVer, targets);

        player.sendMessage(ChatColor.GREEN + "Update process finished.");
        
        // 9. Trigger Cascading Update (V2: Delayed Trigger)
        // Now that we've committed (and unlocked), we can notify parents.
        if (updater != null && updater.getCascadingUpdateManager() != null) {
//            updater.getCascadingUpdateManager().scheduleUpdate(template.getId()); // Wait, this schedules update for THIS template?
            // No, scheduleUpdate(parentId) schedules update for PARENT.
            // But here, WE are the child (potentially) of someone else.
            // So we need to notify OUR parents.
            
            Map<UUID, List<Instance>> parents = meta.getParentTemplateInstances();
            if (!parents.isEmpty()) {
                player.sendMessage(ChatColor.AQUA + "Triggering cascading updates for " + parents.size() + " parent templates...");
                for (UUID parentId : parents.keySet()) {
                    updater.getCascadingUpdateManager().scheduleUpdate(parentId);
                }
            }
        }
    }

    /**
     * Automated commit by the system (e.g., Cascading Update).
     */
    public void commitTemplateSystem(Template template, String message) throws Exception {
        InstanceRepository instanceRepository = IdunnTemplates.getInstance().getInstanceRepository();
        TemplateUpdater templateUpdater = IdunnTemplates.getInstance().getTemplateUpdater();

        // 0. Capture current state from World (Master Region)
        // We need to create a Clipboard from the Master Region in the world.
        TemplateMetadata meta = template.getMetadata();
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(meta.getWorldId());
        if (world == null) {
            IdunnTemplates.getInstance().getLogger().warning("Cannot auto-commit template " + template.getName() + ": World not loaded.");
            return;
        }
        
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

        // 1. Create Version
        String versionId = generateVersionId();
        // Use a System UUID (all zeros) or similar to indicate system
        UUID systemUUID = new UUID(0, 0); 
        TemplateVersion version = new TemplateVersion(versionId, systemUUID, message);

        // 2. Save Version
        storage.saveTemplateVersion(template, version, clipboard);

        // 3. Trigger Update for its instances
        TemplateVersion newVer = template.getLatestVersion();
        List<Instance> allLoaded = instanceRepository.getAllLoadedInstances();
        List<Instance> targets = allLoaded.stream()
                .filter(i -> i.getTemplateId().equals(template.getId()))
                .collect(Collectors.toList());

        if (!targets.isEmpty()) {
            IdunnTemplates.getInstance().getLogger().info("Auto-commit triggering update for " + targets.size() + " instances of " + template.getName());
            templateUpdater.updateInstances(template, newVer, targets);
        }
    }
    
    public Template getTemplate(String path) {
        // path is user input: users/jacky/mytmpl
        // We check pathCache
        return pathCache.get(path);
    }
    
    public Template getTemplate(UUID id) {
        return idCache.get(id);
    }
    
    public Clipboard getTemplateClipboard(Template template, TemplateVersion version) throws IOException {
        return storage.loadSchematic(template, version);
    }

    /**
     * Saves a new template from a player's clipboard.
     * @param player The player creating the template.
     * @param name The name of the template (final folder will be _name).
     * @param subPath Optional subpath. If null/empty, defaults to "users/<playername>".
     * @param clipboard The WorldEdit clipboard.
     * @return The created Template.
     * @throws IllegalArgumentException If permissions are missing or name is invalid.
     * @throws Exception If IO fails.
     */
    public Template createTemplate(Player player, String name, String subPath, Clipboard clipboard) throws Exception {
        // 1. Determine Path and Check Permissions
        String finalPath;
        if (subPath == null || subPath.trim().isEmpty()) {
            if (!player.hasPermission(PermissionNames.Templates.createPersonal)) {
                throw new SecurityException("You do not have permission to save personal templates.");
            }
            finalPath = "users/" + player.getName();
        } else {
            // Path permission check: idunn.template.save.dir1.dir2
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
        TemplateVersion version = new TemplateVersion(versionId, player.getUniqueId(), "Initial creation");

        // 4. Save
        Template t = storage.saveNewTemplate(finalPath, name, metadata, clipboard, version);

        // 5. Update Cache
        idCache.put(t.getId(), t);
        pathCache.put(normalizePath(t.getPath()), t);

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
        long now = System.currentTimeMillis();
        return Long.toString(now);
    }
}