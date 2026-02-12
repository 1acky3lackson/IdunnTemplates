package com.jackyblackson.idunntemplates.backend.dto;

import java.util.List;
import java.util.UUID;

public class RemoteSetDto {
    private Long id;
    private String name;
    private UUID creatorUuid;
    private String namespaceName;
    private List<TemplateWithColorsDto> previewTemplates;

    public RemoteSetDto() {}

    public RemoteSetDto(Long id, String name, UUID creatorUuid, String namespaceName, List<TemplateWithColorsDto> previewTemplates) {
        this.id = id;
        this.name = name;
        this.creatorUuid = creatorUuid;
        this.namespaceName = namespaceName;
        this.previewTemplates = previewTemplates;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getCreatorUuid() {
        return creatorUuid;
    }

    public void setCreatorUuid(UUID creatorUuid) {
        this.creatorUuid = creatorUuid;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public List<TemplateWithColorsDto> getPreviewTemplates() {
        return previewTemplates;
    }

    public void setPreviewTemplates(List<TemplateWithColorsDto> previewTemplates) {
        this.previewTemplates = previewTemplates;
    }
}
