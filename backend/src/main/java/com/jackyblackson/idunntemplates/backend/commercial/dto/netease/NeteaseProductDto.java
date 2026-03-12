package com.jackyblackson.idunntemplates.backend.commercial.dto.netease;

import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProductStatus;
import com.jackyblackson.idunntemplates.core.domain.Template;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
public class NeteaseProductDto {
    private Long id;
    private NeteaseProductStatus internalStatus = NeteaseProductStatus.CREATED;
    private Long updateTimeMs;
    private Long neUserLogId;
    private String applyReviewTime;
    private Long applyReviewTimeMs;
    private Boolean canManageServer;
    private Boolean canSilentOnline;
    private Boolean canSynchronizePc;
    private Boolean canUpdatePc;
    private Integer collectionId;
    private String createTime;
    private Long createTimeMs;
    private String discount;
    private Integer exemptPerfReviewNum;
    private String interceptFields;
    private Integer isEa;
    private Boolean isOriginal;
    private Boolean isSilentOnline;
    private Boolean isSuitablePc;
    private Boolean isSync;
    private Boolean isTestServer;
    private String itemId;
    private String itemName;
    private String lobbyConfigOpLog;
    private String lobbySortKey;
    private String onlineTime;
    private Long onlineTimeMs;
    private Boolean oriWeakOffline;
    private String oriWeakOfflineReason;
    private Boolean peIsAddPlayPlan;
    private String perfData;
    private Integer performanceServiceAvailable;
    private Integer performanceServiceStatus;
    private Integer playPlanExpireMonth;
    private Integer priType;
    private Integer price;
    private Integer priceRank;
    private String priceType;
    private Integer queuePosition;
    private Integer ratingLevel;
    private Boolean remindable;
    private String res;
    private String status;
    private String syncItemInfo;
    private Boolean syncPcFlag;
    private Integer urgentStatus;
    private Boolean weakOffline;
    private String weakOfflineReason;
    private String orderPayload;
    private String statPayload;
    private Project project;
}
