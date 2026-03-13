package com.jackyblackson.idunntemplates.backend.commercial.component.product;

import com.jackyblackson.idunntemplates.backend.commercial.entity.crawler.NePeProductLog;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProduct;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProductStatus;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * 转换器组件：将爬虫日志实体(NePeProductLog)转换为网易产品实体(NeteaseProduct)
 */
@Component
public class NePeProductLogConverter {

    /**
     * 将 NePeProductLog 转换为 NeteaseProduct
     * @param log 爬虫日志实体
     * @return 网易产品实体
     */
    public NeteaseProduct convertToNeteaseProduct(NePeProductLog log) {
        if (log == null) {
            return null;
        }

        NeteaseProduct product = new NeteaseProduct();

        // ID 映射 - 注意：NeteaseProduct 使用 Long 类型的 id，而 NePeProductLog 的 id 是自增主键
        // 这里可能需要根据业务逻辑决定如何设置 ID，暂时先不设置或使用 log 的 ID
        // product.setId(log.getId());

        // 设置更新时间
        syncFromLog(product, log);

        return product;
    }

    /**
     * 将 NePeProductLog 转换为 NeteaseProduct，并指定 ID
     * @param log 爬虫日志实体
     * @param id 要设置的产品ID
     * @return 网易产品实体
     */
    public NeteaseProduct convertToNeteaseProductWithId(NePeProductLog log, Long id) {
        NeteaseProduct product = convertToNeteaseProduct(log);
        if (product != null) {
            product.setId(id);
        }
        return product;
    }

    /**
     * 将 NePeProductLog 转换为 NeteaseProduct，并尝试从 itemId 解析 itemIdInt
     * @param log 爬虫日志实体
     * @return 网易产品实体
     */
    public NeteaseProduct convertToNeteaseProductWithItemIdInt(NePeProductLog log) {
        return convertToNeteaseProduct(log);
    }

    /**
     * 更新已存在的 NeteaseProduct 实体的字段值
     *
     * @param product 已存在的网易产品实体
     * @param log     爬虫日志实体
     */
    public void updateNeteaseProductFromLog(NeteaseProduct product, NePeProductLog log) {
        if (product == null || log == null) {
            return;
        }

        syncFromLog(product, log);
        // relateItemId 保持不变，因为日志中没有
        product.setRemindable(log.getRemindable());
        product.setStatus(log.getStatus());
        product.setSyncPcFlag(log.getSyncPcFlag());
        product.setUrgentStatus(log.getUrgentStatus());
        product.setWeakOffline(log.getWeakOffline());
        product.setWeakOfflineReason(log.getWeakOfflineReason());

        // templates 关联保持不变，因为日志中没有
    }

    private void syncFromLog(NeteaseProduct product, NePeProductLog log) {
        product.setNeUserLogId(log.getNeUserLogId());
        product.setApplyReviewTime(log.getApplyReviewTime());
        product.setApplyReviewTimeMs(log.getApplyReviewTimeMs());
        product.setCanManageServer(log.getCanManageServer());
        product.setCanSilentOnline(log.getCanSilentOnline());
        product.setCanSynchronizePc(log.getCanSynchronizePc());
        product.setCanUpdatePc(log.getCanUpdatePc());
        product.setCollectionId(log.getCollectionId());
        product.setCreateTime(log.getCreateTime());
        product.setCreateTimeMs(log.getCreateTimeMs());
        product.setDiscount(log.getDiscount());
        product.setExemptPerfReviewNum(log.getExemptPerfReviewNum());
        product.setInterceptFields(log.getInterceptFields());
        product.setIsEa(log.getIsEa());
        product.setIsOriginal(log.getIsOriginal());
        product.setIsSilentOnline(log.getIsSilentOnline());
        product.setIsSuitablePc(log.getIsSuitablePc());
        product.setIsSync(log.getIsSync());
        product.setIsTestServer(log.getIsTestServer());
        product.setItemId(log.getItemId());
        product.setItemName(log.getItemName());
        product.setLobbyConfigOpLog(log.getLobbyConfigOpLog());
        product.setLobbySortKey(log.getLobbySortKey());
        product.setOnlineTime(log.getOnlineTime());
        product.setOnlineTimeMs(log.getOnlineTimeMs());
        product.setOriWeakOffline(log.getOriWeakOffline());
        product.setOriWeakOfflineReason(log.getOriWeakOfflineReason());
        product.setPeIsAddPlayPlan(log.getPeIsAddPlayPlan());
        product.setPerfData(log.getPerfData());
        product.setPerformanceServiceAvailable(log.getPerformanceServiceAvailable());
        product.setPerformanceServiceStatus(log.getPerformanceServiceStatus());
        product.setPlayPlanExpireMonth(log.getPlayPlanExpireMonth());
        product.setPriType(log.getPriType());
        product.setPrice(log.getPrice());
        product.setPriceRank(log.getPriceRank());
        product.setPriceType(log.getPriceType());
        product.setQueuePosition(log.getQueuePosition());
        product.setRatingLevel(log.getRatingLevel());
        product.setRemindable(log.getRemindable());
        product.setRes(log.getRes());
        product.setStatus(log.getStatus());
        product.setSyncItemInfo(log.getSyncItemInfo());
        product.setSyncPcFlag(log.getSyncPcFlag());
        product.setUrgentStatus(log.getUrgentStatus());
        product.setWeakOffline(log.getWeakOffline());
        product.setWeakOfflineReason(log.getWeakOfflineReason());
        product.setOrderPayload(log.getOrderPayload());
        product.setStatPayload(log.getStatPayload());

        updateProduct(product, log);
    }

    private NeteaseProduct updateProduct(NeteaseProduct product, NePeProductLog log) {
        if (log.getStatus().equals("online") && !product.getInternalStatus().equals(NeteaseProductStatus.ONLINE)) {
            product.setInternalStatus(NeteaseProductStatus.ONLINE);
        } else if (log.getStatus().equals("reject") && !product.getInternalStatus().equals(NeteaseProductStatus.REJECTED)) {
            product.setInternalStatus(NeteaseProductStatus.REJECTED);
        }

        return product;
    }
}
