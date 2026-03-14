package com.jackyblackson.idunntemplates.backend.commercial.task;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrderStatus;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.CheckoutDetailRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import com.jackyblackson.idunntemplates.backend.commercial.service.CheckoutDetailService;
import com.jackyblackson.idunntemplates.backend.commercial.service.NeteaseOrderSyncService;
import com.jackyblackson.idunntemplates.backend.commercial.service.NeteaseOrderUpdateService;
import com.jackyblackson.idunntemplates.backend.commercial.service.NeteaseProductSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
public class NeteaseSyncTask {

    private final NeteaseProductSyncService syncService;
    private final NeteaseOrderSyncService orderSyncService;
    private final NeteaseOrderRepository neteaseOrderRepository;
    private final NeteaseOrderUpdateService neteaseOrderUpdateService;
    private final CheckoutDetailRepository checkoutDetailRepository;
    private final CheckoutDetailService checkoutDetailService;

    // 可选：加一个应用层面的防重入锁，彻底杜绝任何意外的并发
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

    /**
     * 统一为一个调度任务。
     * initialDelay = 3000: 延迟 3 秒执行第一次（代替 ApplicationReadyEvent，给系统启动缓冲时间）
     * fixedDelay = 300000: 在上一次任务执行【完毕】后，等待 5 分钟再执行下一次（防止 fixedRate 导致的追尾并发）
     */
    @Scheduled(initialDelay = 3000, fixedDelay = 60 * 1000 * 60)
    @Transactional
    public void scheduledSync() {
        // CAS 操作：如果当前正在同步，则直接跳过本次执行
        if (!isSyncing.compareAndSet(false, true)) {
            log.warn("上一次同步任务尚未结束，本次定时触发将跳过");
            return;
        }

        try {
            log.debug("开始执行同步：同步网易PE产品记录...");
            doCrawler();
        } finally {
            // 无论成功还是异常，必须释放锁
            isSyncing.set(false);
        }

        neteaseOrderRepository.findByInternalStatusOrderByIdDesc(NeteaseOrderStatus.ENTERED).forEach(neteaseOrderUpdateService::checkoutEnterToCalculated);
        // 查询所有 CREATED 的结账单
        List<CheckoutDetail> createdDetails = checkoutDetailRepository.findByStatusOrderByCreateTimeMsAsc(CheckoutDetail.Status.CREATED);
        for (CheckoutDetail detail : createdDetails) {
            boolean success = checkoutDetailService.checkoutCreated(detail);
            if (!success) {
                // 记录日志，等待下次处理
                log.info("余额不足以支付，等待下一次处理");
                break;
            }
        }
        // 释放冻结资金
        checkoutDetailService.releaseConfirmedDetails();
    }

    protected void doCrawler() {
        // 确保你的 syncAll 里面是一个普通的 for 循环，绝对不能用 parallelStream()
        var res = this.syncService.syncAll();
        if (res != null) {
            log.debug("已经同步 {} 条网易PE产品记录", res.size());
        }
        this.orderSyncService.syncOrdersFromLogs();
    }
}