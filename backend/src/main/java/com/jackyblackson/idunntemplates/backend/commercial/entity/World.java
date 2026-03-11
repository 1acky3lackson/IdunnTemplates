package com.jackyblackson.idunntemplates.backend.commercial.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 世界实体类
 * 对应数据库表 worlds
 */
@Entity
@Table(name = "worlds",
        uniqueConstraints = {
                @UniqueConstraint(name = "uniq_worlds_name",
                        columnNames = {"name"}),
                @UniqueConstraint(name = "uniq_worlds_mount_name",
                        columnNames = {"mount_name"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("delete_time_ms IS NULL") // 全局过滤器，只查询未删除的记录
public class World implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "display_name", nullable = false, length = 256)
    private String displayName;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "mount_name", nullable = false, length = 128)
    private String mountName;

    @Column(name = "detect_time_ms")
    private Long detectTimeMs;

    @Column(name = "create_time_ms")
    private Long createTimeMs;

    @Column(name = "delete_time_ms")
    private Long deleteTimeMs;

    @Column(name = "perm_managed", nullable = false)
    private Boolean permManaged;

    @Column(name = "mount_managed", nullable = false)
    private Boolean mountManaged;

    /**
     * 游戏规则，存储为JSONB格式
     * 示例: {"pvp": true, "difficulty": "normal", "gameMode": "survival"}
     */
//    @Type(type = "jsonb")
    @Column(name = "game_rules", columnDefinition = "jsonb")
    private String gameRules;

    // 一对多关系：一个世界可以有多个项目
    @OneToMany(mappedBy = "world", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

    // 辅助方法：检查是否已删除
    @Transient
    public boolean isDeleted() {
        return deleteTimeMs != null;
    }

    // 辅助方法：获取检测时间（毫秒转换为LocalDateTime）
    @Transient
    public java.time.LocalDateTime getDetectDateTime() {
        return detectTimeMs != null ?
                java.time.Instant.ofEpochMilli(detectTimeMs)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime() : null;
    }

    // 辅助方法：获取创建时间（毫秒转换为LocalDateTime）
    @Transient
    public java.time.LocalDateTime getCreateDateTime() {
        return createTimeMs != null ?
                java.time.Instant.ofEpochMilli(createTimeMs)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime() : null;
    }

    // 辅助方法：获取删除时间（毫秒转换为LocalDateTime）
    @Transient
    public java.time.LocalDateTime getDeleteDateTime() {
        return deleteTimeMs != null ?
                java.time.Instant.ofEpochMilli(deleteTimeMs)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime() : null;
    }
}
