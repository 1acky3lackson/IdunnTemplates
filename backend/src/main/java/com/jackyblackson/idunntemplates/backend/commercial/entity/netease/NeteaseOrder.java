package com.jackyblackson.idunntemplates.backend.commercial.entity.netease;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "commercial_netease_order")
public class NeteaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ne_pe_product_log_id")
    private Long nePeProductLogId;

    @Column(name = "app_order_id")
    private String appOrderId;

    @Column(name = "app_order_id_int")
    private Long appOrderIdInt;

    @Column(name = "app_uid")
    private String appUid;

    @Column(name = "app_uid_int")
    private Long appUidInt;

    @Column(name = "discount")
    private String discount;

    @Column(name = "official_channel")
    private Integer officialChannel;

    @Column(name = "point")
    private Integer point;

    @Column(name = "point_type")
    private String pointType;

    @Column(name = "price")
    private Integer price;

    @Column(name = "price_type")
    private String priceType;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "purchase_limit")
    private Integer purchaseLimit;

    @Column(name = "refund_status")
    private String refundStatus;

    @Column(name = "ship_time")
    private String shipTime;

    @Column(name = "ship_time_ms")
    private Long shipTimeMs;

    @Column(name = "refund_in_time_ms")
    private Long refundInTimeMs;

    @Column(name = "internal_status")
    private NeteaseOrderStatus internalStatus = NeteaseOrderStatus.ENTERED;

    @ManyToOne(fetch = FetchType.LAZY)  // 默认关联查询为 LAZY 提升性能
    @JoinColumn(name = "product_id")    // 指定外键列名
    private NeteaseProduct product;
}
