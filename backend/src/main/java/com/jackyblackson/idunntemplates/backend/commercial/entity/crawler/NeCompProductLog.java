package com.jackyblackson.idunntemplates.backend.commercial.entity.crawler;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ne_comp_product_logs")
public class NeCompProductLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ne_user_log_id")
    private Long neUserLogId;

    @Column(name = "apply_review_time")
    private String applyReviewTime;

    @Column(name = "apply_review_time_ms")
    private Long applyReviewTimeMs;

    @Column(name = "can_manage_server")
    private Boolean canManageServer;

    @Column(name = "can_silent_online")
    private Boolean canSilentOnline;

    @Column(name = "create_time")
    private String createTime;

    @Column(name = "create_time_ms")
    private Long createTimeMs;

    @Column(name = "discount", columnDefinition = "text")
    private String discount;

    @Column(name = "exempt_perf_review_num")
    private Integer exemptPerfReviewNum;

    @Column(name = "intercept_fields", columnDefinition = "text")
    private String interceptFields;

    @Column(name = "is_ea")
    private Integer isEa;

    @Column(name = "is_original")
    private Boolean isOriginal;

    @Column(name = "is_silent_online")
    private Boolean isSilentOnline;

    @Column(name = "is_sync")
    private Boolean isSync;

    @Column(name = "is_test_server")
    private Boolean isTestServer;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "item_id_int")
    private Long itemIdInt;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "lobby_config_op_log", columnDefinition = "text")
    private String lobbyConfigOpLog;

    @Column(name = "lobby_sort_key", columnDefinition = "text")
    private String lobbySortKey;

    @Column(name = "online_time")
    private String onlineTime;

    @Column(name = "online_time_ms")
    private Long onlineTimeMs;

    @Column(name = "ori_weak_offline")
    private Boolean oriWeakOffline;

    @Column(name = "ori_weak_offline_reason", columnDefinition = "text")
    private String oriWeakOfflineReason;

    @Column(name = "play_plan_expire_month")
    private Integer playPlanExpireMonth;

    @Column(name = "pri_type")
    private Integer priType;

    @Column(name = "price")
    private Integer price;

    @Column(name = "price_rank")
    private Integer priceRank;

    @Column(name = "price_type")
    private String priceType;

    @Column(name = "queue_position")
    private Integer queuePosition;

    @Column(name = "rating_level")
    private Integer ratingLevel;

    @Column(name = "relate_item_id")
    private String relateItemId;

    @Column(name = "remindable")
    private Boolean remindable;

    @Column(name = "status")
    private String status;

    @Column(name = "sync_pc_flag")
    private Boolean syncPcFlag;

    @Column(name = "urgent_status")
    private Integer urgentStatus;

    @Column(name = "weak_offline")
    private Boolean weakOffline;

    @Column(name = "weak_offline_reason", columnDefinition = "text")
    private String weakOfflineReason;

}
