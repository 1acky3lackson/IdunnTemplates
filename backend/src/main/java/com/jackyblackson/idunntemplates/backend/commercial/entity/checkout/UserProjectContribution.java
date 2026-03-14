package com.jackyblackson.idunntemplates.backend.commercial.entity.checkout;

import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "commercial_user_project_contribution")
@NoArgsConstructor
public class UserProjectContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "role")
    private CommercialRoleType role;

    @Column(name = "comment")
    private String comment;

    @Column(name = "contribute_points")
    private Integer contributePoints;

    @Column(name = "contribute_ratio")
    private Double contributeRatio;

    @ManyToOne(fetch = FetchType.EAGER)  // 默认关联查询为 LAZY 提升性能
    @JoinColumn(name = "project_id")    // 指定外键列名
    private Project project;

    @Column(name = "delete_time_ms")
    private Long deleteTimeMs;

    @Column(name = "delete_reason")
    private String deleteReason;

    @Column(name = "delete_username")
    private String deleteUsername;

    @Column(name = "create_username")
    private String createUsername;

    @Column(name = "create_time_ms")
    private String createTimeMs;
}
