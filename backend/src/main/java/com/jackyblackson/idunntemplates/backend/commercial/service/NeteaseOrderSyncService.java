package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductOrderLog;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrderStatus;
import com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler.NePeProductLogRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler.NePeProductOrderLogRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    /**
     * 执行同步操作：
     * 1. 找出每个 appOrderId 对应的最新日志记录（id最大）
     * 2. 对于每个最新记录，如果对应订单不存在则新建，存在则调用 updateOrder 更新
     * 3. 批量保存到数据库
     */
    @Transactional
    public void syncOrdersFromLogs() {
        log.info("开始同步订单数据...");

        // 1. 获取所有非空 appOrderId 的最新日志记录
        List<NePeProductOrderLog> latestLogs = findLatestLogsPerAppOrderId();

        if (latestLogs.isEmpty()) {
            log.info("没有需要同步的日志记录");
            return;
        }

        // 2. 提取所有 appOrderId 用于查询已存在的订单
        List<String> appOrderIds = latestLogs.stream()
                .map(NePeProductOrderLog::getAppOrderId)
                .filter(Objects::nonNull)
                .toList();

        // 3. 查询已存在的订单，按 appOrderId 映射
        Map<String, NeteaseOrder> existingOrderMap = orderRepository.findByAppOrderIdIn(appOrderIds).stream()
                .collect(Collectors.toMap(NeteaseOrder::getAppOrderId, order -> order));

        // 4. 准备待保存的订单列表
        List<NeteaseOrder> ordersToSave = latestLogs.stream()
                .map(orderLog -> {
                    String appOrderId = orderLog.getAppOrderId();
                    if (appOrderId == null) {
                        log.warn("日志记录 id={} 的 appOrderId 为空，跳过", orderLog.getId());
                        return null;
                    }

                    NeteaseOrder order = existingOrderMap.get(appOrderId);
                    if (order == null) {
                        // 不存在，新建
                        order = new NeteaseOrder();
                        var productLog = productLogRepository.findById(orderLog.getNePeProductLogId());
                        if (!productLog.isPresent()) {
                            log.warn("找不到对应 NePeProductLog 的 id 为 " + order.getNePeProductLogId() + " 的记录，丢弃此 Order 的同步");
                            return null;
                        }
                        var product = productRepository.findByItemId(productLog.get().getItemId()).orElseThrow();
                        order.setProduct(product);
                        // 复制基本字段（可根据需要复制更多字段，此处仅示例）
                        copyFromLog(order, orderLog);
                    } else {
                        // 已存在，调用更新方法（由用户后续实现）
                        updateOrder(order, orderLog);
                    }
                    return order;
                })
                .filter(Objects::nonNull)
                .toList();

        // 5. 批量保存
        if (!ordersToSave.isEmpty()) {
            orderRepository.saveAll(ordersToSave);
            log.info("同步完成，共处理 {} 条记录", ordersToSave.size());
        } else {
            log.info("没有需要保存的订单");
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
