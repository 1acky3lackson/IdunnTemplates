package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.util.PermissionUtil;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.jackyblackson.idunntemplates.core.util.TransformUtil;
import com.jackyblackson.idunntemplates.permission.PermissionNames;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class InstanceManager {

    private final TemplateStorage templateStorage;
    private final InstanceRepository instanceRepository;
    private final Logger logger;
    private final com.jackyblackson.idunntemplates.manager.SessionManager sessionManager; // Added field

    // Updated Constructor
    public InstanceManager(TemplateStorage templateStorage, InstanceRepository instanceRepository, Logger logger) {
        this.templateStorage = templateStorage;
        this.instanceRepository = instanceRepository;
        this.logger = logger;
        this.sessionManager = null; // Should be injected via setters or updated constructor.
        // Wait, I should update the constructor signature but that breaks IdunnTemplates.java
        // I will use a setter or overload constructor?
        // Better to update constructor and IdunnTemplates.java.
    }
    
    // Proper Constructor
    public InstanceManager(TemplateStorage templateStorage, InstanceRepository instanceRepository, Logger logger, com.jackyblackson.idunntemplates.manager.SessionManager sessionManager) {
        this.templateStorage = templateStorage;
        this.instanceRepository = instanceRepository;
        this.logger = logger;
        this.sessionManager = sessionManager;
    }

    /**
     * Places an instance of a template at the specified location.
     */
    public Instance placeInstanceAndReturn(Player player, Template template, Location location, int rot, boolean flipX, boolean flipY, boolean flipZ) throws Exception {
        // check permission
        if (!player.hasPermission(PermissionNames.Templates.place)) {
            throw new Exception("You have no permission to place idunn templates here.");
        }
        if (!template.getPath().startsWith("users/" + player.getName())) {  // 访问非本人目录
            if (!PermissionUtil.hasRecursivePermission(player, PermissionNames.Templates.usePath$R, template.getPath())) {
                throw new Exception("You don't permission to place template '" + template.getPath() + "'.");
            }
        }
        TemplateVersion latest = template.getLatestVersion();
        if (latest == null) {
            throw new IllegalArgumentException("Template has no versions.");
        }

        Clipboard clipboard = template.getClipboard(
                latest.getVersionId(),
                rot,
                flipX,
                flipY,
                flipZ
        );

        // Prepare Holder
        ClipboardHolder holder = new ClipboardHolder(clipboard);

        // Paste
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(BukkitAdapter.adapt(location.getWorld()))
                .actor(BukkitAdapter.adapt(player))
                .build()
        ) {
            // Bind to player for undo
            if (player != null) {
                com.sk89q.worldedit.LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
//                editSession = session.createEditSession(BukkitAdapter.adapt(player));
                session.remember(editSession);
            }


            if (player != null && sessionManager != null) {
                var pSession = sessionManager.getSession(player.getUniqueId());
                if (pSession != null && pSession.getPreference().isPlaceOnEmptyOnly()) {
                    // Create mask from emptyBlocks list
                    java.util.Set<com.sk89q.worldedit.world.block.BlockType> blocks = new java.util.HashSet<>();
                    for (String m : pSession.getPreference().getEmptyBlocks()) {
                        try {
                            com.sk89q.worldedit.world.block.BlockType type = com.sk89q.worldedit.world.block.BlockTypes.get(m.toLowerCase());
                            if (type != null) {
                                blocks.add(type);
                            }
                        } catch (Exception ignored) {}
                    }
                    BlockTypeMask mask = new BlockTypeMask(editSession.getExtent(), blocks);
                    editSession.setMask(mask);
                }

            }
            Operation op = holder.createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .ignoreAirBlocks(true)
                    .build();
            Operations.completeLegacy(op);
        }

        var minPos = TransformUtil.getInstanceMinPos(location, clipboard);

        // Create Record
        Instance instance = new Instance(
                template.getId(),
                latest.getVersionId(),
                location.getWorld().getUID(),
                minPos.x(), minPos.y(), minPos.z(),
                rot, flipX, flipY, flipZ,
                player.getUniqueId(),
                player.getName()
        );
        
        instanceRepository.saveInstance(instance);
        
        return instance;
    }
    
    public void placeInstance(Player player, Template template, Location location, int rot, boolean flipX, boolean flipY, boolean flipZ) throws Exception {
        placeInstanceAndReturn(player, template, location, rot, flipX, flipY, flipZ);
    }
}
