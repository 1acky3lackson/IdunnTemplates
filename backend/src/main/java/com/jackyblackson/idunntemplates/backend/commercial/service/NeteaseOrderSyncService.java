package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductLog;
import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductOrderLog;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrderStatus;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProduct;
import com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler.NePeProductLogRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler.NePeProductOrderLogRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 订单同步服务：将爬虫日志中的最新订单记录同步到业务订单表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NeteaseOrderSyncService {
    private final NePeProductOrderLogRepository logRepository;
    private final NePeProductLogRepository productLogRepository;
    private final NeteaseProductRepository productRepository;
    private final NeteaseOrderRepository orderRepository;
    private final NeteaseOrderUpdateService neteaseRefundService;
    // 注入 TransactionTemplate 用于编程式事务控制
    private final TransactionTemplate transactionTemplate;

    /**
     * 顶层方法：负责分页调度，【千万不要加 @Transactional】
     */
    public void syncOrdersFromLogs() {
        log.info("开始分页同步订单数据...");

        int pageSize = 500; // 每批处理 500 条
        int pageNumber = 0;
        long totalProcessed = 0;

        while (true) {
            // Native Query 已经包含了 ORDER BY，所以这里不需要传 Sort
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            Slice<NePeProductOrderLog> slice = logRepository.findLatestLogsPerAppOrderId(pageable);

            if (slice.isEmpty()) {
                break;
            }

            List<NePeProductOrderLog> batchLogs = slice.getContent();

            // 将每个批次的逻辑包裹在一个独立事务中
            transactionTemplate.executeWithoutResult(status -> {
                processBatch(batchLogs);
            });

            totalProcessed += batchLogs.size();
            log.info("同步进度：已处理 {} 条最新订单记录", totalProcessed);

            if (!slice.hasNext()) {
                break; // 处理完最后一页，退出
            }
            pageNumber++; // 原表数据不变，按页递增
        }

        log.info("同步完成，共计处理 {} 条唯一订单记录", totalProcessed);
    }

    /**
     * 处理单批次数据（在 TransactionTemplate 的事务内执行）
     */
    private void processBatch(List<NePeProductOrderLog> batchLogs) {
        // 1. 提取 appOrderId
        List<String> appOrderIds = batchLogs.stream()
                .map(NePeProductOrderLog::getAppOrderId)
                .filter(Objects::nonNull)
                .toList();

        if (appOrderIds.isEmpty()) return;

        // 2. 批量查询已存在的订单
        Map<String, NeteaseOrder> existingOrderMap = orderRepository.findByAppOrderIdIn(appOrderIds).stream()
                .collect(Collectors.toMap(NeteaseOrder::getAppOrderId, order -> order));

        // ================= 消除 N+1 查询的核心逻辑 =================
        // 收集所有需要新建订单的 productLogId
        List<Long> needLogIds = batchLogs.stream()
                .filter(log -> !existingOrderMap.containsKey(log.getAppOrderId()))
                .map(NePeProductOrderLog::getNePeProductLogId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, NePeProductLog> productLogMap = new HashMap<>();
        Map<String, NeteaseProduct> productMap = new HashMap<>();

        if (!needLogIds.isEmpty()) {
            // 批量拉取 ProductLog
            List<NePeProductLog> pLogs = productLogRepository.findAllById(needLogIds);
            productLogMap = pLogs.stream().collect(Collectors.toMap(NePeProductLog::getId, l -> l));

            // 批量拉取 Product
            List<String> itemIds = pLogs.stream().map(NePeProductLog::getItemId).filter(Objects::nonNull).distinct().toList();
            if (!itemIds.isEmpty()) {
                // 假设你有 findByItemIdIn 方法，如果没有请在 Repository 中添加
                List<NeteaseProduct> products = productRepository.findByItemIdIn(itemIds);
                productMap = products.stream().collect(Collectors.toMap(NeteaseProduct::getItemId, p -> p));
            }
        }
        // =========================================================

        List<NeteaseOrder> ordersToSave = new ArrayList<>();

        for (NePeProductOrderLog orderLog : batchLogs) {
            String appOrderId = orderLog.getAppOrderId();
            if (appOrderId == null) continue;

            NeteaseOrder order = existingOrderMap.get(appOrderId);
            if (order == null) {
                // 新建逻辑
                order = new NeteaseOrder();
                NePeProductLog productLog = productLogMap.get(orderLog.getNePeProductLogId());

                if (productLog == null) {
                    log.warn("找不到对应 NePeProductLog 的 id={} 的记录，丢弃此 Order 的同步", orderLog.getNePeProductLogId());
                    continue;
                }

                NeteaseProduct product = productMap.get(productLog.getItemId());
                if (product == null) {
                    log.warn("找不到对应 itemId={} 的 Product 记录，丢弃此 Order 的同步", productLog.getItemId());
                    continue;
                }

                order.setProduct(product);
                copyFromLog(order, orderLog);
            } else {
                // 更新逻辑
                updateOrder(order, orderLog);
            }
            ordersToSave.add(order);
        }

        // 3. 批量保存
        if (!ordersToSave.isEmpty()) {
            orderRepository.saveAll(ordersToSave);
        }
    }

    /**
     * 查找每个 appOrderId 的最新日志记录（id 最大的那条）
     * 使用子查询实现分组取最大 id
     */
    private List<NePeProductOrderLog> findLatestLogsPerAppOrderId() {
        // 使用 JPQL 子查询：先找出每个 appOrderId 的最大 id，再查询这些 id 对应的完整记录
        return logRepository.findLatestLogsPerAppOrderId();
    }

    /**
     * 将日志对象的基本字段复制到新建的订单对象
     * 这里仅复制共同字段，可根据实际情况调整
     */
    private void copyFromLog(NeteaseOrder order, NePeProductOrderLog log) {
        order.setNePeProductLogId(log.getId());
        order.setAppOrderId(log.getAppOrderId());
        order.setAppOrderIdInt(log.getAppOrderIdInt());
        order.setAppUid(log.getAppUid());
        order.setAppUidInt(log.getAppUidInt());
        order.setDiscount(log.getDiscount());
        order.setOfficialChannel(log.getOfficialChannel());
        order.setPoint(log.getPoint());
        order.setPointType(log.getPointType());
        order.setPrice(log.getPrice());
        order.setPriceType(log.getPriceType());
        order.setProductName(log.getProductName());
        order.setPurchaseLimit(log.getPurchaseLimit());
        order.setRefundStatus(log.getRefundStatus());
        order.setShipTime(log.getShipTime());
        order.setShipTimeMs(log.getShipTimeMs());
        // 注意：NeteaseOrder 特有的字段（如 refundInTimeMs, internalStatus）保持默认值
    }

    /**
     * 更新已存在的订单（待实现）
     * 此方法将在后续由用户手动填充具体的更新逻辑
     * @param order 已存在的订单实体
     * @param log   最新的日志记录
     */
    private void updateOrder(NeteaseOrder order, NePeProductOrderLog log) {
        // 退款
        if(!order.getInternalStatus().equals(NeteaseOrderStatus.REFUNDED) && !log.getRefundStatus().isEmpty()) {
            if (order.getRefundInTimeMs() == null || order.getRefundInTimeMs() <= 0) {
                order.setRefundInTimeMs(System.currentTimeMillis());
                if (order.getInternalStatus().equals(NeteaseOrderStatus.ENTERED)) {
                    neteaseRefundService.fromEnter(order, log);
                } else if (order.getInternalStatus().equals(NeteaseOrderStatus.CALCULATED)) {
                    neteaseRefundService.fromCalculated(order, log);
                } else if (order.getInternalStatus().equals(NeteaseOrderStatus.INCOME)) {
                    neteaseRefundService.fromProfitted(order, log);
                } else if (order.getInternalStatus().equals(NeteaseOrderStatus.AFTER_M)) {
                    neteaseRefundService.fromTPlusM(order, log);
                } else if (order.getInternalStatus().equals(NeteaseOrderStatus.AFTER_N)) {
                    neteaseRefundService.fromTPlusN(order, log);
                } else {
                    throw new IllegalStateException("Unknown Netease Order internal status: " + order.getInternalStatus());
                }
            }

        }

        // finally
        copyFromLog(order, log);
    }
}
