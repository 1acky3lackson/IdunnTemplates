package com.jackyblackson.idunntemplates.backend.commercial.dto.checkout;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class SettlementBreakdownDto {
    private Long orderId;
    private Long highlightedCheckoutDetailId;
    private String highlightedRole;
    private String highlightedUsername;
    private String orderStatus;
    private Integer point;
    private String pointType;
    private BigDecimal orderAmount;
    private Long productId;
    private String productName;
    private Long projectId;
    private String projectName;

    private boolean hasProjectRange;
    private boolean hasSettlementSnapshot;
    private boolean usedTemplateSettlement;

    private ParameterSnapshot params = new ParameterSnapshot();
    private ProjectSnapshot project = new ProjectSnapshot();
    private AmountSnapshot amounts = new AmountSnapshot();
    private List<TemplateBreakdown> templates = new ArrayList<>();
    private List<RoleGroupBreakdown> roleGroups = new ArrayList<>();
    private List<String> notes = new ArrayList<>();

    @Data
    public static class ParameterSnapshot {
        private BigDecimal commercialRatio;
        private BigDecimal templateDefectParam;
        private BigDecimal placerRatio;
        private BigDecimal uploaderRatio;
        private Integer releaseDelayDays;
    }

    @Data
    public static class ProjectSnapshot {
        private Long effectiveBlocks;
        private Long scannedAtMs;
        private String sourceServerName;
        private BigDecimal templateDecayedEffectiveBlocksTotal;
        private BigDecimal templateRatio;
        private BigDecimal creationRatio;
    }

    @Data
    public static class AmountSnapshot {
        private BigDecimal templateGross;
        private BigDecimal creationGross;
        private BigDecimal builderFromTemplateGross;
        private BigDecimal modifierFromTemplateGross;
        private BigDecimal templateAuthorGross;
        private BigDecimal builderFromCreationGross;
        private BigDecimal helperFromCreationGross;
        private BigDecimal modifierFromCreationGross;
        private BigDecimal uploaderFromCreationGross;
        private BigDecimal builderGross;
        private BigDecimal modifierGross;
        private BigDecimal uploaderGross;
    }

    @Data
    public static class TemplateBreakdown {
        private UUID templateId;
        private String templateName;
        private String authorUsername;
        private Integer usageCount;
        private Long totalManagedBlocks;
        private BigDecimal averageEffectiveBlocks;
        private BigDecimal geometricSeriesFactor;
        private BigDecimal decayedEffectiveBlocks;
        private BigDecimal shareRatio;
        private BigDecimal gross;
        private BigDecimal placerGross;
        private BigDecimal authorGross;
        private BigDecimal builderGross;
        private BigDecimal modifierGross;
        private List<TemplateVersionBreakdown> versions = new ArrayList<>();
    }

    @Data
    public static class TemplateVersionBreakdown {
        private String versionId;
        private Integer usageCount;
        private Long totalManagedBlocks;
    }

    @Data
    public static class RoleGroupBreakdown {
        private String role;
        private String label;
        private BigDecimal gross;
        private List<RoleMemberBreakdown> members = new ArrayList<>();
    }

    @Data
    public static class RoleMemberBreakdown {
        private Long checkoutDetailId;
        private String username;
        private BigDecimal contributionRatio;
        private BigDecimal orderRatio;
        private BigDecimal amount;
        private String status;
        private boolean highlighted;
    }
}
