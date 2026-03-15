package com.jackyblackson.idunntemplates.backend.commercial.dto.withdraw;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SystemWithdrawCreateRequest {
    private BigDecimal amount;
}
