package com.jackyblackson.idunntemplates.backend.commercial.dto.balance;

import com.jackyblackson.idunntemplates.backend.commercial.entity.balance.UserBalanceRecord;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserBalanceRecordDto {
    private Long id;
    private String username;
    private UserBalanceRecord.RecordType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private Long relatedId;
    private String description;
    private Long createTimeMs;
}
