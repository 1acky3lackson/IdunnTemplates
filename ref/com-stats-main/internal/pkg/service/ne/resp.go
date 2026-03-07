package ne

const (
	statusOk = "ok"
)

type neResp[T any] struct {
	Status string `json:"status"`
	Msg    string `json:"msg,omitempty"`
	Data   T      `json:"data,omitempty"`
}
