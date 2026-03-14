package com.jackyblackson.idunntemplates.backend.commercial.repository.chekout;

import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CheckoutDetailRepository extends
        JpaRepository<CheckoutDetail, Long>,
        JpaSpecificationExecutor<CheckoutDetail> {  // 为动态查询提供支持

    // 功能2：根据用户名和状态查询
    List<CheckoutDetail> findByUsernameAndStatus(String username, CheckoutDetail.Status status);

    // 检查唯一性（用于功能1）
    boolean existsByUsernameAndOrderIdAndRole(String username, Long orderId, CommercialRoleType role);

    List<CheckoutDetail> findByStatusOrderByCreateTimeMsAsc(CheckoutDetail.Status status);

    List<CheckoutDetail> findByStatusAndReleaseTimeMsLessThanEqual(CheckoutDetail.Status status, long releaseTimeMs);

    @Modifying
    @Query("UPDATE CheckoutDetail c SET c.status = :newStatus, c.finishTimeMs = :finishTime WHERE c.id = :id AND c.status = :oldStatus")
    int updateStatusAndFinishTime(@Param("id") Long id,
                                  @Param("oldStatus") CheckoutDetail.Status oldStatus,
                                  @Param("newStatus") CheckoutDetail.Status newStatus,
                                  @Param("finishTime") Long finishTime);

    // 统计指定用户名和状态的 net_profit 总和（用于 CREATED）
    @Query("SELECT COALESCE(SUM(c.netProfit), 0) FROM CheckoutDetail c WHERE c.username = :username AND c.status = :status")
    BigDecimal sumNetProfitByUsernameAndStatus(@Param("username") String username, @Param("status") CheckoutDetail.Status status);

    // 统计指定用户名和状态的 actual_profit 总和（用于 CONFIRMED）
    @Query("SELECT COALESCE(SUM(c.actualProfit), 0) FROM CheckoutDetail c WHERE c.username = :username AND c.status = :status")
    BigDecimal sumActualProfitByUsernameAndStatus(@Param("username") String username, @Param("status") CheckoutDetail.Status status);
}
