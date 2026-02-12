package com.jackyblackson.idunntemplates.backend.dto;

import com.jackyblackson.idunntemplates.backend.domain.RemoteSetSource.SourceType;

public class AddSourceRequest {
    private SourceType type;
    private String path;
    private Long targetSetId;
    private Double weight;

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getTargetSetId() {
        return targetSetId;
    }

    public void setTargetSetId(Long targetSetId) {
        this.targetSetId = targetSetId;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
