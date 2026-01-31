package com.jackyblackson.idunntemplates.core.domain;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.jackyblackson.idunntemplates.core.store.dao.InstanceDao;

import java.util.UUID;

// 指定表名为 idunn_instances
@DatabaseTable(tableName = "idunn_instances", daoClass = InstanceDao.class)
public class Instance {

    // 主键
    @DatabaseField(id = true, canBeNull = false)
    private String id;

    @DatabaseField(columnName = "template_id", canBeNull = false)
    private UUID templateId;

    @DatabaseField(columnName = "current_version_id")
    private String currentVersionId;

    // --- Anchor position ---
    @DatabaseField(columnName = "world_id", canBeNull = false)
    private UUID worldId;

    @DatabaseField(index = true) // 加上索引，因为空间查询经常用到坐标
    private int x;
    @DatabaseField
    private int y;
    @DatabaseField(index = true)
    private int z;

    // --- Transformation ---
    @DatabaseField(columnName = "rotation_y")
    private int rotationY;
    @DatabaseField(columnName = "flip_x")
    private boolean flipX;
    @DatabaseField(columnName = "flip_y")
    private boolean flipY;
    @DatabaseField(columnName = "flip_z")
    private boolean flipZ;

    @DatabaseField(columnName = "auto_update")
    private boolean autoUpdate = true;

    @DatabaseField(columnName = "placed_at")
    private long placedAt;

    @DatabaseField(columnName = "placed_by")
    private UUID placedBy;

    @DatabaseField(columnName = "placed_by_name")
    private String placedByName;

    // Soft delete (null means active)
    @DatabaseField(columnName = "deleted_timestamp")
    private Long deletedTimestamp;

    // --- Mask / Indentation ---
    @DatabaseField
    private int maskXNeg = 0;
    @DatabaseField
    private int maskXPos = 0;
    @DatabaseField
    private int maskYNeg = 0;
    @DatabaseField
    private int maskYPos = 0;
    @DatabaseField
    private int maskZNeg = 0;
    @DatabaseField
    private int maskZPos = 0;

    // --- Parent Template ---
    /**
     * 外键关联。
     * 注意：这里我们存的是 UUID，没有直接用 @ForeignCollection，
     * 保持简单，避免级联加载的复杂性。
     */
    @DatabaseField(columnName = "embedded_in_template_id", index = true)
    private UUID embeddedInTemplateId;

    // --- 构造函数 ---

    // ORM 必须的无参构造函数
    public Instance() {
    }

    // 你的原始构造函数（逻辑保持不变，只是生成 UUID 的逻辑移进来了）
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

    // --- Getters and Setters ---
    // (ORM 需要 Setter 来注入数据，所以原来只有 Getter 的字段现在需要补上 Setter，
    // 或者至少保持字段非 final)

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public String getCurrentVersionId() { return currentVersionId; }
    public void setCurrentVersionId(String currentVersionId) { this.currentVersionId = currentVersionId; }

    public UUID getWorldId() { return worldId; }
    public void setWorldId(UUID worldId) { this.worldId = worldId; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getZ() { return z; }
    public void setZ(int z) { this.z = z; }

    public int getRotationY() { return rotationY; }
    public void setRotationY(int rotationY) { this.rotationY = rotationY; }

    public boolean isFlipX() { return flipX; }
    public void setFlipX(boolean flipX) { this.flipX = flipX; }

    public boolean isFlipY() { return flipY; }
    public void setFlipY(boolean flipY) { this.flipY = flipY; }

    public boolean isFlipZ() { return flipZ; }
    public void setFlipZ(boolean flipZ) { this.flipZ = flipZ; }

    public boolean isAutoUpdate() { return autoUpdate; }
    public void setAutoUpdate(boolean autoUpdate) { this.autoUpdate = autoUpdate; }

    public long getPlacedAt() { return placedAt; }
    public void setPlacedAt(long placedAt) { this.placedAt = placedAt; }

    public UUID getPlacedBy() { return placedBy; }
    public void setPlacedBy(UUID placedBy) { this.placedBy = placedBy; }

    public String getPlacedByName() { return placedByName; }
    public void setPlacedByName(String placedByName) { this.placedByName = placedByName; }

    public boolean isDeleted() { return deletedTimestamp != null; }

    // 注意：这个 Getter 主要是给 ORM 用的，业务逻辑通常判断 isDeleted()
    public Long getDeletedTimestamp() { return deletedTimestamp; }
    public void setDeletedTimestamp(Long deletedTimestamp) { this.deletedTimestamp = deletedTimestamp; }

    // Masks Setters/Getters
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

    // --- 业务逻辑方法 (不加 @DatabaseField) ---

    public Template getTemplate() {
        if (IdunnTemplates.getInstance() == null) return null;
        return IdunnTemplates.getInstance().getTemplateManager().getTemplate(this.templateId);
    }

    public boolean isWild() {
        return this.embeddedInTemplateId == null;
    }

    public void setParentTemplate(Template t) {
        this.embeddedInTemplateId = t.getId();
    }

    public Template getEmbeddedTemplate() {
        if (isWild() || IdunnTemplates.getInstance() == null) {
            return null;
        }
        return IdunnTemplates.getInstance().getTemplateManager().getTemplate(this.embeddedInTemplateId);
    }

    public boolean canUpdate() {
        return (this.isWild()) || ((this.embeddedInTemplateId != null) && (!this.getEmbeddedTemplate().isLocked()));
    }
}