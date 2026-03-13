package com.jackyblackson.idunntemplates.backend.commercial.entity.netease;

import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.core.domain.Template;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "commercial_netease_product",
        indexes = @Index(name = "idx_item_id", columnList = "item_id")
)
public class NeteaseProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "internal_status")
    private NeteaseProductStatus internalStatus = NeteaseProductStatus.CREATED;

    // 添加 update_time 字段
    @Column(name = "update_time_ms")
    private Long updateTimeMs;

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

    @Column(name = "can_synchronize_pc")
    private Boolean canSynchronizePc;

    @Column(name = "can_update_pc")
    private Boolean canUpdatePc;

    @Column(name = "collection_id")
    private Integer collectionId;

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

    @Column(name = "is_suitable_pc")
    private Boolean isSuitablePc;

    @Column(name = "is_sync")
    private Boolean isSync;

    @Column(name = "is_test_server")
    private Boolean isTestServer;

    @Column(name = "item_id")
    private String itemId;

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

    @Column(name = "pe_is_add_play_plan")
    private Boolean peIsAddPlayPlan;

    @Column(name = "perf_data", columnDefinition = "text")
    private String perfData;

    @Column(name = "performance_service_available")
    private Integer performanceServiceAvailable;

    @Column(name = "performance_service_status")
    private Integer performanceServiceStatus;

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

    @Column(name = "remindable")
    private Boolean remindable;

    @Column(name = "res", columnDefinition = "text")
    private String res;

    @Column(name = "status")
    private String status;

    @Column(name = "sync_item_info", columnDefinition = "text")
    private String syncItemInfo;

    @Column(name = "sync_pc_flag")
    private Boolean syncPcFlag;

    @Column(name = "urgent_status")
    private Integer urgentStatus;

    @Column(name = "weak_offline")
    private Boolean weakOffline;

    @Column(name = "weak_offline_reason", columnDefinition = "text")
    private String weakOfflineReason;

    @Column(name = "order_payload", columnDefinition = "text")
    private String orderPayload;

    @Column(name = "stat_payload", columnDefinition = "text")
    private String statPayload;

    @ManyToOne(fetch = FetchType.EAGER)  // 默认关联查询为 LAZY 提升性能
    @JoinColumn(name = "project_id")    // 指定外键列名
    private Project project;
}
