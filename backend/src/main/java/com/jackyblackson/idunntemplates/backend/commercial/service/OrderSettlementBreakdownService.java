package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.ProjectSettlementSnapshotPayload;
import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.SettlementBreakdownDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.GlobalCheckoutParamContext;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.CheckoutDetailRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import com.jackyblackson.idunntemplates.backend.dto.LuckyUserInfo;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermUserInfoService;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class OrderSettlementBreakdownService {

    private static final BigDecimal MIN_UNIT = new BigDecimal("1e-8");

    private final NeteaseOrderRepository orderRepository;
    private final CheckoutDetailRepository checkoutDetailRepository;
    private final UserProjectContributionService userProjectContributionService;
    private final GlobalCheckoutParamContextService globalCheckoutParamContextService;
    private final ProjectSettlementSnapshotService projectSettlementSnapshotService;
    private final TemplateRepository templateRepository;
    private final LuckyPermUserInfoService luckyPermUserInfoService;

    public Optional<SettlementBreakdownDto> buildForOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> build(order, checkoutDetailRepository.findByOrder_Id(orderId), null));
    }

    public Optional<SettlementBreakdownDto> buildForCheckoutDetail(Long checkoutDetailId) {
        return checkoutDetailRepository.findById(checkoutDetailId)
                .filter(detail -> detail.getOrder() != null)
                .map(detail -> {
                    List<CheckoutDetail> details = checkoutDetailRepository.findByOrder_Id(detail.getOrder().getId());
                    return build(detail.getOrder(), details, detail);
                });
    }

    private SettlementBreakdownDto build(
            NeteaseOrder order,
            List<CheckoutDetail> existingDetails,
            CheckoutDetail highlightedDetail
    ) {
        SettlementBreakdownDto dto = new SettlementBreakdownDto();
        dto.setOrderId(order.getId());
        dto.setOrderStatus(order.getInternalStatus() != null ? order.getInternalStatus().name() : null);
        dto.setPoint(order.getPoint());
        dto.setPointType(order.getPointType());
        dto.setOrderAmount(toOrderAmount(order.getPoint()));
        dto.setProductId(order.getProduct() != null ? order.getProduct().getId() : null);
        dto.setProductName(order.getProductName());
        dto.setProjectId(order.getProduct() != null && order.getProduct().getProject() != null ? order.getProduct().getProject().getId() : null);
        dto.setProjectName(order.getProduct() != null && order.getProduct().getProject() != null
                ? firstNonBlank(order.getProduct().getProject().getDisplayName(), order.getProduct().getProject().getName())
                : null);

        if (highlightedDetail != null) {
            dto.setHighlightedCheckoutDetailId(highlightedDetail.getId());
            dto.setHighlightedRole(highlightedDetail.getRole() != null ? highlightedDetail.getRole().name() : null);
            dto.setHighlightedUsername(highlightedDetail.getUsername());
        }

        GlobalCheckoutParamContext params = resolveParamContext(existingDetails);
        fillParams(dto, params);

        Project project = order.getProduct() != null ? order.getProduct().getProject() : null;
        boolean hasProjectRange = hasProjectRange(project);
        dto.setHasProjectRange(hasProjectRange);

        Optional<ProjectSettlementSnapshotPayload> snapshotOptional = project != null
                ? projectSettlementSnapshotService.getPayloadByProjectId(project.getId())
                : Optional.empty();
        dto.setHasSettlementSnapshot(snapshotOptional.isPresent());

        if (project == null) {
            dto.getNotes().add("订单当前未关联建造项目，无法展示项目范围内模板参与计算。");
            populateRoleGroups(dto, existingDetails, Map.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Collections.emptyMap(), highlightedDetail);
            return dto;
        }

        Map<CommercialRoleType, List<UserProjectContribution>> contributors =
                Optional.ofNullable(userProjectContributionService.getProductContributionsGroupedByRole(order.getProduct().getId()))
                        .orElseGet(HashMap::new);

        BigDecimal orderAmount = dto.getOrderAmount();
        BigDecimal lambda = decimal(params.getTemplateDefectParam());
        BigDecimal commercialRatio = decimal(params.getCommercialRatio());
        BigDecimal placerRatio = decimal(params.getPlacerRatio());
        BigDecimal uploaderRatio = decimal(params.getUploaderRatio());

        boolean usedTemplateSettlement = hasProjectRange && snapshotOptional.isPresent()
                && snapshotOptional.get().getProjectEffectiveBlocks() != null
                && snapshotOptional.get().getProjectEffectiveBlocks() > 0;
        dto.setUsedTemplateSettlement(usedTemplateSettlement);

        List<TemplateAllocation> templateAllocations = usedTemplateSettlement
                ? buildTemplateAllocations(snapshotOptional.get(), lambda)
                : List.of();

        BigDecimal totalTemplateDecayed = templateAllocations.stream()
                .map(TemplateAllocation::decayedEffectiveBlocks)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal templateRatio = BigDecimal.ZERO;
        if (usedTemplateSettlement && totalTemplateDecayed.compareTo(BigDecimal.ZERO) > 0) {
            templateRatio = totalTemplateDecayed.divide(
                    BigDecimal.valueOf(snapshotOptional.get().getProjectEffectiveBlocks()), 12, RoundingMode.HALF_UP);
            if (templateRatio.compareTo(BigDecimal.ONE) > 0) {
                templateRatio = BigDecimal.ONE;
            }
        }

        BigDecimal creationRatio = BigDecimal.ONE.subtract(templateRatio).max(BigDecimal.ZERO);
        BigDecimal templateGross = orderAmount.multiply(templateRatio).setScale(8, RoundingMode.HALF_UP);
        BigDecimal creationGross = orderAmount.subtract(templateGross).setScale(8, RoundingMode.HALF_UP);

        dto.getProject().setEffectiveBlocks(snapshotOptional.map(ProjectSettlementSnapshotPayload::getProjectEffectiveBlocks).orElse(null));
        dto.getProject().setScannedAtMs(snapshotOptional.map(ProjectSettlementSnapshotPayload::getScannedAtMs).orElse(null));
        dto.getProject().setSourceServerName(snapshotOptional.map(ProjectSettlementSnapshotPayload::getSourceServerName).orElse(null));
        dto.getProject().setTemplateDecayedEffectiveBlocksTotal(totalTemplateDecayed.setScale(8, RoundingMode.HALF_UP));
        dto.getProject().setTemplateRatio(templateRatio.setScale(8, RoundingMode.HALF_UP));
        dto.getProject().setCreationRatio(creationRatio.setScale(8, RoundingMode.HALF_UP));

        BigDecimal builderFromTemplates = BigDecimal.ZERO;
        BigDecimal modifierFromTemplates = BigDecimal.ZERO;
        BigDecimal templateAuthorGrossTotal = BigDecimal.ZERO;
        Map<String, BigDecimal> templateAuthorGrossByUsername = new LinkedHashMap<>();

        if (!templateAllocations.isEmpty() && templateGross.compareTo(BigDecimal.ZERO) > 0) {
            List<BigDecimal> ratios = templateAllocations.stream()
                    .map(item -> item.decayedEffectiveBlocks().divide(totalTemplateDecayed, 12, RoundingMode.HALF_UP))
                    .toList();
            List<BigDecimal> amounts = split(templateGross, ratios);

            for (int i = 0; i < templateAllocations.size(); i++) {
                TemplateAllocation allocation = templateAllocations.get(i);
                BigDecimal templateAmount = amounts.get(i);
                BigDecimal shareRatio = totalTemplateDecayed.compareTo(BigDecimal.ZERO) > 0
                        ? allocation.decayedEffectiveBlocks().divide(totalTemplateDecayed, 12, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                BigDecimal placerGross = templateAmount.multiply(placerRatio).setScale(8, RoundingMode.HALF_UP);
                BigDecimal authorGross = templateAmount.subtract(placerGross).setScale(8, RoundingMode.HALF_UP);
                BigDecimal builderGross = placerGross.multiply(BigDecimal.ONE.subtract(commercialRatio)).setScale(8, RoundingMode.HALF_UP);
                BigDecimal modifierGross = placerGross.subtract(builderGross).setScale(8, RoundingMode.HALF_UP);

                builderFromTemplates = builderFromTemplates.add(builderGross);
                modifierFromTemplates = modifierFromTemplates.add(modifierGross);
                templateAuthorGrossTotal = templateAuthorGrossTotal.add(authorGross);
                templateAuthorGrossByUsername.merge(allocation.authorUsername(), authorGross, BigDecimal::add);

                SettlementBreakdownDto.TemplateBreakdown templateDto = new SettlementBreakdownDto.TemplateBreakdown();
                templateDto.setTemplateId(allocation.templateId());
                templateDto.setTemplateName(allocation.templateName());
                templateDto.setAuthorUsername(allocation.authorUsername());
                templateDto.setUsageCount(allocation.usageCount());
                templateDto.setTotalManagedBlocks(allocation.totalManagedBlocks());
                templateDto.setAverageEffectiveBlocks(allocation.averageEffectiveBlocks());
                templateDto.setGeometricSeriesFactor(allocation.geometricSeriesFactor());
                templateDto.setDecayedEffectiveBlocks(allocation.decayedEffectiveBlocks());
                templateDto.setShareRatio(shareRatio.setScale(8, RoundingMode.HALF_UP));
                templateDto.setGross(templateAmount);
                templateDto.setPlacerGross(placerGross);
                templateDto.setAuthorGross(authorGross);
                templateDto.setBuilderGross(builderGross);
                templateDto.setModifierGross(modifierGross);
                for (TemplateVersionSlice version : allocation.versions()) {
                    SettlementBreakdownDto.TemplateVersionBreakdown versionDto = new SettlementBreakdownDto.TemplateVersionBreakdown();
                    versionDto.setVersionId(version.versionId());
                    versionDto.setUsageCount(version.usageCount());
                    versionDto.setTotalManagedBlocks(version.totalManagedBlocks());
                    templateDto.getVersions().add(versionDto);
                }
                dto.getTemplates().add(templateDto);
            }
        }

        BigDecimal builderFromCreation = creationGross.multiply(BigDecimal.ONE.subtract(commercialRatio)).setScale(8, RoundingMode.HALF_UP);
        BigDecimal helperFromCreation = creationGross.subtract(builderFromCreation).setScale(8, RoundingMode.HALF_UP);
        BigDecimal uploaderFromCreation = helperFromCreation.multiply(uploaderRatio).setScale(8, RoundingMode.HALF_UP);
        BigDecimal modifierFromCreation = helperFromCreation.subtract(uploaderFromCreation).setScale(8, RoundingMode.HALF_UP);

        BigDecimal builderGross = builderFromCreation.add(builderFromTemplates).setScale(8, RoundingMode.HALF_UP);
        BigDecimal modifierGross = modifierFromCreation.add(modifierFromTemplates).setScale(8, RoundingMode.HALF_UP);
        BigDecimal uploaderGross = uploaderFromCreation.setScale(8, RoundingMode.HALF_UP);

        dto.getAmounts().setTemplateGross(templateGross);
        dto.getAmounts().setCreationGross(creationGross);
        dto.getAmounts().setBuilderFromTemplateGross(builderFromTemplates.setScale(8, RoundingMode.HALF_UP));
        dto.getAmounts().setModifierFromTemplateGross(modifierFromTemplates.setScale(8, RoundingMode.HALF_UP));
        dto.getAmounts().setTemplateAuthorGross(templateAuthorGrossTotal.setScale(8, RoundingMode.HALF_UP));
        dto.getAmounts().setBuilderFromCreationGross(builderFromCreation);
        dto.getAmounts().setHelperFromCreationGross(helperFromCreation);
        dto.getAmounts().setModifierFromCreationGross(modifierFromCreation);
        dto.getAmounts().setUploaderFromCreationGross(uploaderFromCreation);
        dto.getAmounts().setBuilderGross(builderGross);
        dto.getAmounts().setModifierGross(modifierGross);
        dto.getAmounts().setUploaderGross(uploaderGross);

        if (!hasProjectRange) {
            dto.getNotes().add("该项目未录入世界范围，因此模板分成已跳过，订单全部计入创作分成。");
        } else if (!dto.isHasSettlementSnapshot()) {
            dto.getNotes().add("该项目尚未生成模板快照，因此模板分成已跳过，订单全部计入创作分成。");
        }
        if (contributors.getOrDefault(CommercialRoleType.BUILDER, List.of()).isEmpty()) {
            dto.getNotes().add("当前项目未配置建筑制作人员。");
        }
        if (contributors.getOrDefault(CommercialRoleType.MODIFIER, List.of()).isEmpty()) {
            dto.getNotes().add("当前商品未配置修改美化人员。");
        }
        if (contributors.getOrDefault(CommercialRoleType.UPLOADER, List.of()).isEmpty()) {
            dto.getNotes().add("当前商品未配置包装宣传人员。");
        }

        populateRoleGroups(dto, existingDetails, contributors, builderGross, modifierGross, uploaderGross, templateAuthorGrossByUsername, highlightedDetail);
        return dto;
    }

    private void fillParams(SettlementBreakdownDto dto, GlobalCheckoutParamContext params) {
        dto.getParams().setCommercialRatio(decimal(params.getCommercialRatio()));
        dto.getParams().setTemplateDefectParam(decimal(params.getTemplateDefectParam()));
        dto.getParams().setPlacerRatio(decimal(params.getPlacerRatio()));
        dto.getParams().setUploaderRatio(decimal(params.getUploaderRatio()));
        dto.getParams().setReleaseDelayDays(params.getReleaseDelayDays());
    }

    private void populateRoleGroups(
            SettlementBreakdownDto dto,
            List<CheckoutDetail> existingDetails,
            Map<CommercialRoleType, List<UserProjectContribution>> contributors,
            BigDecimal builderGross,
            BigDecimal modifierGross,
            BigDecimal uploaderGross,
            Map<String, BigDecimal> templateAuthorGrossByUsername,
            CheckoutDetail highlightedDetail
    ) {
        dto.getRoleGroups().add(buildRoleGroup(
                CommercialRoleType.BUILDER,
                "建筑制作",
                builderGross,
                existingDetails,
                contributors.getOrDefault(CommercialRoleType.BUILDER, List.of()),
                dto.getOrderAmount(),
                highlightedDetail
        ));
        dto.getRoleGroups().add(buildRoleGroup(
                CommercialRoleType.MODIFIER,
                "修改美化",
                modifierGross,
                existingDetails,
                contributors.getOrDefault(CommercialRoleType.MODIFIER, List.of()),
                dto.getOrderAmount(),
                highlightedDetail
        ));
        dto.getRoleGroups().add(buildRoleGroup(
                CommercialRoleType.UPLOADER,
                "包装宣传",
                uploaderGross,
                existingDetails,
                contributors.getOrDefault(CommercialRoleType.UPLOADER, List.of()),
                dto.getOrderAmount(),
                highlightedDetail
        ));
        dto.getRoleGroups().add(buildTemplateAuthorGroup(
                templateAuthorGrossByUsername,
                existingDetails,
                dto.getOrderAmount(),
                highlightedDetail
        ));
    }

    private SettlementBreakdownDto.RoleGroupBreakdown buildRoleGroup(
            CommercialRoleType role,
            String label,
            BigDecimal gross,
            List<CheckoutDetail> existingDetails,
            List<UserProjectContribution> contributions,
            BigDecimal orderAmount,
            CheckoutDetail highlightedDetail
    ) {
        SettlementBreakdownDto.RoleGroupBreakdown group = new SettlementBreakdownDto.RoleGroupBreakdown();
        group.setRole(role.name());
        group.setLabel(label);
        group.setGross(gross);

        List<CheckoutDetail> roleDetails = existingDetails.stream()
                .filter(detail -> detail.getRole() == role)
                .toList();
        if (!roleDetails.isEmpty()) {
            BigDecimal roleTotal = roleDetails.stream()
                    .map(this::detailAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            for (CheckoutDetail detail : roleDetails) {
                SettlementBreakdownDto.RoleMemberBreakdown member = new SettlementBreakdownDto.RoleMemberBreakdown();
                member.setCheckoutDetailId(detail.getId());
                member.setUsername(detail.getUsername());
                member.setAmount(detailAmount(detail));
                member.setStatus(detail.getStatus() != null ? detail.getStatus().name() : null);
                member.setOrderRatio(orderAmount.compareTo(BigDecimal.ZERO) > 0
                        ? detailAmount(detail).divide(orderAmount, 8, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);
                member.setContributionRatio(roleTotal.compareTo(BigDecimal.ZERO) > 0
                        ? detailAmount(detail).divide(roleTotal, 8, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);
                member.setHighlighted(highlightedDetail != null && Objects.equals(highlightedDetail.getId(), detail.getId()));
                group.getMembers().add(member);
            }
            group.setGross(roleTotal.setScale(8, RoundingMode.HALF_UP));
            return group;
        }

        if (gross.compareTo(BigDecimal.ZERO) <= 0 || contributions.isEmpty()) {
            return group;
        }

        List<BigDecimal> ratios = contributions.stream()
                .map(item -> BigDecimal.valueOf(item.getContributeRatio()).setScale(8, RoundingMode.HALF_UP))
                .toList();
        List<BigDecimal> amounts = split(gross, ratios);
        for (int i = 0; i < contributions.size(); i++) {
            UserProjectContribution contribution = contributions.get(i);
            SettlementBreakdownDto.RoleMemberBreakdown member = new SettlementBreakdownDto.RoleMemberBreakdown();
            member.setUsername(contribution.getUsername());
            member.setAmount(amounts.get(i));
            member.setContributionRatio(ratios.get(i));
            member.setOrderRatio(orderAmount.compareTo(BigDecimal.ZERO) > 0
                    ? amounts.get(i).divide(orderAmount, 8, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            member.setHighlighted(highlightedDetail != null
                    && role == highlightedDetail.getRole()
                    && Objects.equals(highlightedDetail.getUsername(), contribution.getUsername()));
            group.getMembers().add(member);
        }
        return group;
    }

    private SettlementBreakdownDto.RoleGroupBreakdown buildTemplateAuthorGroup(
            Map<String, BigDecimal> templateAuthorGrossByUsername,
            List<CheckoutDetail> existingDetails,
            BigDecimal orderAmount,
            CheckoutDetail highlightedDetail
    ) {
        SettlementBreakdownDto.RoleGroupBreakdown group = new SettlementBreakdownDto.RoleGroupBreakdown();
        group.setRole(CommercialRoleType.TEMPLATE_AUTHOR.name());
        group.setLabel("模板作者");

        List<CheckoutDetail> detailGroup = existingDetails.stream()
                .filter(detail -> detail.getRole() == CommercialRoleType.TEMPLATE_AUTHOR)
                .toList();
        if (!detailGroup.isEmpty()) {
            BigDecimal total = detailGroup.stream().map(this::detailAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            group.setGross(total.setScale(8, RoundingMode.HALF_UP));
            for (CheckoutDetail detail : detailGroup) {
                SettlementBreakdownDto.RoleMemberBreakdown member = new SettlementBreakdownDto.RoleMemberBreakdown();
                member.setCheckoutDetailId(detail.getId());
                member.setUsername(detail.getUsername());
                member.setAmount(detailAmount(detail));
                member.setStatus(detail.getStatus() != null ? detail.getStatus().name() : null);
                member.setOrderRatio(orderAmount.compareTo(BigDecimal.ZERO) > 0
                        ? detailAmount(detail).divide(orderAmount, 8, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);
                member.setContributionRatio(total.compareTo(BigDecimal.ZERO) > 0
                        ? detailAmount(detail).divide(total, 8, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);
                member.setHighlighted(highlightedDetail != null && Objects.equals(highlightedDetail.getId(), detail.getId()));
                group.getMembers().add(member);
            }
            return group;
        }

        BigDecimal total = templateAuthorGrossByUsername.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        group.setGross(total.setScale(8, RoundingMode.HALF_UP));
        for (Map.Entry<String, BigDecimal> entry : templateAuthorGrossByUsername.entrySet()) {
            SettlementBreakdownDto.RoleMemberBreakdown member = new SettlementBreakdownDto.RoleMemberBreakdown();
            member.setUsername(entry.getKey());
            member.setAmount(entry.getValue().setScale(8, RoundingMode.HALF_UP));
            member.setOrderRatio(orderAmount.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(orderAmount, 8, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            member.setContributionRatio(total.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(total, 8, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            member.setHighlighted(highlightedDetail != null
                    && highlightedDetail.getRole() == CommercialRoleType.TEMPLATE_AUTHOR
                    && Objects.equals(highlightedDetail.getUsername(), entry.getKey()));
            group.getMembers().add(member);
        }
        return group;
    }

    private List<BigDecimal> split(BigDecimal total, List<BigDecimal> ratios) {
        int n = ratios.size();
        if (n == 0) return List.of();

        List<BigDecimal> amounts = new ArrayList<>(n);
        BigDecimal sumFloor = BigDecimal.ZERO;
        for (BigDecimal ratio : ratios) {
            BigDecimal raw = total.multiply(ratio);
            BigDecimal floor = raw.setScale(8, RoundingMode.FLOOR);
            amounts.add(floor);
            sumFloor = sumFloor.add(floor);
        }

        BigDecimal diff = total.subtract(sumFloor);
        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            return amounts;
        }

        long diffUnits = diff.divide(MIN_UNIT, 0, RoundingMode.HALF_UP).longValue();
        List<Integer> indices = IntStream.range(0, n)
                .boxed()
                .sorted(Comparator.comparing((Integer i) -> ratios.get(i)).reversed())
                .toList();

        for (int i = 0; i < diffUnits; i++) {
            int idx = indices.get(i % n);
            amounts.set(idx, amounts.get(idx).add(MIN_UNIT));
        }
        return amounts;
    }

    private List<TemplateAllocation> buildTemplateAllocations(
            ProjectSettlementSnapshotPayload snapshot,
            BigDecimal lambda
    ) {
        if (snapshot.getTemplates() == null || snapshot.getTemplates().isEmpty()) {
            return List.of();
        }

        List<TemplateAllocation> allocations = new ArrayList<>();
        for (ProjectSettlementSnapshotPayload.TemplateUsageSnapshot usage : snapshot.getTemplates()) {
            if (usage.getTemplateId() == null || usage.getUsageCount() == null || usage.getUsageCount() <= 0) {
                continue;
            }

            Template template = templateRepository.findById(usage.getTemplateId()).orElse(null);
            if (template == null || template.getMetadata() == null || template.getMetadata().getCreatorId() == null) {
                continue;
            }

            LuckyUserInfo author = luckyPermUserInfoService.getUserInfo(template.getMetadata().getCreatorId().toString());
            if (author == null || author.getName() == null || author.getName().isBlank()) {
                continue;
            }

            BigDecimal totalManagedBlocks = BigDecimal.valueOf(usage.getTotalManagedBlocks() == null ? 0L : usage.getTotalManagedBlocks());
            BigDecimal usageCount = BigDecimal.valueOf(usage.getUsageCount());
            BigDecimal averageEffectiveBlocks = totalManagedBlocks.divide(usageCount, 12, RoundingMode.HALF_UP);
            if (averageEffectiveBlocks.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal geometricSeriesFactor = geometricSeries(lambda, usage.getUsageCount());
            List<TemplateVersionSlice> versions = usage.getVersions() == null ? List.of() : usage.getVersions().stream()
                    .map(version -> new TemplateVersionSlice(
                            version.getVersionId(),
                            version.getUsageCount(),
                            version.getTotalManagedBlocks()))
                    .toList();

            allocations.add(new TemplateAllocation(
                    usage.getTemplateId(),
                    firstNonBlank(usage.getTemplateName(), template.getName()),
                    author.getName(),
                    usage.getUsageCount(),
                    usage.getTotalManagedBlocks() == null ? 0L : usage.getTotalManagedBlocks(),
                    averageEffectiveBlocks.setScale(8, RoundingMode.HALF_UP),
                    geometricSeriesFactor.setScale(8, RoundingMode.HALF_UP),
                    averageEffectiveBlocks.multiply(geometricSeriesFactor).setScale(8, RoundingMode.HALF_UP),
                    versions
            ));
        }
        return allocations;
    }

    private BigDecimal geometricSeries(BigDecimal lambda, int count) {
        if (count <= 0) {
            return BigDecimal.ZERO;
        }
        if (lambda.compareTo(BigDecimal.ONE) == 0) {
            return BigDecimal.valueOf(count);
        }
        BigDecimal numerator = BigDecimal.ONE.subtract(lambda.pow(count));
        BigDecimal denominator = BigDecimal.ONE.subtract(lambda);
        return numerator.divide(denominator, 12, RoundingMode.HALF_UP);
    }

    private GlobalCheckoutParamContext resolveParamContext(List<CheckoutDetail> existingDetails) {
        return existingDetails.stream()
                .map(CheckoutDetail::getParamContext)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> Optional.ofNullable(globalCheckoutParamContextService.getEffectiveConfig())
                        .orElseGet(() -> new GlobalCheckoutParamContext("system")));
    }

    private boolean hasProjectRange(Project project) {
        return project != null
                && project.getWorld() != null
                && project.getMinX() != null
                && project.getMinY() != null
                && project.getMinZ() != null
                && project.getMaxX() != null
                && project.getMaxY() != null
                && project.getMaxZ() != null;
    }

    private BigDecimal toOrderAmount(Integer point) {
        if (point == null || point <= 0) {
            return BigDecimal.ZERO.setScale(8, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(point).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
    }

    private BigDecimal decimal(Double value) {
        return BigDecimal.valueOf(value == null ? 0D : value).setScale(8, RoundingMode.HALF_UP);
    }

    private BigDecimal detailAmount(CheckoutDetail detail) {
        BigDecimal value = detail.getNetProfit() != null ? detail.getNetProfit() : detail.getActualProfit();
        return value == null ? BigDecimal.ZERO : value.setScale(8, RoundingMode.HALF_UP);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private record TemplateAllocation(
            UUID templateId,
            String templateName,
            String authorUsername,
            int usageCount,
            long totalManagedBlocks,
            BigDecimal averageEffectiveBlocks,
            BigDecimal geometricSeriesFactor,
            BigDecimal decayedEffectiveBlocks,
            List<TemplateVersionSlice> versions
    ) {
    }

    private record TemplateVersionSlice(
            String versionId,
            Integer usageCount,
            Long totalManagedBlocks
    ) {
    }
}
