package com.jackyblackson.idunntemplates.backend.domain;

import com.jackyblackson.idunntemplates.core.domain.Template;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "idunn_template_collection_relation")
@Data
public class TemplateCollectionRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private TemplateCollection collection;

    @Column(name = "modify_time_ms")
    private Long modifyTimeMs;
}
