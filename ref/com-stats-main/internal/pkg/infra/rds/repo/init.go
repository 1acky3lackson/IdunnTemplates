package repo

import (
	"database/sql"

	"github.com/taixue-cn/back-sdk-go/pkg/env"
	"github.com/taixue-cn/back-sdk-go/pkg/logx"
	"github.com/taixue-cn/back-sdk-go/pkg/prop"
	"github.com/taixue-cn/com-stats/internal/pkg/infra/rds/query"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var (
	db *gorm.DB
)

func Transactional(fn func(db *gorm.DB) error, opts ...*sql.TxOptions) error {
	return db.Transaction(fn, opts...)
}

func getLogLevel() logger.LogLevel {
	if env.IsProd() {
		return logger.Info
	} else {
		return logger.Silent
	}
}

func initDB() {
	dsn := prop.MustResolveString("rds.dsn")
	dbLocal, err := gorm.Open(postgres.Open(dsn), &gorm.Config{
		Logger: logger.NewSlogLogger(logx.GetLogger(), logger.Config{
			Colorful: true,
			LogLevel: getLogLevel(),
		}),
	})

	if err != nil {
		panic(err)
	}
	db = dbLocal

	if !env.IsProd() {
		db = db.Debug()
	}
}

func GetDB() *gorm.DB {
	return db
}

func Init() {
	initDB()

	query.SetDefault(db)
}
