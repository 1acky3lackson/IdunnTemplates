package com.jackyblackson.idunntemplates.backend.commercial.dto.balance;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserBalanceListItemDto {
    private String username;
    private BigDecimal availableBalance;
    private BigDecimal frozenBalance;
    private BigDecimal pendingBalance;
}
