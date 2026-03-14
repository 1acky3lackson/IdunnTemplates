package com.jackyblackson.idunntemplates.backend.commercial.dto.balance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceInfo {
    private BigDecimal availableBalance;   // 可用余额（已释放）
    private BigDecimal frozenBalance;      // 冻结中余额（CONFIRMED）
    private BigDecimal pendingBalance;     // 待结算余额（CREATED）
}
