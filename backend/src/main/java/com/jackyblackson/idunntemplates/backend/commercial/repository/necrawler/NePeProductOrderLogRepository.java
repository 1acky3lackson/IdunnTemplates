package com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler;

import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductOrderLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    /**
     * 通用方案（JPQL）：通过子查询获取每个 appOrderId 的最大 ID，
     * 然后查询这些 ID 对应的完整记录。
     * 兼容所有数据库，且完美支持 Pageable。
     */
    @Query("SELECT log FROM NePeProductOrderLog log " +
            "WHERE log.id IN (" +
            "    SELECT MAX(subLog.id) " +
            "    FROM NePeProductOrderLog subLog " +
            "    WHERE subLog.appOrderId IS NOT NULL " +
            "    GROUP BY subLog.appOrderId" +
            ") " +
            "ORDER BY log.id DESC") // 明确指定排序，保证分页稳定性
    Slice<NePeProductOrderLog> findLatestLogsPerAppOrderId(Pageable pageable);
}
