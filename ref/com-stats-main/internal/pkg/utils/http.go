package utils

import (
	"errors"
	"io"
	"net/http"

	"github.com/taixue-cn/back-sdk-go/pkg/jsonx"
	"github.com/taixue-cn/back-sdk-go/pkg/logx"
)

var (
	ErrStatusCode = errors.New("status code")
)

var httpClient = &http.Client{}

func RequestGet[T any](url string, headers map[string]string) (T, string, error) {
	var zero T

	logx.New().
		Set("url", url).
		Info("http get").
		Emit()

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return zero, "", err
	}

	for key, value := range headers {
		req.Header.Set(key, value)
	}

	resp, err := httpClient.Do(req)
	if err != nil {
		return zero, "", err
	}
	defer func() {
		err := resp.Body.Close()
		if err != nil {
			logx.New().
				Set("url", url).
				Set("error", err.Error()).
				Error("failed to close response body").
				Emit()
		}
	}()

	logx.New().
		Set("status_code", resp.StatusCode).
		Set("url", url).
		Info("http get response").
		Emit()

	if resp.StatusCode/100 != 2 {
		return zero, "", ErrStatusCode
	}

	bytes, err := io.ReadAll(resp.Body)
	if err != nil {
		return zero, "", err
	}

	res, err := jsonx.Unmarshal[T](bytes)
	if err != nil {
		return zero, "", err
	}

	return res, string(bytes), nil
}
