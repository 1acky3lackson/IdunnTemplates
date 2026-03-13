package configx

import "github.com/taixue-cn/back-sdk-go/pkg/zkx"

var (
	config *zkx.Config[*ComStatsConfig]
)

func Init() {
	configLocal, err := zkx.NewYamlConfig[*ComStatsConfig]("/taixue/services/com.stats/config.yml")
	if err != nil {
		panic(err)
	}
	config = configLocal
}

func GetConfig() (*ComStatsConfig, error) {
	return config.Get()
}
