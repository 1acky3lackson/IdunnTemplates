package ne

import (
	"context"
	"testing"

	"github.com/taixue-cn/com-stats/internal/pkg/configx"
	"github.com/taixue-cn/com-stats/internal/pkg/infra"
	"github.com/taixue-cn/com-stats/internal/pkg/service"
)

func TestCrawler_CrawlAndSave(t *testing.T) {
	infra.Init()
	configx.Init()
	service.Init()

	c := &Crawler{}
	ctx := context.TODO()

	data, err := c.CrawlAndSave(ctx)
	if err != nil {
		t.Fatal(err)
	}

	t.Log(data)
}
