package com.jackyblackson.idunntemplates.backend.commercial.entity.balance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "commercial_user_balance_record", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_related_id", columnList = "related_id")
})
@NoArgsConstructor
public class UserBalanceRecord {

    public enum RecordType {
        INCOME, // 增加
        EXPENSE // 减少
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private RecordType type;

    @Column(name = "amount", precision = 19, scale = 8, nullable = false)
    private BigDecimal amount; // 正数表示增加，负数表示减少（但这里用 type 区分，amount 始终为正）

    @Column(name = "balance_before", precision = 19, scale = 8, nullable = false)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 19, scale = 8, nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "related_id")
    private Long relatedId; // 关联的业务ID，如 checkout_detail_id 或 withdraw_id

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "create_time_ms", nullable = false)
    private Long createTimeMs;
}