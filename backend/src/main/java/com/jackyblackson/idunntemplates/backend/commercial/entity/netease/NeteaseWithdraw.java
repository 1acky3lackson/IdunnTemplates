package com.jackyblackson.idunntemplates.backend.commercial.entity.netease;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "commercial_netease_withdraw")
@NoArgsConstructor
public class NeteaseWithdraw {
    public NeteaseWithdraw(
            String username,
            Double originalValue,
            Double withdrawValue
    ) {
        this.username = username;
        this.originalValue = originalValue;
        this.withdrawValue = withdrawValue;
        this.ratio = withdrawValue / originalValue;
        this.saveTimeMs = System.currentTimeMillis();
    }

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
    @Column(name = "original_value")
    private Double originalValue;

    /**
     * 提现实际提取出的钱
     */
    @Column(name = "withdraw_value")
    private Double withdrawValue;

    /**
     * 提现的费率，计算公式 withdrawValue / originalValue
     */
    @Column(name = "ratio")
    private Double ratio;
}
