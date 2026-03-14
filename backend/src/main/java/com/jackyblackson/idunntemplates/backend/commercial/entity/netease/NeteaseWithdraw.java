package com.jackyblackson.idunntemplates.backend.commercial.entity.netease;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "commercial_netease_withdraw")
@NoArgsConstructor
public class NeteaseWithdraw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "operate_username")
    private String username;

    @Column(name = "save_time_ms")
    private Long saveTimeMs = System.currentTimeMillis();

    /**
     * 不计算费率前提下，提现的钱
     */
    @Column(name = "original_value", precision = 19, scale = 6)
    private BigDecimal originalValue;

    @Column(name = "used_original_value", precision = 19, scale = 8)
    private BigDecimal usedOriginalValue = BigDecimal.ZERO;

    /**
     * 提现实际提取出的钱
     */
    @Column(name = "withdraw_value", precision = 19, scale = 6)
    private BigDecimal withdrawValue;

    /**
     * 提现的费率，计算公式 withdrawValue / originalValue
     */
    @Column(name = "ratio", precision = 19, scale = 8)   // 比率通常需要更高精度
    private BigDecimal ratio;
}
