package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.component.product.NePeProductLogConverter;
import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductLog;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProduct;
import com.jackyblackson.idunntemplates.backend.commercial.repository.necrawler.NePeProductLogRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NeteaseProductSyncService {

    private final NeteaseProductRepository neteaseProductRepository;
    private final NePeProductLogRepository nePeProductLogRepository;
    private final NePeProductLogConverter converter;

    /**
     * 同步所有最新的爬虫记录到业务表
     * 根据 item_id 分组，取每个分组中 id 最大的记录（即最新版本）
     */
    @Transactional
    public List<NeteaseProduct> syncAll() {
        log.info("开始同步所有最新的商品记录...");

        // 直接从数据库获取去重后的最新记录，极大减少内存消耗和网络 I/O
        List<NePeProductLog> latestLogs = nePeProductLogRepository.findLatestRecordsGroupedByItemId();

        if (latestLogs.isEmpty()) {
            log.info("没有找到爬虫记录");
            return Collections.emptyList();
        }

        log.info("找到 {} 个唯一的商品记录（按 item_id 去重后）", latestLogs.size());

        // 批量同步
        return syncProducts(latestLogs);
    }

    /**
     * 同步单个爬虫记录到业务表
     */
    @Transactional
    public NeteaseProduct syncProduct(NePeProductLog crawlLog) {
        if (crawlLog == null || crawlLog.getItemId() == null) {
            log.warn("爬虫记录或itemId为空，跳过同步");
            return null;
        }

        String itemId = crawlLog.getItemId();
        log.debug("开始同步商品: {}", itemId);

        // 查找是否已存在
        return neteaseProductRepository.findByItemId(itemId)
                .map(existingProduct -> {
                    log.debug("更新现有商品: {}", itemId);
                    converter.updateNeteaseProductFromLog(existingProduct, crawlLog);
                    return neteaseProductRepository.save(existingProduct);
                })
                .orElseGet(() -> {
                    log.debug("创建新商品: {}", itemId);
                    NeteaseProduct newProduct = converter.convertToNeteaseProductWithItemIdInt(crawlLog);
                    return neteaseProductRepository.save(newProduct);
                });
    }

    /**
     * 批量同步爬虫记录
     */
    @Transactional
    public List<NeteaseProduct> syncProducts(List<NePeProductLog> crawlLogs) {
        if (crawlLogs == null || crawlLogs.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("开始批量同步 {} 条记录", crawlLogs.size());

        return crawlLogs.stream()
                .map(this::syncProduct)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 同步指定 itemId 的最新记录
     */
    @Transactional
    public Optional<NeteaseProduct> syncLatestByItemId(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            log.warn("itemId为空，跳过同步");
            return Optional.empty();
        }

        // 获取该 itemId 的最新记录（id最大的）
        Optional<NePeProductLog> latestLog = nePeProductLogRepository.findTopByItemIdOrderByIdDesc(itemId);

        if (latestLog.isEmpty()) {
            log.warn("未找到 itemId: {} 的爬虫记录", itemId);
            return Optional.empty();
        }

        return Optional.ofNullable(syncProduct(latestLog.get()));
    }

    /**
     * 根据itemId获取商品
     */
    @Transactional(readOnly = true)
    public Optional<NeteaseProduct> getProductByItemId(String itemId) {
        return neteaseProductRepository.findByItemId(itemId);
    }

    /**
     * 检查商品是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByItemId(String itemId) {
        return neteaseProductRepository.existsByItemId(itemId);
    }

    /**
     * 获取所有需要同步的 itemId 列表（从爬虫表获取去重后的 itemId）
     */
    @Transactional(readOnly = true)
    public List<String> getAllItemIds() {
        return nePeProductLogRepository.findAllDistinctItemIds();
    }

    /**
     * 统计爬虫记录总数
     */
    @Transactional(readOnly = true)
    public long countCrawlLogs() {
        return nePeProductLogRepository.count();
    }

    /**
     * 统计已同步的商品总数
     */
    @Transactional(readOnly = true)
    public long countSyncedProducts() {
        return neteaseProductRepository.count();
    }
}
