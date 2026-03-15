package com.jackyblackson.idunntemplates.backend.commercial.entity.checkout;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseWithdraw;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "commercial_checkout_withdraw_alloc")
@Data
@NoArgsConstructor
public class CheckoutWithdrawAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_detail_id", nullable = false)
    private CheckoutDetail checkoutDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "withdraw_id", nullable = false)
    private NeteaseWithdraw withdraw;

    @Column(name = "allocated_original", precision = 19, scale = 8, nullable = false)
    private BigDecimal allocatedOriginal; // 从提现记录中扣除的 originalValue 金额

    @Column(name = "actual_amount", precision = 19, scale = 8, nullable = false)
    private BigDecimal actualAmount; // 实际到账金额 = allocatedOriginal * withdraw.ratio

    @Column(name = "create_time_ms")
    private Long createTimeMs;
}
