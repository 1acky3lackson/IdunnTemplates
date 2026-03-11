package com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler;

import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductOrderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NePeProductOrderLogRepository extends JpaRepository<NePeProductOrderLog, Long> {
    /**
     * 查询每个 appOrderId 对应的最新记录（id 最大）
     * 注意：排除 appOrderId 为 null 的记录
     */
    @Query("SELECT l FROM NePeProductOrderLog l " +
            "WHERE l.id IN (SELECT MAX(l2.id) FROM NePeProductOrderLog l2 " +
            "WHERE l2.appOrderId IS NOT NULL GROUP BY l2.appOrderId)")
    List<NePeProductOrderLog> findLatestLogsPerAppOrderId();
}
