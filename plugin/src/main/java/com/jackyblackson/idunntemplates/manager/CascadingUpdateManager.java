package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class CascadingUpdateManager {

    private static final int MAX_BATCH_SIZE = 20;
    private static final int INITIAL_BATCH_SIZE = 1;
    private static final double TPS_GROWTH_THRESHOLD = 16.0d;
    private static final double TPS_HALVE_THRESHOLD = 10.0d;

    private final TemplateManager templateManager;
    private final InstanceUpdateScheduler instanceUpdateScheduler;
    private final Logger logger;
    private final Queue<UUID> updateQueue = new LinkedBlockingQueue<>();
    private final Set<UUID> pendingUpdates = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private int currentBatchSize = 0;
    private boolean queuePreviouslyNonEmpty = false;

    public CascadingUpdateManager(TemplateManager templateManager, InstanceUpdateScheduler instanceUpdateScheduler, Logger logger) {
        this.templateManager = templateManager;
        this.instanceUpdateScheduler = instanceUpdateScheduler;
        this.logger = logger;
    }

    public void startTask() {
        IdunnTemplates.getInstance().getServer().getScheduler()
                .runTaskTimer(IdunnTemplates.getInstance(), this::processQueue, 20L, 5L);
    }

    public void scheduleUpdate(UUID parentTemplateId) {
        if (parentTemplateId == null) {
            return;
        }
        if (!pendingUpdates.add(parentTemplateId)) {
            return;
        }
        updateQueue.offer(parentTemplateId);
    }

    private void processQueue() {
        if (updateQueue.isEmpty() && pendingUpdates.isEmpty()) {
            if (queuePreviouslyNonEmpty) {
                queuePreviouslyNonEmpty = false;
                logger.info("Cascading update scheduler queue drained completely; no backlog remains.");
            }
            return;
        }
        queuePreviouslyNonEmpty = true;

        double tps = readTps();
        if (!Double.isNaN(tps) && tps < TPS_HALVE_THRESHOLD) {
            currentBatchSize = 0;
            return;
        }
        if (!Double.isNaN(tps) && tps < TPS_GROWTH_THRESHOLD) {
            currentBatchSize = Math.max(INITIAL_BATCH_SIZE, currentBatchSize / 2);
        } else {
            if (currentBatchSize <= 0) {
                currentBatchSize = INITIAL_BATCH_SIZE;
            } else {
                currentBatchSize = Math.min(MAX_BATCH_SIZE, currentBatchSize + 1);
            }
        }

        int processed = 0;
        while (processed < currentBatchSize) {
            UUID templateId = updateQueue.poll();
            if (templateId == null) {
                break;
            }
            pendingUpdates.remove(templateId);
            processed++;
            processTemplate(templateId);
        }
    }

    private void processTemplate(UUID templateId) {
        Template template = templateManager.getTemplate(templateId);
        if (template == null) {
            return;
        }

        if (template.getMetadata().isLocked()) {
            notifyLockedTemplate(template);
            return;
        }

        TemplateVersion latestVersion = template.getLatestVersion();
        if (latestVersion == null) {
            logger.warning("Skipping cascading update for template " + template.getPath() + " because it has no version.");
            return;
        }

        java.util.List<Instance> childInstances = IdunnTemplates.getInstance()
                .getInstanceRepository()
                .getActiveInstancesByParentTemplate(templateId)
                .join();

        boolean waitingForChildren = false;
        for (Instance childInstance : childInstances) {
            Template childTemplate = templateManager.getTemplate(childInstance.getTemplateId());
            if (childTemplate == null) {
                continue;
            }
            TemplateVersion latestChildVersion = childTemplate.getLatestVersion();
            if (latestChildVersion == null) {
                continue;
            }
            if (!latestChildVersion.getVersionId().equals(childInstance.getCurrentVersionId())) {
                instanceUpdateScheduler.enqueueInstanceUpdate(childTemplate, childInstance, latestChildVersion, "cascading-child:" + templateId);
                waitingForChildren = true;
            } else if (instanceUpdateScheduler.hasPendingTask(childInstance.getId())) {
                waitingForChildren = true;
            }
        }

        if (waitingForChildren) {
            scheduleUpdate(templateId);
            return;
        }

        try {
            templateManager.commitTemplateSystem(template, "自动级联更新");
        } catch (Exception e) {
            logger.severe("Failed to auto-commit cascading template " + template.getPath() + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }

        for (UUID parentId : template.getMetadata().getParentTemplateInstances().keySet()) {
            scheduleUpdate(parentId);
        }
    }

    private void notifyLockedTemplate(Template template) {
        logger.info("Skipped cascading update for locked template " + template.getPath());
        Player creator = Bukkit.getPlayer(template.getMetadata().getCreatorId());
        if (creator != null && creator.isOnline()) {
            creator.sendMessage(ChatColor.YELLOW + "[Idunn] "
                    + ChatColor.RED + "模板 " + ChatColor.WHITE + template.getPath()
                    + ChatColor.RED + " 因为已锁定，已拒绝一次级联更新。");
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
}
