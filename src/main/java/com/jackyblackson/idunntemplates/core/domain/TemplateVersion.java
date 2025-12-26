package com.jackyblackson.idunntemplates.core.domain;

import java.util.UUID;

public class TemplateVersion {
    private final String versionId; // Base64 timestamp
    private final UUID submitterId;
    private final String message;

    public TemplateVersion(String versionId, UUID submitterId, String message) {
        this.versionId = versionId;
        this.submitterId = submitterId;
        this.message = message;
    }

    public String getVersionId() {
        return versionId;
    }

    public UUID getSubmitterId() {
        return submitterId;
    }

    public String getMessage() {
        return message;
    }
}
