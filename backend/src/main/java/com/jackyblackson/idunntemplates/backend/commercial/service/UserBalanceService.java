package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.entity.balance.UserBalance;
import com.jackyblackson.idunntemplates.backend.commercial.entity.balance.UserBalanceRecord;
import com.jackyblackson.idunntemplates.backend.commercial.repository.balance.UserBalanceRecordRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.balance.UserBalanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBalanceService {

    private final UserBalanceRepository userBalanceRepository;
    private final UserBalanceRecordRepository recordRepository;

    /**
     * 增加增加
     * 
     * @param username    用户名
     * @param amount      增加金额（正数）
     * @param relatedId   关联业务ID（如 checkout_detail_id）
     * @param description 描述
     */
    @Transactional
    public void addIncome(String username, BigDecimal amount, Long relatedId, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("增加金额必须为正数");
        }
        updateBalance(username, amount, UserBalanceRecord.RecordType.INCOME, relatedId, description, false);
    }

    /**
     * 增加减少（扣款）
     * 
     * @param username    用户名
     * @param amount      减少金额（正数）
     * @param relatedId   关联业务ID
     * @param description 描述
     * @throws IllegalStateException 余额不足时抛出
     */
    @Transactional
    public void addExpense(String username, BigDecimal amount, Long relatedId, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("减少金额必须为正数");
        }
        updateBalance(username, amount.negate(), UserBalanceRecord.RecordType.EXPENSE, relatedId, description, false);
    }

    /**
     * 退款冲销类支出允许余额变为负数，以确保已发放收益能够被完整扣回。
     */
    @Transactional
    public void addRefundExpense(String username, BigDecimal amount, Long relatedId, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("减少金额必须为正数");
        }
        updateBalance(username, amount.negate(), UserBalanceRecord.RecordType.EXPENSE, relatedId, description, true);
    }

    /**
     * 内部方法：更新余额并记录虚拟点数变动
     * 
     * @param username    用户名
     * @param delta       变动金额（可为正或负）
     * @param type        记录类型
     * @param relatedId   关联ID
     * @param description 描述
     */
    private void updateBalance(String username, BigDecimal delta,
            UserBalanceRecord.RecordType type,
            Long relatedId, String description,
            boolean allowNegativeBalance) {
        // 悲观锁获取用户余额记录
        UserBalance balance = userBalanceRepository.findByUsernameWithLock(username)
                .orElseGet(() -> {
                    // 首次创建
                    UserBalance newBalance = new UserBalance();
                    newBalance.setUsername(username);
                    newBalance.setBalance(BigDecimal.ZERO);
                    newBalance.setVersion(0L);
                    newBalance.setUpdateTimeMs(System.currentTimeMillis());
                    return userBalanceRepository.save(newBalance);
                });

        BigDecimal before = balance.getBalance();
        BigDecimal after = before.add(delta);

        // 检查余额是否足够（如果是减少）
        if (!allowNegativeBalance && delta.compareTo(BigDecimal.ZERO) < 0 && after.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(String.format("用户 %s 余额不足，当前余额 %s，欲减少 %s",
                    username, before.toPlainString(), delta.abs().toPlainString()));
        }

        // 更新余额
        balance.setBalance(after);
        balance.setVersion(balance.getVersion() + 1);
        balance.setUpdateTimeMs(System.currentTimeMillis());
        userBalanceRepository.save(balance);

        // 创建记录
        UserBalanceRecord record = new UserBalanceRecord();
        record.setUsername(username);
        record.setType(type);
        record.setAmount(delta.abs()); // 记录正数金额
        record.setBalanceBefore(before);
        record.setBalanceAfter(after);
        record.setRelatedId(relatedId);
        record.setDescription(description);
        record.setCreateTimeMs(System.currentTimeMillis());
        recordRepository.save(record);

        log.info("用户 {} 余额变动: {} -> {}，变动金额：{}，类型：{}，关联ID：{}",
                username, before, after, delta, type, relatedId);
    }

    /**
     * 查询用户余额（高精度）
     */
    public BigDecimal getBalance(String username) {
        return userBalanceRepository.findByUsername(username)
                .map(UserBalance::getBalance)
                .orElse(BigDecimal.ZERO);
    }
}
