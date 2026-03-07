package com.jackyblackson.idunntemplates.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jackyblackson.idunntemplates.backend.config.*;
import com.jackyblackson.idunntemplates.backend.dto.commercial.*;
import com.jackyblackson.idunntemplates.backend.store.repository.commercial.*;
import com.jackyblackson.idunntemplates.backend.domain.commercial.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NeteaseCrawlerService {

    private final CrawlerProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final NeLogRepository neLogRepository;
    private final NeUserLogRepository neUserLogRepository;
    private final NePeProductLogRepository nePeProductLogRepository;
    private final NePeProductStatLogRepository nePeProductStatLogRepository;
    private final NePeProductOrderLogRepository nePeProductOrderLogRepository;
    private final NeCompProductLogRepository neCompProductLogRepository;
    private final NePeProductCommentLogRepository nePeProductCommentLogRepository;
    private final NePeProductFeedbackLogRepository nePeProductFeedbackLogRepository;

    public void crawlAndSave() {
        long startTimeMs = System.currentTimeMillis();
        log.info("Starting ne crawler");

        List<UserData> usersData = crawlData();

        long endTimeMs = System.currentTimeMillis();
        saveDataTransactionally(startTimeMs, endTimeMs, usersData);

        log.info("Finished ne crawler");
    }

    private List<UserData> crawlData() {
        List<UserData> usersData = new ArrayList<>();
        CrawlerProperties.Ne neProps = properties.getNe();

        if (neProps.getUsers() == null) {
            log.warn("No users configured for crawling");
            return usersData;
        }

        for (Map.Entry<String, CrawlerProperties.NeUser> entry : neProps.getUsers().entrySet()) {
            String userId = entry.getKey();
            CrawlerProperties.NeUser userConfig = entry.getValue();

            log.info("Crawling ne user_id={} user_name={}", userId, userConfig.getName());

            UserData userData = new UserData();
                userData.setUserId(userId);

            try {
                // PeProducts
                Pair<PeProductRespData, String> peProductsData = fetchPeProducts(neProps.getPeProductSpan(), userConfig.getHeaders());
                userData.setPeProductsPayload(peProductsData.getPayload());
                Thread.sleep(neProps.getIntervalMs());

                for (PeProduct p : peProductsData.getData().getItem()) {
                    PeProductData pData = new PeProductData();
                    pData.setProduct(p);

                    OffsetDateTime endDate = OffsetDateTime.now();
                    OffsetDateTime startDateOrder = endDate.minusDays(neProps.getPeProductOrderDays());

                    try {
                        Pair<PeProductOrderRespData, String> ordersData = fetchPeProductOrders(p.getItemId(), startDateOrder, endDate, userConfig.getHeaders());
                        pData.setOrders(ordersData.getData().getOrders());
                        pData.setOrdersPayload(ordersData.getPayload());
                    } catch (Exception e) {
                        log.error("Fail to crawl pe product orders for item_id={}", p.getItemId(), e);
                    }
                    Thread.sleep(neProps.getIntervalMs());

                    OffsetDateTime startDateStat = endDate.minusDays(neProps.getPeProductStatDays());
                    try {
                        Pair<PeProductStatRespData, String> statsData = fetchPeProductStats(p.getItemId(), startDateStat, endDate, userConfig.getHeaders());
                        pData.setStats(statsData.getData().getData());
                        pData.setStatsPayload(statsData.getPayload());
                    } catch (Exception e) {
                        log.error("Fail to crawl pe product stats for item_id={}", p.getItemId(), e);
                    }
                    Thread.sleep(neProps.getIntervalMs());

                    userData.getPeProducts().add(pData);
                }

                // Comments
                Pair<PeProductCommentRespData, String> commentsData = fetchPeProductComments(neProps.getPeProductCommentSpan(), userConfig.getHeaders());
                userData.setComments(commentsData.getData().getData());
                userData.setCommentsPayload(commentsData.getPayload());
                Thread.sleep(neProps.getIntervalMs());

                // Feedbacks
                Pair<PeProductFeedbackRespData, String> feedbacksData = fetchPeProductFeedbacks(neProps.getPeProductFeedbackSpan(), userConfig.getHeaders());
                userData.setFeedbacks(feedbacksData.getData().getData());
                userData.setFeedbacksPayload(feedbacksData.getPayload());
                Thread.sleep(neProps.getIntervalMs());

                // CompProducts
                Pair<CompProductRespData, String> compProductsData = fetchCompProducts(neProps.getCompProductSpan(), userConfig.getHeaders());
                if (compProductsData.getData() != null) {
                    userData.setCompProducts(compProductsData.getData().getItem());
                }
                userData.setCompProductsPayload(compProductsData.getPayload());

            } catch (Exception e) {
                log.error("Error crawling data for user {}", userId, e);
            }

            usersData.add(userData);
        }

        return usersData;
    }

    @Transactional
    public void saveDataTransactionally(long startTimeMs, long endTimeMs, List<UserData> usersData) {
        NeLog neLog = new NeLog();
        neLog.setCreateTimeMs(System.currentTimeMillis());
        neLog.setStartCrawlTimeMs(startTimeMs);
        neLog.setEndCrawlTimeMs(endTimeMs);
        neLog = neLogRepository.save(neLog);

        for (UserData ud : usersData) {
            NeUserLog neUserLog = new NeUserLog();
            neUserLog.setNeLogId(neLog.getId());
            neUserLog.setUserId(ud.getUserId());
            neUserLog.setPeProductsPayload(ud.getPeProductsPayload());
            neUserLog.setPeProductCommentLogsPayload(ud.getCommentsPayload());
            neUserLog.setPeProductFeedbackLogsPayload(ud.getFeedbacksPayload());
            neUserLog.setCompProductLogsPayload(ud.getCompProductsPayload());
            neUserLog = neUserLogRepository.save(neUserLog);

            for (PeProductData pd : ud.getPeProducts()) {
                NePeProductLog pLog = new NePeProductLog();
                pLog.setNeUserLogId(neUserLog.getId());
                pLog.setApplyReviewTime(pd.getProduct().getApplyReviewTime());
                pLog.setApplyReviewTimeMs(parseOffsetTimeMs(pd.getProduct().getApplyReviewTime()));
                pLog.setCanManageServer(pd.getProduct().getCanManageServer());
                pLog.setCanSilentOnline(pd.getProduct().getCanSilentOnline());
                pLog.setCanSynchronizePc(pd.getProduct().getCanSynchronizePc());
                pLog.setCanUpdatePc(pd.getProduct().getCanUpdatePc());
                pLog.setCollectionId(pd.getProduct().getCollectionId());
                pLog.setCreateTime(pd.getProduct().getCreateTime());
                pLog.setCreateTimeMs(parseOffsetTimeMs(pd.getProduct().getCreateTime()));
                pLog.setDiscount(toJsonOrNull(pd.getProduct().getDiscount()));
                pLog.setExemptPerfReviewNum(pd.getProduct().getExemptPerfReviewNum());
                pLog.setInterceptFields(toJsonOrNull(pd.getProduct().getInterceptFields()));
                pLog.setIsEa(pd.getProduct().getIsEa());
                pLog.setIsOriginal(pd.getProduct().getIsOriginal());
                pLog.setIsSilentOnline(pd.getProduct().getIsSilentOnline());
                pLog.setIsSuitablePc(pd.getProduct().getIsSuitablePc());
                pLog.setIsSync(pd.getProduct().getIsSync());
                pLog.setIsTestServer(pd.getProduct().getIsTestServer());
                pLog.setItemId(pd.getProduct().getItemId());
                pLog.setItemName(pd.getProduct().getItemName());
                pLog.setLobbyConfigOpLog(toJsonOrNull(pd.getProduct().getLobbyConfigOpLog()));
                pLog.setLobbySortKey(toJsonOrNull(pd.getProduct().getLobbySortKey()));
                pLog.setOnlineTime(pd.getProduct().getOnlineTime());
                pLog.setOnlineTimeMs(parseOffsetTimeMs(pd.getProduct().getOnlineTime()));
                pLog.setOriWeakOffline(pd.getProduct().getOriWeakOffline());
                pLog.setOriWeakOfflineReason(pd.getProduct().getOriWeakOfflineReason());
                pLog.setPeIsAddPlayPlan(pd.getProduct().getPeIsAddPlayPlan());
                pLog.setPerfData(toJsonOrNull(pd.getProduct().getPerfData()));
                pLog.setPerformanceServiceAvailable(pd.getProduct().getPerformanceServiceAvailable());
                pLog.setPerformanceServiceStatus(pd.getProduct().getPerformanceServiceStatus());
                pLog.setPlayPlanExpireMonth(pd.getProduct().getPlayPlanExpireMonth());
                pLog.setPriType(pd.getProduct().getPriType());
                pLog.setPrice(pd.getProduct().getPrice());
                pLog.setPriceRank(pd.getProduct().getPriceRank());
                pLog.setPriceType(pd.getProduct().getPriceType());
                pLog.setQueuePosition(pd.getProduct().getQueuePosition());
                pLog.setRatingLevel(pd.getProduct().getRatingLevel());
                pLog.setRemindable(pd.getProduct().getRemindable());
                pLog.setRes(toJsonOrNull(pd.getProduct().getRes()));
                pLog.setStatus(pd.getProduct().getStatus());
                pLog.setSyncItemInfo(toJsonOrNull(pd.getProduct().getSyncItemInfo()));
                pLog.setSyncPcFlag(pd.getProduct().getSyncPcFlag());
                pLog.setUrgentStatus(pd.getProduct().getUrgentStatus());
                pLog.setWeakOffline(pd.getProduct().getWeakOffline());
                pLog.setWeakOfflineReason(pd.getProduct().getWeakOfflineReason());
                pLog.setOrderPayload(pd.getOrdersPayload());
                pLog.setStatPayload(pd.getStatsPayload());
                pLog = nePeProductLogRepository.save(pLog);

                if (pd.getStats() != null) {
                    for (PeProductStat s : pd.getStats()) {
                        NePeProductStatLog stat = new NePeProductStatLog();
                        stat.setNePeProductLogId(pLog.getId());
                        stat.setDau(s.getDau());
                        stat.setAvgFirstTypeBuy(s.getAvgFirstTypeBuy());
                        stat.setAvgFirstTypeDiamond(s.getAvgFirstTypeDiamond());
                        stat.setAvgFirstTypeFocus(s.getAvgFirstTypeFocus());
                        stat.setAvgFirstTypeRolePlay(s.getAvgFirstTypeRolePlay());
                        stat.setAvgPlaytime(s.getAvgPlaytime());
                        stat.setAvgTotalFirstTypeBuy(s.getAvgTotalFirstTypeBuy());
                        stat.setCntBuy(s.getCntBuy());
                        stat.setDateId(s.getDateid());
                        stat.setDateMs(parseDateId(s.getDateid()));
                        stat.setDiamond(s.getDiamond());
                        stat.setDownloadNum(s.getDownloadNum());
                        stat.setFirstTypeAvgRoleTime(s.getFirstTypeAvgRoleTime());
                        stat.setFocusCnt(s.getFocusCnt());
                        stat.setIid(s.getIid());
                        stat.setIidInt(parseLongOrNull(s.getIid()));
                        stat.setPassAvgRoleTimeRatio(s.getPassAvgRoleTimeRatio());
                        stat.setPassBuyCntRatio(s.getPassBuyCntRatio());
                        stat.setPassCntRolePlayRatio(s.getPassCntRolePlayRatio());
                        stat.setPassFocusCntRatio(s.getPassFocusCntRatio());
                        stat.setPassPayDiamondRatio(s.getPassPayDiamondRatio());
                        stat.setPlatform(s.getPlatform());
                        stat.setPoints(s.getPoints());
                        stat.setRefundRate(s.getRefundRate());
                        stat.setResName(s.getResName());
                        stat.setStarAdjusted(s.getStarAdjusted());
                        stat.setUploadTime(s.getUploadTime());
                        stat.setUploadTimeMs(parseUploadTimeMs(s.getUploadTime()));
                        nePeProductStatLogRepository.save(stat);
                    }
                }

                if (pd.getOrders() != null) {
                    for (PeProductOrder o : pd.getOrders()) {
                        NePeProductOrderLog order = new NePeProductOrderLog();
                        order.setNePeProductLogId(pLog.getId());
                        order.setAppOrderId(o.getAppOrderId());
                        order.setAppOrderIdInt(parseLongOrNull(o.getAppOrderId()));
                        order.setAppUid(o.getAppUid());
                        order.setAppUidInt(parseLongOrNull(o.getAppUid()));
                        order.setDiscount(o.getDiscount());
                        order.setOfficialChannel(o.getOfficialChannel());
                        order.setPoint(o.getPoint());
                        order.setPointType(o.getPointType());
                        order.setPrice(o.getPrice());
                        order.setPriceType(o.getPriceType());
                        order.setProductName(o.getProductName());
                        order.setPurchaseLimit(o.getPurchaseLimit());
                        order.setRefundStatus(o.getRefundStatus());
                        order.setShipTime(o.getShipTime());
                        order.setShipTimeMs(parseOffsetTimeMs(o.getShipTime()));
                        nePeProductOrderLogRepository.save(order);
                    }
                }
            }

            if (ud.getCompProducts() != null) {
                for (CompProduct p : ud.getCompProducts()) {
                    NeCompProductLog cLog = new NeCompProductLog();
                    cLog.setNeUserLogId(neUserLog.getId());
                    cLog.setApplyReviewTime(p.getApplyReviewTime());
                    cLog.setApplyReviewTimeMs(parseOffsetTimeMs(p.getApplyReviewTime()));
                    cLog.setCanManageServer(p.getCanManageServer());
                    cLog.setCanSilentOnline(p.getCanSilentOnline());
                    cLog.setCreateTime(p.getCreateTime());
                    cLog.setCreateTimeMs(parseOffsetTimeMs(p.getCreateTime()));
                    cLog.setDiscount(toJsonOrNull(p.getDiscount()));
                    cLog.setExemptPerfReviewNum(p.getExemptPerfReviewNum());
                    cLog.setInterceptFields(toJsonOrNull(p.getInterceptFields()));
                    cLog.setIsEa(p.getIsEa());
                    cLog.setIsOriginal(p.getIsOriginal());
                    cLog.setIsSilentOnline(p.getIsSilentOnline());
                    cLog.setIsSync(p.getIsSync());
                    cLog.setIsTestServer(p.getIsTestServer());
                    cLog.setItemId(p.getItemId());
                    cLog.setItemIdInt(parseLongOrNull(p.getItemId()));
                    cLog.setItemName(p.getItemName());
                    cLog.setLobbyConfigOpLog(toJsonOrNull(p.getLobbyConfigOpLog()));
                    cLog.setLobbySortKey(toJsonOrNull(p.getLobbySortKey()));
                    cLog.setOnlineTime(p.getOnlineTime());
                    cLog.setOnlineTimeMs(parseOffsetTimeMs(p.getOnlineTime()));
                    cLog.setOriWeakOffline(p.getOriWeakOffline());
                    cLog.setOriWeakOfflineReason(p.getOriWeakOfflineReason());
                    cLog.setPlayPlanExpireMonth(p.getPlayPlanExpireMonth());
                    cLog.setPriType(p.getPriType());
                    cLog.setPrice(p.getPrice());
                    cLog.setPriceRank(p.getPriceRank());
                    cLog.setPriceType(p.getPriceType());
                    cLog.setQueuePosition(p.getQueuePosition());
                    cLog.setRatingLevel(p.getRatingLevel());
                    cLog.setRelateItemId(p.getRelateItemId());
                    cLog.setRemindable(p.getRemindable());
                    cLog.setStatus(p.getStatus());
                    cLog.setSyncPcFlag(p.getSyncPcFlag());
                    cLog.setUrgentStatus(p.getUrgentStatus());
                    cLog.setWeakOffline(p.getWeakOffline());
                    cLog.setWeakOfflineReason(p.getWeakOfflineReason());
                    neCompProductLogRepository.save(cLog);
                }
            }
        }
    }

    // --- Helpers for fetching data ---

    private <T> Pair<T, String> doFetch(String url, Map<String, String> headersMap, ParameterizedTypeReference<NeResp<T>> typeRef) {
        HttpHeaders headers = new HttpHeaders();
        if (headersMap != null) {
            headersMap.forEach(headers::add);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> rawResp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String payload = rawResp.getBody();
        try {
            NeResp<T> resp = objectMapper.readValue(payload, objectMapper.getTypeFactory().constructType(typeRef.getType()));
            if (!"ok".equals(resp.getStatus())) {
                throw new RuntimeException("Unexpected resp status: " + resp.getStatus() + ": " + resp.getMsg());
            }
            return new Pair<>(resp.getData(), payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<PeProductRespData, String> fetchPeProducts(long span, Map<String, String> headers) {
        String url = String.format("https://mc-launcher.webapp.163.com/items/categories/pe/?is_third_party=false&start=0&span=%d", span);
        return doFetch(url, headers, new ParameterizedTypeReference<NeResp<PeProductRespData>>() {});
    }

    private Pair<PeProductOrderRespData, String> fetchPeProductOrders(String itemId, OffsetDateTime start, OffsetDateTime end, Map<String, String> headers) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String url = String.format("https://mc-launcher.webapp.163.com/items/categories/pe/%s/incomes/?begin_time=%s&end_time=%s",
                itemId, start.format(dtf), end.format(dtf));
        return doFetch(url, headers, new ParameterizedTypeReference<NeResp<PeProductOrderRespData>>() {});
    }

    private Pair<PeProductStatRespData, String> fetchPeProductStats(String itemId, OffsetDateTime start, OffsetDateTime end, Map<String, String> headers) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        String url = String.format("https://mc-launcher.webapp.163.com/data_analysis/day_detail/?platform=pe&category=pe&start_date=%s&end_date=%s&item_list_str=%s&sort=dateid&order=ASC&start=0&span=999999999&is_need_us_rank_data=true",
                start.format(dtf), end.format(dtf), itemId);
        return doFetch(url, headers, new ParameterizedTypeReference<NeResp<PeProductStatRespData>>() {});
    }

    private Pair<PeProductCommentRespData, String> fetchPeProductComments(long span, Map<String, String> headers) {
        String url = String.format("https://mc-launcher.webapp.163.com/items/comment/pe/?start=0&span=%d", span);
        return doFetch(url, headers, new ParameterizedTypeReference<NeResp<PeProductCommentRespData>>() {});
    }

    private Pair<PeProductFeedbackRespData, String> fetchPeProductFeedbacks(long span, Map<String, String> headers) {
        String url = String.format("https://mc-launcher.webapp.163.com/items/feedback/pe/?start=0&span=%d", span);
        return doFetch(url, headers, new ParameterizedTypeReference<NeResp<PeProductFeedbackRespData>>() {});
    }

    private Pair<CompProductRespData, String> fetchCompProducts(long span, Map<String, String> headers) {
        String url = String.format("https://mc-launcher.webapp.163.com/items/categories/comp/?is_third_party=false&start=0&span=%d", span);
        return doFetch(url, headers, new ParameterizedTypeReference<NeResp<CompProductRespData>>() {});
    }


    // --- Util Methods ---

    private String toJsonOrNull(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Long parseOffsetTimeMs(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return OffsetDateTime.parse(s).toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseUploadTimeMs(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
            return Instant.from(formatter.parse(s)).toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseDateId(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(s, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLongOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static class UserData {
        private String userId;
        private List<PeProductData> peProducts = new ArrayList<>();
        private String peProductsPayload;

        private List<PeProductComment> comments;
        private String commentsPayload;

        private List<PeProductFeedback> feedbacks;
        private String feedbacksPayload;

        private List<CompProduct> compProducts;
        private String compProductsPayload;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public List<PeProductData> getPeProducts() { return peProducts; }
        public void setPeProducts(List<PeProductData> peProducts) { this.peProducts = peProducts; }

        public String getPeProductsPayload() { return peProductsPayload; }
        public void setPeProductsPayload(String peProductsPayload) { this.peProductsPayload = peProductsPayload; }

        public List<PeProductComment> getComments() { return comments; }
        public void setComments(List<PeProductComment> comments) { this.comments = comments; }

        public String getCommentsPayload() { return commentsPayload; }
        public void setCommentsPayload(String commentsPayload) { this.commentsPayload = commentsPayload; }

        public List<PeProductFeedback> getFeedbacks() { return feedbacks; }
        public void setFeedbacks(List<PeProductFeedback> feedbacks) { this.feedbacks = feedbacks; }

        public String getFeedbacksPayload() { return feedbacksPayload; }
        public void setFeedbacksPayload(String feedbacksPayload) { this.feedbacksPayload = feedbacksPayload; }

        public List<CompProduct> getCompProducts() { return compProducts; }
        public void setCompProducts(List<CompProduct> compProducts) { this.compProducts = compProducts; }

        public String getCompProductsPayload() { return compProductsPayload; }
        public void setCompProductsPayload(String compProductsPayload) { this.compProductsPayload = compProductsPayload; }
    }

    public static class PeProductData {
        private PeProduct product;
        private List<PeProductOrder> orders;
        private List<PeProductStat> stats;
        private String ordersPayload;
        private String statsPayload;

        public PeProduct getProduct() { return product; }
        public void setProduct(PeProduct product) { this.product = product; }

        public List<PeProductOrder> getOrders() { return orders; }
        public void setOrders(List<PeProductOrder> orders) { this.orders = orders; }

        public List<PeProductStat> getStats() { return stats; }
        public void setStats(List<PeProductStat> stats) { this.stats = stats; }

        public String getOrdersPayload() { return ordersPayload; }
        public void setOrdersPayload(String ordersPayload) { this.ordersPayload = ordersPayload; }

        public String getStatsPayload() { return statsPayload; }
        public void setStatsPayload(String statsPayload) { this.statsPayload = statsPayload; }
    }

    private static class Pair<A, B> {
        private final A data;
        private final B payload;

        public Pair(A data, B payload) {
            this.data = data;
            this.payload = payload;
        }

        public A getData() {
            return data;
        }

        public B getPayload() {
            return payload;
        }
    }
}
