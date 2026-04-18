package com.jackyblackson.idunntemplates.backend.commercial.dto.checkout;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ProjectSettlementSnapshotPayload {
    private Long projectEffectiveBlocks = 0L;
    private Long scannedAtMs = System.currentTimeMillis();
    private String sourceServerName;
    private List<TemplateUsageSnapshot> templates = new ArrayList<>();
    private List<InstanceUsageSnapshot> instances = new ArrayList<>();

    @Data
    public static class TemplateUsageSnapshot {
        private UUID templateId;
        private String templateName;
        private Integer usageCount = 0;
        private Long totalManagedBlocks = 0L;
        private List<TemplateVersionUsageSnapshot> versions = new ArrayList<>();
    }

    @Data
    public static class TemplateVersionUsageSnapshot {
        private String versionId;
        private Integer usageCount = 0;
        private Long totalManagedBlocks = 0L;
    }

    @Data
    public static class InstanceUsageSnapshot {
        private String instanceId;
        private UUID templateId;
        private String templateName;
        private String versionId;
        private String placedByName;
        private Long placedAt;
        private Integer minX;
        private Integer minY;
        private Integer minZ;
        private Integer maxX;
        private Integer maxY;
        private Integer maxZ;
        private Long managedBlocks = 0L;
    }
}
