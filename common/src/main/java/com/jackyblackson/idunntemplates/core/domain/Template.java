package com.jackyblackson.idunntemplates.core.domain;

import com.jackyblackson.idunntemplates.core.store.dao.TemplateDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import jakarta.persistence.*;

import java.io.File;
import java.util.UUID;

@Entity
@Table(name = "idunn_templates")
@DatabaseTable(tableName = "idunn_templates", daoClass = TemplateDao.class)
public class Template {

    @Id
    @DatabaseField(id = true)
    private UUID id;

    @Column(unique = true, nullable = false)
    @DatabaseField(unique = true, canBeNull = false)
    private String path;

    @Column
    @DatabaseField(index = true)
    private String name;

    // 外键关联：TemplateMetadata
    // foreignAutoRefresh = true 会自动加载 metadata 数据
    // foreignAutoCreate = true 会在保存 template 时自动保存 metadata
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "metadata_id")
    @DatabaseField(foreign = true, foreignAutoRefresh = true, foreignAutoCreate = true, columnName = "metadata_id")
    private TemplateMetadata metadata;

    // ORM required
    public Template() {}

    public Template(String name, String path, TemplateMetadata metadata) {
        this.name = name;
        this.path = path;
        this.metadata = metadata;
        this.id = metadata.getTemplateId();
    }

    public Template(String name, String filePath, File templateDir, TemplateMetadata metadata) {
    }

    // --- Business Logic ---
    // Logic dependent on Bukkit/WorldEdit has been moved to Plugin module.

    public boolean isLocked() { return getMetadata().isLocked(); }

    // --- Getters ---
    public UUID getId() { return id; }
    public String getPath() { return path; }
    public String getName() { return name; }
    public TemplateMetadata getMetadata() { return metadata; }
    public void setMetadata(TemplateMetadata metadata) { this.metadata = metadata; }

    public TemplateVersion getLatestVersion() {
        if (metadata.getVersions().isEmpty()) return null;
        return metadata.getVersions().get(metadata.getVersions().size() - 1);
    }
}
