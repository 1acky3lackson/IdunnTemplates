package com.jackyblackson.idunntemplates.backend.commercial.repository.netease;

import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseWithdraw;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NeteaseWithdrawRepository extends JpaRepository<NeteaseWithdraw, Long> {
    // 查询所有还有余额的提现记录，按创建时间升序（最早的优先）
    @Query("SELECT w FROM NeteaseWithdraw w WHERE w.usedOriginalValue < w.originalValue ORDER BY w.saveTimeMs ASC")
    List<NeteaseWithdraw> findAvailableWithdraws();

    // 带悲观锁的版本（用于事务内锁定）
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM NeteaseWithdraw w WHERE w.id IN :ids")
    List<NeteaseWithdraw> findByIdsWithLock(@Param("ids") List<Long> ids);
}
