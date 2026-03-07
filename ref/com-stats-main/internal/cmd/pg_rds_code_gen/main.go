package main

import (
	"fmt"
	"os"

	"gorm.io/driver/postgres"
	"gorm.io/gen"
	"gorm.io/gorm"
)

func main() {
	dsn, ok := os.LookupEnv("RDS_DSN")
	if !ok {
		panic("RDS_DSN environment variable is not set")
	}

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		panic(fmt.Errorf("failed to connect database: %w (dsn=%s)", err, dsn))
	}

	g := gen.NewGenerator(gen.Config{
		OutPath:        "internal/pkg/infra/rds/query",
		Mode:           gen.WithDefaultQuery | gen.WithQueryInterface,
		FieldNullable:  true,
		FieldCoverable: true,
	})

	_ = os.RemoveAll("internal/pkg/infra/rds/query")
	_ = os.RemoveAll("internal/pkg/infra/rds/model")

	g.UseDB(db)

	tableNames := []string{
		"ne_comp_product_logs",
		"ne_logs",
		"ne_pe_product_comment_logs",
		"ne_pe_product_feedback_logs",
		"ne_pe_product_logs",
		"ne_pe_product_order_logs",
		"ne_pe_product_stat_logs",
		"ne_user_logs",
	}
	for _, tableName := range tableNames {
		meta := g.GenerateModel(tableName)
		g.ApplyBasic(meta)
	}

	g.Execute()
}
