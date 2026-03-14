package com.jackyblackson.idunntemplates.backend.commercial.task;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrderStatus;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.CheckoutDetailRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import com.jackyblackson.idunntemplates.backend.commercial.service.CheckoutDetailService;
import com.jackyblackson.idunntemplates.backend.commercial.service.NeteaseOrderSyncService;
import com.jackyblackson.idunntemplates.backend.commercial.service.NeteaseOrderUpdateService;
import com.jackyblackson.idunntemplates.backend.commercial.service.NeteaseProductSyncService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;

    // 可选：加一个应用层面的防重入锁，彻底杜绝任何意外的并发
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

    @Scheduled(cron = "0 0 4 * * ?", zone = "GMT+8")
    @Transactional
    public void scheduledSync() {
        try {// CAS 操作：如果当前正在同步，则直接跳过本次执行
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
        } finally {
            isSyncing.set(false);
            // 执行完大规模结算后
            log.info("同步完成，尝试手动回收内存...");
            entityManager.clear();
            System.gc();

            // 如果你坚持要重启：
            // this.restartSelf();
        }
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