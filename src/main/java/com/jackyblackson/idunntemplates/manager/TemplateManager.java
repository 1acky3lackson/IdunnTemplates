package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class TemplateManager {

    private final TemplateStorage storage;

    public TemplateManager(TemplateStorage storage) {
        this.storage = storage;
    }

    /**
     * Saves a new template from a player's clipboard.
     * @param player The player creating the template.
     * @param name The name of the template (final folder will be _name).
     * @param subPath Optional subpath. If null/empty, defaults to "users/<playername>".
     * @param clipboard The WorldEdit clipboard.
     * @return The created Template.
     * @throws IllegalArgumentException If permissions are missing or name is invalid.
     * @throws IOException If IO fails.
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
                // Check parent permissions if strict match fails? 
                // Doc says: "If player has parent dir permission, child also recursively."
                // So we check from specific to general? No, usually wildcards handle this, 
                // or we check if they have "idunn.template.save.dir1" which implies dir1.*
                // For now, let's implement a simple check. If they have the exact node or a parent node.
                if (!hasRecursivePermission(player, subPath)) {
                    throw new SecurityException("You do not have permission to save to " + subPath);
                }
            }
            finalPath = subPath;
        }

        // 2. Prepare Metadata
        Region region = clipboard.getRegion();
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 origin = clipboard.getOrigin(); 
        // Note: Clipboard origin is where the player copied from relative to the selection.
        // The doc says "Anchor Point: minX, minY, minZ of the selection".
        // But WorldEdit clipboard usually works relative to an origin. 
        // If we strictly follow the doc: "Anchor Point = min coords of cuboid".
        // Let's store the min coords as the anchor.
        
        // However, for correct pasting, we usually care about the relation between 0,0,0 in the schem and the paste loc.
        // In the schematic, the blocks are stored relative to the clipboard origin.
        
        TemplateMetadata metadata = new TemplateMetadata(
                player.getUniqueId(),
                System.currentTimeMillis(),
                player.getWorld().getUID(), // This might be issue if WE selection is cross-world (unlikely)
                min.getX(), min.getY(), min.getZ(),
                region.getWidth(), region.getHeight(), region.getLength()
        );

        // 3. Prepare Version
        String versionId = generateVersionId();
        TemplateVersion version = new TemplateVersion(versionId, player.getUniqueId(), "Initial creation");

        // 4. Save
        return storage.saveNewTemplate(finalPath, name, metadata, clipboard, version);
    }

    private boolean hasRecursivePermission(Player player, String path) {
        // Check "idunn.template.save.dir1.dir2"
        // Check "idunn.template.save.dir1"
        // Check "idunn.template.save"
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
        return Base64.getUrlEncoder().withoutPadding().encodeToString(String.valueOf(now).getBytes(StandardCharsets.UTF_8));
    }
}
