package com.jackyblackson.idunntemplates.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class TemplateSearchCriteria {
    // Getters & Setters ...
    // 基础路径查询
    private String pathPrefix;
    private String pathLike;
    private String nameLike;

    private Boolean sortByLatestVersionTime;
    private String versionMessageLike;

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

}