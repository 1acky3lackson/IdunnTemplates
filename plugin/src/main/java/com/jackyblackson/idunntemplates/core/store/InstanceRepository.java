package com.jackyblackson.idunntemplates.core.store;

import com.jackyblackson.idunntemplates.core.domain.Instance;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface InstanceRepository {
    
    /**
     * Saves or updates an instance to the storage.
     * This should handle partitioning based on instance coordinates.
     */
    CompletableFuture<Void> saveInstance(Instance instance);

    /**
     * Loads instances for a specific partition group.
     * Triggered when a chunk in that group is loaded.
     * @param worldId The world UUID
     * @param chunkX The X coordinate of the chunk
     * @param chunkZ The Z coordinate of the chunk
     * @return A list of loaded instances (may be empty if no file exists)
     */
    CompletableFuture<List<Instance>> loadInstancesForChunk(UUID worldId, int chunkX, int chunkZ);

    /**
     * Unloads instances for a specific chunk.
     * Should be called when a chunk is unloaded to free memory.
     * @param worldId The world UUID
     * @param chunkX The X coordinate of the chunk
     * @param chunkZ The Z coordinate of the chunk
     */
    void unloadInstancesForChunk(UUID worldId, int chunkX, int chunkZ);
    
    /**
     * Optional: explicitly save a partition to disk immediately.
     */
    void flushPartition(UUID worldId, int partitionX, int partitionZ);
    
    /**
     * Returns all currently loaded instances across all worlds.
     * Useful for global updates.
     */
    List<Instance> getAllLoadedInstances();

    CompletableFuture<List<Instance>> getActiveInstancesInWorld(UUID worldId);

    CompletableFuture<List<Instance>> getActiveInstancesByTemplate(UUID templateId);

    CompletableFuture<List<Instance>> getActiveInstancesByParentTemplate(UUID parentTemplateId);

    /**
     * Permanently removes an instance from the repository.
     * @param instance The instance to remove
     */
    CompletableFuture<Void> hardDelete(Instance instance);
}
