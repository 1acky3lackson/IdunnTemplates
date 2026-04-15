package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.dto.balance.BalanceInfo;
import com.jackyblackson.idunntemplates.backend.commercial.entity.balance.UserBalance;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail;
import com.jackyblackson.idunntemplates.backend.commercial.repository.balance.UserBalanceRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.CheckoutDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

        private final UserBalanceRepository userBalanceRepository;
        private final CheckoutDetailRepository checkoutDetailRepository;

        /**
         * 获取用户虚拟点数的综合余额信息
         * 
         * @param username 用户名
         * @return BalanceInfo 包含可用余额、冻结中余额、待结算余额
         */
        public BalanceInfo getInfo(String username) {
                // 1. 查询可用余额
                BigDecimal availableBalance = userBalanceRepository.findByUsername(username)
                                .map(UserBalance::getBalance)
                                .orElse(BigDecimal.ZERO);

                // 2. 查询冻结中余额（CONFIRMED 状态的实际利润总和）
                BigDecimal frozenBalance = checkoutDetailRepository.sumActualProfitByUsernameAndStatus(
                                username, CheckoutDetail.Status.CONFIRMED);

                // 3. 查询待结算余额（CREATED 状态的原始利润总和）
                BigDecimal pendingBalance = checkoutDetailRepository.sumNetProfitByUsernameAndStatus(
                                username, CheckoutDetail.Status.CREATED);

                // 处理可能的 null（COALESCE 已保证不为 null，但保险起见）
                if (frozenBalance == null)
                        frozenBalance = BigDecimal.ZERO;
                if (pendingBalance == null)
                        pendingBalance = BigDecimal.ZERO;

                return new BalanceInfo(username, availableBalance, frozenBalance, pendingBalance);
        }
}