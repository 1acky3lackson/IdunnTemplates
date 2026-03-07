package cn.taixue.comstats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PeProduct {
    @JsonProperty("apply_review_time")
    private String applyReviewTime;

    @JsonProperty("can_manage_server")
    private Boolean canManageServer;

    @JsonProperty("can_silent_online")
    private Boolean canSilentOnline;

    @JsonProperty("can_synchronize_pc")
    private Boolean canSynchronizePc;

    @JsonProperty("can_update_pc")
    private Boolean canUpdatePc;

    @JsonProperty("collection_id")
    private Integer collectionId;

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

    @JsonProperty("is_suitable_pc")
    private Boolean isSuitablePc;

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

    @JsonProperty("pe_is_add_play_plan")
    private Boolean peIsAddPlayPlan;

    @JsonProperty("perf_data")
    private Object perfData;

    @JsonProperty("performance_service_available")
    private Integer performanceServiceAvailable;

    @JsonProperty("performance_service_status")
    private Integer performanceServiceStatus;

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

    private Boolean remindable;

    private Object res;

    private String status;

    @JsonProperty("sync_item_info")
    private Object syncItemInfo;

    @JsonProperty("sync_pc_flag")
    private Boolean syncPcFlag;

    @JsonProperty("urgent_status")
    private Integer urgentStatus;

    @JsonProperty("weak_offline")
    private Boolean weakOffline;

    @JsonProperty("weak_offline_reason")
    private String weakOfflineReason;
}
