package ne

import (
	"fmt"
	"strconv"
	"time"

	"github.com/bytedance/gg/gptr"
	"github.com/taixue-cn/com-stats/internal/pkg/utils"
)

type PeProduct struct {
	ApplyReviewTime             *string `json:"apply_review_time"`
	ApplyReviewTimeMs           *int64  `json:"apply_review_time_ms"`
	CanManageServer             bool    `json:"can_manage_server"`
	CanSilentOnline             bool    `json:"can_silent_online"`
	CanSynchronizePc            bool    `json:"can_synchronize_pc"`
	CanUpdatePc                 bool    `json:"can_update_pc"`
	CollectionId                int32   `json:"collection_id"`
	CreateTime                  *string `json:"create_time"`
	CreateTimeMs                *int64  `json:"create_time_ms"`
	Discount                    any     `json:"discount"`
	ExemptPerfReviewNum         int32   `json:"exempt_perf_review_num"`
	InterceptFields             any     `json:"intercept_fields"`
	IsEa                        int32   `json:"is_ea"`
	IsOriginal                  bool    `json:"is_original"`
	IsSilentOnline              bool    `json:"is_silent_online"`
	IsSuitablePc                bool    `json:"is_suitable_pc"`
	IsSync                      bool    `json:"is_sync"`
	IsTestServer                bool    `json:"is_test_server"`
	ItemId                      string  `json:"item_id"`
	ItemName                    string  `json:"item_name"`
	LobbyConfigOpLog            any     `json:"lobby_config_op_log"`
	LobbySortKey                any     `json:"lobby_sort_key"`
	OnlineTime                  *string `json:"online_time"`
	OnlineTimeMs                *int64  `json:"online_time_ms"`
	OriWeakOffline              bool    `json:"ori_weak_offline"`
	OriWeakOfflineReason        string  `json:"ori_weak_offline_reason"`
	PeIsAddPlayPlan             bool    `json:"pe_is_add_play_plan"`
	PerfData                    any     `json:"perf_data"`
	PerformanceServiceAvailable int32   `json:"performance_service_available"`
	PerformanceServiceStatus    int32   `json:"performance_service_status"`
	PlayPlanExpireMonth         int32   `json:"play_plan_expire_month"`
	PriType                     int32   `json:"pri_type"`
	Price                       int32   `json:"price"`
	PriceRank                   int32   `json:"price_rank"`
	PriceType                   string  `json:"price_type"`
	QueuePosition               int32   `json:"queue_position"`
	RatingLevel                 int32   `json:"rating_level"`
	Remindable                  bool    `json:"remindable"`
	Res                         any     `json:"res"`
	Status                      string  `json:"status"`
	SyncItemInfo                any     `json:"sync_item_info"`
	SyncPcFlag                  bool    `json:"sync_pc_flag"`
	UrgentStatus                int32   `json:"urgent_status"`
	WeakOffline                 bool    `json:"weak_offline"`
	WeakOfflineReason           string  `json:"weak_offline_reason"`
}

type peProductRespData struct {
	Count int          `json:"count"`
	Item  []*PeProduct `json:"item"`
}

func (p *PeProduct) ensureProceed() {
	p.CreateTimeMs = normalizeTime(p.CreateTime)
	p.ApplyReviewTimeMs = normalizeTime(p.ApplyReviewTime)
	p.OnlineTimeMs = normalizeTime(p.OnlineTime)
}

func fetchPeProducts(span int64, headers map[string]string) ([]*PeProduct, string, error) {
	url := fmt.Sprintf("https://mc-launcher.webapp.163.com/items/categories/pe/?is_third_party=false&start=0&span=%d", span)

	// {
	//        "apply_review_time": "2026-01-06T04:32:42.392000+00:00",
	//        "can_manage_server": false,
	//        "can_silent_online": false,
	//        "can_synchronize_pc": false,
	//        "can_update_pc": false,
	//        "collection_id": 0,
	//        "create_time": "2025-12-26T10:28:58.519000+00:00",
	//        "discount": [],
	//        "exempt_perf_review_num": 0,
	//        "intercept_fields": [],
	//        "is_ea": 0,
	//        "is_original": true,
	//        "is_silent_online": false,
	//        "is_suitable_pc": true,
	//        "is_sync": false,
	//        "is_test_server": false,
	//        "item_id": "4684715890695005221",
	//        "item_name": "\u52c7\u95ef\u795e\u79d8\u738b\u5e9c\uff0c\u5f00\u542f\u751f\u5b58\u5192\u9669\uff01",
	//        "lobby_config_op_log": [],
	//        "lobby_sort_key": {},
	//        "online_time": "2026-01-13T04:10:36.244000+00:00",
	//        "ori_weak_offline": false,
	//        "ori_weak_offline_reason": "",
	//        "pe_is_add_play_plan": false,
	//        "perf_data": {
	//          "mem_size": 107,
	//          "mem_warning": false
	//        },
	//        "performance_service_available": 0,
	//        "performance_service_status": 0,
	//        "play_plan_expire_month": 0,
	//        "pri_type": 1,
	//        "price": 300,
	//        "price_rank": 0,
	//        "price_type": "diamond",
	//        "queue_position": 0,
	//        "rating_level": 0,
	//        "remindable": false,
	//        "res": [
	//          {
	//            "add_version": true,
	//            "cdn_info": {
	//              "res_md5": "707636b4d00a084e8bae27671b891c95",
	//              "res_size": 4249900,
	//              "res_time": "2025-12-26T10:36:54.050000+00:00"
	//            },
	//            "cdn_url": "https://x19.gph.netease.com/item_4684715890695005221_2_v2_auu7ydkb.zip",
	//            "mc_version": [],
	//            "res_id": 2,
	//            "res_info": {
	//              "res_md5": "ad957fbb4fdee07eafb8079d640d7ec6",
	//              "res_size": 4236925,
	//              "res_time": "2025-12-26T10:35:56.709000+00:00"
	//            },
	//            "res_name": "631846b3b26448d99952c07433f0afbd.zip",
	//            "res_url": "res_url"
	//          }
	//        ],
	//        "status": "online",
	//        "sync_item_info": {
	//          "available_scope": "client/server",
	//          "brief": "\u4f60\u662f\u4e0d\u662f\u73a9\u817b\u4e86\u5343\u7bc7\u4e00\u5f8b\u7684\u751f\u5b58\u6a21\u5f0f\uff1f\u662f\u4e0d\u662f\u6e34\u671b\u5728\u4e00\u4e2a\u5145\u6ee1\u4e1c\u65b9\u9b45\u529b\u7684\u53e4\u8001\u4e16\u754c\u91cc\uff0c\u4f53\u9a8c\u4e0d\u4e00\u6837\u7684\u5192\u9669\uff1f\u90a3\u5c31\u5bf9\u4e86\uff01\u795e\u79d8\u738b\u5e9c\u5730\u56fe\u5305\uff0c\u5c31\u662f\u4e3a\u4f60\u91cf\u8eab\u6253\u9020\u7684\uff01",
	//          "category": "comp",
	//          "channel": [
	//            {
	//              "channel_id": 7,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e06f972bd2ae12f525cfcO7vy9SAR07",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 9,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e070172bd2ae12f525d10MSjuhSkf07",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 10,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e0705a7fba797a1d53b29deAHBUzf07",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 11,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e070a01617737440efceadrKE873l07",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 12,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e070f01617737440efcfa7Xomghpy07",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 14,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e0714a7fba797a1d53b6fLMrK634m07",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 29,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e07177ce982df1731ed03OuYjiShR07",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 1001,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e071e01617737440efd19HefV3GI307",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 1002,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/695c905e58793f28bc3a7891FK7cj1Oz07",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 1003,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e0726a7fba797a1d53bc74HBm6maw07",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 1004,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e072c284cc74128302fcfD142xse807",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 1005,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e0730a60a1c45f027a4b4DYOfeAMT07",
	//              "version": 1
	//            },
	//            {
	//              "channel_id": 8,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e06f972bd2ae12f525cfcO7vy9SAR07"
	//            },
	//            {
	//              "channel_id": 13,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e070f01617737440efcfa7Xomghpy07"
	//            },
	//            {
	//              "channel_id": 16,
	//              "channel_url": "https://x19.fp.ps.netease.com/file/694e070f01617737440efcfa7Xomghpy07"
	//            }
	//          ],
	//          "game_host": null,
	//          "include_map": true,
	//          "info": "<p><strong>\u5404\u4f4d\u5c0f\u63a2\u9669\u5bb6\u3001\u5efa\u7b51\u5927\u5e08\u3001\u751f\u5b58\u9ad8\u624b\u4eec\uff0c\u4f60\u4eec\u51c6\u5907\u597d\u4e86\u5417\uff1f\uff01</strong> \ud83d\udce3</p><p><br></p><p>\u4f60\u662f\u4e0d\u662f\u73a9\u817b\u4e86\u5343\u7bc7\u4e00\u5f8b\u7684\u751f\u5b58\u6a21\u5f0f\uff1f\u662f\u4e0d\u662f\u6e34\u671b\u5728\u4e00\u4e2a\u5145\u6ee1\u4e1c\u65b9\u9b45\u529b\u7684\u53e4\u8001\u4e16\u754c\u91cc\uff0c\u4f53\u9a8c\u4e0d\u4e00\u6837\u7684\u5192\u9669\uff1f\u90a3\u5c31\u5bf9\u4e86\uff01\u795e\u79d8\u738b\u5e9c\u5730\u56fe\u5305\uff0c\u5c31\u662f\u4e3a\u4f60\u91cf\u8eab\u6253\u9020\u7684\uff01</p>",
	//          "item_id": "4684715890706151622",
	//          "item_name": "\u52c7\u95ef\u795e\u79d8\u738b\u5e9c\uff0c\u5f00\u542f\u751f\u5b58\u5192\u9669\uff01",
	//          "mc_version": [
	//            "100.0.0"
	//          ],
	//          "pri_type": 5,
	//          "rarity": 0,
	//          "requirement": [],
	//          "status": "online",
	//          "sub_type": 22,
	//          "tag": [
	//            111,
	//            115,
	//            129
	//          ],
	//          "weak_offline": false,
	//          "weak_offline_reason": ""
	//        },
	//        "sync_pc_flag": true,
	//        "urgent_status": 0,
	//        "weak_offline": false,
	//        "weak_offline_reason": ""
	//      }
	resp, payload, err := utils.RequestGet[*neResp[*peProductRespData]](url, headers)
	if err != nil {
		return nil, "", err
	}
	if resp.Status != statusOk {
		return nil, "", fmt.Errorf("unexpected resp status: %s: %s", resp.Status, resp.Msg)
	}

	for _, p := range resp.Data.Item {
		p.ensureProceed()
	}

	return resp.Data.Item, payload, nil
}

type PeProductStat struct {
	//日期
	//资源ID
	//资源名称
	//上传时间
	//新增购买
	//销售总量
	//收益
	//绿宝石
	//日活
	//玩家平均游戏时长（分钟）
	//退款率
	//组件涨粉

	DAU                  int32   `json:"DAU"`
	AvgFirstTypeBuy      float64 `json:"avg_first_type_buy"`
	AvgFirstTypeDiamond  float64 `json:"avg_first_type_diamond"`
	AvgFirstTypeFocus    float64 `json:"avg_first_type_focus"`
	AvgFirstTypeRolePlay float64 `json:"avg_first_type_role_play"`
	AvgPlaytime          float64 `json:"avg_playtime"`
	AvgTotalFirstTypeBuy float64 `json:"avg_total_first_type_buy"`
	CntBuy               int32   `json:"cnt_buy"`
	DateId               string  `json:"dateid"`
	DateMs               *int64  `json:"date_ms,omitempty"`
	Diamond              int32   `json:"diamond"`
	DownloadNum          int32   `json:"download_num"`
	FirstTypeAvgRoleTime float64 `json:"first_type_avg_role_time"`
	FocusCnt             int32   `json:"focus_cnt"`
	Iid                  string  `json:"iid"`
	IidInt               *int64  `json:"iid_int,omitempty"`
	PassAvgRoleTimeRatio float64 `json:"pass_avg_role_time_ratio"`
	PassBuyCntRatio      float64 `json:"pass_buy_cnt_ratio"`
	PassCntRolePlayRatio float64 `json:"pass_cnt_role_play_ratio"`
	PassFocusCntRatio    float64 `json:"pass_focus_cnt_ratio"`
	PassPayDiamondRatio  float64 `json:"pass_pay_diamond_ratio"`
	Platform             string  `json:"platform"`
	Points               int32   `json:"points"`
	RefundRate           float64 `json:"refund_rate"`
	ResName              string  `json:"res_name"`
	StarAdjusted         float64 `json:"star_adjusted"`
	UploadTime           *string `json:"upload_time"` // 2026-01-13 12:10:39
	UploadTimeMs         *int64  `json:"upload_time_ms"`
}

type peProductStatRespData struct {
	Data []*PeProductStat `json:"data"`
}

func (p *PeProductStat) ensureProceed() {
	dateIdInt := utils.ParseInt64OrNil(p.DateId)
	if dateIdInt != nil {
		t, err := time.Parse(strconv.FormatInt(*dateIdInt, 10), "20060102")
		if err == nil {
			p.DateMs = gptr.Of(t.UnixMilli())
		}
	}

	p.IidInt = utils.ParseInt64OrNil(p.Iid)

	if p.UploadTime != nil {
		t, err := time.Parse("2006-01-02 15:04:05", *p.UploadTime)
		if err != nil {
			return
		}

		p.UploadTimeMs = gptr.Of(t.UnixMilli())
	}
}

func fetchPeProductStats(
	itemId string,
	startDate time.Time,
	endDate time.Time,
	headers map[string]string,
) ([]*PeProductStat, string, error) {
	startDateStr := startDate.Format("20060102")
	endDateStr := endDate.Format("20060102")

	// https://mc-launcher.webapp.163.com/data_analysis/day_detail/?platform=pe&category=pe&start_date=20250728&end_date=20260123&item_list_str=4684715890695005221&sort=dateid&order=ASC&start=10&span=10&is_need_us_rank_data=true
	url := fmt.Sprintf("https://mc-launcher.webapp.163.com/data_analysis/day_detail/?platform=pe&category=pe&start_date=%s&end_date=%s&item_list_str=%s&sort=dateid&order=ASC&start=0&span=999999999&is_need_us_rank_data=true", startDateStr, endDateStr, itemId)

	// {
	//        "DAU": 5,
	//        "avg_first_type_buy": 41.36,
	//        "avg_first_type_diamond": 135.53,
	//        "avg_first_type_focus": 1.56,
	//        "avg_first_type_role_play": 41.88,
	//        "avg_playtime": 2.93,
	//        "avg_total_first_type_buy": 43314.56,
	//        "cnt_buy": 4,
	//        "dateid": "20260114",
	//        "diamond": 800,
	//        "download_num": 6,
	//        "first_type_avg_role_time": 21.0,
	//        "focus_cnt": 0,
	//        "iid": "4684715890695005221",
	//        "pass_avg_role_time_ratio": 0.55,
	//        "pass_buy_cnt_ratio": 0.77,
	//        "pass_cnt_role_play_ratio": 0.76,
	//        "pass_focus_cnt_ratio": 0.0,
	//        "pass_pay_diamond_ratio": 0.99,
	//        "platform": "PE",
	//        "points": 0,
	//        "refund_rate": 0.0,
	//        "res_name": "\u52c7\u95ef\u795e\u79d8\u738b\u5e9c\uff0c\u5f00\u542f\u751f\u5b58\u5192\u9669\uff01",
	//        "star_adjusted": 0.0,
	//        "upload_time": "2026-01-13 12:10:39"
	//      }

	resp, payload, err := utils.RequestGet[*neResp[*peProductStatRespData]](url, headers)
	if err != nil {
		return nil, "", err
	}
	if resp.Status != statusOk {
		return nil, "", fmt.Errorf("unexpected resp status: %s: %s", resp.Status, resp.Msg)
	}

	for _, stat := range resp.Data.Data {
		stat.ensureProceed()
	}

	return resp.Data.Data, payload, nil
}

type PeProductOrder struct {
	AppOrderId      string `json:"app_orderid"`
	AppOrderIdInt   *int64 `json:"app_orderid_int,omitempty"`
	AppUid          string `json:"app_uid"`
	AppUidInt       *int64 `json:"app_uid_int,omitempty"`
	Discount        string `json:"discount"`
	OfficialChannel int32  `json:"official_channel"`
	Point           int32  `json:"point"`
	PointType       string `json:"point_type"`
	Price           int32  `json:"price"`
	PriceType       string `json:"price_type"`
	ProductName     string `json:"product_name"`
	PurchaseLimit   int32  `json:"purchase_limit"`
	RefundStatus    string `json:"refund_status"`

	// 购买时间
	ShipTime   *string `json:"ship_time"` // 2026-01-24T22:38:34+08:00
	ShipTimeMs *int64  `json:"ship_time_ms,omitempty"`
}

func (p *PeProductOrder) ensureProceed() {
	p.AppUidInt = utils.ParseInt64OrNil(p.AppUid)
	p.AppOrderIdInt = utils.ParseInt64OrNil(p.AppOrderId)

	if p.ShipTime != nil {
		t, err := time.Parse(time.RFC3339, *p.ShipTime)
		if err != nil {
			return
		}

		p.ShipTimeMs = gptr.Of(t.UnixMilli())
	}
}

type peProductOrderRespData struct {
	Count        int               `json:"count"`
	Orders       []*PeProductOrder `json:"orders"`
	TotalDiamond int               `json:"total_diamond"`
	TotalPoints  int               `json:"total_points"`
}

func fetchPeProductOrders(
	itemId string,
	startDate time.Time,
	endDate time.Time,
	headers map[string]string,
) (*peProductOrderRespData, string, error) {
	// 2025-12-25T16:05:19.792Z
	startDateStr := startDate.Format("2006-01-02T15:04:05.000Z")
	endDateStr := endDate.Format("2006-01-02T15:04:05.000Z")

	// https://mc-launcher.webapp.163.com/items/categories/pe/4684715890695005221/incomes/?begin_time=2025-12-25T16:05:19.792Z&end_time=2026-01-25T16:05:19.791Z
	url := fmt.Sprintf("https://mc-launcher.webapp.163.com/items/categories/pe/%s/incomes/?begin_time=%s&end_time=%s", itemId, startDateStr, endDateStr)

	// {
	//        "app_orderid": "4685361157807533181",
	//        "app_uid": "526451565",
	//        "discount": "",
	//        "official_channel": 1,
	//        "point": 100,
	//        "point_type": "\u4ed8\u8d39\u94bb\u77f3",
	//        "price": 0,
	//        "price_type": "",
	//        "product_name": "\u52c7\u95ef\u795e\u79d8\u738b\u5e9c\uff0c\u5f00\u542f\u751f\u5b58\u5192\u9669\uff01",
	//        "purchase_limit": 0,
	//        "refund_status": "",
	//        "ship_time": "2026-01-24T22:38:34+08:00"
	//      }

	resp, str, err := utils.RequestGet[*neResp[*peProductOrderRespData]](url, headers)
	if err != nil {
		return nil, "", err
	}
	if resp.Status != statusOk {
		return nil, "", fmt.Errorf("unexpected resp status: %s: %s", resp.Status, resp.Msg)
	}

	for _, order := range resp.Data.Orders {
		order.ensureProceed()
	}

	return resp.Data, str, nil
}

type PeProductFeedback struct {
	Id              string `json:"_id"`
	CommitNickname  string `json:"commit_nickname"`
	CommitUid       string `json:"commit_uid"`
	CommitUidInt    *int64 `json:"commit_uid_int,omitempty"`
	Content         string `json:"content"`
	CreateTime      int64  `json:"create_time"`
	FeedbackLogFile string `json:"feedback_log_file"`
	ForbidReply     bool   `json:"forbid_reply"`
	HaveLogFile     bool   `json:"have_log_file"`
	Iid             string `json:"iid"`
	IidInt          *int64 `json:"iid_int,omitempty"`
	PicList         any    `json:"pic_list"`
	ResName         string `json:"res_name"`
	Type            string `json:"type"`
}

func (p *PeProductFeedback) ensureProceed() {
	p.CommitUidInt = utils.ParseInt64OrNil(p.CommitUid)
	p.IidInt = utils.ParseInt64OrNil(p.Iid)
}

type peProductFeedbackRespData struct {
	Count int                  `json:"count"`
	Item  []*PeProductFeedback `json:"item"`
}

func fetchPeProductFeedbacks(
	span int64,
	headers map[string]string,
) ([]*PeProductFeedback, string, error) {
	// https://mc-launcher.webapp.163.com/items/feedback/pe/?start=0&span=10
	url := fmt.Sprintf("https://mc-launcher.webapp.163.com/items/feedback/pe/?start=0&span=%d", span)

	// {
	//        "_id": "1365429",
	//        "commit_nickname": "\u4e00\u666e\u901a\u7537\u7684l",
	//        "commit_uid": "2327870415",
	//        "content": "\u6811\u628a\u5c4b\u6a90\u90fd\u9876\u6389\u4e86\uff0c\u623f\u5c4b\u8854\u63a5\u7a7a\u767d\uff0c\u4ec0\u4e48\u65f6\u5019\u4fee\u590d\uff1f",
	//        "create_time": 1735274284,
	//        "feedback_log_file": "",
	//        "forbid_reply": false,
	//        "have_log_file": false,
	//        "iid": "4666381914351687531",
	//        "pic_list": [],
	//        "res_name": "\u3010\u82cf\u5dde\u56ed\u6797\u3011\u6e05\u5578\u56ed",
	//        "type": "\u6545\u969c\u95ee\u9898\u53cd\u9988"
	//      }

	resp, str, err := utils.RequestGet[*neResp[*peProductFeedbackRespData]](url, headers)
	if err != nil {
		return nil, "", err
	}
	if resp.Status != statusOk {
		return nil, "", fmt.Errorf("unexpected resp status: %s: %s", resp.Status, resp.Msg)
	}

	for _, feedback := range resp.Data.Item {
		feedback.ensureProceed()
	}

	return resp.Data.Item, str, nil
}

type PeProductComment struct {
	Id              string `json:"_id"`
	CommentState    string `json:"comment_state"`
	CommentTag      string `json:"comment_tag"`
	CommentedNum    string `json:"commented_num"`
	CommentedNumInt *int64 `json:"commented_num_int,omitempty"`
	GoodNum         string `json:"good_num"`
	GoodNumInt      *int64 `json:"good_num_int,omitempty"`
	Iid             string `json:"iid"`
	IidInt          *int64 `json:"iid_int,omitempty"`
	MasterId        string `json:"master_id"`
	MasterIdInt     *int64 `json:"master_id_int,omitempty"`
	Nickname        string `json:"nickname"`
	PublishTime     string `json:"publish_time"`
	PublishTimeMs   *int64 `json:"publish_time_ms"`
	ReplyId         string `json:"reply_id"`
	ReplyIdInt      *int64 `json:"reply_id_int,omitempty"`
	ResName         string `json:"res_name"`
	Stars           string `json:"stars"`
	StarsInt        *int64 `json:"stars_int,omitempty"`
	Uid             string `json:"uid"`
	UidInt          *int64 `json:"uid_int,omitempty"`
	UserComment     string `json:"user_comment"`
}

func (p *PeProductComment) ensureProceed() {
	p.CommentedNumInt = utils.ParseInt64OrNil(p.CommentedNum)
	p.GoodNumInt = utils.ParseInt64OrNil(p.GoodNum)
	p.IidInt = utils.ParseInt64OrNil(p.Iid)
	p.MasterIdInt = utils.ParseInt64OrNil(p.MasterId)
	p.ReplyIdInt = utils.ParseInt64OrNil(p.ReplyId)
	p.StarsInt = utils.ParseInt64OrNil(p.Stars)
	p.UidInt = utils.ParseInt64OrNil(p.Uid)

	if p.PublishTime != "" {
		// "1768640168"
		timestamp, err := strconv.Atoi(p.PublishTime)
		if err != nil {
			return
		}

		p.PublishTimeMs = gptr.Of(int64(timestamp * 1000))
	}
}

type peProductCommentRespData struct {
	Count int                 `json:"count"`
	Item  []*PeProductComment `json:"item"`
}

func fetchPeProductComments(
	span int64,
	headers map[string]string,
) ([]*PeProductComment, string, error) {
	// https://mc-launcher.webapp.163.com/items/comment/pe/?start=0&span=10
	url := fmt.Sprintf("https://mc-launcher.webapp.163.com/items/comment/pe/?start=0&span=%d", span)

	// {
	//        "_id": "49453632",
	//        "comment_state": "0",
	//        "comment_tag": "\u9ed8\u8ba4\u7c7b\u578b",
	//        "commented_num": "0",
	//        "good_num": "0",
	//        "iid": "4667688751751628251",
	//        "master_id": "0",
	//        "nickname": "DARK_AGE",
	//        "publish_time": "1768640168",
	//        "reply_id": "0",
	//        "res_name": "\u3010\u592a\u5b66 \u4e2d\u5f0f\u5efa\u7b51 \u79cb\u4e91\u697c\u3011",
	//        "stars": "3",
	//        "uid": "2561750901",
	//        "user_comment": ""
	//      }

	resp, str, err := utils.RequestGet[*neResp[*peProductCommentRespData]](url, headers)
	if err != nil {
		return nil, "", err
	}
	if resp.Status != statusOk {
		return nil, "", fmt.Errorf("unexpected resp status: %s: %s", resp.Status, resp.Msg)
	}

	for _, comment := range resp.Data.Item {
		comment.ensureProceed()
	}

	return resp.Data.Item, str, nil
}
