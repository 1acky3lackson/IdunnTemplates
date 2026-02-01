package com.jackyblackson.idunntemplates.backend.dto;

import java.util.UUID;

public class VersionSearchCriteria {

    // 筛选特定模板的所有版本
    private UUID templateId;

    // 筛选特定提交者的版本
    private UUID submitterId;

    // 模糊搜索提交信息 (commit message)
    private String messageKeyword;

    // 时间范围筛选 (例如：查找最近一周的版本)
    private Long minCreatedAt;
    private Long maxCreatedAt;

    // 精确查找特定 versionId string (业务ID)
    private String versionId;

    // Getters & Setters
    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public UUID getSubmitterId() { return submitterId; }
    public void setSubmitterId(UUID submitterId) { this.submitterId = submitterId; }

    public String getMessageKeyword() { return messageKeyword; }
    public void setMessageKeyword(String messageKeyword) { this.messageKeyword = messageKeyword; }

    public Long getMinCreatedAt() { return minCreatedAt; }
    public void setMinCreatedAt(Long minCreatedAt) { this.minCreatedAt = minCreatedAt; }

    public Long getMaxCreatedAt() { return maxCreatedAt; }
    public void setMaxCreatedAt(Long maxCreatedAt) { this.maxCreatedAt = maxCreatedAt; }

    public String getVersionId() { return versionId; }
    public void setVersionId(String versionId) { this.versionId = versionId; }
}