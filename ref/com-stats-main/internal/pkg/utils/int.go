package utils

import "strconv"

func ParseInt64OrNil(s string) *int64 {
	res, err := strconv.ParseInt(s, 10, 64)
	if err != nil {
		return nil
	}
	return &res
}
