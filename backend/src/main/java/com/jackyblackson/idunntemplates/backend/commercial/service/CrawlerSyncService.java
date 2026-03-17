package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductLog;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProduct;
import com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler.NePeProductLogRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CrawlerSyncService {
    NePeProductLogRepository nePeProductLogRepository;
    NeteaseProductSyncService neteaseProductSyncService;

    public void syncAllWithPaging() {
        log.info("开始分页同步所有最新的商品记录...");

        int pageSize = 100; // 同步任务可以适当调大每页数量
        int currentPage = 0;
        long totalSynced = 0;

        while (true) {
            // 1. 分页查询，按 ID 排序保证分页稳定性
            Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by("id").ascending());
            Slice<NePeProductLog> slice = nePeProductLogRepository.findLatestRecordsGroupedByItemId(pageable);

            if (slice.isEmpty()) {
                break;
            }

            // 2. 调用现有的批量处理逻辑
            // 注意：建议在 syncProducts 内部去掉 @Transactional，
            // 或者确保 syncProduct 的事务传播级别是 REQUIRED（默认值）
            List<NeteaseProduct> results = neteaseProductSyncService.syncProducts(slice.getContent());

            totalSynced += results.size();
            log.info("已同步进度: {} 条", totalSynced);

            // 3. 判断是否有下一页
            if (!slice.hasNext()) {
                break;
            }
            currentPage++;
        }

        log.info("同步任务结束，共处理 {} 条唯一商品记录", totalSynced);
    }
}
