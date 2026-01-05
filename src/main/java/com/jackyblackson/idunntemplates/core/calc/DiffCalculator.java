package com.jackyblackson.idunntemplates.core.calc;

import com.fastasyncworldedit.core.extent.processor.lighting.RelightMode;
import com.fastasyncworldedit.core.wrappers.WorldWrapper;
import com.jackyblackson.idunntemplates.core.util.CapturingExtent;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

public class DiffCalculator {

    private final BlockComparator comparator;
    private final java.util.logging.Logger logger;

    public DiffCalculator(BlockComparator comparator, java.util.logging.Logger logger) {
        this.comparator = comparator;
        this.logger = logger;
    }

    /**
     * Calculates the differences and returns a map of changes to apply.
     *
     * @param oldClip   The old version clipboard.
     * @param newClip   The new version clipboard.
     * @param transform The transformation applied to the instance (rotation/flip).
     * @param origin    The 'anchor' location of the instance in the world.
     * @param world     The Bukkit world.
     * @return A map of World Position -> New BlockState to set.
     */
    public Map<BlockVector3, BlockState> calculateDiff(
            Clipboard oldClip,
            Clipboard newClip,
            AffineTransform transform,
            BlockVector3 origin,
            World world
    ) {
//        logger.info(String.format("Starting Diff Calculation. Origin: %s, World: %s", origin, world.getName()));
        
        Map<BlockVector3, BlockState> changes = new HashMap<>();

        // 1. "Paste" Old Clipboard to memory to get transformed B_old
//        logger.info("Simulating paste for Old Clipboard...");
        Map<BlockVector3, BlockState> oldBlocks = getTransformedBlocks(oldClip, transform);

        // 2. "Paste" New Clipboard to memory to get transformed B_new
//        logger.info("Simulating paste for New Clipboard...");
        Map<BlockVector3, BlockState> newBlocks = getTransformedBlocks(newClip, transform);

        // 3. Determine Union of Bounds (Relative to Origin 0,0,0 of the paste)
        Set<BlockVector3> allPositions = new HashSet<>();
        allPositions.addAll(oldBlocks.keySet());
        allPositions.addAll(newBlocks.keySet());
        
//        logger.info("Total unique block positions to check: " + allPositions.size());

        int countIgnored = 0;
        int countSkippedNull = 0;
        int countUpdateUnmodified = 0;
        int countUpdateIdle = 0;
        int countSkippedConflict = 0;
        int countNoChangeNeeded = 0;

        // 4. Iterate and Compare
        for (BlockVector3 relPos : allPositions) {
            // Absolute World Position
            BlockVector3 worldPos = relPos.add(origin);
//            System.out.printf("  ===> %s -> %s\n", relPos, worldPos);
            
            // B_old: Block in old template (transformed)
            BlockState bo = oldBlocks.get(relPos);
            // B_new: Block in new template (transformed)
            BlockState bn = newBlocks.get(relPos);
            
            // If B_new is ignored (Structural Void), skip
            if (comparator.isIgnored(bn)) {
                countIgnored++;
                continue;
            }

            // B_real: Block currently in world
            Block br = world.getBlockAt(worldPos.x(), worldPos.y(), worldPos.z());

            boolean shouldUpdate = false;
            String reason = "";

            if (bn == null) {
                // Treated as AIR/Removal, but currently unsafe to auto-remove without explicit air handling.
                countSkippedNull++;
                continue; 
            }

            // Case 1: Unmodified
            // If br == bo -> update to bn
            if (comparator.isSimilar(bo, br)) {
                if (!bn.equals(bo)) {
                    shouldUpdate = true;
                    countUpdateUnmodified++;
                    // logger.fine("Update Unmodified at " + relPos + ": " + bo + " -> " + bn);
                } else {
                    countNoChangeNeeded++;
                }
            }
            // Case 2: Idle Space
            // If br is idle (Air/Water) AND bn is NOT idle -> update to bn
            else if (comparator.isIdle(br) && !comparator.isIdle(bn)) {
                shouldUpdate = true;
                countUpdateIdle++;
                // logger.fine("Update Idle at " + relPos + ": " + br.getType() + " -> " + bn);
            } else {
                // Conflict / Player modified
                countSkippedConflict++;
                // logger.fine("Conflict at " + relPos + ": World(" + br.getType() + ") != Old(" + (bo==null?"null":bo.getBlockType()) + ")");
            }
            
            if (shouldUpdate) {
                changes.put(worldPos, bn);
            }
        }

//        logger.info(String.format("Diff Stats: [Total: %d] [Changes: %d] [Ignored: %d] [SkippedNull: %d] [Updated(Clean): %d] [Updated(Idle): %d] [Skipped(Conflict): %d] [NoChange: %d]",
//                allPositions.size(), changes.size(), countIgnored, countSkippedNull, countUpdateUnmodified, countUpdateIdle, countSkippedConflict, countNoChangeNeeded));

        return changes;
    }

    public Map<BlockVector3, BlockState> getTransformedBlocks(Clipboard clipboard, AffineTransform transform) {
        Map<BlockVector3, BlockState> blockMap = new HashMap<>();
        Region region = clipboard.getRegion();
        BlockVector3 minPos = region.getMinimumPoint();
//        System.out.println("getMinimumPoint = " + minPos);

        // Iterate through all blocks in the clipboard's region
        for (BlockVector3 position : region) {

            // 1. Get the original block at this position
            BlockState block = clipboard.getBlock(position);

            // 1.5 make the pos to relative val to the MIN point of the region
            BlockVector3 relPos = position.subtract(minPos);

            // 2. Apply the transform to the position
            // Note: Transforms usually operate on Vector3 (doubles)
            Vector3 transformedVector = transform.apply(relPos.toVector3());

            // 3. Convert back to BlockVector3 (integer coordinates)
            BlockVector3 newPos = transformedVector.toBlockPoint();
//            System.out.printf("      -=-> at %s = r%s => %s, block = %s\n", position, relPos, newPos, block);
            // 4. Store in your map
            blockMap.put(newPos, block);
        }

        return blockMap;
    }

    /**
     * Calculates which blocks in the world currently belong to the instance (unmodified).
     *
     * @param clipboard The instance's template clipboard.
     * @param transform The transformation applied to the instance.
     * @param origin    The instance's origin in the world.
     * @param world     The Bukkit world.
     * @return A set of world coordinates that are considered "managed" by the instance.
     */
    public Set<BlockVector3> calculateManagedBlocks(
            Clipboard clipboard,
            AffineTransform transform,
            BlockVector3 origin,
            World world
    ) {
        Set<BlockVector3> managedBlocks = new HashSet<>();
        Map<BlockVector3, BlockState> instanceBlocks = getTransformedBlocks(clipboard, transform);

        for (Map.Entry<BlockVector3, BlockState> entry : instanceBlocks.entrySet()) {
            BlockVector3 relPos = entry.getKey();
            BlockState expectedState = entry.getValue();

            // Skip ignored (structural void) or idle (air/water/etc in template) blocks
            // Note: If the template has AIR, we generally don't "manage" it in the sense of needing to remove it,
            // unless we want to restore what was behind it? But for deletion, we only care about removing placed blocks.
            if (expectedState == null || comparator.isIgnored(expectedState) || comparator.isIdle(expectedState)) {
                continue;
            }

            BlockVector3 worldPos = relPos.add(origin);
            Block currentBlock = world.getBlockAt(worldPos.x(), worldPos.y(), worldPos.z());

            // If current block matches the expected block from the instance, it is managed.
            if (comparator.isSimilar(expectedState, currentBlock)) {
                managedBlocks.add(worldPos);
            }
        }
        return managedBlocks;
    }


    private Map<BlockVector3, BlockState> simulatePaste(World world, Clipboard clipboard, AffineTransform transform) {
        if (clipboard == null) {
            logger.warning("SimulatePaste: Clipboard is null!");
            return new HashMap<>();
        }
        logger.info("SimulatePaste: Clipboard Volume: " + clipboard.getRegion().getVolume() + ", Bounds: " + clipboard.getRegion().getMinimumPoint() + " -> " + clipboard.getRegion().getMaximumPoint());

        CapturingExtent capturingExtent = new CapturingExtent(
                300000,
                2047,
                300000
        );

        // 1. Create a session that targets your capturing extent
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(BukkitAdapter.adapt(world)) // Force the session to use your extent
                .fastMode(false)        // Disable fast mode to ensure standard setBlock calls
                .changeSetNull()        // Optimization: don't track history
                .relightMode(RelightMode.NONE) // disable light engine
                .build()) {

            // 2. Setup ClipboardHolder with transform
            ClipboardHolder holder = new ClipboardHolder(clipboard);
            holder.setTransform(holder.getTransform().combine(transform));

            // 3. Create Paste Operation targeting the EditSession
            Operation operation = holder
                    .createPaste(editSession) // Use the session, not the raw extent
                    .to(BlockVector3.ZERO)
                    .ignoreAirBlocks(false)
                    .build();

            Operations.completeLegacy(operation);
//            editSession.q
            editSession.flushQueue(); // Crucial for FAWE to finish processing
        } catch (WorldEditException e) {
            System.out.println(e.getRichMessage());
            e.printStackTrace();
        }

        return capturingExtent.getCapturedBlocks();
    }
}
