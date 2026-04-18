package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Logger;

public class InstanceUpdateScheduler {

    private static final double MAX_BUDGET_MS = 12.0d;
    private static final double MEDIUM_BUDGET_MS = 8.0d;
    private static final double LOW_BUDGET_MS = 5.0d;
    private static final double MIN_BUDGET_MS = 2.0d;
    private static final double ADDITIVE_STEP_MS = 1.0d;
    private static final double INITIAL_WINDOW_MS = 2.0d;
    private static final double MIN_RECOVERY_WINDOW_MS = 1.0d;
    private static final double TPS_GROWTH_THRESHOLD = 16.0d;
    private static final double TPS_HALVE_THRESHOLD = 10.0d;

    private static final int MAX_BATCH_SIZE_PER_WORLD = 24;

    private final TemplateManager templateManager;
    private final TemplateUpdater templateUpdater;
    private final Logger logger;
    private final Object lock = new Object();
    private final Queue<String> taskOrder = new ArrayDeque<>();
    private final Map<String, InstanceUpdateTask> pendingTasks = new LinkedHashMap<>();

    private double congestionWindowMs = INITIAL_WINDOW_MS;
    private double slowStartThresholdMs = MEDIUM_BUDGET_MS;
    private boolean queuePreviouslyNonEmpty = false;

    public InstanceUpdateScheduler(TemplateManager templateManager, TemplateUpdater templateUpdater, Logger logger) {
        this.templateManager = templateManager;
        this.templateUpdater = templateUpdater;
        this.logger = logger;
    }

    public void startTask() {
        IdunnTemplates.getInstance().getServer().getScheduler()
                .runTaskTimer(IdunnTemplates.getInstance(), this::processQueue, 1L, 1L);
    }

    public void enqueueTemplateInstances(Template template, TemplateVersion newVersion, String source) {
        IdunnTemplates.getInstance().getInstanceRepository()
                .getActiveInstancesByTemplate(template.getId())
                .thenAccept(instances -> {
                    for (Instance instance : instances) {
                        enqueueInstanceUpdate(template, instance, newVersion, source);
                    }
                })
                .exceptionally(ex -> {
                    logger.warning("Failed to enqueue template instances for update: " + ex.getMessage());
                    return null;
                });
    }

    public void enqueueInstanceUpdate(Template template, Instance instance, TemplateVersion newVersion, String source) {
        if (template == null || instance == null || newVersion == null) {
            return;
        }
        if (!instance.isAutoUpdate()) {
            return;
        }

        String taskKey = instance.getId();
        synchronized (lock) {
            InstanceUpdateTask existing = pendingTasks.get(taskKey);
            if (existing != null) {
                existing.refresh(template.getId(), newVersion.getVersionId(), instance, source);
                return;
            }
            pendingTasks.put(taskKey, new InstanceUpdateTask(
                    instance.getId(),
                    template.getId(),
                    newVersion.getVersionId(),
                    instance,
                    source
            ));
            taskOrder.offer(taskKey);
        }
    }

    public boolean hasPendingTask(String instanceId) {
        synchronized (lock) {
            return pendingTasks.containsKey(instanceId);
        }
    }

    private void processQueue() {
        if (hasNoPendingTasks()) {
            if (queuePreviouslyNonEmpty) {
                queuePreviouslyNonEmpty = false;
                logger.info("Instance update scheduler queue drained completely; no backlog remains.");
            }
            return;
        }
        queuePreviouslyNonEmpty = true;

        double tps = readTps();
        double budgetCapMs = determineBudgetCap(tps);
        if (budgetCapMs <= 0.0d) {
            onReset();
            return;
        }

        adjustWindowForTps(tps, budgetCapMs);

        long budgetNs = (long) (congestionWindowMs * 1_000_000.0d);
        long startedAt = System.nanoTime();

        List<InstanceUpdateTask> reserved = reserveTasksForTick();
        if (reserved.isEmpty()) {
            return;
        }

        Map<UUID, List<InstanceUpdateTask>> groupedByWorld = groupTasksByWorld(reserved);
        for (Map.Entry<UUID, List<InstanceUpdateTask>> entry : groupedByWorld.entrySet()) {
            UUID worldId = entry.getKey();
            World world = Bukkit.getWorld(worldId);
            List<InstanceUpdateTask> tasks = entry.getValue();

            if (world == null) {
                for (InstanceUpdateTask task : tasks) {
                    task.retryCount++;
                    if (task.retryCount <= 20) {
                        requeue(task);
                    } else {
                        logger.warning("Dropping instance update task for " + task.instanceId
                                + " because world " + task.instance.getWorldId()
                                + " is not loaded after multiple retries.");
                    }
                }
                continue;
            }

            try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                for (InstanceUpdateTask task : tasks) {
                    processTask(task, session);

                    long elapsedNs = System.nanoTime() - startedAt;
                    if (elapsedNs >= budgetNs) {
                        requeueRemaining(groupedByWorld, worldId, tasks, task);
                        return;
                    }
                }
            } catch (Exception e) {
                logger.severe("Failed to execute batched instance updates in world "
                        + world.getName() + ": " + e.getMessage());
                e.printStackTrace();
                requeueAll(tasks);
            }
        }
    }

    private void adjustWindowForTps(double tps, double budgetCapMs) {
        if (congestionWindowMs <= 0.0d) {
            congestionWindowMs = INITIAL_WINDOW_MS;
        }

        if (!Double.isNaN(tps) && tps < TPS_HALVE_THRESHOLD) {
            onReset();
            return;
        }

        if (!Double.isNaN(tps) && tps < TPS_GROWTH_THRESHOLD) {
            congestionWindowMs = Math.max(MIN_RECOVERY_WINDOW_MS, congestionWindowMs / 2.0d);
            slowStartThresholdMs = Math.max(MIN_RECOVERY_WINDOW_MS, congestionWindowMs);
            congestionWindowMs = Math.min(congestionWindowMs, budgetCapMs);
            return;
        }

        if (congestionWindowMs < slowStartThresholdMs) {
            congestionWindowMs = Math.min(budgetCapMs, congestionWindowMs * 2.0d);
        } else {
            congestionWindowMs = Math.min(budgetCapMs, congestionWindowMs + ADDITIVE_STEP_MS);
        }
    }

    private void onReset() {
        slowStartThresholdMs = Math.max(MIN_RECOVERY_WINDOW_MS, congestionWindowMs / 2.0d);
        congestionWindowMs = 0.0d;
    }

    private double determineBudgetCap(double tps) {
        if (Double.isNaN(tps) || tps >= 18.0d) {
            return MAX_BUDGET_MS;
        }
        if (tps >= 16.0d) {
            return MEDIUM_BUDGET_MS;
        }
        if (tps >= 10.0d) {
            return LOW_BUDGET_MS;
        }
        return 0.0d;
    }

    private List<InstanceUpdateTask> reserveTasksForTick() {
        List<InstanceUpdateTask> reserved = new ArrayList<>();
        synchronized (lock) {
            while (true) {
                String taskKey = taskOrder.poll();
                if (taskKey == null) {
                    break;
                }
                InstanceUpdateTask task = pendingTasks.remove(taskKey);
                if (task != null) {
                    reserved.add(task);
                }
            }
        }
        return reserved;
    }

    private Map<UUID, List<InstanceUpdateTask>> groupTasksByWorld(List<InstanceUpdateTask> tasks) {
        Map<UUID, List<InstanceUpdateTask>> grouped = new LinkedHashMap<>();
        List<InstanceUpdateTask> overflow = new ArrayList<>();

        for (InstanceUpdateTask task : tasks) {
            List<InstanceUpdateTask> worldTasks = grouped.computeIfAbsent(task.instance.getWorldId(), ignored -> new ArrayList<>());
            if (worldTasks.size() >= MAX_BATCH_SIZE_PER_WORLD) {
                overflow.add(task);
                continue;
            }
            worldTasks.add(task);
        }

        if (!overflow.isEmpty()) {
            requeueAll(overflow);
        }

        return grouped;
    }

    private boolean hasNoPendingTasks() {
        synchronized (lock) {
            return pendingTasks.isEmpty() && taskOrder.isEmpty();
        }
    }

    private void processTask(InstanceUpdateTask task, EditSession session) {
        Template template = templateManager.getTemplate(task.templateId);
        if (template == null) {
            logger.warning("Dropping instance update task because template " + task.templateId + " no longer exists.");
            return;
        }

        TemplateVersion targetVersion = template.getMetadata().getVersions().stream()
                .filter(version -> version.getVersionId().equals(task.targetVersionId))
                .findFirst()
                .orElseGet(template::getLatestVersion);
        if (targetVersion == null) {
            logger.warning("Dropping instance update task because target version no longer exists. Template=" + template.getPath());
            return;
        }

        if (task.instance.isDeleted()) {
            return;
        }
        if (!task.instance.isAutoUpdate()) {
            return;
        }
        if (targetVersion.getVersionId().equals(task.instance.getCurrentVersionId())) {
            return;
        }

        try {
            templateUpdater.updateSingleInstance(template, task.instance, targetVersion, session);
        } catch (Exception e) {
            logger.severe("Failed to execute instance update task " + task.instanceId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void requeueRemaining(
            Map<UUID, List<InstanceUpdateTask>> groupedByWorld,
            UUID currentWorldId,
            List<InstanceUpdateTask> currentWorldTasks,
            InstanceUpdateTask processedTask
    ) {
        boolean inCurrentWorld = false;
        for (Map.Entry<UUID, List<InstanceUpdateTask>> entry : groupedByWorld.entrySet()) {
            List<InstanceUpdateTask> tasks = entry.getValue();

            if (!inCurrentWorld) {
                if (!entry.getKey().equals(currentWorldId)) {
                    continue;
                }
                inCurrentWorld = true;
                boolean pastProcessed = false;
                for (InstanceUpdateTask task : tasks) {
                    if (!pastProcessed) {
                        if (task == processedTask) {
                            pastProcessed = true;
                        }
                        continue;
                    }
                    requeue(task);
                }
                continue;
            }

            requeueAll(tasks);
        }
    }

    private void requeueAll(List<InstanceUpdateTask> tasks) {
        for (InstanceUpdateTask task : tasks) {
            requeue(task);
        }
    }

    private void requeue(InstanceUpdateTask task) {
        synchronized (lock) {
            InstanceUpdateTask existing = pendingTasks.get(task.instanceId);
            if (existing != null) {
                existing.refresh(task.templateId, task.targetVersionId, task.instance, task.source);
                return;
            }
            pendingTasks.put(task.instanceId, task);
            taskOrder.offer(task.instanceId);
        }
    }

    private double readTps() {
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getTPS");
            Object result = method.invoke(Bukkit.getServer());
            if (result instanceof double[] values && values.length > 0) {
                return values[0];
            }
        } catch (Exception ignored) {
        }
        try {
            Method method = Bukkit.getServer().getClass().getMethod("recentTps");
            Object result = method.invoke(Bukkit.getServer());
            if (result instanceof double[] values && values.length > 0) {
                return values[0];
            }
        } catch (Exception ignored) {
        }

        double mspt = readMspt();
        if (Double.isNaN(mspt) || mspt <= 0.0d) {
            return Double.NaN;
        }
        return Math.min(20.0d, 1000.0d / mspt);
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

    private static final class InstanceUpdateTask {
        private final String instanceId;
        private UUID templateId;
        private String targetVersionId;
        private Instance instance;
        private String source;
        private int retryCount = 0;

        private InstanceUpdateTask(String instanceId, UUID templateId, String targetVersionId, Instance instance, String source) {
            this.instanceId = instanceId;
            this.templateId = templateId;
            this.targetVersionId = targetVersionId;
            this.instance = instance;
            this.source = source;
        }

        private void refresh(UUID templateId, String targetVersionId, Instance instance, String source) {
            this.templateId = templateId;
            this.targetVersionId = targetVersionId;
            this.instance = instance;
            this.source = source == null ? this.source : source;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "InstanceUpdateTask{id=%s, template=%s, version=%s, source=%s}",
                    instanceId, templateId, targetVersionId, source);
        }
    }
}
