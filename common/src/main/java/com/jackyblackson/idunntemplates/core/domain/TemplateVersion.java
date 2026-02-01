package com.jackyblackson.idunntemplates.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "idunn_template_versions")
@DatabaseTable(tableName = "idunn_template_versions")
public class TemplateVersion {

    // 数据库内部主键 (自增 ID 对于索引性能最好，也可以用 UUID)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @DatabaseField(generatedId = true)
    private int id;

    // 外键关联到 Template 表
    // foreign = true: 声明这是外键
    // columnName = "template_id": 数据库中的列名
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @JsonIgnore
    @DatabaseField(foreign = true, columnName = "template_id", canBeNull = false)
    private Template template;

    // 原有的业务 ID (Base64 Timestamp)
    @Column(name = "version_str_id", nullable = false)
    @DatabaseField(columnName = "version_str_id", canBeNull = false)
    private String versionId;

    @Column(name = "submitter_id")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @DatabaseField(columnName = "submitter_id")
    private UUID submitterId;

    @Column
    @DatabaseField
    private String message;

    @Column(name = "created_at")
    @DatabaseField(columnName = "created_at")
    private long createdAt;

    // ORM 必须的无参构造
    public TemplateVersion() {}

    public TemplateVersion(Template template, String versionId, UUID submitterId, String message) {
        this.template = template;
        this.versionId = versionId;
        this.submitterId = submitterId;
        this.message = message;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public int getId() { return id; }
    public Template getTemplate() { return template; }
    public String getVersionId() { return versionId; }
    public UUID getSubmitterId() { return submitterId; }
    public String getMessage() { return message; }
    public long getCreatedAt() { return createdAt; }

    // Setters (ORM 需要)
    public void setTemplate(Template template) { this.template = template; }
}