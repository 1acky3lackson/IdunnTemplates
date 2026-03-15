package com.jackyblackson.idunntemplates.backend.commercial.dto.withdraw;

import com.jackyblackson.idunntemplates.backend.commercial.entity.withdraw.SystemWithdraw;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SystemWithdrawDto {
    private Long id;
    private String username;
    private BigDecimal amount;
    private SystemWithdraw.Status status;
    private String rejectReason;
    private String errorReason;
    private String transferProof;
    private Long createTimeMs;
    private Long approveTimeMs;
    private Long rejectTimeMs;
    private Long paidTimeMs;
    private Long finishTimeMs;
    private Long errorTimeMs;
}
