package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrderStatus;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.CheckoutDetailRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CheckoutCalculationService {

    private final CheckoutDetailService checkoutDetailService;
    private final CheckoutDetailRepository checkoutDetailRepository;
    private final NeteaseOrderRepository neteaseOrderRepository;
    private final NeteaseOrderUpdateService neteaseOrderUpdateService;

    public void processAllEnteredOrders() {
        int pageSize = 20;
        int count = 0;

        // 因为你的业务是 OrderByIdDesc（从大到小），初始游标设为 Long 的最大值
        // 这样第一次查询时，能抓取到所有实际存在的 ID
        long lastId = Long.MAX_VALUE;

        while (true) {
            // 注意：这里的 PageRequest 永远传 0 页，它只起到了 LIMIT 20 的作用
            Pageable limit = PageRequest.of(0, pageSize);

            java.util.List<NeteaseOrder> orders = neteaseOrderRepository.findByInternalStatusAndIdLessThanOrderByIdDesc(
                    NeteaseOrderStatus.ENTERED,
                    lastId,
                    limit
            );

            // 如果查不到数据了，说明所有符合条件的记录都已经遍历过了，退出
            if (orders.isEmpty()) {
                break;
            }

            // 处理当前批次
            for (NeteaseOrder order : orders) {
                // 无论 checkoutEnterToCalculated 成功还是失败，都不会阻碍程序继续往下走
                neteaseOrderUpdateService.checkoutEnterToCalculated(order);

                // 【核心】：不断更新游标为当前读取到的最小 ID
                // 因为集合是倒序排列的，最后一条一定是这个批次里 ID 最小的
                lastId = order.getId();
            }

            // 依然保留一个安全网，防止极端情况的无限循环
            count++;
            if (count > 600) {
                log.warn("批处理次数超过 600 次，强制退出以保护系统");
                break;
            }
        }
    }

    public void processAllCreatedDetails() {
        int pageSize = 20;
        // 始终定义抓取第 0 页
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("createTimeMs").ascending());

        while (true) {
            // 1. 获取当前最靠前的 20 条待处理记录
            Slice<CheckoutDetail> slice = checkoutDetailRepository.findByStatusOrderByCreateTimeMsAsc(
                    CheckoutDetail.Status.CREATED,
                    pageable
            );

            if (slice.isEmpty()) {
                break; // 全部处理完毕
            }

            boolean shouldStop = false;
            for (CheckoutDetail detail : slice.getContent()) {
                // 2. 调用你的业务方法
                boolean success = checkoutDetailService.checkoutCreated(detail);

                if (!success) {
                    // 如果失败（余额不足），说明后面的记录大概率也无法支付
                    log.info("余额不足以支付 ID: {}，停止本次批处理", detail.getId());
                    shouldStop = true;
                    break; // 跳出 for 循环
                }
            }

            // 3. 退出 while 循环的条件：
            // a. 业务处理失败中断
            // b. 没有更多数据（hasNext 为 false）
            if (shouldStop || !slice.hasNext()) {
                break;
            }

            // 注意：因为处理成功的记录状态变成了 CONFIRMED，
            // 它们会从 findByStatus(CREATED) 的结果中消失，
            // 所以我们不需要 currentPage++，下一次循环依然查第 0 页。
        }
    }
}
