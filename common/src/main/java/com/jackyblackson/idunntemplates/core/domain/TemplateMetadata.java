package com.jackyblackson.idunntemplates.core.domain;

import com.google.gson.Gson;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "idunn_template_metadata")
@DatabaseTable(tableName = "idunn_template_metadata")
public class TemplateMetadata {

    // 使用 templateId 作为主键
    @Id
    @Column(name = "template_id")
    @DatabaseField(id = true, columnName = "template_id")
    private UUID templateId;

    @Column(name = "creator_id")
    @DatabaseField(columnName = "creator_id")
    private UUID creatorId;

    @Column(name = "creation_time")
    @DatabaseField(columnName = "creation_time")
    private long creationTime;

    @Column(name = "world_id")
    @DatabaseField(columnName = "world_id")
    private UUID worldId;

    // Anchor & Dimensions
    @Column
    @DatabaseField
    private int anchorX;
    @Column
    @DatabaseField
    private int anchorY;
    @Column
    @DatabaseField
    private int anchorZ;
    @Column
    @DatabaseField
    private int width;
    @Column
    @DatabaseField
    private int height;
    @Column
    @DatabaseField
    private int length;

    // Status
    @Column(name = "deleted_timestamp")
    @DatabaseField(columnName = "deleted_timestamp")
    private Long deletedTimestamp;

    @Column
    @DatabaseField
    private boolean locked = false;

    @Column(name = "locked_timestamp")
    @DatabaseField(columnName = "locked_timestamp")
    private Long lockedTimestamp = null;

    // --- Complex Types (JSON Storage) ---

    // [变更] 这个列表现在不直接存 Metadata 表，而是从 idunn_template_versions 表查出来
    @Transient
    private final transient List<TemplateVersion> versions = new ArrayList<>();

    @Column(name = "staged_changes_json", length = 65535)
    @DatabaseField(columnName = "staged_changes_json", dataType = DataType.LONG_STRING)
    private String stagedChangesJson;

    // --- Transient Fields (Not in this table) ---

    @Transient
    private transient StagedChanges stagedChanges;

    // 这些 Map 不存储在 Metadata 表中，而是通过 DAO 查询 Instance 表来动态填充
    @Transient
    private transient Map<UUID, List<Instance>> childTemplateInstances = new HashMap<>();
    @Transient
    private transient Map<UUID, List<Instance>> parentTemplateInstances = new HashMap<>();

    private static final Gson gson = new Gson();

    // ORM Constructor
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

    // --- Logic for JSON Conversion (ORMLite Hooks) ---
    // 每次保存前，将对象同步到 JSON 字符串
    public void prePersist() {
        if (this.stagedChanges != null) {
            this.stagedChangesJson = gson.toJson(this.stagedChanges);
        }
    }

    // 每次加载后，将 JSON 字符串解析为对象
    public void postLoad() {
        if (stagedChangesJson != null) {
            this.stagedChanges = gson.fromJson(stagedChangesJson, StagedChanges.class);
        }
    }

    // --- Getters & Setters ---

    // [新增] 供 DAO 调用，注入版本列表
    public void hydrateVersions(List<TemplateVersion> loadedVersions) {
        this.versions.clear();
        this.versions.addAll(loadedVersions);
    }

    // [变更] addVersion 现在只操作内存，需要调用 DAO saveTemplateVersion 来持久化
    public void addVersion(TemplateVersion version) {
        this.versions.add(version);
    }

    // Getters 保持不变
    public List<TemplateVersion> getVersions() {
        return versions;
    }

    public StagedChanges getStagedChanges() {
        if (stagedChanges == null) {
            if (stagedChangesJson != null) {
                postLoad();
            } else {
                stagedChanges = new StagedChanges();
            }
        }
        return stagedChanges;
    }

    public void setStagedChanges(StagedChanges stagedChanges) {
        this.stagedChanges = stagedChanges;
        prePersist();
    }

    // --- The Complex Maps (Hydrated by DAO) ---

    public Map<UUID, List<Instance>> getChildTemplateInstances() {
        if (childTemplateInstances == null) childTemplateInstances = new HashMap<>();
        return childTemplateInstances;
    }

    public Map<UUID, List<Instance>> getParentTemplateInstances() {
        if (parentTemplateInstances == null) parentTemplateInstances = new HashMap<>();
        return parentTemplateInstances;
    }

    // 允许 DAO 注入这些数据
    public void hydrateChildInstances(List<Instance> instances) {
        this.childTemplateInstances = new HashMap<>();
        for (Instance i : instances) {
            // Key is the ID of the template of the instance
            this.childTemplateInstances.computeIfAbsent(i.getTemplateId(), k -> new ArrayList<>()).add(i);
        }
    }

    // 注入此模板作为子代时的父级关系
    public void hydrateParentInstances(List<Instance> instances) {
        this.parentTemplateInstances = new HashMap<>();
        for (Instance i : instances) {
            if (i.getEmbeddedInTemplateId() != null) {
                this.parentTemplateInstances.computeIfAbsent(i.getEmbeddedInTemplateId(), k -> new ArrayList<>()).add(i);
            }
        }
    }

    // Standard Getters/Setters
    public void setLocked(boolean locked) {
        if (!this.locked && locked) {
            this.lockedTimestamp = System.currentTimeMillis();
        } else if (this.locked && !locked) {
            this.lockedTimestamp = null; // Use null for DB compatibility
        }
        this.locked = locked;
    }

    public boolean isLocked() { return locked; }
    public UUID getTemplateId() { return templateId; }
    public Long getLockedTimestamp() { return lockedTimestamp; }
    public boolean isDeleted() { return deletedTimestamp != null; }
    public void setDeletedTimestamp(Long deletedTimestamp) { this.deletedTimestamp = deletedTimestamp; }
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
