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
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("id").descending());
        int count = 0;
        while (true) {
            // 1. 始终查询第 0 页
            Page<NeteaseOrder> page = neteaseOrderRepository.findByInternalStatusOrderByIdDesc(
                    NeteaseOrderStatus.ENTERED,
                    pageable
            );

            // 2. 处理当前页数据
            page.getContent().forEach(neteaseOrderUpdateService::checkoutEnterToCalculated);

            // 3. 如果没有下一页了，或者当前页没满（说明后面没数据了），则退出
            if (!page.hasNext()) {
                break;
            }

            // 可选：如果担心死循环（比如 service 没能成功修改状态），可以加一个安全计数器
            count ++;
            if (count > 200) {
                log.warn("循环次数过多，大于2000");
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
