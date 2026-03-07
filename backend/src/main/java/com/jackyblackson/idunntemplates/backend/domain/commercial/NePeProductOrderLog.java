package com.jackyblackson.idunntemplates.backend.domain.commercial;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ne_pe_product_order_logs")
public class NePeProductOrderLog {

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

}
