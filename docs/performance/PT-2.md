# PT-2 压力测试

## 目标

验证系统在 `100` 个并发用户同时进行“订单查询 / 结算链路展示”相关操作时仍可稳定运行：

- 订单列表查询
- 订单详情查询
- 订单结算过程拆解查询
- 按订单筛选收益分成记录

可选地，还可以在专门测试环境中开启少量“补点触发结算”请求，用于压测订单补点后的结算链路。

验收标准：

- 并发数：`100`
- 平均响应时间：`<= 3s`
- 无报错：HTTP 失败率 `= 0`

对应脚本：

- [PT-2-k6.js](./PT-2-k6.js)

## 测试工具

本测试使用 [k6](https://k6.io/) 编写，适合在本地或 CI 中重复执行，并直接输出阈值是否通过。

## 前置条件

1. 后端服务已启动，并可从压测机访问。
2. 测试账号具备以下接口的访问权限：
   - `/api/v1/commercial/netease-orders`
   - `/api/v1/commercial/netease-orders/{id}`
   - `/api/v1/commercial/netease-orders/{id}/settlement-breakdown`
   - `/api/v1/commercial/balance/checkout-details`
3. 系统中已有一定数量的订单与收益分成记录，避免“空数据”影响参考价值。
4. 如果要开启写链路压测，请准备专门的测试订单，避免污染生产数据。

## 运行方式

### 方式一：只读压测

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e AUTH_TOKEN=你的登录Token \
  -e DURATION=3m \
  docs/performance/PT-2-k6.js
```

### 方式二：指定订单样本

如果你希望固定压某几笔订单，可以显式传入订单 ID：

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e AUTH_TOKEN=你的登录Token \
  -e TEST_ORDER_IDS=12,13,18,25 \
  docs/performance/PT-2-k6.js
```

### 方式三：开启少量补点写操作

仅建议在测试环境使用：

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e AUTH_TOKEN=你的登录Token \
  -e TEST_ORDER_IDS=12,13,18 \
  -e ENABLE_POINT_PATCH=true \
  -e POINT_PATCH_PERCENT=5 \
  -e PATCH_POINT_VALUE=300 \
  -e PATCH_POINT_TYPE=付费钻石 \
  docs/performance/PT-2-k6.js
```

这表示约 `5%` 的轮次会额外对测试订单发起一次补点请求，以触发结算链路。

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
- `ORDER_PAGE_SIZE`
  - 订单列表请求每页条数，默认 `20`
- `ORDER_SAMPLE_SIZE`
  - `setup` 阶段自动抓取的订单样本数量，默认 `20`
- `TEST_ORDER_IDS`
  - 手动指定订单 ID 列表，逗号分隔
- `ENABLE_POINT_PATCH`
  - 是否开启补点写链路，默认 `false`
- `POINT_PATCH_PERCENT`
  - 开启写链路后，约多少百分比的轮次执行补点，默认 `5`
- `PATCH_POINT_VALUE`
  - 补点值，默认 `300`
- `PATCH_POINT_TYPE`
  - 补点类型，默认 `付费钻石`
- `PATCH_ALLOWED_STATUSES`
  - 仅允许哪些订单状态进入补点候选，默认不限制；如需限制，请传真实枚举值，例如 `ENTERED,CALCULATED`

## 脚本行为

每个虚拟用户会循环执行以下链路：

1. 查询订单列表
2. 随机选择一笔订单，查询订单详情
3. 查询该订单的结算过程拆解
4. 使用 `order.id` 作为筛选条件，拉取该订单关联的收益分成记录

若开启 `ENABLE_POINT_PATCH=true`，则还会在少量轮次中额外执行：

5. 对测试订单发起补点请求，触发结算链路

默认模式为只读，不会修改业务数据。

说明：

- PT-2 中订单状态筛选使用的是当前后端真实枚举值，例如 `ENTERED`、`CALCULATED`。
- 如果你要使用 `PATCH_ALLOWED_STATUSES`，请务必填写后端真实状态名，而不是业务描述文案。

## 阈值定义

脚本内已写死以下阈值：

- 全局：
  - `http_req_failed: rate == 0`
  - `http_req_duration: avg < 3000`
  - `http_req_duration: p(95) < 5000`
- 分接口：
  - `order_list`
  - `order_detail`
  - `settlement_breakdown`
  - `order_checkout_details`
  - `order_point_patch`
  - 都要求 `avg < 3000`

## 通过判定

当 k6 输出中所有 threshold 均为 `✓` 时，可判定：

- 在 100 个并发用户同时进行订单查询与结算链路相关操作时，
- 系统仍能稳定运行，
- 平均响应时间不超过 3 秒，
- 且无报错，
- PT-2 通过。

## 建议留档内容

建议将以下内容一并保存到测试报告中：

1. 本次执行命令
2. 测试环境说明
   - CPU / 内存
   - 数据库类型
   - 服务器部署方式
3. 压测是否开启了补点写链路
4. k6 终端输出截图
5. 关键指标汇总
   - `http_req_duration avg`
   - `http_req_duration p(95)`
   - `http_req_failed`
6. 最终结论
   - `通过 / 不通过`
