package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.history.IdunnHistoryWrapper;
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

import java.util.Objects;
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
    public Instance placeInstanceAndReturn(
            Player player, Template template, Location location,
            int rot, boolean flipX, boolean flipY, boolean flipZ
    ) throws Exception {
        return placeInstanceAndReturn(
                player, template, location,
                rot, flipX, flipY, flipZ,
                0, 0,
                0, 0,
                0, 0
        );
    }

    public Instance placeInstanceAndReturn(
            Player player, Template template, Location location,
            int rot, boolean flipX, boolean flipY, boolean flipZ,
            int maskXNeg, int maskXPos,
            int maskYNeg, int maskYPos,
            int maskZNeg, int maskZPos
    ) throws Exception {
        // check permission
        if (!player.hasPermission(PermissionNames.Templates.place)) {
            throw new Exception("You have no permission to place idunn templates here.");
        }
        if (!template.getPath().startsWith("users/" + player.getName())) {  // 访问非本人目录
            if (!PermissionUtil.hasRecursivePermission(player, PermissionNames.Templates.usePath$R, template.getPath())) {
                throw new Exception("You don't have permission to place template '" + template.getPath() + "'.");
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
//            com.sk89q.worldedit.LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
//                editSession = session.createEditSession(BukkitAdapter.adapt(player));
//            session.remember(editSession);

            com.sk89q.worldedit.function.mask.Mask mask = null;

            if (sessionManager != null) {
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
                    mask = new BlockTypeMask(editSession.getExtent(), blocks);
                }
            }

            // Apply Instance Mask
            if (maskXNeg > 0 || maskXPos > 0 || maskYNeg > 0 || maskYPos > 0 || maskZNeg > 0 || maskZPos > 0) {
                com.sk89q.worldedit.regions.Region validRegion = calculateWorldRegion(clipboard, location, rot, flipX, flipY, flipZ, maskXNeg, maskXPos, maskYNeg, maskYPos, maskZNeg, maskZPos);
                com.sk89q.worldedit.function.mask.Mask regionMask = new com.sk89q.worldedit.function.mask.RegionMask(validRegion);
                if (mask != null) {
                    mask = new com.sk89q.worldedit.function.mask.MaskIntersection(mask, regionMask);
                } else {
                    mask = regionMask;
                }
            }
            
            if (mask != null) {
                editSession.setMask(mask);
            }

            Operation op = holder.createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .ignoreAirBlocks(true)
                    .build();
            Operations.completeLegacy(op);




            var minPos = TransformUtil.getInstanceMinPos(location, clipboard);

            // Create Record
            Instance instance = new Instance(
                    template.getId(),
                    latest.getVersionId(),
                    Objects.requireNonNull(Objects.requireNonNull(location.getWorld()).getUID()),
                    minPos.x(), minPos.y(), minPos.z(),
                    rot, flipX, flipY, flipZ,
                    player.getUniqueId(),
                    player.getName()
            );

            // Set Masks
            instance.setMaskXNeg(maskXNeg);
            instance.setMaskXPos(maskXPos);
            instance.setMaskYNeg(maskYNeg);
            instance.setMaskYPos(maskYPos);
            instance.setMaskZNeg(maskZNeg);
            instance.setMaskZPos(maskZPos);

            instanceRepository.saveInstance(instance);

            IdunnTemplates.getInstance().getHistoryManager().remember(player, editSession, IdunnHistoryWrapper.placeInstanceHistory(player, instance));
            IdunnTemplates.getInstance().getSessionManager().saveSession(player.getUniqueId());
            return instance;
        }
    }
    
    private com.sk89q.worldedit.regions.Region calculateWorldRegion(Clipboard clipboard, Location target, int rot, boolean flipX, boolean flipY, boolean flipZ,
                                                                    int mxn, int mxp, int myn, int myp, int mzn, int mzp) {
        // 1. Local Bounds
        BlockVector3 min = clipboard.getRegion().getMinimumPoint();
        BlockVector3 max = clipboard.getRegion().getMaximumPoint();
        
        int minX = min.x() + mxn;
        int maxX = max.x() - mxp;
        int minY = min.y() + myn;
        int maxY = max.y() - myp;
        int minZ = min.z() + mzn;
        int maxZ = max.z() - mzp;
        
        // Ensure bounds are valid (min <= max)
        if (minX > maxX || minY > maxY || minZ > maxZ) {
             // Return empty or very small region?
             // Returning a 0-size region at target?
             return new com.sk89q.worldedit.regions.CuboidRegion(BlockVector3.at(0,0,0), BlockVector3.at(0,0,0));
        }

        // 2. Transform Setup
        com.sk89q.worldedit.math.transform.AffineTransform transform = new com.sk89q.worldedit.math.transform.AffineTransform();
        transform = transform.rotateY(rot);
        if (flipX) transform = transform.scale(BlockVector3.at(-1, 1, 1).toVector3());
        if (flipY) transform = transform.scale(BlockVector3.at(1, -1, 1).toVector3());
        if (flipZ) transform = transform.scale(BlockVector3.at(1, 1, -1).toVector3());

        BlockVector3 origin = clipboard.getOrigin();
        
        // 3. Corners
        BlockVector3[] corners = new BlockVector3[8];
        corners[0] = BlockVector3.at(minX, minY, minZ);
        corners[1] = BlockVector3.at(minX, minY, maxZ);
        corners[2] = BlockVector3.at(minX, maxY, minZ);
        corners[3] = BlockVector3.at(minX, maxY, maxZ);
        corners[4] = BlockVector3.at(maxX, minY, minZ);
        corners[5] = BlockVector3.at(maxX, minY, maxZ);
        corners[6] = BlockVector3.at(maxX, maxY, minZ);
        corners[7] = BlockVector3.at(maxX, maxY, maxZ);

        int wMinX = Integer.MAX_VALUE, wMinY = Integer.MAX_VALUE, wMinZ = Integer.MAX_VALUE;
        int wMaxX = Integer.MIN_VALUE, wMaxY = Integer.MIN_VALUE, wMaxZ = Integer.MIN_VALUE;

        BlockVector3 targetVec = BlockVector3.at(target.getBlockX(), target.getBlockY(), target.getBlockZ());

        for (BlockVector3 c : corners) {
            // Rel to Origin
            com.sk89q.worldedit.math.Vector3 v = c.toVector3().subtract(origin.toVector3());
            // Transform
            com.sk89q.worldedit.math.Vector3 t = transform.apply(v);
            // Add to Target
            BlockVector3 worldPos = t.toBlockPoint().add(targetVec);

            if (worldPos.x() < wMinX) wMinX = worldPos.x();
            if (worldPos.y() < wMinY) wMinY = worldPos.y();
            if (worldPos.z() < wMinZ) wMinZ = worldPos.z();
            if (worldPos.x() > wMaxX) wMaxX = worldPos.x();
            if (worldPos.y() > wMaxY) wMaxY = worldPos.y();
            if (worldPos.z() > wMaxZ) wMaxZ = worldPos.z();
        }
        
        return new com.sk89q.worldedit.regions.CuboidRegion(
                BlockVector3.at(wMinX, wMinY, wMinZ),
                BlockVector3.at(wMaxX, wMaxY, wMaxZ)
        );
    }
    
    public void placeInstance(Player player, Template template, Location location, int rot, boolean flipX, boolean flipY, boolean flipZ) throws Exception {
        placeInstanceAndReturn(player, template, location, rot, flipX, flipY, flipZ);
    }
}
