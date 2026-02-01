package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.Instance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class CascadingUpdateManager {
    private final TemplateManager templateManager;
    private final Logger logger;
    
    // Queue of Parent Templates that need to be committed/updated.
    private final Queue<UUID> updateQueue = new LinkedBlockingQueue<>();
    
    // Set to avoid adding the same parent multiple times in the queue (pending processing).
    private final Set<UUID> pendingUpdates = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    // History of updates in the current "chain" to detect cycles (though queue naturally handles it, we might want to log or limit depth).
    // Actually, simple queue is BFS. A cycle means A updates B, B updates A.
    // A -> Queue: [B]
    // Process B -> Updates A -> Queue: [A]
    // Process A -> Updates B -> Queue: [B]
    // This is an infinite loop. We need a way to stop it.
    // We can use a "Cool-down" map: Allow a template to be auto-updated only once every X seconds.
    private final Map<UUID, Long> lastAutoUpdateTimestamp = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 0; // 2 seconds cooldown

    public CascadingUpdateManager(TemplateManager templateManager, Logger logger) {
        this.templateManager = templateManager;
        this.logger = logger;
    }

    public void startTask() {
        // Run every 5 ticks (0.25s) to process one batch
        IdunnTemplates.getInstance().getServer().getScheduler().runTaskTimer(IdunnTemplates.getInstance(), this::processQueue, 20L, 5L);
    }

    public void scheduleUpdate(UUID parentTemplateId) {
        if (pendingUpdates.contains(parentTemplateId)) {
            return; // Already queued
        }
        
        // Cooldown Check
        long now = System.currentTimeMillis();
        long last = lastAutoUpdateTimestamp.getOrDefault(parentTemplateId, 0L);
        if (now - last < COOLDOWN_MS) {
            logger.warning("Skipping cascading update for Parent Template " + parentTemplateId + " due to cooldown (possible cycle).");
            return;
        }

        updateQueue.offer(parentTemplateId);
        pendingUpdates.add(parentTemplateId);
        // logger.info("Scheduled cascading update for Parent Template: " + parentTemplateId);
    }

    private void processQueue() {
        if (updateQueue.isEmpty()) return;

        // Process 1 item per tick (or per run) as requested to spread load
        UUID parentId = updateQueue.poll();
        if (parentId == null) return;
        
        pendingUpdates.remove(parentId);
        
        try {
            Template parent = templateManager.getTemplate(parentId);
            if (parent == null) return;
            if (parent.isLocked()) {
                logger.info("Skipped cascading auto-commit for Parent Template: " + parent.getName() + ", because it is locked");
                return;
            }

            // Mark timestamp
            lastAutoUpdateTimestamp.put(parentId, System.currentTimeMillis());

            // Trigger Auto-Commit
            // This needs a new method in TemplateManager that doesn't require a Player object (system commit)
            // Or we mock a system player/console.
            logger.info("Executing cascading auto-commit for Parent Template: " + parent.getName());
            
            // We need to commit the current state of the parent's region (which now contains the updated child instance blocks)
            // to a new version.
            templateManager.commitTemplateSystem(parent, "Auto-commit: Cascading update from child instances.");
            
        } catch (Exception e) {
            logger.severe("Failed to process cascading update for template " + parentId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
