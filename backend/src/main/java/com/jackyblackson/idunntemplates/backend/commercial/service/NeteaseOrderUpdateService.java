package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductOrderLog;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrderStatus;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import com.jackyblackson.idunntemplates.core.IdunnConstants;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
@Slf4j
public class NeteaseOrderUpdateService {

    private final UserProjectContributionService userProjectContributionService;
    private final GlobalCheckoutParamContextService globalCheckoutParamContextService;
    private final CheckoutDetailService checkoutDetailService;
    private final NeteaseOrderRepository orderRepository;

    private static final List<String> DIAMOND_WORD_LIST = List.of(
            "付费钻石",
            "钻石",
            "diamond",
            "Diamond",
            "DIAMOND",
            "diamonds",
            "Diamonds",
            "DIAMONDS"
    );

    // 最小金额单位：1e-8 元（对应数据库 DECIMAL(19,8) 的最小精度）
    private static final BigDecimal MIN_UNIT = new BigDecimal("1e-8");

    public void fromEnter(NeteaseOrder order, NePeProductOrderLog log) {
        // 其他状态处理方法保持为空或按需实现
    }

    public void fromCalculated(NeteaseOrder order, NePeProductOrderLog log) {
    }

    public void fromProfitted(NeteaseOrder order, NePeProductOrderLog log) {
    }

    public void fromTPlusM(NeteaseOrder order, NePeProductOrderLog log) {
    }

    public void fromTPlusN(NeteaseOrder order, NePeProductOrderLog log) {
    }

    /**
     * 高精度分配工具：将总金额 total 按比例列表 ratios 分配给多人，
     * 保证每个分配值都是 MIN_UNIT 的整数倍，且总和等于 total。
     *
     * @param total  总金额（已保证是 MIN_UNIT 的整数倍，即 scale ≤ 8）
     * @param ratios 比例列表（每个比例应为 0~1 之间，总和为 1）
     * @return 分配后的金额列表（每个元素 scale = 8，总和等于 total）
     */
    private List<BigDecimal> split(BigDecimal total, List<BigDecimal> ratios) {
        int n = ratios.size();
        if (n == 0) return List.of();

        // 第一步：向下取整到 MIN_UNIT
        List<BigDecimal> amounts = new ArrayList<>(n);
        BigDecimal sumFloor = BigDecimal.ZERO;
        for (BigDecimal ratio : ratios) {
            // 理论值：total * ratio（使用足够精度）
            BigDecimal raw = total.multiply(ratio);
            // 向下取整到 MIN_UNIT
            BigDecimal floor = raw.setScale(8, RoundingMode.FLOOR);
            amounts.add(floor);
            sumFloor = sumFloor.add(floor);
        }

        // 计算差额（应为 MIN_UNIT 的整数倍，且 0 ≤ diff < n * MIN_UNIT）
        BigDecimal diff = total.subtract(sumFloor);
        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            return amounts;
        }

        // 将差额转换为最小单位个数
        long diffUnits = diff.divide(MIN_UNIT, 0, RoundingMode.HALF_UP).longValue();
        // 按比例从大到小排序索引
        List<Integer> indices = IntStream.range(0, n)
                .boxed()
                .sorted(Comparator.comparing((Integer i) -> ratios.get(i)).reversed())
                .collect(Collectors.toList());

        // 循环分配最小单位（优先给比例大的，以减小误差）
        for (int i = 0; i < diffUnits; i++) {
            int idx = indices.get(i % n); // 循环分配，避免集中于前几人
            amounts.set(idx, amounts.get(idx).add(MIN_UNIT));
        }

        return amounts;
    }

    @Transactional
    public boolean checkoutEnterToCalculated(NeteaseOrder order) {
        // 状态检查
        if (!order.getInternalStatus().equals(NeteaseOrderStatus.ENTERED)) {
            log.info("订单 {} 状态为 {}，不是 ENTERED，跳过结算", order.getId(), order.getInternalStatus());
            return false;
        }

        // 钻石类型和点数检查
        if (!(DIAMOND_WORD_LIST.contains(order.getPointType()) && order.getPoint() > 0)) {
            order.setInternalStatus(NeteaseOrderStatus.AFTER_N);
            orderRepository.save(order);
            log.info("订单 {} 非钻石或点数为零 (pointType={}, point={})，标记为 AFTER_N 并跳过结算",
                    order.getId(), order.getPointType(), order.getPoint());
            return false;
        }

        Project project = order.getProduct().getProject();
        if (project == null) {
            log.warn("订单 {} 关联的产品项目为空，跳过结算等待下次", order.getId());
            return false;
        }

        var contributors = userProjectContributionService.getContributionsGroupedByRole(project.getId());
        var params = globalCheckoutParamContextService.getEffectiveConfig();
        if (contributors == null || params == null) {
            log.info("订单 {} 参与者或结算参数为空 (contributors={}, params={})，跳过结算等待下次",
                    order.getId(), contributors, params);
            return false;
        }

        // 检查各角色至少有一人
        if (!contributors.containsKey(CommercialRoleType.BUILDER) || contributors.get(CommercialRoleType.BUILDER).isEmpty() ||
                !contributors.containsKey(CommercialRoleType.MODIFIER) || contributors.get(CommercialRoleType.MODIFIER).isEmpty() ||
                !contributors.containsKey(CommercialRoleType.UPLOADER) || contributors.get(CommercialRoleType.UPLOADER).isEmpty()) {
            log.info("订单 {} 项目 {} 缺少 BUILDER/MODIFIER/UPLOADER 参与者，跳过结算", order.getId(), project.getId());
            return false;
        }

        // 1. 钻石转化为人民币，保留 8 位小数（确保是 MIN_UNIT 的整数倍）
        BigDecimal rmbPrice = BigDecimal.valueOf(order.getPoint())
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);

        // 2. 太学收入（高精度）
        BigDecimal taixueRatio = BigDecimal.valueOf(params.getTaixueRatio()); // 假设 params 中已是 BigDecimal
        BigDecimal taixueProfit = rmbPrice.multiply(taixueRatio); // 未舍入，但后续存入时会自动舍入到 8 位
        checkoutDetailService.createCheckoutDetail(
                IdunnConstants.SYSTEM_USER_NAME,
                order.getId(),
                CommercialRoleType.SYSTEM,
                taixueRatio.setScale(8, RoundingMode.HALF_UP), // 比率也舍入到 8 位
                taixueProfit.setScale(8, RoundingMode.HALF_UP),
                rmbPrice,
                params
        );

        // 剩余待分配金额（精确减法）
        BigDecimal remaining = rmbPrice.subtract(taixueProfit);

        // 3. 商务占比
        BigDecimal commercialRatio = BigDecimal.valueOf(params.getCommercialRatio());

        // 建筑组理论利润
        BigDecimal builderGross = remaining.multiply(BigDecimal.ONE.subtract(commercialRatio));
        // 商务组理论利润（剩余部分）
        BigDecimal commercialGross = remaining.subtract(builderGross);

        // 建筑组比率（用于 ratio 字段）
        BigDecimal builderGroupRatio = BigDecimal.ONE.subtract(taixueRatio)
                .multiply(BigDecimal.ONE.subtract(commercialRatio))
                .setScale(8, RoundingMode.HALF_UP);

        // 4. 建筑人员分配
        List<UserProjectContribution> builderContribs = contributors.get(CommercialRoleType.BUILDER);
        List<BigDecimal> builderRatios = builderContribs.stream()
                .map(c -> BigDecimal.valueOf(c.getContributeRatio()).setScale(8, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
        List<BigDecimal> builderAmounts = split(builderGross, builderRatios);

        for (int i = 0; i < builderContribs.size(); i++) {
            UserProjectContribution c = builderContribs.get(i);
            BigDecimal personalRatio = builderGroupRatio.multiply(BigDecimal.valueOf(c.getContributeRatio()))
                    .setScale(8, RoundingMode.HALF_UP);
            checkoutDetailService.createCheckoutDetail(
                    c.getUsername(),
                    order.getId(),
                    CommercialRoleType.BUILDER,
                    personalRatio,
                    builderAmounts.get(i),
                    rmbPrice,
                    params
            );
        }

        // 5. 上传者比例
        BigDecimal uploaderRatio = BigDecimal.valueOf(params.getUploaderRatio());

        // 上传组理论利润
        BigDecimal uploaderGross = commercialGross.multiply(uploaderRatio);
        // 修改组理论利润
        BigDecimal modifierGross = commercialGross.subtract(uploaderGross);

        // 上传组比率
        BigDecimal uploaderGroupRatio = BigDecimal.ONE.subtract(taixueRatio)
                .multiply(commercialRatio)
                .multiply(uploaderRatio)
                .setScale(8, RoundingMode.HALF_UP);

        // 6. 上传人员分配
        List<UserProjectContribution> uploaderContribs = contributors.get(CommercialRoleType.UPLOADER);
        List<BigDecimal> uploaderRatios = uploaderContribs.stream()
                .map(c -> BigDecimal.valueOf(c.getContributeRatio()).setScale(8, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
        List<BigDecimal> uploaderAmounts = split(uploaderGross, uploaderRatios);

        for (int i = 0; i < uploaderContribs.size(); i++) {
            UserProjectContribution c = uploaderContribs.get(i);
            BigDecimal personalRatio = uploaderGroupRatio.multiply(BigDecimal.valueOf(c.getContributeRatio()))
                    .setScale(8, RoundingMode.HALF_UP);
            checkoutDetailService.createCheckoutDetail(
                    c.getUsername(),
                    order.getId(),
                    CommercialRoleType.UPLOADER,
                    personalRatio,
                    uploaderAmounts.get(i),
                    rmbPrice,
                    params
            );
        }

        // 7. 修改组比率
        BigDecimal modifierGroupRatio = BigDecimal.ONE.subtract(taixueRatio)
                .multiply(commercialRatio)
                .multiply(BigDecimal.ONE.subtract(uploaderRatio))
                .setScale(8, RoundingMode.HALF_UP);

        // 8. 修改人员分配
        List<UserProjectContribution> modifierContribs = contributors.get(CommercialRoleType.MODIFIER);
        List<BigDecimal> modifierRatios = modifierContribs.stream()
                .map(c -> BigDecimal.valueOf(c.getContributeRatio()).setScale(8, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
        List<BigDecimal> modifierAmounts = split(modifierGross, modifierRatios);

        for (int i = 0; i < modifierContribs.size(); i++) {
            UserProjectContribution c = modifierContribs.get(i);
            BigDecimal personalRatio = modifierGroupRatio.multiply(BigDecimal.valueOf(c.getContributeRatio()))
                    .setScale(8, RoundingMode.HALF_UP);
            checkoutDetailService.createCheckoutDetail(
                    c.getUsername(),
                    order.getId(),
                    CommercialRoleType.MODIFIER,
                    personalRatio,
                    modifierAmounts.get(i),
                    rmbPrice,
                    params
            );
        }

        log.info("订单 {} 结算拆分完成", order.getId());
        order.setInternalStatus(NeteaseOrderStatus.CALCULATED);
        orderRepository.save(order);
        return true;
    }
}