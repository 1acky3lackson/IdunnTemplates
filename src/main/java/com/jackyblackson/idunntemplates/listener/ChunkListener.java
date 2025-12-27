package com.jackyblackson.idunntemplates.listener;

import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.logging.Logger;

public class ChunkListener implements Listener {
    
    private final InstanceRepository repository;
    private final Logger logger;

    public ChunkListener(InstanceRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        // Load instances for this chunk's partition
        // The repository handles the logic of checking if the partition (8x8 chunks) is already loaded/cached.
        // It's safe to call this for every chunk load as it checks cache first.
        repository.loadInstancesForChunk(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ())
                .thenAccept(instances -> {
                    // Optional: Debug log
//                    logger.info("Loaded " + instances.size() + " instances for partition covering chunk " + chunk.getX() + "," + chunk.getZ());
                });
    }
}
