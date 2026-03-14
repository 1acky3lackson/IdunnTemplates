package com.jackyblackson.idunntemplates.backend.commercial.entity.checkout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseWithdraw;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "commercial_checkout_detail",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "netease_order_id", "role"}),
        indexes = {
                @Index(name = "idx_username_status", columnList = "username, status"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_order_id", columnList = "netease_order_id"),
                @Index(name = "idx_withdraw_id", columnList = "ne_withdraw_id")
        })
@NoArgsConstructor
public class CheckoutDetail {

    public enum Status {
        CREATED,
        CONFIRMED,
        RELEASED,
        FINISHED,
        REFUNDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)  // 显式指定存储方式
    private Status status = Status.CREATED;  // 默认值

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "netease_order_id")
    private NeteaseOrder order;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_context_id")
    private GlobalCheckoutParamContext paramContext;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private CommercialRoleType role;

    @Column(name = "username")
    private String username;

    @Column(name = "ratio", precision = 19, scale = 8)   // 比率通常需要更高精度
    private BigDecimal ratio;

    @Column(name = "order_profit", precision = 19, scale = 6)
    private BigDecimal orderProfit;

    @Column(name = "net_profit", precision = 19, scale = 8)
    private BigDecimal netProfit;

    @Column(name = "actual_profit", precision = 19, scale = 8)
    private BigDecimal actualProfit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ne_withdraw_id")
    private NeteaseWithdraw withdraw;

    @Column(name = "create_time_ms")
    private Long createTimeMs;

    @Column(name = "confirm_time_ms")
    private Long confirmTimeMs;

    @Column(name = "release_time_ms")
    private Long releaseTimeMs;

    @Column(name = "finish_time_ms")
    private Long finishTimeMs;

    @Column(name = "refund_time_ms")
    private Long refundTimeMs;

    public boolean isRefunded() {
        return this.refundTimeMs != null;
    }
}
