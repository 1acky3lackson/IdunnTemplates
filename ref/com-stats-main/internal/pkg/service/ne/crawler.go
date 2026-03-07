package ne

import (
	"context"
	"time"

	"github.com/bytedance/gg/gptr"
	"github.com/taixue-cn/back-sdk-go/pkg/idx"
	"github.com/taixue-cn/back-sdk-go/pkg/jsonx"
	"github.com/taixue-cn/back-sdk-go/pkg/logx"
	"github.com/taixue-cn/back-sdk-go/pkg/timex"
	"github.com/taixue-cn/com-stats/internal/pkg/configx"
	"github.com/taixue-cn/com-stats/internal/pkg/infra/rds/model"
	"github.com/taixue-cn/com-stats/internal/pkg/infra/rds/repo"
	"gorm.io/gorm"
)

type Crawler struct{}

type PeProductsDataItem struct {
	Orders []*PeProductOrder
	Stats  []*PeProductStat

	OrdersPayload string
	StatsPayload  string
}

type PeProductData struct {
	Product *PeProduct

	Orders []*PeProductOrder
	Stats  []*PeProductStat

	OrdersPayload string
	StatsPayload  string
}

type User struct {
	Id   string
	Name string

	PeProducts        []*PeProductData
	PeProductsPayload string

	PeProductComments        []*PeProductComment
	PeProductCommentsPayload string

	PeProductFeedbacks        []*PeProductFeedback
	PeProductFeedbacksPayload string

	CompProducts        []*CompProduct
	CompProductsPayload string
}

type Data struct {
	StartTimeMs int64
	EndTimeMs   int64

	Users []*User
}

func (c *Crawler) Crawl(ctx context.Context) (*Data, error) {
	config, err := configx.GetConfig()
	if err != nil {
		return nil, err
	}

	logx.InfoContext(ctx, "starting ne crawler")
	startTimeMs := timex.GetCurrentTimeMs()

	res := &Data{
		StartTimeMs: startTimeMs,
		Users:       make([]*User, 0),
	}

	for userId, user := range config.Ne.Users {
		logx.New().With(ctx).
			Set("user_id", userId).
			Set("user_name", user.Name).
			Info("crawling ne user").
			Emit()

		now := time.Now()

		u := &User{
			Id:         userId,
			Name:       user.Name,
			PeProducts: make([]*PeProductData, 0),
		}
		res.Users = append(res.Users, u)

		peProducts, peProductPayload, err := fetchPeProducts(config.Ne.PeProductSpan, user.Headers)
		if err != nil {
			return nil, err
		}
		time.Sleep(time.Duration(config.Ne.IntervalMs) * time.Millisecond)
		u.PeProductsPayload = peProductPayload

		for _, peProduct := range peProducts {
			peProductOrders, peProductOrderPayload, err := fetchPeProductOrders(
				peProduct.ItemId,
				now.Add(-time.Duration(config.Ne.PeProductOrderDays)*24*time.Hour),
				now,
				user.Headers,
			)
			if err != nil {
				logx.New().With(ctx).
					Err(err).
					Set("user_id", userId).
					Set("user_name", user.Name).
					Set("item_id", peProduct.ItemId).
					Set("item_name", peProduct.ItemName).
					Error("fail to crawl pe product orders").
					Emit()
				continue
			}
			time.Sleep(time.Duration(config.Ne.IntervalMs) * time.Millisecond)

			peProductStats, peProductStatsPayload, err := fetchPeProductStats(
				peProduct.ItemId,
				now.Add(-time.Duration(config.Ne.PeProductStatDays)*24*time.Hour),
				now,
				user.Headers,
			)
			if err != nil {
				logx.New().With(ctx).
					Err(err).
					Set("user_id", userId).
					Set("user_name", user.Name).
					Set("item_id", peProduct.ItemId).
					Set("item_name", peProduct.ItemName).
					Error("fail to crawl pe product stats").
					Emit()
				continue
			}
			time.Sleep(time.Duration(config.Ne.IntervalMs) * time.Millisecond)

			u.PeProducts = append(u.PeProducts, &PeProductData{
				Product: peProduct,

				Orders: peProductOrders.Orders,
				Stats:  peProductStats,

				OrdersPayload: peProductOrderPayload,
				StatsPayload:  peProductStatsPayload,
			})
		}

		peProductComments, peProductCommentPayload, err := fetchPeProductComments(config.Ne.PeProductCommentSpan, user.Headers)
		if err != nil {
			return nil, err
		}
		time.Sleep(time.Duration(config.Ne.IntervalMs) * time.Millisecond)

		u.PeProductComments = peProductComments
		u.PeProductCommentsPayload = peProductCommentPayload

		peProductFeedbacks, peProductFeedbackPayload, err := fetchPeProductFeedbacks(config.Ne.PeProductFeedbackSpan, user.Headers)
		if err != nil {
			return nil, err
		}
		time.Sleep(time.Duration(config.Ne.IntervalMs) * time.Millisecond)

		u.PeProductFeedbacks = peProductFeedbacks
		u.PeProductFeedbacksPayload = peProductFeedbackPayload

		compProducts, compProductPayload, err := fetchCompProducts(config.Ne.CompProductSpan, user.Headers)
		if err != nil {
			return nil, err
		}

		u.CompProducts = compProducts
		u.CompProductsPayload = compProductPayload
	}

	endTimeMs := timex.GetCurrentTimeMs()
	res.EndTimeMs = endTimeMs
	logx.InfoContext(ctx, "finished ne crawler")

	return res, nil
}

func (c *Crawler) CrawlAndSave(ctx context.Context) (*Data, error) {
	data, err := c.Crawl(ctx)
	if err != nil {
		return nil, err
	}

	gen, err := idx.GetSnowflakeGen()
	if err != nil {
		return nil, err
	}

	err = repo.Transactional(func(db *gorm.DB) error {
		neLogId, err := gen.NextId()
		if err != nil {
			return err
		}

		neLog := &model.NeLog{
			ID:               neLogId,
			StartCrawlTimeMs: gptr.Of(data.StartTimeMs),
			EndCrawlTimeMs:   gptr.Of(data.EndTimeMs),
		}
		err = db.Create(neLog).Error
		if err != nil {
			return err
		}

		for _, u := range data.Users {
			neUserLogId, err := gen.NextId()
			if err != nil {
				return err
			}

			neUserLog := &model.NeUserLog{
				ID:                           neUserLogId,
				NeLogID:                      &neLogId,
				UserID:                       u.Id,
				PeProductsPayload:            &u.PeProductsPayload,
				PeProductCommentLogsPayload:  &u.PeProductCommentsPayload,
				PeProductFeedbackLogsPayload: &u.PeProductFeedbacksPayload,
				CompProductLogsPayload:       &u.CompProductsPayload,
			}
			err = db.Create(neUserLog).Error
			if err != nil {
				return err
			}

			for _, p := range u.PeProducts {
				nePeProductLogId, err := gen.NextId()
				if err != nil {
					return err
				}

				nePeProductLog := &model.NePeProductLog{
					ID:                          nePeProductLogId,
					NeUserLogID:                 &neUserLogId,
					ApplyReviewTime:             p.Product.ApplyReviewTime,
					ApplyReviewTimeMs:           p.Product.ApplyReviewTimeMs,
					CanManageServer:             &p.Product.CanManageServer,
					CanSilentOnline:             &p.Product.CanSilentOnline,
					CanSynchronizePc:            &p.Product.CanSynchronizePc,
					CanUpdatePc:                 &p.Product.CanUpdatePc,
					CollectionID:                &p.Product.CollectionId,
					CreateTime:                  p.Product.CreateTime,
					CreateTimeMs:                p.Product.CreateTimeMs,
					Discount:                    gptr.Of(jsonx.MarshalStringOrEmpty(&p.Product.Discount)),
					ExemptPerfReviewNum:         &p.Product.ExemptPerfReviewNum,
					InterceptFields:             gptr.Of(jsonx.MarshalStringOrEmpty(&p.Product.InterceptFields)),
					IsEa:                        &p.Product.IsEa,
					IsOriginal:                  &p.Product.IsOriginal,
					IsSilentOnline:              &p.Product.IsSilentOnline,
					IsSuitablePc:                &p.Product.IsSuitablePc,
					IsSync:                      &p.Product.IsSync,
					IsTestServer:                &p.Product.IsTestServer,
					ItemID:                      &p.Product.ItemId,
					ItemName:                    &p.Product.ItemName,
					LobbyConfigOpLog:            gptr.Of(jsonx.MarshalStringOrEmpty(&p.Product.LobbyConfigOpLog)),
					LobbySortKey:                gptr.Of(jsonx.MarshalStringOrEmpty(&p.Product.LobbySortKey)),
					OnlineTime:                  gptr.Of(jsonx.MarshalStringOrEmpty(&p.Product.OnlineTime)),
					OnlineTimeMs:                p.Product.OnlineTimeMs,
					OriWeakOffline:              &p.Product.OriWeakOffline,
					OriWeakOfflineReason:        &p.Product.OriWeakOfflineReason,
					PeIsAddPlayPlan:             &p.Product.PeIsAddPlayPlan,
					PerfData:                    gptr.Of(jsonx.MarshalStringOrEmpty(&p.Product.PerfData)),
					PerformanceServiceAvailable: &p.Product.PerformanceServiceAvailable,
					PerformanceServiceStatus:    &p.Product.PerformanceServiceStatus,
					PlayPlanExpireMonth:         &p.Product.PlayPlanExpireMonth,
					PriType:                     &p.Product.PriType,
					Price:                       &p.Product.Price,
					PriceRank:                   &p.Product.PriceRank,
					PriceType:                   &p.Product.PriceType,
					QueuePosition:               &p.Product.QueuePosition,
					RatingLevel:                 &p.Product.RatingLevel,
					Remindable:                  &p.Product.Remindable,
					Res:                         gptr.Of(jsonx.MarshalStringOrEmpty(p.Product.Res)),
					Status:                      &p.Product.Status,
					SyncItemInfo:                gptr.Of(jsonx.MarshalStringOrEmpty(&p.Product.SyncItemInfo)),
					SyncPcFlag:                  &p.Product.SyncPcFlag,
					UrgentStatus:                &p.Product.UrgentStatus,
					WeakOffline:                 &p.Product.WeakOffline,
					WeakOfflineReason:           &p.Product.WeakOfflineReason,
					OrderPayload:                &p.OrdersPayload,
					StatPayload:                 &p.StatsPayload,
				}
				err = db.Create(nePeProductLog).Error
				if err != nil {
					return err
				}

				for _, s := range p.Stats {
					stat := &model.NePeProductStatLog{
						//ID: &s.ID,
						NePeProductLogID:     &nePeProductLogId,
						Dau:                  &s.DAU,
						AvgFirstTypeBuy:      &s.AvgFirstTypeBuy,
						AvgFirstTypeDiamond:  &s.AvgFirstTypeDiamond,
						AvgFirstTypeFocus:    &s.AvgFirstTypeFocus,
						AvgFirstTypeRolePlay: &s.AvgFirstTypeRolePlay,
						AvgPlaytime:          &s.AvgPlaytime,
						AvgTotalFirstTypeBuy: &s.AvgTotalFirstTypeBuy,
						CntBuy:               &s.CntBuy,
						DateID:               &s.DateId,
						DateMs:               s.DateMs,
						Diamond:              &s.Diamond,
						DownloadNum:          &s.DownloadNum,
						FirstTypeAvgRoleTime: &s.FirstTypeAvgRoleTime,
						FocusCnt:             &s.FocusCnt,
						Iid:                  &s.Iid,
						IidInt:               s.IidInt,
						PassAvgRoleTimeRatio: &s.PassAvgRoleTimeRatio,
						PassBuyCntRatio:      &s.PassBuyCntRatio,
						PassCntRolePlayRatio: &s.PassCntRolePlayRatio,
						PassFocusCntRatio:    &s.PassFocusCntRatio,
						PassPayDiamondRatio:  &s.PassPayDiamondRatio,
						Platform:             &s.Platform,
						Points:               &s.Points,
						RefundRate:           &s.RefundRate,
						ResName:              &s.ResName,
						StarAdjusted:         &s.StarAdjusted,
						UploadTime:           s.UploadTime,
						UploadTimeMs:         s.UploadTimeMs,
					}
					err = db.Create(stat).Error
					if err != nil {
						return err
					}
				}

				for _, o := range p.Orders {
					order := &model.NePeProductOrderLog{
						//ID: &o.ID,
						NePeProductLogID: &nePeProductLogId,
						AppOrderID:       &o.AppOrderId,
						AppOrderIDInt:    o.AppOrderIdInt,
						AppUID:           &o.AppUid,
						AppUIDInt:        o.AppUidInt,
						Discount:         &o.Discount,
						OfficialChannel:  &o.OfficialChannel,
						Point:            &o.Point,
						PointType:        &o.PointType,
						Price:            &o.Price,
						PriceType:        &o.PriceType,
						ProductName:      &o.ProductName,
						PurchaseLimit:    &o.PurchaseLimit,
						RefundStatus:     &o.RefundStatus,
						ShipTime:         o.ShipTime,
						ShipTimeMs:       o.ShipTimeMs,
					}
					err = db.Create(order).Error
					if err != nil {
						return err
					}
				}
			}

			for _, p := range u.CompProducts {
				neCompProductLog := &model.NeCompProductLog{
					//ID:                   &p.ID,
					NeUserLogID:          &neUserLogId,
					ApplyReviewTime:      p.ApplyReviewTime,
					ApplyReviewTimeMs:    p.ApplyReviewTimeMs,
					CanManageServer:      &p.CanManageServer,
					CanSilentOnline:      &p.CanSilentOnline,
					CreateTime:           p.CreateTime,
					CreateTimeMs:         p.CreateTimeMs,
					Discount:             gptr.Of(jsonx.MarshalStringOrEmpty(&p.Discount)),
					ExemptPerfReviewNum:  &p.ExemptPerfReviewNum,
					InterceptFields:      gptr.Of(jsonx.MarshalStringOrEmpty(&p.InterceptFields)),
					IsEa:                 &p.IsEa,
					IsOriginal:           &p.IsOriginal,
					IsSilentOnline:       &p.IsSilentOnline,
					IsSync:               &p.IsSync,
					IsTestServer:         &p.IsTestServer,
					ItemID:               &p.ItemId,
					ItemIDInt:            p.ItemIdInt,
					ItemName:             &p.ItemName,
					LobbyConfigOpLog:     gptr.Of(jsonx.MarshalStringOrEmpty(&p.LobbyConfigOpLog)),
					LobbySortKey:         gptr.Of(jsonx.MarshalStringOrEmpty(&p.LobbySortKey)),
					OnlineTime:           p.OnlineTime,
					OnlineTimeMs:         p.OnlineTimeMs,
					OriWeakOffline:       &p.OriWeakOffline,
					OriWeakOfflineReason: &p.OriWeakOfflineReason,
					PlayPlanExpireMonth:  &p.PlayPlanExpireMonth,
					PriType:              &p.PriType,
					Price:                &p.Price,
					PriceRank:            &p.PriceRank,
					PriceType:            &p.PriceType,
					QueuePosition:        &p.QueuePosition,
					RatingLevel:          &p.RatingLevel,
					RelateItemID:         &p.RelateItemId,
					Remindable:           &p.Remindable,
					Status:               &p.Status,
					SyncPcFlag:           &p.SyncPcFlag,
					UrgentStatus:         &p.UrgentStatus,
					WeakOffline:          &p.WeakOffline,
					WeakOfflineReason:    &p.WeakOfflineReason,
				}
				err = db.Create(neCompProductLog).Error
				if err != nil {
					return err
				}
			}
		}

		return nil
	})
	if err != nil {
		return nil, err
	}

	return data, nil
}
