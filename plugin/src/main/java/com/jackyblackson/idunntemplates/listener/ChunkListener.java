package com.jackyblackson.idunntemplates.listener;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.jackyblackson.idunntemplates.manager.TemplateUpdater;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ChunkListener implements Listener {
    
    private final InstanceRepository repository;
    private final TemplateManager templateManager;
    private final TemplateUpdater templateUpdater;
    private final Logger logger;

    public ChunkListener(InstanceRepository repository, TemplateManager templateManager, TemplateUpdater templateUpdater, Logger logger) {
        this.repository = repository;
        this.templateManager = templateManager;
        this.templateUpdater = templateUpdater;
        this.logger = logger;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        // Load instances for this chunk's partition
        repository.loadInstancesForChunk(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ())
                .thenAccept(instances -> {
                    if (instances.isEmpty()) return;
                    
                    // Filter instances that are actually in this chunk (partition logic returns 8x8 chunk area)
                    // The prompt didn't strictly say "only instances inside this chunk", but usually we only care about updating what's relevant.
                    // However, updateInstances takes a list.
                    // Let's check all loaded instances in this partition.
                    // Wait, if I update instances in neighbor chunks (same partition) which are not loaded yet?
                    // Safe, as long as World is loaded.
                    // But maybe we should only check instances whose center is in THIS chunk?
                    // The prompt says: "When loading instance from chunk... check each instance".
                    // Given `instances` is from `loadInstancesForChunk` (partition), it contains neighbors.
                    // Let's filter for this chunk to avoid repeated checks for every chunk load in the partition.
                    
                    List<Instance> inChunkInstances = new ArrayList<>();
                    for (Instance inst : instances) {
                        int cx = inst.getX() >> 4;
                        int cz = inst.getZ() >> 4;
                        if (cx == chunk.getX() && cz == chunk.getZ()) {
                            inChunkInstances.add(inst);
                        }
                    }
                    
                    if (inChunkInstances.isEmpty()) return;

                    // Group by Template to batch updates
                    Map<Template, List<Instance>> updatesNeeded = new HashMap<>();
                    
                    for (Instance instance : inChunkInstances) {
                        if (!instance.isAutoUpdate()) continue;
                        
                        Template template = templateManager.getTemplate(instance.getTemplateId());
                        if (template == null) continue;
                        
                        String latestVer = template.getLatestVersion().getVersionId();
                        if (!instance.getCurrentVersionId().equals(latestVer)) {
                            updatesNeeded.computeIfAbsent(template, k -> new ArrayList<>()).add(instance);
                        }
                    }
                    
                    if (updatesNeeded.isEmpty()) return;
                    
                    // Execute updates on Main Thread
                    IdunnTemplates.getInstance().getServer().getScheduler().runTask(IdunnTemplates.getInstance(), () -> {
                        for (Map.Entry<Template, List<Instance>> entry : updatesNeeded.entrySet()) {
                            Template tmpl = entry.getKey();
                            templateUpdater.updateInstances(tmpl, tmpl.getLatestVersion(), entry.getValue());
                        }
                    });
                });
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        repository.unloadInstancesForChunk(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
    }
}
