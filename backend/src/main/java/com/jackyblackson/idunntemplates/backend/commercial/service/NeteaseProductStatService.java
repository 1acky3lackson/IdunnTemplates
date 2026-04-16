package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.dto.netease.ProductOrderStatsDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NeteaseProductStatService {

    private final NeteaseOrderRepository neteaseOrderRepository;

    /**
     * 计算指定产品的订单统计数据
     *
     * @param productId 产品ID
     * @return 统计结果 DTO
     */
    public ProductOrderStatsDto calculateProductStats(Long productId) {
        // 1. 获取该产品下的所有订单，按 shipTimeMs 升序排列
        List<NeteaseOrder> orders = neteaseOrderRepository.findByProductIdOrderByShipTimeMsAsc(productId);

        // 处理空数据情况
        if (orders == null || orders.isEmpty()) {
            return ProductOrderStatsDto.builder()
                    .orderCount(0)
                    .totalAmount(0L)
                    .averageAmount(0.0)
                    .avgTimeDiffMs(0.0)
                    .maxTimeDiffMs(0L)
                    .minTimeDiffMs(0L)
                    .medianTimeDiffMs(0.0)
                    .build();
        }

        long totalAmount = 0L;
        int validPriceCount = 0;

        List<Long> timeDiffs = new ArrayList<>();
        Long previousShipTime = null;

        // 2. 遍历订单收集数据
        for (NeteaseOrder order : orders) {
            // 累加原始虚拟点数
            if (order.getPoint() != null && order.getPoint() > 0) {
                totalAmount += order.getPoint();
                validPriceCount++;
            }

            // 计算相邻订单的时间差 (忽略 shipTimeMs 为 null 的数据)
            if (order.getShipTimeMs() != null) {
                if (previousShipTime != null) {
                    long diff = order.getShipTimeMs() - previousShipTime;
                    timeDiffs.add(diff);
                }
                previousShipTime = order.getShipTimeMs();
            }
        }

        // 3. 计算金额均值
        double averageAmount = validPriceCount > 0 ? (double) totalAmount / validPriceCount : 0.0;

        // 4. 计算时间差统计
        Double avgTimeDiff = 0.0;
        Long maxTimeDiff = 0L;
        Long minTimeDiff = 0L;
        Double medianTimeDiff = 0.0;

        if (!timeDiffs.isEmpty()) {
            // 对时间差列表进行排序，以便寻找极值和中位数
            Collections.sort(timeDiffs);

            minTimeDiff = timeDiffs.get(0);
            maxTimeDiff = timeDiffs.get(timeDiffs.size() - 1);
            avgTimeDiff = timeDiffs.stream().mapToLong(Long::longValue).average().orElse(0.0);

            // 计算中位数
            int size = timeDiffs.size();
            if (size % 2 == 0) {
                // 偶数个：取中间两项的平均值
                medianTimeDiff = (timeDiffs.get(size / 2 - 1) + timeDiffs.get(size / 2)) / 2.0;
            } else {
                // 奇数个：直接取中间项
                medianTimeDiff = (double) timeDiffs.get(size / 2);
            }
        }

        // 5. 构建并返回结果
        return ProductOrderStatsDto.builder()
                .orderCount(orders.size())
                .totalAmount(totalAmount)
                .averageAmount(averageAmount)
                .avgTimeDiffMs(avgTimeDiff)
                .maxTimeDiffMs(maxTimeDiff)
                .minTimeDiffMs(minTimeDiff)
                .medianTimeDiffMs(medianTimeDiff)
                .build();
    }
}
