package com.jackyblackson.idunntemplates.backend.commercial.entity.withdraw;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "commercial_system_withdraw")
@NoArgsConstructor
public class SystemWithdraw {

    public enum Status {
        CREATED,    // 用户创建
        APPROVED,   // 管理员审批通过
        REJECTED,   // 管理员拒绝
        PAID,       // 管理员已转账
        FINISHED,   // 用户确认收款
        ERROR       // 转账或收款异常
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "amount", precision = 19, scale = 8, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.CREATED;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "error_reason", length = 500)
    private String errorReason;

    @Column(name = "transfer_proof", length = 500)
    private String transferProof;

    @Column(name = "create_time_ms", nullable = false)
    private Long createTimeMs;

    @Column(name = "approve_time_ms")
    private Long approveTimeMs;

    @Column(name = "reject_time_ms")
    private Long rejectTimeMs;

    @Column(name = "paid_time_ms")
    private Long paidTimeMs;

    @Column(name = "finish_time_ms")
    private Long finishTimeMs;

    @Column(name = "error_time_ms")
    private Long errorTimeMs;

}
