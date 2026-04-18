# PT-1 压力测试

## 目标

验证系统在 `100` 个并发用户同时进行以下 Web 管理端查询操作时仍可稳定运行：

- 模板检索
- 收益分成记录拉取
- 虚拟点数变动记录拉取

验收标准：

- 并发数：`100`
- 平均响应时间：`<= 3s`
- 无报错：HTTP 失败率 `= 0`

对应脚本：

- [PT-1-k6.js](./PT-1-k6.js)

## 测试工具

本测试使用 [k6](https://k6.io/) 编写，适合在本地或 CI 中重复执行，并直接输出阈值是否通过。

## 前置条件

1. 后端服务已启动，并可从压测机访问。
2. 测试账号具备以下页面/接口的访问权限：
   - `/api/v1/templates`
   - `/api/v1/commercial/balance/checkout-details`
   - `/api/v1/commercial/balance/records`
3. 系统中已有一定数量的模板、收益分成记录、虚拟点数变动记录，以避免“空数据”影响结果参考性。

## 运行方式

### 方式一：使用 Bearer Token

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e AUTH_TOKEN=你的登录Token \
  -e DURATION=3m \
  docs/performance/PT-1-k6.js
```

### 方式二：直接传完整 Authorization 头

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e AUTH_HEADER="Bearer 你的登录Token" \
  docs/performance/PT-1-k6.js
```

### 方式三：使用 Cookie

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e COOKIE="auth_token=你的Token" \
  docs/performance/PT-1-k6.js
```

## 可调参数

脚本支持以下环境变量：

- `BASE_URL`
  - 默认值：`http://localhost:8080`
- `AUTH_TOKEN`
  - 登录 token，脚本会自动拼成 `Bearer xxx`
- `AUTH_HEADER`
  - 自定义完整认证头，优先级高于 `AUTH_TOKEN`
- `COOKIE`
  - 认证 Cookie
- `DURATION`
  - 测试持续时间，默认 `3m`
- `THINK_TIME_MS`
  - 每轮请求后的思考时间，默认 `200`
- `ACCEPT_LANGUAGE`
  - 默认 `zh-CN`

## 脚本行为

每个虚拟用户会循环执行以下请求：

1. 模板检索
2. 收益分成记录拉取
3. 虚拟点数变动记录拉取

脚本会对不同请求使用不同的筛选条件，尽量贴近真实后台使用方式，而不是只重复打一个固定 URL。

## 阈值定义

脚本内已写死以下阈值：

- 全局：
  - `http_req_failed: rate == 0`
  - `http_req_duration: avg < 3000`
  - `http_req_duration: p(95) < 5000`
- 分接口：
  - `templates_search`
  - `checkout_details`
  - `balance_records`
  - 三者都要求 `avg < 3000`

## 通过判定

当 k6 输出中所有 threshold 均为 `✓` 时，可判定：

- 在 100 个并发用户同时进行模板检索与收益分成记录、虚拟点数变动记录拉取等多线程操作时，
- 系统仍能稳定运行，
- 平均响应时间不超过 3 秒，
- 且无报错，
- PT-1 通过。

## 建议留档内容

建议将以下内容一并保存到测试报告中：

1. 本次执行命令
2. 测试环境说明
   - CPU / 内存
   - 数据库类型
   - 服务器部署方式
3. k6 终端输出截图
4. 关键指标汇总
   - `http_req_duration avg`
   - `http_req_duration p(95)`
   - `http_req_failed`
5. 最终结论
   - `通过 / 不通过`
