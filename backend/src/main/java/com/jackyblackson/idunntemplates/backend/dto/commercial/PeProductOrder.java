package com.jackyblackson.idunntemplates.backend.dto.commercial;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PeProductOrder {
    @JsonProperty("app_orderid")
    private String appOrderId;

    @JsonProperty("app_uid")
    private String appUid;

    private String discount;

    @JsonProperty("official_channel")
    private Integer officialChannel;

    private Integer point;

    @JsonProperty("point_type")
    private String pointType;

    private Integer price;

    @JsonProperty("price_type")
    private String priceType;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("purchase_limit")
    private Integer purchaseLimit;

    @JsonProperty("refund_status")
    private String refundStatus;

    @JsonProperty("ship_time")
    private String shipTime;
}
