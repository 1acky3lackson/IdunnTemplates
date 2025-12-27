package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    
    public void setUpdater(TemplateUpdater updater) {
        this.updater = updater;
    }

    /**
     * Commits a new version to an existing template.
     */
    public void commitTemplate(Player player, Template template, String message, Clipboard clipboard) throws Exception {
        InstanceRepository instanceRepository = IdunnTemplates.getInstance().getInstanceRepository();
        TemplateUpdater templateUpdater = IdunnTemplates.getInstance().getTemplateUpdater();
        
         // 1. Create Version
        String versionId = generateVersionId();
        TemplateVersion version = new TemplateVersion(versionId, player.getUniqueId(), message);

         // 2. Save Version
        storage.saveTemplateVersion(template, version, clipboard);
        
        // Update cache (ensure metadata/versions are up to date in cache objects)
        // Since 'template' reference is what we modify, and it's in cache, we might be fine.
        // But if storage returned a new object or we want to be safe:
        // (TemplateStorage.saveTemplateVersion updates the passed template object in memory too? Yes, see code)
        
         // 3. Trigger Update
        TemplateVersion newVer = template.getLatestVersion();
        List<Instance> allLoaded = instanceRepository.getAllLoadedInstances();

        // Filter by template ID
        List<Instance> targets = allLoaded.stream()
                .filter(i -> i.getTemplateId().equals(template.getId()))
                .collect(Collectors.toList());

        player.sendMessage(ChatColor.YELLOW + "Found " + targets.size() + " active instances. Updating...");

        templateUpdater.updateInstances(template, newVer, targets);

        player.sendMessage(ChatColor.GREEN + "Update process finished.");
    }
    
    public Template getTemplate(String path) {
        // path is user input: users/jacky/mytmpl
        // We check pathCache
        return pathCache.get(path);
    }
    
    public Template getTemplate(UUID id) {
        return idCache.get(id);
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
            if (!player.hasPermission("idunn.template.save.personal")) {
                throw new SecurityException("You do not have permission to save personal templates.");
            }
            finalPath = "users/" + player.getName();
        } else {
            // Path permission check: idunn.template.save.dir1.dir2
            String permNode = "idunn.template.save." + subPath.replace("/", ".");
            if (!player.hasPermission(permNode)) {
                if (!hasRecursivePermission(player, subPath)) {
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

    private boolean hasRecursivePermission(Player player, String path) {
        String[] parts = path.split("/");
        StringBuilder current = new StringBuilder("idunn.template.save");
        if (player.hasPermission(current.toString())) return true;

        for (String part : parts) {
            current.append(".").append(part);
            if (player.hasPermission(current.toString())) return true;
        }
        return false;
    }

    private String generateVersionId() {
        long now = System.currentTimeMillis();
        return Long.toString(now);
    }
}