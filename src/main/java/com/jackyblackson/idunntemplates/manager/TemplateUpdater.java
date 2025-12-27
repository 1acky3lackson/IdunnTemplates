package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.core.calc.BlockComparator;
import com.jackyblackson.idunntemplates.core.calc.DiffCalculator;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ChangeSetExecutor;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.changeset.BlockOptimizedHistory;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TemplateUpdater {

    private final TemplateStorage templateStorage;
    private final InstanceRepository instanceRepository;
    private final DiffCalculator diffCalculator;
    private final Logger logger;

    public TemplateUpdater(TemplateStorage templateStorage, InstanceRepository instanceRepository, BlockComparator comparator, Logger logger) {
        this.templateStorage = templateStorage;
        this.instanceRepository = instanceRepository;
        this.diffCalculator = new DiffCalculator(comparator, logger);
        this.logger = logger;
    }
    
    // Triggered by manual commit or scheduled check
    public void updateInstances(Template template, TemplateVersion newVersion, List<Instance> instances) {
        logger.info("Starting update for template: " + template.getName() + " -> Ver: " + newVersion.getVersionId() + ". Target instances: " + instances.size());
        int updatedCount = 0;
        int skippedCount = 0;
        
        for (Instance instance : instances) {
            // Only update if auto-update is on
            if (!instance.isAutoUpdate()) {
                skippedCount++;
                continue;
            }
            
            // Check if version is different
            if (instance.getCurrentVersionId().equals(newVersion.getVersionId())) {
                skippedCount++;
                continue;
            }

            updateSingleInstance(template, instance, newVersion);
            updatedCount++;
        }
//        logger.info("Update batch complete. Processed: " + updatedCount + ", Skipped: " + skippedCount);
    }
    
    private void updateSingleInstance(Template template, Instance instance, TemplateVersion newVersion) {
        logger.info("Updating Instance [" + instance.getId() + "] (World: " + instance.getWorldId() + ") from " + instance.getCurrentVersionId() + " to " + newVersion.getVersionId());
        
        // Load Variations
        int rot = instance.getRotationY();
        boolean flipX = instance.isFlipX();
        boolean flipY = instance.isFlipY();
        boolean flipZ = instance.isFlipZ(); // Assumed FlipZ based on previous context, prompt said Z flip.
        
        Clipboard oldClip = loadTransformedClipboard(template, instance.getCurrentVersionId(), rot, flipX, flipY, flipZ);
        if (oldClip == null) {
            logger.warning("Skipping update for instance " + instance.getId() + ": Old version " + instance.getCurrentVersionId() + " not found.");
            return;
        }

        Clipboard newClip = loadTransformedClipboard(template, newVersion.getVersionId(), rot, flipX, flipY, flipZ);
        if (newClip == null) {
             logger.warning("Skipping update for instance " + instance.getId() + ": New version " + newVersion.getVersionId() + " not found.");
             return;
        }

        // Use Identity transform as clipboards are already transformed
        AffineTransform transform = new AffineTransform();

        BlockVector3 origin = BlockVector3.at(instance.getX(), instance.getY(), instance.getZ());
        
        World world = Bukkit.getWorld(instance.getWorldId());
        if (world == null) {
            logger.warning("Instance " + instance.getId() + " is in unloaded world " + instance.getWorldId() + ". Skipping.");
            return; // World not loaded
        }

        // 4. Calculate Diff
        Map<BlockVector3, BlockState> changes = diffCalculator.calculateDiff(oldClip, newClip, transform, origin, world);

        if (changes.isEmpty()) {
            // Just update version if no physical changes
            instance.setCurrentVersionId(newVersion.getVersionId());
            instanceRepository.saveInstance(instance);
            logger.info("Instance " + instance.getId() + " updated version ID (No physical block changes).");
            return;
        }
        
//        logger.info("Applying " + changes.size() + " block changes to instance " + instance.getId());


        // 5. Apply Changes
        try (
                EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))
        ) {
//            try(var changeSet = new BlockOptimizedHistory()){
//
//                session.setBlocks(changeSet, ChangeSetExecutor.Type.REDO);
//            }
            for (Map.Entry<BlockVector3, BlockState> entry : changes.entrySet()) {
                var pos = entry.getKey();
                session.setBlock(
                        pos.x(), pos.y(), pos.z(),
                        entry.getValue()
                );
            }
            // Session auto-flush on close
        } catch (Exception e) {
            logger.severe("Failed to apply updates to instance " + instance.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 6. Update Instance Record
        instance.setCurrentVersionId(newVersion.getVersionId());
        instanceRepository.saveInstance(instance);
//        logger.info("Successfully updated instance " + instance.getId() + " to version " + newVersion.getVersionId());
    }

    private Clipboard loadTransformedClipboard(Template template, String versionId, int rot, boolean flipX, boolean flipY, boolean flipZ) {
        // Load base and transform
        return template.getClipboard(versionId, rot, flipX, flipY, flipZ);
    }
}
