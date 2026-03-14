package com.jackyblackson.idunntemplates.backend.commercial.entity.balance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "commercial_user_balance",
        uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@NoArgsConstructor
public class UserBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "balance", precision = 19, scale = 8, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "version") // 乐观锁版本号
    private Long version;

    @Column(name = "update_time_ms")
    private Long updateTimeMs;
}
