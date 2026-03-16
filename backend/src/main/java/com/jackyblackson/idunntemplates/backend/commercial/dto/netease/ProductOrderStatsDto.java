package com.jackyblackson.idunntemplates.backend.commercial.dto.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderStatsDto {
    // 订单基础统计
    private Integer orderCount;         // 订单总数
    private Long totalAmount;           // 总金额 (防溢出使用 Long)
    private Double averageAmount;       // 平均金额

    // 订单时间差统计 (单位：毫秒)
    private Double avgTimeDiffMs;       // 平均时间差
    private Long maxTimeDiffMs;         // 最大时间差
    private Long minTimeDiffMs;         // 最小时间差
    private Double medianTimeDiffMs;    // 时间差中位数
}