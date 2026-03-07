package ne

import (
	"time"

	"github.com/bytedance/gg/gptr"
)

func normalizeTime(s *string) *int64 {
	if s == nil {
		return nil
	}

	t, err := time.Parse(time.RFC3339Nano, *s)
	if err != nil {
		return nil
	}
	return gptr.Of(t.UnixMilli())
}
