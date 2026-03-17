// ~/common/netease-product/NeteaseProductDetail.tsx
import React, { useEffect, useState } from "react";
import { IDUNN_API } from "~/api";
import { deepNullToUndefined } from "~/common/util/null-to-undefined";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import type { NeteaseProduct } from "~/api/generated";
import type { Route } from "./+types/page";
import {
  OrderDisplay,
  type FetchOrders,
} from "~/common/netease-order/OrderDisplay";
import { Link } from "react-router";

// 从生成的 API 导入产品类型（假设为 NeteaseProduct）

export function meta({ params }: Route.MetaArgs) {
  return [{ title: `Netease Project ${params.id}` }];
}

export function clientLoader({ params }: Route.ClientLoaderArgs) {
  return { id: Number.parseInt(params.id) };
}

/**
 * 产品详情页面
 * 路由参数：id
 */
export default function NeteaseProductDetail({
  loaderData,
}: Route.ComponentProps) {
  const { id } = loaderData;
  const [product, setProduct] = useState<NeteaseProduct | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  document.title = `网易商品详情 - [${id}]: ${product?.itemName || "未找到项目"}`;

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    IDUNN_API.apiV1CommercialNeteaseProductsIdGet(id)
      .then((response) => {
        setProduct(response.data);

        setError(null);
      })
      .catch((err) => {
        setError(err.message || "加载失败");
      })
      .finally(() => {
        setLoading(false);
      });
  }, [id]);

  if (loading) {
    return <DetailSkeleton />;
  }

  if (error || !product) {
    return (
      <div className="container mx-auto p-6">
        <div className="p-4 text-red-500 bg-red-50 rounded">
          加载失败: {error}
        </div>
      </div>
    );
  }

  const fetchOrdersProject: FetchOrders = async (page, size, search, sort) => {
    // 直接调用生成的 API，假设后端已经支持解析 search 和 sort 字符串
    const response =
      await IDUNN_API.apiV1CommercialNeteaseProductsProductIdOrdersGet(
        String(id),
        search,
        page,
        size,
        sort,
      );
    return response.data;
  };

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* 顶部基本信息 */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <span className="truncate">{product.itemName}</span>
            <Badge variant={getStatusVariant(product.internalStatus)}>
              {product.internalStatus}
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            <InfoItem label="ID" value={product.id} />
            <InfoItem label="物品ID" value={product.itemId} />
            <InfoItem
              label="价格"
              value={
                product.price != null
                  ? `${product.price} ${product.priceType || ""}`
                  : "-"
              }
            />
            <InfoItem
              label="创建时间"
              value={formatTime(product.createTimeMs)}
            />
            <InfoItem
              label="更新时间"
              value={formatTime(deepNullToUndefined(product.updateTimeMs))}
            />
            <InfoItem
              label="项目ID"
              value={
                product.project && product.project !== null ? (
                  <Link
                    className="font-bold inline-block"
                    to={`/commercial/projects/${product.project?.id}`}
                  >
                    【{product.project?.id}】
                  </Link>
                ) : (
                  "未关联项目"
                )
              }
            />
            <InfoItem
              label="项目名称"
              value={
                product.project && product.project !== null ? (
                  <Link
                    className="font-bold inline-block"
                    to={`/commercial/projects/${product.project?.id}`}
                  >
                    【{product.project?.displayName}】
                  </Link>
                ) : (
                  "未关联项目"
                )
              }
            />
          </div>
        </CardContent>
      </Card>

      {/* 销售订单统计看板 (调用新API) */}
      <OrderStatsDashboard productId={id} />

      {/* 统计数据图表 (statPayload) */}
      {product.statPayload && <StatCharts statPayload={product.statPayload} />}

      <Card>
        <CardHeader>
          <CardTitle>产品订单列表</CardTitle>
        </CardHeader>
        <CardContent>
          <OrderDisplay pageSize={10} fetchOrders={fetchOrdersProject} />
        </CardContent>
      </Card>

      {/* 所有其他字段展示 */}
      <OtherFields product={product} />
    </div>
  );
}

// ---------- 辅助组件 ----------

function InfoItem({ label, value }: { label: string; value: any }) {
  return (
    <div>
      <div className="text-sm text-muted-foreground">{label}</div>
      <div className="font-medium truncate">{value ?? "-"}</div>
    </div>
  );
}

function formatTime(ms?: number): string {
  if (!ms) return "-";
  return new Date(ms).toLocaleString();
}

function getStatusVariant(
  status: string,
): "default" | "secondary" | "destructive" | "outline" {
  switch (status) {
    case "ONLINE":
      return "default";
    case "CREATED":
      return "secondary";
    case "REJECTED":
      return "destructive";
    default:
      return "outline";
  }
}

// 时间差格式化辅助函数：将毫秒格式化为易读的文本
function formatDuration(ms?: number): string {
  if (!ms || ms <= 0) return "0秒";
  const seconds = Math.floor(ms / 1000);
  if (seconds < 60) return `${seconds}秒`;

  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}分钟`;

  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}小时 ${minutes % 60}分`;

  const days = Math.floor(hours / 24);
  return `${days}天 ${hours % 24}小时`;
}

// 独立的订单统计看板组件（内部请求 API）
function OrderStatsDashboard({ productId }: { productId: number }) {
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    IDUNN_API.apiV1CommercialNeteaseProductsIdStatsGet(productId)
      .then((res) => {
        setStats(res.data);
        setError(null);
      })
      .catch((err) => {
        setError(err.message || "统计数据加载失败");
      })
      .finally(() => {
        setLoading(false);
      });
  }, [productId]);

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>订单统计看板</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
            <Skeleton className="h-32 w-full rounded-xl" />
            <Skeleton className="h-32 w-full rounded-xl" />
            <Skeleton className="h-32 w-full rounded-xl" />
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error || !stats) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>订单统计看板</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-red-500 bg-red-50 p-4 rounded-lg">
            {error || "暂无数据"}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>订单统计看板</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 模块 1：订单数 (蓝色调) */}
          <div className="flex flex-col justify-center rounded-xl border border-blue-100 bg-blue-50/50 p-6 dark:border-blue-900/50 dark:bg-blue-950/20">
            <div className="text-sm font-medium text-blue-600/80 dark:text-blue-400/80 mb-2">
              总订单数
            </div>
            <div className="text-4xl font-bold tracking-tight text-blue-700 dark:text-blue-400">
              {stats.orderCount ?? 0}
            </div>
          </div>

          {/* 模块 2：金额 (绿色调) */}
          <div className="flex flex-col justify-center rounded-xl border border-emerald-100 bg-emerald-50/50 p-6 dark:border-emerald-900/50 dark:bg-emerald-950/20">
            <div className="text-sm font-medium text-emerald-600/80 dark:text-emerald-400/80 mb-2">
              总金额
            </div>
            <div className="text-4xl font-bold tracking-tight text-emerald-700 dark:text-emerald-400">
              ¥ {stats.totalAmount ?? 0}
            </div>
            <div className="mt-2 text-sm text-emerald-600/70 dark:text-emerald-400/70">
              平均金额: ¥{" "}
              {stats.averageAmount ? stats.averageAmount.toFixed(2) : "0.00"}
            </div>
          </div>

          {/* 模块 3：下单间隔 (紫色调) */}
          <div className="flex flex-col justify-center rounded-xl border border-violet-100 bg-violet-50/50 p-6 dark:border-violet-900/50 dark:bg-violet-950/20">
            <div className="text-sm font-medium text-violet-600/80 dark:text-violet-400/80 mb-2">
              平均下单间隔
            </div>
            <div className="text-4xl font-bold tracking-tight text-violet-700 dark:text-violet-400">
              {formatDuration(stats.avgTimeDiffMs)}
            </div>

            {/* 间隔数据的下级详细信息 */}
            <div className="mt-4 flex items-center justify-between text-xs text-violet-600/70 dark:text-violet-400/70">
              <div className="flex flex-col gap-1">
                <span className="opacity-80">最短间隔</span>
                <span className="font-medium text-violet-700 dark:text-violet-300">
                  {formatDuration(stats.minTimeDiffMs)}
                </span>
              </div>
              <div className="h-6 w-px bg-violet-200 dark:bg-violet-800/50"></div>
              <div className="flex flex-col gap-1">
                <span className="opacity-80">最长间隔</span>
                <span className="font-medium text-violet-700 dark:text-violet-300">
                  {formatDuration(stats.maxTimeDiffMs)}
                </span>
              </div>
              <div className="h-6 w-px bg-violet-200 dark:bg-violet-800/50"></div>
              <div className="flex flex-col gap-1">
                <span className="opacity-80">中位间隔</span>
                <span className="font-medium text-violet-700 dark:text-violet-300">
                  {formatDuration(stats.medianTimeDiffMs)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function DashboardCard({ label, value }: { label: string; value: any }) {
  return (
    <div className="bg-muted p-4 rounded-lg text-center flex flex-col justify-center">
      <div className="text-2xl font-bold wrap-break-word">{value ?? 0}</div>
      <div className="text-sm text-muted-foreground mt-1">{label}</div>
    </div>
  );
}

// 统计图表组件
function StatCharts({ statPayload }: { statPayload: string }) {
  let data: any = null;
  try {
    data = JSON.parse(statPayload);
  } catch (e) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>统计数据趋势</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-red-500">statPayload 解析失败</div>
        </CardContent>
      </Card>
    );
  }
  const dailyData = data.data?.data || [];
  if (!dailyData.length) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>统计数据趋势</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-muted-foreground">暂无统计数据</div>
        </CardContent>
      </Card>
    );
  }

  // 转换数据格式，只选取几个关键指标
  const chartData = dailyData.map((item: any) => ({
    date: formatDate(item.dateid),
    DAU: item.DAU,
    下载量: item.download_num,
    购买数: item.cnt_buy,
    钻石: item.diamond / 100,
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle>统计数据趋势</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={400}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" />
            <YAxis yAxisId="left" />
            <YAxis yAxisId="right" orientation="right" />
            <Tooltip />
            <Legend />
            <Line
              type="monotone"
              dataKey="DAU"
              stroke="#8884d8"
              yAxisId="left"
            />
            <Line
              type="monotone"
              dataKey="下载量"
              stroke="#82ca9d"
              yAxisId="right"
            />
            <Line
              type="monotone"
              dataKey="购买数"
              stroke="#ffc658"
              yAxisId="left"
            />
            <Line
              type="monotone"
              dataKey="钻石"
              stroke="#ff7300"
              name="销售额（100钻石=1元）"
              yAxisId="left"
            />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}

function formatDate(dateid: string): string {
  if (!dateid || dateid.length !== 8) return dateid;
  return `${dateid.slice(4, 6)}-${dateid.slice(6, 8)}`;
}

// 其他字段展示（使用标签页分组）
function OtherFields({ product }: { product: NeteaseProduct }) {
  // 定义各组要展示的字段
  const basicKeys = [
    "itemName",
    "itemId",
    "priType",
    "ratingLevel",
    "queuePosition",
    "remindable",
    "collectionId",
    "exemptPerfReviewNum",
  ];
  const timeKeys = [
    "createTime",
    "createTimeMs",
    "updateTimeMs",
    "onlineTime",
    "onlineTimeMs",
    "applyReviewTime",
    "applyReviewTimeMs",
  ];
  const configKeys = [
    "canManageServer",
    "canSilentOnline",
    "canSynchronizePc",
    "canUpdatePc",
    "isEa",
    "isOriginal",
    "isSilentOnline",
    "isSuitablePc",
    "isSync",
    "isTestServer",
    "syncPcFlag",
    "peIsAddPlayPlan",
  ];
  const otherKeys = [
    "discount",
    "interceptFields",
    "lobbyConfigOpLog",
    "lobbySortKey",
    "oriWeakOffline",
    "oriWeakOfflineReason",
    "perfData",
    "performanceServiceAvailable",
    "performanceServiceStatus",
    "playPlanExpireMonth",
    "syncItemInfo",
    "urgentStatus",
    "weakOffline",
    "weakOfflineReason",
    "res",
    "orderPayload",
    "statPayload",
    "templates",
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle>所有字段</CardTitle>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="basic">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="basic">基本信息</TabsTrigger>
            <TabsTrigger value="times">时间信息</TabsTrigger>
            <TabsTrigger value="config">配置信息</TabsTrigger>
            <TabsTrigger value="other">其他</TabsTrigger>
          </TabsList>
          <TabsContent value="basic">
            <div className="grid grid-cols-2 gap-2">
              <KeyValueList
                obj={product}
                keys={basicKeys}
                formatter={(v) =>
                  v !== undefined && v !== null ? String(v) : "-"
                }
              />
            </div>
          </TabsContent>
          <TabsContent value="times">
            <div className="grid grid-cols-2 gap-2">
              <KeyValueList
                obj={product}
                keys={timeKeys}
                formatter={(v) =>
                  typeof v === "number" ? formatTime(v) : (v ?? "-")
                }
              />
            </div>
          </TabsContent>
          <TabsContent value="config">
            <div className="grid grid-cols-2 gap-2">
              <KeyValueList
                obj={product}
                keys={configKeys}
                formatter={(v) => (v !== undefined ? String(v) : "-")}
              />
            </div>
          </TabsContent>
          <TabsContent value="other">
            <div className="grid grid-cols-2 gap-2">
              <KeyValueList
                obj={product}
                keys={otherKeys}
                formatter={(v) => {
                  if (v === null || v === undefined) return "-";
                  if (typeof v === "object") {
                    return (
                      <pre className="text-xs bg-muted p-2 rounded overflow-auto max-h-40">
                        {JSON.stringify(v, null, 2)}
                      </pre>
                    );
                  }
                  return String(v);
                }}
              />
            </div>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
}

function KeyValueList({
  obj,
  keys,
  formatter,
}: {
  obj: any;
  keys: string[];
  formatter: (v: any) => React.ReactNode;
}) {
  return (
    <>
      {keys.map((key) => (
        <div key={key} className="border-b py-2">
          <div className="text-sm text-muted-foreground">{key}</div>
          <div className="font-mono text-sm break-all">
            {formatter(obj[key])}
          </div>
        </div>
      ))}
    </>
  );
}

function DetailSkeleton() {
  return (
    <div className="container mx-auto p-6 space-y-6">
      <Skeleton className="h-32 w-full" />
      <Skeleton className="h-48 w-full" />
      <Skeleton className="h-96 w-full" />
    </div>
  );
}
