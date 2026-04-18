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
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class ProjectSettlementSyncManager {

    private static final long REFRESH_INTERVAL_TICKS = 20L * 60L;
    private static final double ABSOLUTELY_HEALTHY_MSPT = 25.0d;

    private final IdunnTemplates plugin;
    private final BackendApiClient backendApiClient;
    private final TemplateManager templateManager;
    private final InstanceRepository instanceRepository;
    private final BlockComparator blockComparator;
    private final DiffCalculator diffCalculator;
    private final ProjectCatalogManager projectCatalogManager;
    private final Logger logger;
    private final Map<Long, Long> refreshingProjects = new ConcurrentHashMap<>();
    private final Map<Long, DirtyProjectState> dirtyProjects = new ConcurrentHashMap<>();
    private final Queue<Long> dirtyOrder = new ConcurrentLinkedQueue<>();
    private final Object dirtyLock = new Object();
    private BukkitTask refreshTask;

    public ProjectSettlementSyncManager(
            IdunnTemplates plugin,
            BackendApiClient backendApiClient,
            TemplateManager templateManager,
            InstanceRepository instanceRepository,
            BlockComparator blockComparator,
            ProjectCatalogManager projectCatalogManager,
            Logger logger
    ) {
        this.plugin = plugin;
        this.backendApiClient = backendApiClient;
        this.templateManager = templateManager;
        this.instanceRepository = instanceRepository;
        this.blockComparator = blockComparator;
        this.diffCalculator = new DiffCalculator(blockComparator, logger);
        this.projectCatalogManager = projectCatalogManager;
        this.logger = logger;
    }

    public void startTask() {
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        refreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::processDirtyProjects,
                REFRESH_INTERVAL_TICKS,
                REFRESH_INTERVAL_TICKS
        );
    }

    public void stopTask() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    public CompletableFuture<Boolean> refreshProject(long projectId) {
        return refreshProject(projectId, RefreshSource.MANUAL);
    }

    public void markProjectDirty(long projectId) {
        markProjectDirty(projectId, "unspecified");
    }

    public void markProjectDirty(long projectId, String reason) {
        synchronized (dirtyLock) {
            DirtyProjectState state = dirtyProjects.computeIfAbsent(projectId, ignored -> new DirtyProjectState(projectId));
            state.mark(reason);
            if (!state.queued) {
                state.queued = true;
                dirtyOrder.offer(projectId);
            }
        }
    }

    public void markProjectsDirty(Collection<Long> projectIds, String reason) {
        if (projectIds == null) {
            return;
        }
        for (Long projectId : projectIds) {
            if (projectId != null) {
                markProjectDirty(projectId, reason);
            }
        }
    }

    public void markProjectsOverlappingRegion(
            String worldName,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ,
            String reason
    ) {
        List<BackendApiClient.ProjectDetails> cachedProjects = projectCatalogManager.getCachedProjects();
        if (cachedProjects.isEmpty()) {
            return;
        }

        Set<Long> overlapping = new LinkedHashSet<>();
        for (BackendApiClient.ProjectDetails project : cachedProjects) {
            if (project == null || project.id == null || !projectCatalogManager.hasBounds(project)) {
                continue;
            }
            if (!matchesWorld(project, worldName)) {
                continue;
            }
            if (project.minX <= maxX && project.maxX >= minX
                    && project.minY <= maxY && project.maxY >= minY
                    && project.minZ <= maxZ && project.maxZ >= minZ) {
                overlapping.add(project.id);
            }
        }

        if (!overlapping.isEmpty()) {
            markProjectsDirty(overlapping, reason);
        }
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

            markProjectsOverlappingRegion(
                    world.getName(),
                    region.getMinimumPoint().x(),
                    region.getMinimumPoint().y(),
                    region.getMinimumPoint().z(),
                    region.getMaximumPoint().x(),
                    region.getMaximumPoint().y(),
                    region.getMaximumPoint().z(),
                    "instance-overlap:" + instance.getId()
            );
        } catch (Exception ex) {
            logger.warning("Failed to mark dirty projects for instance " + instance.getId() + ": " + ex.getMessage());
        }
    }

    private void processDirtyProjects() {
        double mspt = readMspt();
        if (!Double.isNaN(mspt) && mspt > ABSOLUTELY_HEALTHY_MSPT) {
            return;
        }

        Long projectId = pollNextDirtyProject();
        if (projectId == null) {
            return;
        }

        refreshProject(projectId, RefreshSource.SCHEDULED).thenAccept(success -> {
            if (!success) {
                markProjectDirty(projectId, "scheduled-retry");
            }
        });
    }

    private CompletableFuture<Boolean> refreshProject(long projectId, RefreshSource source) {
        if (refreshingProjects.putIfAbsent(projectId, System.currentTimeMillis()) != null) {
            return CompletableFuture.completedFuture(false);
        }

        CompletableFuture<BackendApiClient.ProjectDetails> detailsFuture = resolveProject(projectId);
        return detailsFuture.thenCompose(project -> {
            if (project == null) {
                refreshingProjects.remove(projectId);
                return CompletableFuture.completedFuture(false);
            }

            CompletableFuture<Boolean> result = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    BackendApiClient.ProjectSettlementSnapshotRequest payload = scanProject(project);
                    backendApiClient.uploadProjectSettlementSnapshot(projectId, payload)
                            .whenComplete((success, throwable) -> {
                                refreshingProjects.remove(projectId);
                                boolean completed = throwable == null && Boolean.TRUE.equals(success);
                                if (completed) {
                                    clearDirtyProject(projectId);
                                } else if (throwable != null) {
                                    logger.warning("Failed to upload project snapshot for #" + projectId + " from " + source.name().toLowerCase() + ": " + throwable.getMessage());
                                } else {
                                    logger.warning("Project snapshot upload returned failure for #" + projectId + " from " + source.name().toLowerCase());
                                }
                                result.complete(completed);
                            });
                } catch (Exception ex) {
                    logger.warning("Failed to scan project " + projectId + " from " + source.name().toLowerCase() + ": " + ex.getMessage());
                    refreshingProjects.remove(projectId);
                    result.complete(false);
                }
            });
            return result;
        }).exceptionally(ex -> {
            logger.warning("Failed to resolve project " + projectId + " before settlement sync: " + ex.getMessage());
            refreshingProjects.remove(projectId);
            return false;
        });
    }

    private CompletableFuture<BackendApiClient.ProjectDetails> resolveProject(long projectId) {
        BackendApiClient.ProjectDetails cached = projectCatalogManager.getCachedProjects().stream()
                .filter(project -> project != null && project.id != null && project.id == projectId)
                .findFirst()
                .orElse(null);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return backendApiClient.getProject(projectId);
    }

    private Long pollNextDirtyProject() {
        synchronized (dirtyLock) {
            while (true) {
                Long projectId = dirtyOrder.poll();
                if (projectId == null) {
                    return null;
                }
                DirtyProjectState state = dirtyProjects.get(projectId);
                if (state == null) {
                    continue;
                }
                state.queued = false;
                return projectId;
            }
        }
    }

    private void clearDirtyProject(long projectId) {
        synchronized (dirtyLock) {
            dirtyProjects.remove(projectId);
        }
    }

    private boolean matchesWorld(BackendApiClient.ProjectDetails project, String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return false;
        }
        return worldName.equals(project.worldName) || worldName.equals(project.worldMountName);
    }

    private double readMspt() {
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getAverageTickTime");
            Object result = method.invoke(Bukkit.getServer());
            if (result instanceof Number number) {
                return number.doubleValue();
            }
        } catch (Exception ignored) {
        }
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getAverageTickMillis");
            Object result = method.invoke(Bukkit.getServer());
            if (result instanceof Number number) {
                return number.doubleValue();
            }
        } catch (Exception ignored) {
        }
        return Double.NaN;
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

    private enum RefreshSource {
        MANUAL,
        SCHEDULED
    }

    private static final class DirtyProjectState {
        private final long projectId;
        private long firstMarkedAtMs;
        private long lastMarkedAtMs;
        private int markCount;
        private String lastReason = "unspecified";
        private boolean queued;

        private DirtyProjectState(long projectId) {
            this.projectId = projectId;
            long now = System.currentTimeMillis();
            this.firstMarkedAtMs = now;
            this.lastMarkedAtMs = now;
            this.markCount = 0;
        }

        private void mark(String reason) {
            long now = System.currentTimeMillis();
            if (markCount == 0) {
                firstMarkedAtMs = now;
            }
            lastMarkedAtMs = now;
            markCount++;
            if (reason != null && !reason.isBlank()) {
                lastReason = reason;
            }
        }
    }
}
