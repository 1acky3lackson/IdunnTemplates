package com.jackyblackson.idunntemplates.backend.commercial.dto.withdraw;

import lombok.Data;

@Data
public class SystemWithdrawStatusUpdateRequest {
    private String status;
    private String reason;
    private String transferProof;
}
