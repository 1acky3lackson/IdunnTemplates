package com.jackyblackson.idunntemplates.backend.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "template_color_schemes")
public class TemplateColorScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", unique = true, nullable = false)
    private UUID templateId;

    @Column(nullable = false)
    private String version;

    // Stores 6 colors as comma separated string
    @Column(nullable = false)
    private String colors;

    public TemplateColorScheme() {
    }

    public TemplateColorScheme(UUID templateId, String version, String colors) {
        this.templateId = templateId;
        this.version = version;
        this.colors = colors;
    }

    public Long getId() {
        return id;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getColors() {
        return colors;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }
}
