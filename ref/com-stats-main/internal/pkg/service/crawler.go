package service

import "context"

type Crawler[T any] interface {
	Crawl(ctx context.Context) (T, error)
	CrawlAndSave(ctx context.Context) (T, error)
}
