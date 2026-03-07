package configx

type Ne struct {
	CompProductSpan int64 `yaml:"comp-product-span"`

	PeProductSpan         int64 `yaml:"pe-product-span"`
	PeProductOrderDays    int64 `yaml:"pe-product-order-days"`
	PeProductStatDays     int64 `yaml:"pe-product-stat-days"`
	PeProductCommentSpan  int64 `yaml:"pe-product-comment-span"`
	PeProductFeedbackSpan int64 `yaml:"pe-product-feedback-span"`

	Users      map[string]NeUser `yaml:"users"`
	IntervalMs int64             `yaml:"interval-ms"`
}

type NeUser struct {
	Headers map[string]string `yaml:"headers"`
	Name    string            `yaml:"name"`
}
