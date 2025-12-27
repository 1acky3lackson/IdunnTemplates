package com.jackyblackson.idunntemplates.core.domain;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
