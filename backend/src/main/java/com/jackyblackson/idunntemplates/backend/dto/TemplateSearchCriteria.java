package com.jackyblackson.idunntemplates.backend.dto;

import java.util.UUID;

public class TemplateSearchCriteria {
    // 基础路径查询
    private String pathPrefix;

    // Metadata 过滤条件
    private UUID creatorId;
    private UUID worldId;
    private Boolean locked;

    // 尺寸范围过滤 (例如：搜索宽度大于 100 的模板)
    private Integer minWidth;
    private Integer maxWidth;
    private Integer minHeight;
    private Integer maxHeight;
    private Integer minLength;
    private Integer maxLength;

    // Getters & Setters ...
    public String getPathPrefix() { return pathPrefix; }
    public void setPathPrefix(String pathPrefix) { this.pathPrefix = pathPrefix; }
    public UUID getCreatorId() { return creatorId; }
    public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }
    public UUID getWorldId() { return worldId; }
    public void setWorldId(UUID worldId) { this.worldId = worldId; }
    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }
    public Integer getMinWidth() { return minWidth; }
    public void setMinWidth(Integer minWidth) { this.minWidth = minWidth; }
    public Integer getMaxWidth() { return maxWidth; }
    public void setMaxWidth(Integer maxWidth) { this.maxWidth = maxWidth; }
    public Integer getMinHeight() { return minHeight; }
    public void setMinHeight(Integer minHeight) { this.minHeight = minHeight; }
    public Integer getMaxHeight() { return maxHeight; }
    public void setMaxHeight(Integer maxHeight) { this.maxHeight = maxHeight; }
    public Integer getMinLength() { return minLength; }
    public void setMinLength(Integer minLength) { this.minLength = minLength; }
    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
}