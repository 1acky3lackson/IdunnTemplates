package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.api.BackendApiClient;
import com.jackyblackson.idunntemplates.core.calc.BlockComparator;
import com.jackyblackson.idunntemplates.core.calc.DiffCalculator;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.util.EntityHelper;
import com.jackyblackson.idunntemplates.util.InstanceRegionUtil;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ProjectSettlementSyncManager {

    private final IdunnTemplates plugin;
    private final BackendApiClient backendApiClient;
    private final TemplateManager templateManager;
    private final InstanceRepository instanceRepository;
    private final BlockComparator blockComparator;
    private final DiffCalculator diffCalculator;
    private final Logger logger;
    private final Map<Long, Long> refreshingProjects = new ConcurrentHashMap<>();

    public ProjectSettlementSyncManager(
            IdunnTemplates plugin,
            BackendApiClient backendApiClient,
            TemplateManager templateManager,
            InstanceRepository instanceRepository,
            BlockComparator blockComparator,
            Logger logger
    ) {
        this.plugin = plugin;
        this.backendApiClient = backendApiClient;
        this.templateManager = templateManager;
        this.instanceRepository = instanceRepository;
        this.blockComparator = blockComparator;
        this.diffCalculator = new DiffCalculator(blockComparator, logger);
        this.logger = logger;
    }

    public void refreshProject(long projectId) {
        if (refreshingProjects.putIfAbsent(projectId, System.currentTimeMillis()) != null) {
            return;
        }

        backendApiClient.getProject(projectId).thenAccept(project -> {
            if (project == null) {
                refreshingProjects.remove(projectId);
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    BackendApiClient.ProjectSettlementSnapshotRequest payload = scanProject(project);
                    backendApiClient.uploadProjectSettlementSnapshot(projectId, payload)
                            .whenComplete((ignored, throwable) -> refreshingProjects.remove(projectId));
                } catch (Exception ex) {
                    logger.warning("Failed to scan project " + projectId + ": " + ex.getMessage());
                    refreshingProjects.remove(projectId);
                }
            });
        }).exceptionally(ex -> {
            logger.warning("Failed to fetch project " + projectId + " before settlement sync: " + ex.getMessage());
            refreshingProjects.remove(projectId);
            return null;
        });
    }

    public void refreshProjectsOverlappingInstance(Instance instance) {
        try {
            Template template = templateManager.getTemplate(instance.getTemplateId());
            if (template == null) return;

            Clipboard clipboard = EntityHelper.getClipboard(
                    template,
                    instance.getCurrentVersionId(),
                    instance.getRotationY(),
                    instance.isFlipX(),
                    instance.isFlipY(),
                    instance.isFlipZ()
            );
            if (clipboard == null) return;

            World world = Bukkit.getWorld(instance.getWorldId());
            if (world == null) return;

            var region = InstanceRegionUtil.calculateWorldRegion(
                    clipboard,
                    new Location(world, instance.getX(), instance.getY(), instance.getZ()),
                    instance
            );

            backendApiClient.findOverlappingProjects(
                    world.getName(),
                    region.getMinimumPoint().x(),
                    region.getMinimumPoint().y(),
                    region.getMinimumPoint().z(),
                    region.getMaximumPoint().x(),
                    region.getMaximumPoint().y(),
                    region.getMaximumPoint().z()
            ).thenAccept(projects -> {
                if (projects == null) return;
                for (BackendApiClient.ProjectDetails project : projects) {
                    if (project != null && project.id != null) {
                        refreshProject(project.id);
                    }
                }
            }).exceptionally(ex -> {
                logger.warning("Failed to find overlapping projects for instance " + instance.getId() + ": " + ex.getMessage());
                return null;
            });
        } catch (Exception ex) {
            logger.warning("Failed to trigger settlement sync for instance " + instance.getId() + ": " + ex.getMessage());
        }
    }

    private BackendApiClient.ProjectSettlementSnapshotRequest scanProject(BackendApiClient.ProjectDetails project) {
        World world = resolveWorld(project);
        validateBounds(project);

        BackendApiClient.ProjectSettlementSnapshotRequest payload = new BackendApiClient.ProjectSettlementSnapshotRequest();
        payload.projectEffectiveBlocks = countProjectEffectiveBlocks(world, project);
        payload.scannedAtMs = System.currentTimeMillis();
        payload.sourceServerName = Bukkit.getServer().getName();
        payload.templates = new ArrayList<>();
        payload.instances = new ArrayList<>();

        List<Instance> instances = instanceRepository.getActiveInstancesInWorld(world.getUID()).join();
        Map<UUID, BackendApiClient.ProjectTemplateUsageSnapshot> grouped = new LinkedHashMap<>();

        for (Instance instance : instances) {
            Template template = templateManager.getTemplate(instance.getTemplateId());
            if (template == null) continue;

            TemplateVersion version = template.getVersions().stream()
                    .filter(item -> item.getVersionId().equals(instance.getCurrentVersionId()))
                    .findFirst()
                    .orElseGet(template::getLatestVersion);
            if (version == null) continue;

            Clipboard clipboard = EntityHelper.getClipboard(
                    template,
                    version.getVersionId(),
                    instance.getRotationY(),
                    instance.isFlipX(),
                    instance.isFlipY(),
                    instance.isFlipZ()
            );
            if (clipboard == null) continue;

            var instanceRegion = InstanceRegionUtil.calculateWorldRegion(
                    clipboard,
                    new Location(world, instance.getX(), instance.getY(), instance.getZ()),
                    instance
            );
            if (!overlaps(project, instanceRegion)) continue;

            long managedBlocks = countManagedBlocksInsideProject(world, project, clipboard, instance);
            payload.instances.add(toInstanceUsage(instance, template, version, instanceRegion, managedBlocks));

            BackendApiClient.ProjectTemplateUsageSnapshot templateUsage = grouped.computeIfAbsent(
                    template.getId(),
                    ignored -> {
                        BackendApiClient.ProjectTemplateUsageSnapshot created = new BackendApiClient.ProjectTemplateUsageSnapshot();
                        created.templateId = template.getId().toString();
                        created.templateName = template.getName();
                        return created;
                    }
            );
            templateUsage.usageCount += 1;
            templateUsage.totalManagedBlocks += managedBlocks;

            BackendApiClient.ProjectTemplateVersionUsageSnapshot versionUsage = templateUsage.versions.stream()
                    .filter(item -> version.getVersionId().equals(item.versionId))
                    .findFirst()
                    .orElseGet(() -> {
                        BackendApiClient.ProjectTemplateVersionUsageSnapshot created = new BackendApiClient.ProjectTemplateVersionUsageSnapshot();
                        created.versionId = version.getVersionId();
                        templateUsage.versions.add(created);
                        return created;
                    });
            versionUsage.usageCount += 1;
            versionUsage.totalManagedBlocks += managedBlocks;
        }

        payload.templates.addAll(grouped.values());
        return payload;
    }

    private BackendApiClient.ProjectInstanceUsageSnapshot toInstanceUsage(
            Instance instance,
            Template template,
            TemplateVersion version,
            com.sk89q.worldedit.regions.Region instanceRegion,
            long managedBlocks
    ) {
        BackendApiClient.ProjectInstanceUsageSnapshot usage = new BackendApiClient.ProjectInstanceUsageSnapshot();
        usage.instanceId = instance.getId();
        usage.templateId = template.getId().toString();
        usage.templateName = template.getName();
        usage.versionId = version.getVersionId();
        usage.placedByName = instance.getPlacedByName();
        usage.placedAt = instance.getPlacedAt();
        usage.minX = instanceRegion.getMinimumPoint().x();
        usage.minY = instanceRegion.getMinimumPoint().y();
        usage.minZ = instanceRegion.getMinimumPoint().z();
        usage.maxX = instanceRegion.getMaximumPoint().x();
        usage.maxY = instanceRegion.getMaximumPoint().y();
        usage.maxZ = instanceRegion.getMaximumPoint().z();
        usage.managedBlocks = managedBlocks;
        return usage;
    }

    private World resolveWorld(BackendApiClient.ProjectDetails project) {
        World world = null;
        if (project.worldMountName != null && !project.worldMountName.isBlank()) {
            world = Bukkit.getWorld(project.worldMountName);
        }
        if (world == null && project.worldName != null && !project.worldName.isBlank()) {
            world = Bukkit.getWorld(project.worldName);
        }
        if (world == null) {
            throw new IllegalStateException("World not found for project #" + project.id);
        }
        return world;
    }

    private void validateBounds(BackendApiClient.ProjectDetails project) {
        if (project.minX == null || project.minY == null || project.minZ == null
                || project.maxX == null || project.maxY == null || project.maxZ == null) {
            throw new IllegalStateException("Project bounds are incomplete for project #" + project.id);
        }
    }

    private long countProjectEffectiveBlocks(World world, BackendApiClient.ProjectDetails project) {
        long total = 0L;
        for (int x = project.minX; x <= project.maxX; x++) {
            for (int y = project.minY; y <= project.maxY; y++) {
                for (int z = project.minZ; z <= project.maxZ; z++) {
                    if (!blockComparator.isIdle(world.getBlockAt(x, y, z))) {
                        total++;
                    }
                }
            }
        }
        return total;
    }

    private long countManagedBlocksInsideProject(
            World world,
            BackendApiClient.ProjectDetails project,
            Clipboard clipboard,
            Instance instance
    ) {
        Set<BlockVector3> managedBlocks = diffCalculator.calculateManagedBlocks(
                clipboard,
                new AffineTransform(),
                BlockVector3.at(instance.getX(), instance.getY(), instance.getZ()),
                world,
                instance
        );
        return managedBlocks.stream()
                .filter(pos -> pos.x() >= project.minX && pos.x() <= project.maxX)
                .filter(pos -> pos.y() >= project.minY && pos.y() <= project.maxY)
                .filter(pos -> pos.z() >= project.minZ && pos.z() <= project.maxZ)
                .count();
    }

    private boolean overlaps(BackendApiClient.ProjectDetails project, com.sk89q.worldedit.regions.Region instanceRegion) {
        return project.minX <= instanceRegion.getMaximumPoint().x() && project.maxX >= instanceRegion.getMinimumPoint().x()
                && project.minY <= instanceRegion.getMaximumPoint().y() && project.maxY >= instanceRegion.getMinimumPoint().y()
                && project.minZ <= instanceRegion.getMaximumPoint().z() && project.maxZ >= instanceRegion.getMinimumPoint().z();
    }
}
