package ne

import (
	"fmt"

	"github.com/taixue-cn/com-stats/internal/pkg/utils"
)

type CompProduct struct {
	ApplyReviewTime   *string `json:"apply_review_time"`
	ApplyReviewTimeMs *int64  `json:"apply_review_time_ms,omitempty"`

	CanManageServer bool `json:"can_manage_server"`
	CanSilentOnline bool `json:"can_silent_online"`

	CreateTime   *string `json:"create_time"`
	CreateTimeMs *int64  `json:"create_time_ms,omitempty"`

	Discount            any   `json:"discount"`
	ExemptPerfReviewNum int32 `json:"exempt_perf_review_num"`
	InterceptFields     any   `json:"intercept_fields"`
	IsEa                int32 `json:"is_ea"`
	IsOriginal          bool  `json:"is_original"`
	IsSilentOnline      bool  `json:"is_silent_online"`
	IsSync              bool  `json:"is_sync"`
	IsTestServer        bool  `json:"is_test_server"`
	// ID
	ItemId               string  `json:"item_id"`
	ItemIdInt            *int64  `json:"item_id_int,omitempty"`
	ItemName             string  `json:"item_name"`
	LobbyConfigOpLog     any     `json:"lobby_config_op_log"`
	LobbySortKey         any     `json:"lobby_sort_key"`
	OnlineTime           *string `json:"online_time"`
	OnlineTimeMs         *int64  `json:"online_time_ms,omitempty"`
	OriWeakOffline       bool    `json:"ori_weak_offline"`
	OriWeakOfflineReason string  `json:"ori_weak_offline_reason"`
	PlayPlanExpireMonth  int32   `json:"play_plan_expire_month"`
	PriType              int32   `json:"pri_type"`
	Price                int32   `json:"price"`
	PriceRank            int32   `json:"price_rank"`
	PriceType            string  `json:"price_type"`
	QueuePosition        int32   `json:"queue_position"`
	RatingLevel          int32   `json:"rating_level"`
	RelateItemId         string  `json:"relate_item_id"`
	Remindable           bool    `json:"remindable"`
	Status               string  `json:"status"`
	SyncPcFlag           bool    `json:"sync_pc_flag"`
	UrgentStatus         int32   `json:"urgent_status"`
	WeakOffline          bool    `json:"weak_offline"`
	WeakOfflineReason    string  `json:"weak_offline_reason"`
}

func (p *CompProduct) ensureProceed() {
	p.CreateTimeMs = normalizeTime(p.CreateTime)
	p.ApplyReviewTimeMs = normalizeTime(p.ApplyReviewTime)
	p.OnlineTimeMs = normalizeTime(p.OnlineTime)
	p.ItemIdInt = utils.ParseInt64OrNil(p.ItemId)
}

type compProductRespData struct {
	Count int            `json:"count"`
	Item  []*CompProduct `json:"item"`
}

func fetchCompProducts(span int64, headers map[string]string) ([]*CompProduct, string, error) {
	url := fmt.Sprintf("https://mc-launcher.webapp.163.com/items/categories/comp/?is_third_party=false&start=0&span=%d", span)

	// {
	//  "resp": {
	//    "count": 1,
	//    "item": [
	//      {
	//        "apply_review_time": "2026-01-06T04:32:42.423000+00:00",
	//        "can_manage_server": false,
	//        "can_silent_online": false,
	//        "create_time": "2025-12-26T10:28:58.565000+00:00",
	//        "discount": [],
	//        "exempt_perf_review_num": 0,
	//        "intercept_fields": [],
	//        "is_ea": 0,
	//        "is_original": true,
	//        "is_silent_online": false,
	//        "is_sync": false,
	//        "is_test_server": false,
	//        "item_id": "4684715890706151622",
	//        "item_name": "\u52c7\u95ef\u795e\u79d8\u738b\u5e9c\uff0c\u5f00\u542f\u751f\u5b58\u5192\u9669\uff01",
	//        "lobby_config_op_log": [],
	//        "lobby_sort_key": {},
	//        "online_time": "2026-01-13T04:10:36.260000+00:00",
	//        "ori_weak_offline": false,
	//        "ori_weak_offline_reason": "",
	//        "play_plan_expire_month": 0,
	//        "pri_type": 5,
	//        "price": 300,
	//        "price_rank": 0,
	//        "price_type": "diamond",
	//        "queue_position": 0,
	//        "rating_level": 0,
	//        "relate_item_id": "4684715890695005221",
	//        "remindable": false,
	//        "status": "online",
	//        "sync_pc_flag": true,
	//        "urgent_status": 0,
	//        "weak_offline": false,
	//        "weak_offline_reason": ""
	//      }
	//    ]
	//  },
	//  "status": "ok"
	//}

	resp, str, err := utils.RequestGet[*neResp[*compProductRespData]](url, headers)
	if err != nil {
		return nil, "", err
	}
	if resp.Status != statusOk {
		return nil, "", fmt.Errorf("unexpected resp status: %s: %s", resp.Status, resp.Msg)
	}

	for _, item := range resp.Data.Item {
		item.ensureProceed()
	}

	return resp.Data.Item, str, nil
}
