package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.calc.DiffCalculator;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.history.IdunnHistoryWrapper;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.util.PermissionUtil;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.jackyblackson.idunntemplates.core.util.TransformUtil;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import com.jackyblackson.idunntemplates.util.EntityHelper;
import com.jackyblackson.idunntemplates.util.InstanceRegionUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage;

public class InstanceManager {

    private final TemplateStorage templateStorage;
    private final InstanceRepository instanceRepository;
    private final Logger logger;
    private final com.jackyblackson.idunntemplates.manager.SessionManager sessionManager;
    private final TemplateManager templateManager; // Added field

    private final DiffCalculator diffCalculator;

    public InstanceManager(TemplateStorage templateStorage, InstanceRepository instanceRepository, Logger logger, com.jackyblackson.idunntemplates.manager.SessionManager sessionManager, TemplateManager templateManager, DiffCalculator diffCalculator) {
        this.templateStorage = templateStorage;
        this.instanceRepository = instanceRepository;
        this.logger = logger;
        this.sessionManager = sessionManager;
        this.templateManager = templateManager;
        this.diffCalculator = diffCalculator;
    }

    /**
     * Places an instance of a template at the specified location.
     */
    @Nullable
    public Instance placeInstanceAndReturn(
            Player player, Template template, Location location,
            int rot, boolean flipX, boolean flipY, boolean flipZ
    ) throws Exception {
        return placeInstanceAndReturn(
                player, template, location,
                rot, flipX, flipY, flipZ,
                0, 0,
                0, 0,
                0, 0,
                null // default confirm
        );
    }

    @Nullable
    public Instance placeInstanceAndReturn(
            Player player, Template template, Location location,
            int rot, boolean flipX, boolean flipY, boolean flipZ,
            int maskXNeg, int maskXPos,
            int maskYNeg, int maskYPos,
            int maskZNeg, int maskZPos
    ) throws Exception {
        return placeInstanceAndReturn(
                player, template, location,
                rot, flipX, flipY, flipZ,
                maskXNeg, maskXPos,
                maskYNeg, maskYPos,
                maskZNeg, maskZPos,
                null
        );
    }

    @Nullable
    public Instance placeInstanceAndReturn(
            Player player, Template template, Location location,
            int rot, boolean flipX, boolean flipY, boolean flipZ,
            int maskXNeg, int maskXPos,
            int maskYNeg, int maskYPos,
            int maskZNeg, int maskZPos,
            java.util.UUID confirmedParentId // Changed from boolean
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

        Clipboard clipboard = EntityHelper.getClipboard(
                template,
                latest.getVersionId(),
                rot,
                flipX,
                flipY,
                flipZ
        );

        // --- Recursive Detection Logic ---
        // Calculate the effective world region of the instance to be placed
        com.sk89q.worldedit.regions.Region targetRegion = calculateWorldRegion(clipboard, location, rot, flipX, flipY, flipZ, maskXNeg, maskXPos, maskYNeg, maskYPos, maskZNeg, maskZPos);
        Template parentTemplate = null;
        
        if (templateManager != null) {
            BlockVector3 min = targetRegion.getMinimumPoint();
            BlockVector3 max = targetRegion.getMaximumPoint();
            java.util.List<Template> intersecting = templateManager.getIntersectingTemplates(
                    location.getWorld().getUID(),
                    min.x(), min.y(), min.z(),
                    max.x(), max.y(), max.z()
            );
            
            if (!intersecting.isEmpty()) {
                if (confirmedParentId == null) {
                    sendRecursiveConfirmation(player, template, intersecting, rot, flipX, flipY, flipZ, maskXNeg, maskXPos, maskYNeg, maskYPos, maskZNeg, maskZPos);
                    return null;
                } else {
                    // Logic to handle confirmed recursive placement (Phase 3 & 4)
                    // Find the confirmed parent in the intersecting list
                    parentTemplate = intersecting.stream()
                            .filter(t -> t.getId().equals(confirmedParentId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("The specified parent template '" + confirmedParentId + "' does not overlap with the instance placement area."));
                    
                    // Cycle Check (V2 addition)
                    if (checkCycle(template.getId(), parentTemplate.getId())) {
                        player.sendMessage(ChatColor.RED + "⚠ Cyclic dependency detected!");
                        player.sendMessage(ChatColor.RED + "Cannot place '" + template.getName() + "' inside '" + parentTemplate.getName() + "' because '" + parentTemplate.getName() + "' is already a child (directly or indirectly) of '" + template.getName() + "'.");
                        return null; // Abort placement
                    }

                    // 1. Calculate Cuts in World Space
                    TemplateMetadata pMeta = parentTemplate.getMetadata();
                    int pMinX = pMeta.getAnchorX();
                    int pMinY = pMeta.getAnchorY();
                    int pMinZ = pMeta.getAnchorZ();
                    int pMaxX = pMinX + pMeta.getWidth() - 1;
                    int pMaxY = pMinY + pMeta.getHeight() - 1;
                    int pMaxZ = pMinZ + pMeta.getLength() - 1;
                    
                    int cMinX = min.x(); int cMaxX = max.x();
                    int cMinY = min.y(); int cMaxY = max.y();
                    int cMinZ = min.z(); int cMaxZ = max.z();
                    
                    int cutWMinX = Math.max(0, pMinX - cMinX);
                    int cutWMaxX = Math.max(0, cMaxX - pMaxX);
                    int cutWMinY = Math.max(0, pMinY - cMinY);
                    int cutWMaxY = Math.max(0, cMaxY - pMaxY);
                    int cutWMinZ = Math.max(0, pMinZ - cMinZ);
                    int cutWMaxZ = Math.max(0, cMaxZ - pMaxZ);
                    
                    // 2. Map World Cuts to Local Masks
                    int[] localMasks = mapWorldCutsToLocal(rot, flipX, flipZ, cutWMinX, cutWMaxX, cutWMinZ, cutWMaxZ);
                    
                    // Combine with existing masks
                    maskXNeg += localMasks[0];
                    maskXPos += localMasks[1];
                    maskZNeg += localMasks[2];
                    maskZPos += localMasks[3];
                    
                    maskYNeg += cutWMinY;
                    maskYPos += cutWMaxY;
                }
            }
        }
        // -------------------------------

        // V2 UX: Warn if the template itself is locked (preventing updates from propagating if we place it elsewhere)
        // Note: This check refers to the template being placed (Child), not the Parent it is placed into.
        if (template.getMetadata().isLocked()) {
            player.sendMessage("");
            player.sendMessage(getMessage(player, "recursive.locked.header"));
            player.sendMessage(getMessage(player, "recursive.locked.status", template.getPath()));
            player.sendMessage(getMessage(player, "recursive.locked.updates_not_visible"));
            player.sendMessage(getMessage(player, "recursive.locked.contact_owner",
                    Bukkit.getPlayer(template.getMetadata().getCreatorId()) == null ? template.getMetadata().getCreatorId().toString() : Objects.requireNonNull(Bukkit.getPlayer(template.getMetadata().getCreatorId())).getName()
            ));
            player.sendMessage("");
        }

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

            // Phase 3: Data Binding
            if (parentTemplate != null) {
                instance.setEmbeddedInTemplateId(parentTemplate.getId());
                
                // V2: Locking & Staging Logic
                com.jackyblackson.idunntemplates.core.domain.TemplateMetadata pMeta = parentTemplate.getMetadata();
                
                // 1. Auto-Lock if not locked
                if (!pMeta.isLocked()) {
                    pMeta.setLocked(true);
                    // UX Notification is handled in Phase 4 (PlaceCommand/Listener)
                    // But we can send a basic message here as per V2 design
                    sendLockedTitle(player, parentTemplate);
                }

                // 2. Add to Staging Area
                pMeta.getStagedChanges().getAddedInstances().add(instance);
                
                // 3. Save Parent Metadata (with lock & staging info)
                templateManager.saveTemplateMetadata(parentTemplate);
                
                // 4. Save Child Metadata (Self - still need to record that I am embedded, 
                //    but parent might not acknowledge me yet officially.
                //    However, for the child to know its parent, we set it.
                //    But strictly, if the parent reverts, this child relationship is invalid.
                //    V2 says "staged instances... saved to disk". 
                //    Let's keep the child knowing its parent, but the parent only knows the child via Staging.)
                java.util.Map<UUID, java.util.List<Instance>> parentMap = template.getMetadata().getParentTemplateInstances();
                parentMap.computeIfAbsent(parentTemplate.getId(), k -> new java.util.ArrayList<>()).add(instance);
                templateManager.saveTemplateMetadata(template);
                
                logger.info("Recursive placement staged: Child " + template.getName() + " -> Parent " + parentTemplate.getName() + " (Locked)");
                
                // 5. NO Cascading Update
                // "The template is locked, so no cascading update is triggered."
                
                // 6. Record Staged History
                IdunnTemplates.getInstance().getHistoryManager().remember(
                        player,
                        editSession,
                        IdunnHistoryWrapper.stagedPlaceHistory(
                                player,
                                instance,
                                parentTemplate.getId(),
                                parentTemplate.getMetadata().getLockedTimestamp()
                        )
                );

            } else {
                // Normal Placement
                instanceRepository.saveInstance(instance);
                IdunnTemplates.getInstance().getHistoryManager().remember(player, editSession, IdunnHistoryWrapper.placeInstanceHistory(player, instance));
            }
            
            // Always save instance record (it exists in the world)
            if (parentTemplate != null) instanceRepository.saveInstance(instance);
            IdunnTemplates.getInstance().getProjectSettlementSyncManager().refreshProjectsOverlappingInstance(instance);

            IdunnTemplates.getInstance().getSessionManager().saveSession(player.getUniqueId());
            return instance;
        }
    }

    private static void sendLockedTitle(Player player, Template parentTemplate) {
        player.sendTitle(
                getMessage(player, "recursive.toggled.title"),
                getMessage(player, "recursive.toggled.subtitle", parentTemplate.getPath()),
                10, 70, 20);
    }

    private void sendRecursiveConfirmation(Player player, Template template, java.util.List<Template> parents,
                                       int rot, boolean flipX, boolean flipY, boolean flipZ,
                                       int maskXNeg, int maskXPos, int maskYNeg, int maskYPos, int maskZNeg, int maskZPos) {
        player.sendMessage("");
        player.sendMessage(getMessage(player, "recursive.detected.header"));
        player.sendMessage(getMessage(player, "recursive.detected.status", template.getName(), String.valueOf(parents.size())));
        player.sendMessage(getMessage(player, "recursive.detected.confirm"));

        String placePath = normalizePath(template.getPath());

        // Reconstruct base command string
        StringBuilder cmdBase = new StringBuilder("/idunn template place " + placePath + " " + rot + " " + flipX + " " + flipY + " " + flipZ);
        if (maskXPos > 0) cmdBase.append(" -x+:").append(maskXPos);
        if (maskXNeg > 0) cmdBase.append(" -x-:").append(maskXNeg);
        if (maskYPos > 0) cmdBase.append(" -y+:").append(maskYPos);
        if (maskYNeg > 0) cmdBase.append(" -y-:").append(maskYNeg);
        if (maskZPos > 0) cmdBase.append(" -z+:").append(maskZPos);
        if (maskZNeg > 0) cmdBase.append(" -z-:").append(maskZNeg);
        
        // List all parents with buttons
        for (Template parent : parents) {
            TextComponent btn = new TextComponent(getMessage(player, "recursive.detected.button", parent.getName()));
            btn.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            btn.setBold(true);

            String fullCmd = cmdBase.toString() + " -parent:" + parent.getId().toString();
            
            btn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, fullCmd));
            btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(getMessage(player, "recursive.detected.hover", parent.getPath())).create()));
            
            player.spigot().sendMessage(btn);
        }
        player.sendMessage("");
    }

    public void hardDelete(Instance target, Player player) {
        try {
            int count = IdunnTemplates.getInstance().getInstanceManager().removeInstanceBlocks(target, player);
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "instance.delete.removed_blocks", String.valueOf(count)));
        } catch (Exception e) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "instance.delete.error_blocks", e.getMessage()));
            e.printStackTrace();
            return;
        }

        // Hard delete record
        instanceRepository.hardDelete(target);
        IdunnTemplates.getInstance().getProjectSettlementSyncManager().refreshProjectsOverlappingInstance(target);
    }

    public void softDelete(Instance target, Player player) {
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "instance.delete.skip_blocks"));

        // Soft delete record
        target.setDeletedTimestamp(System.currentTimeMillis());
        instanceRepository.saveInstance(target);
        IdunnTemplates.getInstance().getProjectSettlementSyncManager().refreshProjectsOverlappingInstance(target);
    }

    public int removeInstanceBlocks(Instance instance, Player player) throws IOException {
        Template template = templateManager.getTemplate(instance.getTemplateId());
        if (template == null) {
            throw new IOException("Template not found for this instance.");
        }

        TemplateVersion version = template.getMetadata().getVersions().stream()
                .filter(v -> v.getVersionId().equals(instance.getCurrentVersionId()))
                .findFirst()
                .orElse(null);

        if (version == null) {
            throw new IOException("Version info missing for this instance.");
        }

        // Load Variations
        int rot = instance.getRotationY();
        boolean flipX = instance.isFlipX();
        boolean flipY = instance.isFlipY();
        boolean flipZ = instance.isFlipZ();
        Clipboard clipboard = EntityHelper.getClipboard(template, version.getVersionId(), rot, flipX, flipY, flipZ);
        if (clipboard == null) {
            throw new IOException("Failed to load clipboard.");
        }

        World world = Bukkit.getWorld(instance.getWorldId());
        if (world == null) {
            throw new IOException("World not loaded.");
        }



        // 1. Construct Transform
        AffineTransform transform = new AffineTransform();
//        transform = transform.rotateY(instance.getRotationY());
//        if (instance.isFlipX()) transform = transform.scale(BlockVector3.at(-1, 1, 1).toVector3());
//        if (instance.isFlipY()) transform = transform.scale(BlockVector3.at(1, -1, 1).toVector3());
//        if (instance.isFlipZ()) transform = transform.scale(BlockVector3.at(1, 1, -1).toVector3());

        // 2. Origin
        BlockVector3 origin = BlockVector3.at(instance.getX(), instance.getY(), instance.getZ());

        // 3. Get managed blocks using DiffCalculator
        Set<BlockVector3> managedBlocks = diffCalculator.calculateManagedBlocks(clipboard, transform, origin, world, instance);

        if (managedBlocks.isEmpty()) {
            return 0;
        }

        // 4. Remove blocks using WorldEdit
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(BukkitAdapter.adapt(world))
                .actor(BukkitAdapter.adapt(player))
                .build()
        ) {

            for (BlockVector3 pos : managedBlocks) {
                assert BlockTypes.AIR != null;
                editSession.setBlock(pos, BlockTypes.AIR.getDefaultState());
            }
            editSession.close();

            // DATA BINDING
            if (instance.isWild()) {
                IdunnTemplates.getInstance().getHistoryManager().remember(
                        player, editSession,
                        IdunnHistoryWrapper.deleteInstanceHistory(
                                player, instance
                        )
                );
//                player.sendMessage("history of deleting this instance is remembered");
            } else {
                var parentTemplate = templateManager.getTemplate(instance.getEmbeddedInTemplateId());
                if (parentTemplate == null) {
                    return -1;
                }
                parentTemplate.getMetadata().setLocked(true);
                sendLockedTitle(player, parentTemplate);
                IdunnTemplates.getInstance().getHistoryManager().remember(
                        player, editSession,
                        IdunnHistoryWrapper.stagedDeleteHistory(
                                player, instance, instance.getEmbeddedInTemplateId(), parentTemplate.getMetadata().getLockedTimestamp()
                        )
                );
//                player.sendMessage("history of deleting this instance is remembered and staged");
            }

            IdunnTemplates.getInstance().getSessionManager().saveSession(player.getUniqueId());
        } catch (Exception e) {
            throw new IOException("WorldEdit error: " + e.getMessage(), e);
        }

        return managedBlocks.size();
    }
    
    private String normalizePath(String rawPath) {
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
    
    /**
     * Maps world-space cuts (MinX, MaxX, MinZ, MaxZ) to local-space masks (XNeg, XPos, ZNeg, ZPos).
     * @return int[] { maskXNeg, maskXPos, maskZNeg, maskZPos }
     */
    private int[] mapWorldCutsToLocal(int rot, boolean flipX, boolean flipZ, int wCutMinX, int wCutMaxX, int wCutMinZ, int wCutMaxZ) {
        int lMinX, lMaxX, lMinZ, lMaxZ;

        // 1. Rotation (Y-axis clockwise)
        // Standard WorldEdit Rotation:
        // 0:   X+ -> X+, Z+ -> Z+  (Normal)
        // 90:  X+ -> Z+, Z+ -> X-
        // 180: X+ -> X-, Z+ -> Z-
        // 270: X+ -> Z-, Z+ -> X+
        
        // However, we are mapping "Cuts" which correspond to Faces.
        // We want to know: Which Local Face is pointing at World West (MinX)?
        // If Rot=0: Local XNeg points to World MinX. So lMinX = wCutMinX.
        
        switch (rot) {
            case 90:
                // Local X+ points to World South (MaxZ). lMaxX = wCutMaxZ
                // Local Z+ points to World West (MinX).  lMaxZ = wCutMinX
                // Local X- points to World North (MinZ). lMinX = wCutMinZ
                // Local Z- points to World East (MaxX).  lMinZ = wCutMaxX
                lMinX = wCutMinZ;
                lMaxX = wCutMaxZ; // Wait, X+ -> Z+ (South/MaxZ). Yes.
                lMinZ = wCutMaxX;
                lMaxZ = wCutMinX;
                break;
            case 180:
                // Local X+ points to World West (MinX). lMaxX = wCutMinX
                // Local Z+ points to World North (MinZ). lMaxZ = wCutMinZ
                lMinX = wCutMaxX;
                lMaxX = wCutMinX;
                lMinZ = wCutMaxZ;
                lMaxZ = wCutMinZ;
                break;
            case 270:
                // Local X+ points to World North (MinZ). lMaxX = wCutMinZ
                // Local Z+ points to World East (MaxX).  lMaxZ = wCutMaxX
                lMinX = wCutMaxZ;
                lMaxX = wCutMinZ;
                lMinZ = wCutMinX;
                lMaxZ = wCutMaxX;
                break;
            case 0:
            default:
                lMinX = wCutMinX;
                lMaxX = wCutMaxX;
                lMinZ = wCutMinZ;
                lMaxZ = wCutMaxZ;
                break;
        }
        
        // 2. Flip (FlipX usually swaps Left/Right, i.e., XNeg/XPos)
        if (flipX) {
            int tmp = lMinX; lMinX = lMaxX; lMaxX = tmp;
        }
        if (flipZ) {
            int tmp = lMinZ; lMinZ = lMaxZ; lMaxZ = tmp;
        }
        
        return new int[] { lMinX, lMaxX, lMinZ, lMaxZ };
    }
    
    private com.sk89q.worldedit.regions.Region calculateWorldRegion(Clipboard clipboard, Location target, int rot, boolean flipX, boolean flipY, boolean flipZ,
                                                                    int mxn, int mxp, int myn, int myp, int mzn, int mzp) {
        return InstanceRegionUtil.calculateWorldRegion(clipboard, target, rot, flipX, flipY, flipZ, mxn, mxp, myn, myp, mzn, mzp);
    }

    public void placeInstance(Player player, Template template, Location location, int rot, boolean flipX, boolean flipY, boolean flipZ) throws Exception {
        placeInstanceAndReturn(player, template, location, rot, flipX, flipY, flipZ);
    }

    /**
     * Checks if placing child into parent would create a cycle.
     * @param childId The ID of the template being placed as a child
     * @param parentId The ID of the template becoming the parent
     * @return true if a cycle is detected (i.e., parent is already a descendant of child)
     */
    private boolean checkCycle(UUID childId, UUID parentId) {
        // BFS to check if 'childId' is reachable from 'parentId' by traversing UPWARDS (parent -> parent's parent)
        // No, wait. A cycle means: Child -> Parent -> ... -> Child
        // So we need to check if 'Child' is already an ancestor of 'Parent'.
        // i.e., Can we reach 'Child' by climbing up from 'Parent'?
        
        java.util.Set<UUID> visited = new java.util.HashSet<>();
        java.util.Queue<UUID> queue = new java.util.LinkedList<>();
        
        queue.add(parentId);
        visited.add(parentId);
        
        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            if (current.equals(childId)) {
                return true; // Found the child in the ancestry chain!
            }
            
            Template t = templateManager.getTemplate(current);
            if (t != null) {
                // Get parents of current template
                // Note: We need to check both established parents AND staged parents (if we want strict check)
                // Staged parents are stored in StagedChanges of parents... wait.
                // Staged relationship: Parent StagedChanges has "addedInstances".
                // Child doesn't easily know its staged parents unless we traverse all templates.
                // For performance, let's stick to established relationships (parentTemplateInstances).
                // If a cycle is formed via staging, it will be caught at Commit time or we accept it as temporary.
                // But let's check established ones.
                
                java.util.Map<UUID, java.util.List<Instance>> parents = t.getMetadata().getParentTemplateInstances();
                for (UUID pId : parents.keySet()) {
                    if (visited.add(pId)) {
                        queue.add(pId);
                    }
                }
            }
        }
        return false;
    }
}
