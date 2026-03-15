package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseWithdraw;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseWithdrawRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@AllArgsConstructor
public class NeteaseWithdrawService {

    private final NeteaseWithdrawRepository withdrawRepository;

    /**
     * 分页查询提现记录
     */
    public Page<NeteaseWithdraw> findAll(Specification<NeteaseWithdraw> spec, Pageable pageable) {
        return withdrawRepository.findAll(spec, pageable);
    }

    /**
     * 创建提现记录，并自动计算比率
     */
    @Transactional
    public NeteaseWithdraw create(NeteaseWithdraw withdraw) {
        if (withdraw.getOriginalValue() != null && withdraw.getWithdrawValue() != null
                && withdraw.getOriginalValue().compareTo(BigDecimal.ZERO) > 0) {

            // 计算比率：实际提取 / 原始价值
            BigDecimal ratio = withdraw.getWithdrawValue().divide(
                    withdraw.getOriginalValue(), 8, RoundingMode.HALF_UP);
            withdraw.setRatio(ratio);
        }

        if (withdraw.getSaveTimeMs() == null) {
            withdraw.setSaveTimeMs(System.currentTimeMillis());
        }

        return withdrawRepository.save(withdraw);
    }
}