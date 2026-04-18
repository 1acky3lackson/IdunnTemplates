package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.ProjectSettlementSnapshotPayload;
import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.GlobalCheckoutParamContext;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrderStatus;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import com.jackyblackson.idunntemplates.backend.dto.LuckyUserInfo;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermUserInfoService;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
@Slf4j
public class NeteaseOrderUpdateService {

    private static final BigDecimal MIN_UNIT = new BigDecimal("1e-8");

    private final UserProjectContributionService userProjectContributionService;
    private final GlobalCheckoutParamContextService globalCheckoutParamContextService;
    private final ProjectSettlementSnapshotService projectSettlementSnapshotService;
    private final CheckoutDetailService checkoutDetailService;
    private final NeteaseOrderRepository orderRepository;
    private final TemplateRepository templateRepository;
    private final LuckyPermUserInfoService luckyPermUserInfoService;

    public void fromEnter(NeteaseOrder order, Object log) {
        refundOrder(order);
    }

    public void fromCalculated(NeteaseOrder order, Object log) {
        refundOrder(order);
    }

    public void fromProfitted(NeteaseOrder order, Object log) {
        refundOrder(order);
    }

    public void fromTPlusM(NeteaseOrder order, Object log) {
        refundOrder(order);
    }

    public void fromTPlusN(NeteaseOrder order, Object log) {
        refundOrder(order);
    }

    @Transactional
    public boolean refundOrder(NeteaseOrder order) {
        if (order.getInternalStatus() == NeteaseOrderStatus.REFUNDED) {
            log.info("订单 {} 已是退款状态，跳过重复处理", order.getId());
            return false;
        }

        checkoutDetailService.refundOrderDetails(order);
        order.setInternalStatus(NeteaseOrderStatus.REFUNDED);
        if (order.getRefundInTimeMs() == null || order.getRefundInTimeMs() <= 0) {
            order.setRefundInTimeMs(System.currentTimeMillis());
        }
        orderRepository.save(order);
        log.info("订单 {} 已完成退款流程", order.getId());
        return true;
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
                .collect(Collectors.toList());

        for (int i = 0; i < diffUnits; i++) {
            int idx = indices.get(i % n);
            amounts.set(idx, amounts.get(idx).add(MIN_UNIT));
        }

        return amounts;
    }

    @Transactional
    public boolean checkoutEnterToCalculated(NeteaseOrder order) {
        if (!order.getInternalStatus().equals(NeteaseOrderStatus.ENTERED)) {
            log.info("订单 {} 状态为 {}，不是 ENTERED，跳过结算", order.getId(), order.getInternalStatus());
            return false;
        }

        if (order.getPoint() == null || order.getPoint() <= 0) {
            log.info("订单 {} 尚未填写有效虚拟点数，保持 ENTERED 等待补充", order.getId());
            return false;
        }

        if (order.getProduct() == null) {
            log.warn("订单 {} 未关联商品，跳过结算等待下次", order.getId());
            return false;
        }

        Project project = order.getProduct().getProject();
        if (project == null) {
            log.warn("订单 {} 关联的产品项目为空，跳过结算等待下次", order.getId());
            return false;
        }

        var contributors = userProjectContributionService.getProductContributionsGroupedByRole(order.getProduct().getId());
        GlobalCheckoutParamContext params = globalCheckoutParamContextService.getEffectiveConfig();
        Optional<ProjectSettlementSnapshotPayload> snapshotOptional = projectSettlementSnapshotService.getPayloadByProjectId(project.getId());
        if (contributors == null || params == null) {
            log.info("订单 {} 参与者或结算参数为空，跳过结算", order.getId());
            return false;
        }

        BigDecimal orderAmount = BigDecimal.valueOf(order.getPoint())
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        BigDecimal lambda = BigDecimal.valueOf(params.getTemplateDefectParam());
        BigDecimal commercialRatio = BigDecimal.valueOf(params.getCommercialRatio());
        BigDecimal placerRatio = BigDecimal.valueOf(params.getPlacerRatio());
        BigDecimal uploaderRatio = BigDecimal.valueOf(params.getUploaderRatio());

        boolean canUseTemplateSettlement = hasProjectRange(project) && snapshotOptional.isPresent()
                && snapshotOptional.get().getProjectEffectiveBlocks() != null
                && snapshotOptional.get().getProjectEffectiveBlocks() > 0;

        List<TemplateUsageAllocation> templateAllocations = canUseTemplateSettlement
                ? buildTemplateAllocations(snapshotOptional.get(), lambda)
                : List.of();
        BigDecimal totalTemplateDecayed = templateAllocations.stream()
                .map(TemplateUsageAllocation::decayedEffectiveBlocks)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal templateRatio = BigDecimal.ZERO;
        if (canUseTemplateSettlement && totalTemplateDecayed.compareTo(BigDecimal.ZERO) > 0) {
            templateRatio = totalTemplateDecayed.divide(
                    BigDecimal.valueOf(snapshotOptional.get().getProjectEffectiveBlocks()), 12, RoundingMode.HALF_UP);
            if (templateRatio.compareTo(BigDecimal.ONE) > 0) {
                templateRatio = BigDecimal.ONE;
            }
        }

        BigDecimal templateGross = orderAmount.multiply(templateRatio).setScale(8, RoundingMode.HALF_UP);
        BigDecimal creationGross = orderAmount.subtract(templateGross).setScale(8, RoundingMode.HALF_UP);

        BigDecimal builderFromTemplates = BigDecimal.ZERO;
        BigDecimal modifierFromTemplates = BigDecimal.ZERO;
        Map<String, BigDecimal> templateAuthorGrossByUsername = new HashMap<>();

        if (!templateAllocations.isEmpty() && templateGross.compareTo(BigDecimal.ZERO) > 0) {
            List<BigDecimal> ratios = templateAllocations.stream()
                    .map(item -> item.decayedEffectiveBlocks().divide(totalTemplateDecayed, 12, RoundingMode.HALF_UP))
                    .toList();
            List<BigDecimal> amounts = split(templateGross, ratios);

            for (int i = 0; i < templateAllocations.size(); i++) {
                TemplateUsageAllocation allocation = templateAllocations.get(i);
                BigDecimal templateAmount = amounts.get(i);
                BigDecimal placerGross = templateAmount.multiply(placerRatio).setScale(8, RoundingMode.HALF_UP);
                BigDecimal authorGross = templateAmount.subtract(placerGross).setScale(8, RoundingMode.HALF_UP);
                BigDecimal builderGross = placerGross.multiply(BigDecimal.ONE.subtract(commercialRatio))
                        .setScale(8, RoundingMode.HALF_UP);
                BigDecimal modifierGross = placerGross.subtract(builderGross).setScale(8, RoundingMode.HALF_UP);

                builderFromTemplates = builderFromTemplates.add(builderGross);
                modifierFromTemplates = modifierFromTemplates.add(modifierGross);
                templateAuthorGrossByUsername.merge(allocation.authorUsername(), authorGross, BigDecimal::add);
            }
        }

        BigDecimal builderFromCreation = creationGross.multiply(BigDecimal.ONE.subtract(commercialRatio))
                .setScale(8, RoundingMode.HALF_UP);
        BigDecimal helperFromCreation = creationGross.subtract(builderFromCreation).setScale(8, RoundingMode.HALF_UP);
        BigDecimal uploaderFromCreation = helperFromCreation.multiply(uploaderRatio).setScale(8, RoundingMode.HALF_UP);
        BigDecimal modifierFromCreation = helperFromCreation.subtract(uploaderFromCreation).setScale(8, RoundingMode.HALF_UP);

        BigDecimal builderGross = builderFromCreation.add(builderFromTemplates).setScale(8, RoundingMode.HALF_UP);
        BigDecimal modifierGross = modifierFromCreation.add(modifierFromTemplates).setScale(8, RoundingMode.HALF_UP);
        BigDecimal uploaderGross = uploaderFromCreation.setScale(8, RoundingMode.HALF_UP);

        if (!ensureContributorsPresent(contributors, CommercialRoleType.BUILDER, builderGross, order.getId())) return false;
        if (!ensureContributorsPresent(contributors, CommercialRoleType.MODIFIER, modifierGross, order.getId())) return false;
        if (!ensureContributorsPresent(contributors, CommercialRoleType.UPLOADER, uploaderGross, order.getId())) return false;

        createContributionDetails(order, orderAmount, params, CommercialRoleType.BUILDER,
                contributors.getOrDefault(CommercialRoleType.BUILDER, List.of()), builderGross);
        createContributionDetails(order, orderAmount, params, CommercialRoleType.MODIFIER,
                contributors.getOrDefault(CommercialRoleType.MODIFIER, List.of()), modifierGross);
        createContributionDetails(order, orderAmount, params, CommercialRoleType.UPLOADER,
                contributors.getOrDefault(CommercialRoleType.UPLOADER, List.of()), uploaderGross);
        createTemplateAuthorDetails(order, orderAmount, params, templateAuthorGrossByUsername);

        log.info("订单 {} 结算拆分完成", order.getId());
        order.setInternalStatus(NeteaseOrderStatus.CALCULATED);
        orderRepository.save(order);
        return true;
    }

    private boolean hasProjectRange(Project project) {
        return project.getWorld() != null
                && project.getMinX() != null
                && project.getMinY() != null
                && project.getMinZ() != null
                && project.getMaxX() != null
                && project.getMaxY() != null
                && project.getMaxZ() != null;
    }

    private boolean ensureContributorsPresent(
            Map<CommercialRoleType, List<UserProjectContribution>> contributors,
            CommercialRoleType role,
            BigDecimal gross,
            Long orderId
    ) {
        if (gross.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        if (!contributors.containsKey(role) || contributors.get(role).isEmpty()) {
            log.info("订单 {} 缺少 {} 参与者，跳过结算", orderId, role);
            return false;
        }
        return true;
    }

    private void createContributionDetails(
            NeteaseOrder order,
            BigDecimal orderAmount,
            GlobalCheckoutParamContext params,
            CommercialRoleType role,
            List<UserProjectContribution> contributions,
            BigDecimal gross
    ) {
        if (gross.compareTo(BigDecimal.ZERO) <= 0 || contributions.isEmpty()) {
            return;
        }

        List<BigDecimal> ratios = contributions.stream()
                .map(item -> BigDecimal.valueOf(item.getContributeRatio()).setScale(8, RoundingMode.HALF_UP))
                .toList();
        List<BigDecimal> amounts = split(gross, ratios);

        for (int i = 0; i < contributions.size(); i++) {
            UserProjectContribution contribution = contributions.get(i);
            BigDecimal amount = amounts.get(i).setScale(8, RoundingMode.HALF_UP);
            BigDecimal ratio = orderAmount.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(orderAmount, 8, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            checkoutDetailService.createCheckoutDetail(
                    contribution.getUsername(),
                    order.getId(),
                    role,
                    ratio,
                    amount,
                    orderAmount,
                    params
            );
        }
    }

    private void createTemplateAuthorDetails(
            NeteaseOrder order,
            BigDecimal orderAmount,
            GlobalCheckoutParamContext params,
            Map<String, BigDecimal> templateAuthorGrossByUsername
    ) {
        for (Map.Entry<String, BigDecimal> entry : templateAuthorGrossByUsername.entrySet()) {
            BigDecimal amount = entry.getValue().setScale(8, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal ratio = orderAmount.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(orderAmount, 8, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            checkoutDetailService.createCheckoutDetail(
                    entry.getKey(),
                    order.getId(),
                    CommercialRoleType.TEMPLATE_AUTHOR,
                    ratio,
                    amount,
                    orderAmount,
                    params
            );
        }
    }

    private List<TemplateUsageAllocation> buildTemplateAllocations(
            ProjectSettlementSnapshotPayload snapshot,
            BigDecimal lambda
    ) {
        if (snapshot.getTemplates() == null || snapshot.getTemplates().isEmpty()) {
            return List.of();
        }

        List<TemplateUsageAllocation> allocations = new ArrayList<>();
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

            BigDecimal decayedEffectiveBlocks = averageEffectiveBlocks.multiply(
                    geometricSeries(lambda, usage.getUsageCount()));
            allocations.add(new TemplateUsageAllocation(
                    usage.getTemplateId(),
                    author.getName(),
                    usage.getUsageCount(),
                    averageEffectiveBlocks.setScale(8, RoundingMode.HALF_UP),
                    decayedEffectiveBlocks.setScale(8, RoundingMode.HALF_UP)
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

    private record TemplateUsageAllocation(
            UUID templateId,
            String authorUsername,
            int usageCount,
            BigDecimal averageEffectiveBlocks,
            BigDecimal decayedEffectiveBlocks
    ) {
    }
}
