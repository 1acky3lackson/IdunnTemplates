package com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler;

import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NePeProductLogRepository extends JpaRepository<NePeProductLog, Long> {
    // 根据 itemId 查询，按 id 降序排序，取第一条（即最新的一条）
    Optional<NePeProductLog> findTopByItemIdOrderByIdDesc(String itemId);

    // 查询所有不重复的 itemId
    @Query("SELECT DISTINCT l.itemId FROM NePeProductLog l WHERE l.itemId IS NOT NULL")
    List<String> findAllDistinctItemIds();

    // 根据 itemId 查询所有记录，按 id 降序排序
    List<NePeProductLog> findByItemIdOrderByIdDesc(String itemId);

    /**
     * 过滤出 itemId 不为空的记录，按 itemId 分组取 id 最大的记录，最后按 itemId 排序
     */
    @Query(value =
            "SELECT p.* FROM ne_pe_product_logs p " +
                    "INNER JOIN (" +
                    "    SELECT item_id, MAX(id) AS max_id " +
                    "    FROM ne_pe_product_logs " +
                    "    WHERE item_id IS NOT NULL " + // 养成好习惯，过滤无效数据
                    "    GROUP BY item_id" +
                    ") max_p ON p.id = max_p.max_id",
            nativeQuery = true)
    List<NePeProductLog> findLatestRecordsGroupedByItemId();
}
