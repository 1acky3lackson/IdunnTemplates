package com.jackyblackson.idunntemplates.backend.commercial.dto.necrawler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CompProduct {
    @JsonProperty("apply_review_time")
    private String applyReviewTime;

    @JsonProperty("can_manage_server")
    private Boolean canManageServer;

    @JsonProperty("can_silent_online")
    private Boolean canSilentOnline;

    @JsonProperty("create_time")
    private String createTime;

    private Object discount;

    @JsonProperty("exempt_perf_review_num")
    private Integer exemptPerfReviewNum;

    @JsonProperty("intercept_fields")
    private Object interceptFields;

    @JsonProperty("is_ea")
    private Integer isEa;

    @JsonProperty("is_original")
    private Boolean isOriginal;

    @JsonProperty("is_silent_online")
    private Boolean isSilentOnline;

    @JsonProperty("is_sync")
    private Boolean isSync;

    @JsonProperty("is_test_server")
    private Boolean isTestServer;

    @JsonProperty("item_id")
    private String itemId;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("lobby_config_op_log")
    private Object lobbyConfigOpLog;

    @JsonProperty("lobby_sort_key")
    private Object lobbySortKey;

    @JsonProperty("online_time")
    private String onlineTime;

    @JsonProperty("ori_weak_offline")
    private Boolean oriWeakOffline;

    @JsonProperty("ori_weak_offline_reason")
    private String oriWeakOfflineReason;

    @JsonProperty("play_plan_expire_month")
    private Integer playPlanExpireMonth;

    @JsonProperty("pri_type")
    private Integer priType;

    private Integer price;

    @JsonProperty("price_rank")
    private Integer priceRank;

    @JsonProperty("price_type")
    private String priceType;

    @JsonProperty("queue_position")
    private Integer queuePosition;

    @JsonProperty("rating_level")
    private Integer ratingLevel;

    @JsonProperty("relate_item_id")
    private String relateItemId;

    private Boolean remindable;

    private String status;

    @JsonProperty("sync_pc_flag")
    private Boolean syncPcFlag;

    @JsonProperty("urgent_status")
    private Integer urgentStatus;

    @JsonProperty("weak_offline")
    private Boolean weakOffline;

    @JsonProperty("weak_offline_reason")
    private String weakOfflineReason;
}
