// ~/common/netease-product/NeteaseProductDetail.tsx
import React, { useEffect, useState } from 'react';
import { IDUNN_API } from '~/api';
import { deepNullToUndefined } from '~/common/util/null-to-undefined';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';
import { Separator } from '@/components/ui/separator';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import type { NeteaseProduct } from '~/api/generated';
import type { Route } from './+types/page';
import { OrderDisplay, type FetchOrders } from '~/common/netease-order/OrderDisplay';
import { Link } from 'react-router';

// 从生成的 API 导入产品类型（假设为 NeteaseProduct）

export function meta({ params }: Route.MetaArgs) {
  return [
    { title: `Netease Project ${params.id}` },
  ];
}

export function clientLoader({ params }: Route.ClientLoaderArgs) {
  return { id: Number.parseInt(params.id) };
}



/**
 * 产品详情页面
 * 路由参数：id
 */
export default function NeteaseProductDetail({ loaderData }: Route.ComponentProps) {
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
        setError(err.message || '加载失败');
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
    const response = await IDUNN_API.apiV1CommercialNeteaseProductsProductIdOrdersGet(
      String(id),
      search,
      page,
      size,
      sort
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
                  ? `${product.price} ${product.priceType || ''}`
                  : '-'
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
            <InfoItem label="项目ID" value={(product.project && product.project !== null) ? <Link className='font-bold inline-block' to={`/commercial/projects/${product.project?.id}`}>【{product.project?.id}】</Link> : '未关联项目'} />
            <InfoItem label="项目名称" value={(product.project && product.project !== null) ? <Link className='font-bold inline-block' to={`/commercial/projects/${product.project?.id}`}>【{product.project?.displayName}】</Link> : '未关联项目'} />
          </div>
        </CardContent>
      </Card>

      {/* 销售数据看板 (orderPayload) */}
      {product.orderPayload && (
        <SalesDashboard orderPayload={product.orderPayload} />
      )}

      {/* 统计数据图表 (statPayload) */}
      {product.statPayload && (
        <StatCharts statPayload={product.statPayload} />
      )}

      <Card>
        <CardHeader>
          <CardTitle>产品订单列表</CardTitle>
        </CardHeader>
        <CardContent>
          <OrderDisplay
            pageSize={10}
            fetchOrders={fetchOrdersProject}
          />
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
      <div className="font-medium truncate">{value ?? '-'}</div>
    </div>
  );
}

function formatTime(ms?: number): string {
  if (!ms) return '-';
  return new Date(ms).toLocaleString();
}

function getStatusVariant(
  status: string
): 'default' | 'secondary' | 'destructive' | 'outline' {
  switch (status) {
    case 'ONLINE':
      return 'default';
    case 'CREATED':
      return 'secondary';
    case 'REJECTED':
      return 'destructive';
    default:
      return 'outline';
  }
}

// 销售看板组件
function SalesDashboard({ orderPayload }: { orderPayload: string }) {
  let data: any = null;
  try {
    data = JSON.parse(orderPayload);
  } catch (e) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>销售数据看板</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-red-500">orderPayload 解析失败</div>
        </CardContent>
      </Card>
    );
  }
  const payload = data.data || {};
  return (
    <Card>
      <CardHeader>
        <CardTitle>销售数据看板</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <DashboardCard label="总订单数" value={payload.count} />
          <DashboardCard label="销售额（100钻石=1元）" value={payload.total_diamonds / 100} />
          <DashboardCard label="总积分" value={payload.total_points} />
          <DashboardCard
            label="订单列表"
            value={payload.orders?.length ?? 0}
          />
        </div>
      </CardContent>
    </Card>
  );
}

function DashboardCard({ label, value }: { label: string; value: any }) {
  return (
    <div className="bg-muted p-4 rounded-lg text-center">
      <div className="text-2xl font-bold">{value ?? 0}</div>
      <div className="text-sm text-muted-foreground">{label}</div>
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
            <Line type="monotone" dataKey="DAU" stroke="#8884d8" yAxisId="left" />
            <Line type="monotone" dataKey="下载量" stroke="#82ca9d" yAxisId="right" />
            <Line type="monotone" dataKey="购买数" stroke="#ffc658" yAxisId="left" />
            <Line type="monotone" dataKey="钻石" stroke="#ff7300" name="销售额（100钻石=1元）" yAxisId="left" />
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
    'itemName',
    'itemId',
    'priType',
    'ratingLevel',
    'queuePosition',
    'remindable',
    'collectionId',
    'exemptPerfReviewNum',
  ];
  const timeKeys = [
    'createTime',
    'createTimeMs',
    'updateTimeMs',
    'onlineTime',
    'onlineTimeMs',
    'applyReviewTime',
    'applyReviewTimeMs',
  ];
  const configKeys = [
    'canManageServer',
    'canSilentOnline',
    'canSynchronizePc',
    'canUpdatePc',
    'isEa',
    'isOriginal',
    'isSilentOnline',
    'isSuitablePc',
    'isSync',
    'isTestServer',
    'syncPcFlag',
    'peIsAddPlayPlan',
  ];
  const otherKeys = [
    'discount',
    'interceptFields',
    'lobbyConfigOpLog',
    'lobbySortKey',
    'oriWeakOffline',
    'oriWeakOfflineReason',
    'perfData',
    'performanceServiceAvailable',
    'performanceServiceStatus',
    'playPlanExpireMonth',
    'syncItemInfo',
    'urgentStatus',
    'weakOffline',
    'weakOfflineReason',
    'res',
    'orderPayload',
    'statPayload',
    'templates',
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
                formatter={(v) => (v !== undefined && v !== null ? String(v) : '-')}
              />
            </div>
          </TabsContent>
          <TabsContent value="times">
            <div className="grid grid-cols-2 gap-2">
              <KeyValueList
                obj={product}
                keys={timeKeys}
                formatter={(v) =>
                  typeof v === 'number' ? formatTime(v) : v ?? '-'
                }
              />
            </div>
          </TabsContent>
          <TabsContent value="config">
            <div className="grid grid-cols-2 gap-2">
              <KeyValueList
                obj={product}
                keys={configKeys}
                formatter={(v) => (v !== undefined ? String(v) : '-')}
              />
            </div>
          </TabsContent>
          <TabsContent value="other">
            <div className="grid grid-cols-2 gap-2">
              <KeyValueList
                obj={product}
                keys={otherKeys}
                formatter={(v) => {
                  if (v === null || v === undefined) return '-';
                  if (typeof v === 'object') {
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