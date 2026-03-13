package com.jackyblackson.idunntemplates.backend.commercial.dto.netease;

import lombok.Data;

@Data
public class NeteaseOrderDto {
    private Long id;
    private Long nePeProductLogId;
    private String appOrderId;
    private Long appOrderIdInt;
    private String appUid;
    private Long appUidInt;
    private String discount;
    private Integer officialChannel;
    private Integer point;
    private String pointType;
    private Integer price;
    private String priceType;
    private String productName;
    private Integer purchaseLimit;
    private String refundStatus;
    private String shipTime;
    private Long shipTimeMs;
    private Long refundInTimeMs;
    private String internalStatus; // 枚举名称
    private Long productId;         // 关联的商品ID（可选）
}