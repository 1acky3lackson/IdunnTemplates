package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.calc.BlockComparator;
import com.jackyblackson.idunntemplates.core.calc.DiffCalculator;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.jackyblackson.idunntemplates.util.EntityHelper;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TemplateUpdater {

    private final TemplateStorage templateStorage;
    private final InstanceRepository instanceRepository;
    private final DiffCalculator diffCalculator;
    private final Logger logger;
    private CascadingUpdateManager cascadingUpdateManager;

    public TemplateUpdater(TemplateStorage templateStorage, InstanceRepository instanceRepository, BlockComparator comparator, Logger logger) {
        this.templateStorage = templateStorage;
        this.instanceRepository = instanceRepository;
        this.diffCalculator = new DiffCalculator(comparator, logger);
        this.logger = logger;
    }

    public void setCascadingUpdateManager(CascadingUpdateManager cascadingUpdateManager) {
        this.cascadingUpdateManager = cascadingUpdateManager;
    }
    
    public CascadingUpdateManager getCascadingUpdateManager() {
        return cascadingUpdateManager;
    }
    
    // Triggered by manual commit or scheduled check
    public void updateInstances(Template template, TemplateVersion newVersion, List<Instance> instances) {
        logger.info("Starting update for template: " + template.getName() + " -> Ver: " + newVersion.getVersionId() + ". Target instances: " + instances.size());
        AtomicInteger updatedCount = new AtomicInteger();
        AtomicInteger skippedCount = new AtomicInteger();

        Map<UUID, List<Instance>> result = instances.stream()
                .collect(Collectors.groupingBy(Instance::getWorldId));
        result.forEach((worldId, instanceList) -> {
            World world = Bukkit.getWorld(worldId);
            if (world == null) {
                skippedCount.getAndAdd(instanceList.size());
                logger.warning("Skipped updating " + instanceList.size() + " instances in world with id " + worldId + " because the world no longer exists. The instance ids:");
                instanceList.forEach(instance -> {
                    logger.warning(instance.getId());
                });
            }
            try (
                    EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))
            ) {

                for (Instance instance : instances) {
                    // Only update if auto-update is on
                    if (!instance.isAutoUpdate()) {
                        skippedCount.getAndIncrement();
                        continue;
                    }

                    // Check if version is different
                    if (instance.getCurrentVersionId().equals(newVersion.getVersionId())) {
                        skippedCount.getAndIncrement();
                        continue;
                    }

                    if (!EntityHelper.canUpdate(instance)) {
                        skippedCount.getAndIncrement();
                        logger.info("Skip updating for instance because its parent is locked. Instance: " + instance.getId());
                        continue;
                    }

                    updateSingleInstance(template, instance, newVersion, session);
                    updatedCount.getAndIncrement();
                }
            } catch (Exception e) {
                logger.severe("Failed to apply updates to a instance because: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        });

//        logger.info("Update batch complete. Processed: " + updatedCount + ", Skipped: " + skippedCount);
    }
    
    public void updateSingleInstance(Template template, Instance instance, TemplateVersion newVersion, EditSession session) {
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
        Map<BlockVector3, BlockState> changes = diffCalculator.calculateDiff(oldClip, newClip, transform, origin, world, instance);

        if (changes.isEmpty()) {
            // Just update version if no physical changes
            instance.setCurrentVersionId(newVersion.getVersionId());
            instanceRepository.saveInstance(instance);
            logger.info("Instance " + instance.getId() + " updated version ID (No physical block changes).");
            return;
        }
        
//        logger.info("Applying " + changes.size() + " block changes to instance " + instance.getId());


        // 5. Apply Changes
//        try (
//                EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))
//        ) {
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
//        } catch (Exception e) {
//            logger.severe("Failed to apply updates to instance " + instance.getId() + ": " + e.getMessage());
//            e.printStackTrace();
//            return;
//        }

        // 6. Update Instance Record
        instance.setCurrentVersionId(newVersion.getVersionId());
        instanceRepository.saveInstance(instance);
        
        // V2 FIX: Sync version to Child Template Metadata (parentTemplateInstances)
        // This ensures that the child template knows its instance in the parent has been updated.
        TemplateManager tm = getTemplateManager();
        if (tm != null && instance.getEmbeddedInTemplateId() != null) {
            // "template" here is the template of the instance (Child Template)
            // But 'template' passed to this method IS the child template.
            
            // We need to update the list in childTemplate -> parentTemplateInstances -> get(parentId)
            com.jackyblackson.idunntemplates.core.domain.TemplateMetadata meta = template.getMetadata();
            java.util.List<Instance> instancesInParent = meta.getParentTemplateInstances().get(instance.getEmbeddedInTemplateId());
            
            if (instancesInParent != null) {
                boolean modified = false;
                for (Instance storedInst : instancesInParent) {
                    if (storedInst.getId().equals(instance.getId())) {
                        storedInst.setCurrentVersionId(newVersion.getVersionId());
                        modified = true;
                    }
                }
                if (modified) {
                    tm.saveTemplateMetadata(template);
                }
            }
        }
        
        // V2 FIX: Also Sync with Parent Template Metadata (childTemplateInstances)
        // This ensures the parent template knows its child instance has been updated.
        if (tm != null && instance.getEmbeddedInTemplateId() != null) {
            Template parentTemplate = tm.getTemplate(instance.getEmbeddedInTemplateId());
            if (parentTemplate != null) {
                com.jackyblackson.idunntemplates.core.domain.TemplateMetadata pMeta = parentTemplate.getMetadata();
                java.util.List<Instance> instancesInChildMap = pMeta.getChildTemplateInstances().get(template.getId());
                
                if (instancesInChildMap != null) {
                    boolean modified = false;
                    for (Instance storedInst : instancesInChildMap) {
                        if (storedInst.getId().equals(instance.getId())) {
                            storedInst.setCurrentVersionId(newVersion.getVersionId());
                            modified = true;
                        }
                    }
                    if (modified) {
                        tm.saveTemplateMetadata(parentTemplate);
                    }
                }
            }
        }
        
        logger.info("Successfully updated instance " + instance.getId() + " to version " + newVersion.getVersionId());

        // 7. Trigger Cascading Update (Phase 4)
        if (cascadingUpdateManager != null && !instance.isWild()) {
            TemplateManager tm2 = getTemplateManager();
            if (tm2 != null) {
                Template parent = tm2.getTemplate(instance.getEmbeddedInTemplateId());
                if (parent != null && !parent.getMetadata().isLocked()) {
                    cascadingUpdateManager.scheduleUpdate(parent.getId());
                }
            }
        }
    }

    private TemplateManager getTemplateManager() {
        return IdunnTemplates.getInstance().getTemplateManager();
    }

    private Clipboard loadTransformedClipboard(Template template, String versionId, int rot, boolean flipX, boolean flipY, boolean flipZ) {
        // Load base and transform
        return EntityHelper.getClipboard(template, versionId, rot, flipX, flipY, flipZ);
    }
}
