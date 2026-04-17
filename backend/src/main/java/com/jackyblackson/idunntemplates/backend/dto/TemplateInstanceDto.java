package com.jackyblackson.idunntemplates.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TemplateInstanceDto {
    private String id;
    private UUID templateId;
    private String currentVersionId;
    private UUID worldId;
    private Integer x;
    private Integer y;
    private Integer z;
    private Integer rotationY;
    private Boolean flipX;
    private Boolean flipY;
    private Boolean flipZ;
    private Boolean autoUpdate;
    private Long placedAt;
    private UUID placedBy;
    private String placedByName;
    private Long deletedTimestamp;
    private Integer maskXNeg;
    private Integer maskXPos;
    private Integer maskYNeg;
    private Integer maskYPos;
    private Integer maskZNeg;
    private Integer maskZPos;
    private UUID embeddedInTemplateId;
    private Boolean deleted;
    private Boolean wild;
}
