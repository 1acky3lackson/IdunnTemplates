package com.jackyblackson.idunntemplates.core.domain;

import org.bukkit.util.Vector;

import java.util.*;

public class TemplateMetadata {
    private UUID templateId;
    private UUID creatorId;
    private long creationTime;
    private UUID worldId;
    
    // Anchor Point (minX, minY, minZ) of the original selection
    private int anchorX;
    private int anchorY;
    private int anchorZ;

    // Dimensions
    private int width;
    private int height;
    private int length;

    private Long deletedTimestamp; // null if active
    
    private final List<TemplateVersion> versions = new ArrayList<>();

    /**
     * 记录位于此模板 Master Region 内的其他模板（子模板）的完整实例信息。
     * <p>
     * Key: Child Template UUID (子模板的 ID)
     * Value: List of Instance (该子模板在当前模板内的所有实例列表)
     * <p>
     * 用途：当我们需要渲染或更新当前模板（作为父模板）时，
     * 可以直接读取此列表知道有哪些子模板实例在里面，
     * 而不需要去全局 InstanceRepository 搜索。
     */
    private Map<UUID, List<Instance>> childTemplateInstances = new HashMap<>();

    /**
     * 记录此模板作为子模板，放置在哪些父模板中，以及对应的实例信息。
     * <p>
     * Key: Parent Template UUID (父模板的 ID)
     * Value: List of Instance (当前模板在父模板内的所有实例列表)
     * <p>
     * 用途：当 当前模板（作为子模板）发生变化时，
     * 遍历此 Map 的 Key (Parent UUIDs)，触发父模板的自动更新。
     * Value 中的 Instance 信息是冗余存储，用于快速校验或恢复。
     */
    private Map<UUID, List<Instance>> parentTemplateInstances = new HashMap<>();

    // Getters
    public Map<UUID, List<Instance>> getChildTemplateInstances() {
        if (childTemplateInstances == null) {
            childTemplateInstances = new HashMap<>();
        }
        return childTemplateInstances;
    }

    public Map<UUID, List<Instance>> getParentTemplateInstances() {
        if (parentTemplateInstances == null) {
            parentTemplateInstances = new HashMap<>();
        }
        return parentTemplateInstances;
    }

    // Setters
    public void setChildTemplateInstances(Map<UUID, List<Instance>> childTemplateInstances) {
        this.childTemplateInstances = childTemplateInstances;
    }

    public void setParentTemplateInstances(Map<UUID, List<Instance>> parentTemplateInstances) {
        this.parentTemplateInstances = parentTemplateInstances;
    }

    // No-args constructor for serialization
    public TemplateMetadata() {}

    public TemplateMetadata(UUID templateId, UUID creatorId, long creationTime, UUID worldId, int anchorX, int anchorY, int anchorZ, int width, int height, int length) {
        this.templateId = templateId;
        this.creatorId = creatorId;
        this.creationTime = creationTime;
        this.worldId = worldId;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.anchorZ = anchorZ;
        this.width = width;
        this.height = height;
        this.length = length;
    }

    public void addVersion(TemplateVersion version) {
        this.versions.add(version);
    }

    public List<TemplateVersion> getVersions() {
        return versions;
    }

    public boolean isDeleted() {
        return deletedTimestamp != null;
    }

    public void setDeletedTimestamp(Long deletedTimestamp) {
        this.deletedTimestamp = deletedTimestamp;
    }
    
    // Getters
    public UUID getTemplateId() { return templateId; }
    public UUID getCreatorId() { return creatorId; }
    public long getCreationTime() { return creationTime; }
    public UUID getWorldId() { return worldId; }
    public int getAnchorX() { return anchorX; }
    public int getAnchorY() { return anchorY; }
    public int getAnchorZ() { return anchorZ; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLength() { return length; }
}
