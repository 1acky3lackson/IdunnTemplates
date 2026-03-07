package main

import (
	"context"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/taixue-cn/back-sdk-go/pkg/logx"
	"github.com/taixue-cn/com-stats/internal/pkg/configx"
	"github.com/taixue-cn/com-stats/internal/pkg/infra"
	"github.com/taixue-cn/com-stats/internal/pkg/service"
	"github.com/taixue-cn/com-stats/internal/pkg/service/ne"
)

func main() {
	infra.Init()
	configx.Init()
	service.Init()

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	sigs := make(chan os.Signal, 1)
	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		sig := <-sigs
		logx.InfoContext(ctx, "received signal: "+sig.String())
		cancel()
	}()

	neCrawler := &ne.Crawler{}

	for {
		select {
		case <-ctx.Done():
			logx.InfoContext(ctx, "shutting down netease crawler gracefully...")
			return
		default:
			config, err := configx.GetConfig()
			if err != nil {
				logx.New().With(ctx).
					Err(err).
					Error("fail to get config").
					Emit()
				time.Sleep(15 * time.Minute)
				continue
			}

			logx.InfoContext(ctx, "netease crawler started")
			_, err = neCrawler.CrawlAndSave(ctx)
			if err != nil {
				logx.New().With(ctx).
					Err(err).
					Warn("fail to crawl netease data").
					Emit()
			}

			logx.InfoContext(ctx, "data crawled, waiting for next action ...")
			select {
			case <-time.After(time.Duration(config.IntervalMs) * time.Millisecond):
				// loop continues
			case <-ctx.Done():
				logx.InfoContext(ctx, "shutting down netease crawler gracefully...")
				return
			}
		}
	}
}
