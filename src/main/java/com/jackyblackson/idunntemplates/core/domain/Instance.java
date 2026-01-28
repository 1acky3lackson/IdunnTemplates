package com.jackyblackson.idunntemplates.core.domain;

import com.jackyblackson.idunntemplates.IdunnTemplates;

import java.util.UUID;

public class Instance {
    private final String id;
    private final UUID templateId; // Changed from String templatePath
    private String currentVersionId;

    // Anchor position in the World
    private final UUID worldId;
    private final int x;
    private final int y;
    private final int z;

    // Transformation
    private final int rotationY; // 0, 90, 180, 270
    private final boolean flipX;
    private final boolean flipY;
    private final boolean flipZ;

    private boolean autoUpdate = true;
    private final long placedAt;
    private final UUID placedBy;

    private final String placedByName;
    
    private Long deletedTimestamp; // Soft delete

    // Mask / Indentation (Default 0)
    private int maskXNeg = 0;
    private int maskXPos = 0;
    private int maskYNeg = 0;
    private int maskYPos = 0;
    private int maskZNeg = 0;
    private int maskZPos = 0;

    // parent Template
    /**
     * 如果此实例是放置在某个模板（父模板）的 Master Region 内，
     * 则此字段存储该父模板的 UUID。
     * 如果是放置在野外（Wild），则为 null。
     */
    private UUID embeddedInTemplateId;

    public Instance(UUID templateId, String currentVersionId, UUID worldId, int x, int y, int z, int rotationY, boolean flipX, boolean flipY, boolean flipZ, UUID placedBy, String placedByName) {
        this.id = UUID.randomUUID().toString();
        this.templateId = templateId;
        this.currentVersionId = currentVersionId;
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotationY = rotationY;
        this.flipX = flipX;
        this.flipY = flipY;
        this.flipZ = flipZ;
        this.placedBy = placedBy;
        this.placedAt = System.currentTimeMillis();
        this.placedByName = placedByName;
    }

    // Getters and Setters
    public String getId() { return id; }
    public UUID getTemplateId() { return templateId; }
    public String getCurrentVersionId() { return currentVersionId; }
    public void setCurrentVersionId(String currentVersionId) { this.currentVersionId = currentVersionId; }
    
    public UUID getWorldId() { return worldId; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    
    public int getRotationY() { return rotationY; }
    public boolean isFlipX() { return flipX; }
    public boolean isFlipY() { return flipY; }
    public boolean isFlipZ() { return flipZ; }
    
    public boolean isAutoUpdate() { return autoUpdate; }
    public void setAutoUpdate(boolean autoUpdate) { this.autoUpdate = autoUpdate; }

    public Template getTemplate() {
        return IdunnTemplates.getInstance().getTemplateManager().getTemplate(this.templateId);
    }
    
    public long getPlacedAt() { return placedAt; }
    public UUID getPlacedBy() { return placedBy; }
    
    public boolean isDeleted() { return deletedTimestamp != null; }
    public void setDeletedTimestamp(Long deletedTimestamp) { this.deletedTimestamp = deletedTimestamp; }

    public int getMaskXNeg() { return maskXNeg; }
    public void setMaskXNeg(int maskXNeg) { this.maskXNeg = maskXNeg; }

    public int getMaskXPos() { return maskXPos; }
    public void setMaskXPos(int maskXPos) { this.maskXPos = maskXPos; }

    public int getMaskYNeg() { return maskYNeg; }
    public void setMaskYNeg(int maskYNeg) { this.maskYNeg = maskYNeg; }

    public int getMaskYPos() { return maskYPos; }
    public void setMaskYPos(int maskYPos) { this.maskYPos = maskYPos; }

    public int getMaskZNeg() { return maskZNeg; }
    public void setMaskZNeg(int maskZNeg) { this.maskZNeg = maskZNeg; }

    public int getMaskZPos() { return maskZPos; }
    public void setMaskZPos(int maskZPos) { this.maskZPos = maskZPos; }

    public UUID getEmbeddedInTemplateId() { return embeddedInTemplateId; }

    public void setEmbeddedInTemplateId(UUID embeddedInTemplateId) { this.embeddedInTemplateId = embeddedInTemplateId; }

    public boolean isWild() {
        return this.embeddedInTemplateId == null;
    }

    public void setParentTemplate(Template t) {
        this.embeddedInTemplateId = t.getId();
    }
}
