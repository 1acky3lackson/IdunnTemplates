package com.jackyblackson.idunntemplates.backend.commercial.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 项目实体类
 * 对应数据库表 projects
 */
@Entity
@Table(name = "projects",
        uniqueConstraints = {
                @UniqueConstraint(name = "uniq_projects_name",
                        columnNames = {"name"}),
                @UniqueConstraint(name = "uniq_projects_parent_project_id_name",
                        columnNames = {"parent_project_id", "name"}),
                @UniqueConstraint(name = "uniq_projects_path_name",
                        columnNames = {"path_name"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("delete_time_ms IS NULL") // 全局过滤器，只查询未删除的记录
public class Project implements Serializable {

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

    @Column(name = "path_name", nullable = false, length = 1024)
    private String pathName;

    @Column(name = "kind", nullable = false, length = 32)
    private String kind;

    @Column(name = "model_kind", length = 32)
    private String modelKind;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "world_id", foreignKey = @ForeignKey(name = "fk_projects_worlds_world_id"))
//    @JsonIgnore
    private World world;

    @Column(name = "min_x")
    private Integer minX;

    @Column(name = "min_y")
    private Integer minY;

    @Column(name = "min_z")
    private Integer minZ;

    @Column(name = "max_x")
    private Integer maxX;

    @Column(name = "max_y")
    private Integer maxY;

    @Column(name = "max_z")
    private Integer maxZ;

    @Column(name = "tp_x")
    private Double tpX;

    @Column(name = "tp_y")
    private Double tpY;

    @Column(name = "tp_z")
    private Double tpZ;

    @Column(name = "tp_yaw")
    private Double tpYaw;

    @Column(name = "tp_pitch")
    private Double tpPitch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_project_id", foreignKey = @ForeignKey(name = "fk_projects_projects_parent_project_id"))
    private Project parentProject;

    @OneToMany(mappedBy = "parentProject", fetch = FetchType.LAZY)
    private Set<Project> childProjects = new HashSet<>();

    @Column(name = "create_time_ms", nullable = false)
    private Long createTimeMs;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "create_user_id", foreignKey = @ForeignKey(name = "fk_projects_users_create_user_id"))
//    private User createUser;

    @Column(name = "delete_time_ms")
    private Long deleteTimeMs;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "delete_user_id", foreignKey = @ForeignKey(name = "fk_projects_users_delete_user_id"))
//    private User deleteUser;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "icon_file_id", foreignKey = @ForeignKey(name = "fk_projects_files_avatar_icon_id"))
//    private File iconFile;

    // 辅助方法：添加子项目
    public void addChildProject(Project child) {
        childProjects.add(child);
        child.setParentProject(this);
    }

    // 辅助方法：移除子项目
    public void removeChildProject(Project child) {
        childProjects.remove(child);
        child.setParentProject(null);
    }

    // 辅助方法：检查是否已删除
    @Transient
    public boolean isDeleted() {
        return deleteTimeMs != null;
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
