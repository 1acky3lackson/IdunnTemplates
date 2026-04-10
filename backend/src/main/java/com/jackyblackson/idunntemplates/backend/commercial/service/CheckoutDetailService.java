package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutWithdrawAllocation;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.GlobalCheckoutParamContext;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseWithdraw;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.CheckoutDetailRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.CheckoutWithdrawAllocationRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseWithdrawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail.Status.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutDetailService {

    private final CheckoutDetailRepository checkoutDetailRepository;
    private final NeteaseOrderRepository neteaseOrderRepository;

    private final NeteaseWithdrawRepository neteaseWithdrawRepository;
    private final CheckoutWithdrawAllocationRepository allocationRepository;

    private final UserBalanceService userBalanceService;

    /**
     * 功能1：创建结算明细，保证 (username, orderId, role) 唯一
     */
    @Transactional
    public CheckoutDetail createCheckoutDetail(
            String username, Long orderId,
            CommercialRoleType role,
            BigDecimal ratio,
            BigDecimal netProfit,
            BigDecimal orderProfit,
            GlobalCheckoutParamContext paramContext
    ) {
        // 检查唯一性（可选，数据库唯一约束会最终保证，但提前检查可提供更友好提示）
//        if (checkoutDetailRepository.existsByUsernameAndOrder_IdAndRole(username, orderId, role)) {
//            throw new IllegalArgumentException(
//                    String.format("CheckoutDetail already exists for username=%s, orderId=%d, role=%s",
//                            username, orderId, role));
//        }

        // 加载关联的 NeteaseOrder
        NeteaseOrder order = neteaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("NeteaseOrder not found with id: " + orderId));

        CheckoutDetail detail = new CheckoutDetail();
        detail.setUsername(username);
        detail.setOrder(order);
        detail.setRole(role);
        detail.setRatio(ratio);
        detail.setNetProfit(netProfit);
        detail.setStatus(CREATED);
        detail.setCreateTimeMs(System.currentTimeMillis());
        detail.setParamContext(paramContext);
        detail.setOrderProfit(orderProfit);

        try {
            return checkoutDetailRepository.save(detail);
        } catch (DataIntegrityViolationException e) {
            // 捕获唯一约束冲突（并发情况）
            throw new IllegalArgumentException("Duplicate entry: combination (username, orderId, role) already exists", e);
        }
    }

    /**
     * 功能2：根据用户名和状态查询所有记录
     */
    @Transactional(readOnly = true)
    public List<CheckoutDetail> findByUsernameAndStatus(String username, CheckoutDetail.Status status) {
        return checkoutDetailRepository.findByUsernameAndStatus(username, status);
    }

    /**
     * 尝试支付一个 CREATED 状态的结账单
     * @param detail 待支付的结账单（netProfit 为原始金额）
     * @return true 表示支付成功，结账单变为 CONFIRMED；false 表示余额不足，未支付
     */
    @Transactional
    public boolean checkoutCreated(CheckoutDetail detail) {
        // 只处理 CREATED 状态的
        if (detail.getStatus() != CREATED) {
            log.warn("结账单 {} 状态不是 CREATED，当前状态：{}", detail.getId(), detail.getStatus());
            return false;
        }

        BigDecimal need = detail.getNetProfit();
        if (need == null || need.compareTo(BigDecimal.ZERO) <= 0) {
            // 无需支付，直接确认并立即释放？但 0 元无需处理
            detail.setStatus(CONFIRMED);
            detail.setActualProfit(BigDecimal.ZERO);
            detail.setConfirmTimeMs(System.currentTimeMillis());
            // 计划释放时间设为当前，这样释放任务会立即处理
            detail.setReleaseTimeMs(System.currentTimeMillis());
            checkoutDetailRepository.save(detail);
            log.info("结账单 {} net_profit 为 0，直接确认", detail.getId());
            return true;
        }

        // 查询可用提现记录并锁定（同前）
        List<NeteaseWithdraw> available = neteaseWithdrawRepository.findAvailableWithdraws();
        if (available.isEmpty()) {
            log.info("无可用的提现记录，结账单 {} 暂不支付", detail.getId());
            return false;
        }
        List<Long> ids = available.stream().map(NeteaseWithdraw::getId).collect(Collectors.toList());
        List<NeteaseWithdraw> lockedWithdraws = neteaseWithdrawRepository.findByIdsWithLock(ids);
        BigDecimal totalAvailable = lockedWithdraws.stream()
                .map(w -> w.getOriginalValue().subtract(w.getUsedOriginalValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalAvailable.compareTo(need) < 0) {
            log.info("可用提现总额 {} 不足以支付结账单 {} 所需 {}，暂不支付", totalAvailable, detail.getId(), need);
            return false;
        }

        // 分配提现记录（同前）
        BigDecimal remaining = need;
        List<CheckoutWithdrawAllocation> allocations = new ArrayList<>();
        for (NeteaseWithdraw withdraw : lockedWithdraws) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal availableForThis = withdraw.getOriginalValue().subtract(withdraw.getUsedOriginalValue());
            if (availableForThis.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal deduct = remaining.min(availableForThis);
            BigDecimal actual = deduct.multiply(withdraw.getRatio()).setScale(8, RoundingMode.HALF_UP);
            withdraw.setUsedOriginalValue(withdraw.getUsedOriginalValue().add(deduct));
            neteaseWithdrawRepository.save(withdraw);
            CheckoutWithdrawAllocation alloc = new CheckoutWithdrawAllocation();
            alloc.setCheckoutDetail(detail);
            alloc.setWithdraw(withdraw);
            alloc.setAllocatedOriginal(deduct);
            alloc.setActualAmount(actual);
            alloc.setCreateTimeMs(System.currentTimeMillis());
            allocations.add(alloc);
            remaining = remaining.subtract(deduct);
        }
        allocationRepository.saveAll(allocations);

        BigDecimal totalActual = allocations.stream()
                .map(CheckoutWithdrawAllocation::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        detail.setActualProfit(totalActual);
        detail.setConfirmTimeMs(System.currentTimeMillis());

        // 计算计划释放时间：订单时间 + N 天
        // 注意：订单时间可以从 detail.getOrder().getCreateTimeMs() 获取
        long orderTime = detail.getOrder().getShipTimeMs(); // 假设 NeteaseOrder 有 createTimeMs 字段
        Integer delayDays = detail.getParamContext().getReleaseDelayDays();
        if (delayDays == null) delayDays = 0; // 默认不延迟
        long scheduledRelease = orderTime + delayDays * 24L * 3600L * 1000L;
        detail.setReleaseTimeMs(scheduledRelease);

        detail.setStatus(CONFIRMED);  // 进入冻结状态
        checkoutDetailRepository.save(detail);

        log.info("结账单 {} 支付成功，使用 {} 笔提现记录，总实际到账 {}，计划释放时间：{}",
                detail.getId(), allocations.size(), totalActual, scheduledRelease);
        return true;
    }

    /**
     * 释放所有已到期的冻结资金，将资金记入用户余额
     * 此方法应由定时任务定期调用（例如每分钟一次）
     */
    @Transactional
    public void releaseConfirmedDetails() {
        long now = System.currentTimeMillis();
        // 查询所有已确认且计划释放时间 <= 当前时间的明细
        List<CheckoutDetail> details = checkoutDetailRepository.findByStatusAndReleaseTimeMsLessThanEqual(
                CONFIRMED, now);
        if (details.isEmpty()) {
            log.debug("没有待释放的结账单");
            return;
        }

        for (CheckoutDetail detail : details) {
            // 使用悲观锁或乐观锁防止并发重复释放
            // 这里用 status 作为乐观锁，更新时检查 status 仍为 CONFIRMED
            int updated = checkoutDetailRepository.updateStatusAndFinishTime(
                    detail.getId(), CONFIRMED, RELEASED, now);
            if (updated == 0) {
                // 可能已被其他线程释放，跳过
                log.warn("结账单 {} 状态已变更，跳过释放", detail.getId());
                continue;
            }
            // 释放资金到用户余额
            userBalanceService.addIncome(
                    detail.getUsername(),
                    detail.getActualProfit(), // 实际到账金额
                    detail.getId(),
                    "订单收入释放，结账单 #" + detail.getId()
            );
            log.info("结账单 {} 已释放，用户 {} 收入 {} 元", detail.getId(), detail.getUsername(), detail.getActualProfit());
        }
    }
}
