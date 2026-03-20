package com.jackyblackson.idunntemplates.backend.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "idunn_collection")
@Data
public class TemplateCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(name = "create_time_ms")
    private Long createTimeMs;

    @Column(name = "update_time_ms")
    private Long updateTimeMs;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_private")
    private boolean privateCollection;
}
