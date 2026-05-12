import React from "react";
import { IDUNN_API } from "~/api";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ArrowLeft, Boxes, ChartPie, Expand, ReceiptText, UserRound } from "lucide-react";
import { Link } from "react-router";
import apiClient from "@/lib/axios";
import type { CheckoutDetailDto, NeteaseOrder } from "~/api/generated";
import CheckoutDetails from "../checkout-details/CheckoutDetails";
import { SettlementBreakdownView } from "../settlement/SettlementBreakdownView";
import { toPointTypeDisplay } from "../util/point-type-display";
import {
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from "recharts";

const orderStatusColor = (status?: string | null) => {
  switch (status) {
    case "ENTERED":
      return "bg-blue-500 text-white";
    case "CALCULATED":
      return "bg-yellow-500 text-black";
    case "INCOME":
      return "bg-lime-400 text-slate-900";
    case "AFTER_N":
      return "bg-green-600 text-white";
    case "REFUNDED":
      return "bg-red-400 text-slate-950";
    default:
      return "bg-slate-300 text-slate-900";
  }
};

const orderStatusText = (status?: string | null) => {
  switch (status) {
    case "ENTERED":
      return "录入";
    case "CALCULATED":
      return "分成";
    case "INCOME":
      return "结款";
    case "AFTER_N":
      return "完成";
    case "REFUNDED":
      return "退款";
    default:
      return status || "-";
  }
};

const checkoutRoleTextMap: Record<string, string> = {
  BUILDER: "建筑制作",
  MODIFIER: "修改美化",
  UPLOADER: "包装宣传",
  SYSTEM: "系统",
  TEMPLATE_AUTHOR: "模板作者",
};

const shareChartColors = [
  "#0f766e",
  "#2563eb",
  "#d97706",
  "#dc2626",
  "#7c3aed",
  "#65a30d",
  "#0891b2",
  "#be185d",
];

type ShareChartMode = "role" | "user";

type ShareSlice = {
  name: string;
  value: number;
};

type ShareChartGroup = {
  key: string;
  title: string;
  totalRatio: number;
  slices: ShareSlice[];
};

const normalizeRatio = (ratio?: number | null) => {
  const numeric = Number(ratio ?? 0);
  return Number.isFinite(numeric) ? numeric : 0;
};

const formatPercent = (value?: number | null) => `${(normalizeRatio(value) * 100).toFixed(2)}%`;
const formatChartTooltipValue = (
  value: number | string | ReadonlyArray<number | string> | undefined,
) => formatPercent(typeof value === "number" ? value : Number(value ?? 0));

const buildOrderCheckoutSearch = (orderId: number) => `order.id:${orderId}`;

function mergeByName(details: CheckoutDetailDto[]) {
  const grouped = new Map<string, number>();

  for (const detail of details) {
    const name = detail.username?.trim() || "未命名用户";
    grouped.set(name, (grouped.get(name) || 0) + normalizeRatio(detail.ratio));
  }

  return [...grouped.entries()]
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value);
}

function buildShareChartGroups(
  details: CheckoutDetailDto[],
  mode: ShareChartMode,
): ShareChartGroup[] {
  if (mode === "user") {
    const slices = mergeByName(details);
    const totalRatio = slices.reduce((sum, item) => sum + item.value, 0);
    return totalRatio > 0
      ? [
          {
            key: "all-users",
            title: "按用户聚合",
            totalRatio,
            slices,
          },
        ]
      : [];
  }

  const slices = details
    .map((detail) => {
      const role = detail.role?.trim() || "UNKNOWN";
      const username = detail.username?.trim() || "未命名用户";
      return {
        name: `${username}-${checkoutRoleTextMap[role] || role}`,
        value: normalizeRatio(detail.ratio),
      };
    })
    .filter((item) => item.value > 0)
    .sort((a, b) => b.value - a.value);

  const totalRatio = slices.reduce((sum, item) => sum + item.value, 0);
  return totalRatio > 0
    ? [
        {
          key: "all-roles",
          title: "按角色拆分",
          totalRatio,
          slices,
        },
      ]
    : [];
}

function ProfitSharePieCard({ group }: { group: ShareChartGroup }) {
  return (
    <div className="space-y-4 rounded-2xl border border-slate-200 bg-slate-50/60 p-4">
      <div className="space-y-1">
        <div className="text-base text-center font-semibold">{group.title}</div>
        <div className="text-center text-sm text-muted-foreground">
          合计占比 {formatPercent(group.totalRatio)}
        </div>
      </div>

      <div className="h-72">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={group.slices}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={90}
              paddingAngle={2}
            >
              {group.slices.map((slice, index) => (
                <Cell
                  key={`${group.key}-${slice.name}`}
                  fill={shareChartColors[index % shareChartColors.length]}
                />
              ))}
            </Pie>
            <Tooltip formatter={formatChartTooltipValue} />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div className="space-y-2">
        {group.slices.map((slice, index) => (
          <div
            key={`${group.key}-legend-${slice.name}`}
            className="flex items-center justify-between gap-3 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm"
          >
            <div className="flex min-w-0 items-center gap-2">
              <span
                className="h-2.5 w-2.5 shrink-0 rounded-full"
                style={{ backgroundColor: shareChartColors[index % shareChartColors.length] }}
              />
              <span className="truncate font-medium">{slice.name}</span>
            </div>
            <span className="shrink-0 font-mono text-slate-700">
              {formatPercent(slice.value)}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function OrderProfitShareOverview({ orderId }: { orderId: number }) {
  const [mode, setMode] = React.useState<ShareChartMode>("role");
  const [details, setDetails] = React.useState<CheckoutDetailDto[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    let cancelled = false;

    const loadAllDetails = async () => {
      setLoading(true);
      setError(null);

      try {
        const allDetails: CheckoutDetailDto[] = [];
        let page = 0;
        let isLastPage = false;

        while (!isLastPage) {
          const response = await IDUNN_API.listBalanceCheckoutDetails(
            buildOrderCheckoutSearch(orderId),
            page,
            100,
            "createTimeMs,desc",
          );

          allDetails.push(...(response.data.content || []));
          isLastPage = Boolean(response.data.last);
          page += 1;
        }

        if (!cancelled) {
          setDetails(allDetails);
        }
      } catch (loadError) {
        if (!cancelled) {
          console.error(loadError);
          setError("分成比例可视化加载失败");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void loadAllDetails();

    return () => {
      cancelled = true;
    };
  }, [orderId]);

  const groups = React.useMemo(() => buildShareChartGroups(details, mode), [details, mode]);

  return (
    <Card className="border-slate-200">
      <CardHeader className="space-y-4">
        <div className="space-y-2 text-center">
          <CardTitle className="flex items-center justify-center gap-2 text-lg">
            <ChartPie className="h-4 w-4" />
            分成比例可视化
          </CardTitle>
        </div>

        <div className="flex justify-center">
          <Tabs value={mode} onValueChange={(value) => setMode(value as ShareChartMode)}>
            <TabsList>
              <TabsTrigger value="role">按角色</TabsTrigger>
              <TabsTrigger value="user">按用户</TabsTrigger>
            </TabsList>
          </Tabs>
        </div>
      </CardHeader>

      <CardContent className="min-w-0">
        {loading ? (
          <div className="grid gap-4 md:grid-cols-2">
            <Skeleton className="h-[26rem] w-full rounded-2xl" />
            <Skeleton className="h-[26rem] w-full rounded-2xl" />
          </div>
        ) : error ? (
          <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        ) : groups.length > 0 ? (
          <div className={`grid gap-4 ${groups.length > 1 ? "md:grid-cols-2" : "grid-cols-1"}`}>
            {groups.map((group) => (
              <ProfitSharePieCard key={group.key} group={group} />
            ))}
          </div>
        ) : (
          <div className="rounded-xl border border-dashed border-slate-200 bg-slate-50/70 px-4 py-8 text-center text-sm text-muted-foreground">
            当前订单还没有可展示的分成比例记录。
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function DetailField({
  label,
  value,
  mono = false,
}: {
  label: string;
  value: React.ReactNode;
  mono?: boolean;
}) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white/80 p-4">
      <div className="text-xs uppercase tracking-[0.18em] text-muted-foreground">
        {label}
      </div>
      <div className={`mt-2 text-sm font-medium ${mono ? "font-mono" : ""}`}>
        {value}
      </div>
    </div>
  );
}

function OrderHero({ order }: { order: NeteaseOrder }) {
  return (
    <Card className="overflow-hidden border-slate-200 bg-[linear-gradient(135deg,#f8fafc_0%,#ffffff_45%,#eff6ff_100%)]">
      <CardHeader className="gap-4 border-b border-slate-200/80 pb-5">
        <div className="flex flex-wrap items-center gap-3">
          <Badge variant="outline">订单 #{order.id}</Badge>
          {order.productId ? (
            <Link to={`/commercial/products/${order.productId}`}>
              <Badge variant="secondary">商品 #{order.productId}</Badge>
            </Link>
          ) : null}
          <Badge className={orderStatusColor(order.internalStatus)}>
            {orderStatusText(order.internalStatus)}
          </Badge>
        </div>
        <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
          <div className="space-y-2">
            <CardTitle className="text-3xl tracking-tight">
              {order.productName || "订单详情"}
            </CardTitle>
            <CardDescription className="max-w-3xl text-sm leading-6">
              查看订单基础信息、关联收益分成记录，以及完整的结算过程提示视图。横向流程图已限制在模块内部滚动，不会撑破页面布局。
            </CardDescription>
          </div>
          <div className="rounded-2xl border border-sky-200 bg-sky-50/80 px-5 py-4">
            <div className="text-xs uppercase tracking-[0.18em] text-sky-700/70">
              订单虚拟点数
            </div>
            <div className="mt-2 text-3xl font-semibold text-sky-950">
              {order.point?.toLocaleString() || "0"}
            </div>
            <div className="mt-1 text-sm text-sky-900/70">
              {toPointTypeDisplay(order.pointType) || "未设置类型"}
            </div>
          </div>
        </div>
      </CardHeader>
      <CardContent className="grid gap-4 p-5 md:grid-cols-2 xl:grid-cols-4">
        <DetailField label="订单编号" value={order.appOrderId || "-"} mono />
        <DetailField label="用户 ID" value={order.appUid || "-"} mono />
        <DetailField
          label="创建时间"
          value={order.shipTimeMs ? new Date(order.shipTimeMs).toLocaleString() : "-"}
        />
        <DetailField
          label="数字订单号"
          value={order.appOrderIdInt ?? "-"}
          mono
        />
      </CardContent>
    </Card>
  );
}

function OrderDetailZoomDialog({
  open,
  onOpenChange,
  title,
  description,
  children,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description: string;
  children: React.ReactNode;
}) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="flex h-[92vh] w-[96vw] max-w-[96vw] flex-col overflow-hidden p-0 sm:max-w-[96vw]">
        <DialogHeader className="shrink-0 border-b px-6 py-4">
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        <div className="min-h-0 flex-1 overflow-y-auto overflow-x-hidden px-6 py-5">
          {children}
        </div>
      </DialogContent>
    </Dialog>
  );
}

export default function NeteaseOrderDetailView({ id }: { id: string }) {
  const [order, setOrder] = React.useState<NeteaseOrder | null>(null);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);
  const [settlementDialogOpen, setSettlementDialogOpen] = React.useState(false);
  const [checkoutDialogOpen, setCheckoutDialogOpen] = React.useState(false);

  React.useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    apiClient
      .get(`/api/v1/commercial/netease-orders/${id}`)
      .then((response) => {
        if (!cancelled) {
          setOrder(response.data as NeteaseOrder);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          console.error(err);
          setError("订单详情读取失败");
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  if (loading) {
    return (
      <div className="space-y-6 p-6">
        <Skeleton className="h-52 w-full rounded-2xl" />
        <div className="grid gap-6 xl:grid-cols-[minmax(0,1.15fr)_minmax(22rem,0.85fr)]">
          <Skeleton className="h-[32rem] w-full rounded-2xl" />
          <Skeleton className="h-[32rem] w-full rounded-2xl" />
        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="space-y-4 p-6">
        <Link
          to="/orders"
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          返回订单管理
        </Link>
        <Card className="border-red-200 bg-red-50">
          <CardHeader>
            <CardTitle className="text-red-800">无法加载订单详情</CardTitle>
            <CardDescription className="text-red-700">
              {error || "没有找到对应订单。"}
            </CardDescription>
          </CardHeader>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-w-0 space-y-6 p-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link
          to="/orders"
          className="inline-flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          返回订单管理
        </Link>
      </div>

      <OrderHero order={order} />

      <div className="grid min-w-0 gap-6 xl:grid-cols-[minmax(0,1.15fr)_minmax(22rem,0.85fr)]">
        <div className="min-w-0 space-y-6">
          <Card className="border-slate-200">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-lg">
                <ReceiptText className="h-4 w-4" />
                订单信息
              </CardTitle>
              <CardDescription>展示订单本身的基础字段与业务信息。</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-4 md:grid-cols-2">
              <DetailField label="数据库 ID" value={order.id} mono />
              <DetailField label="系统状态" value={orderStatusText(order.internalStatus)} />
              <DetailField label="商品名称" value={order.productName || "-"} />
              <DetailField label="点数类型" value={toPointTypeDisplay(order.pointType) || "-"} />
              <DetailField label="应用用户 ID" value={order.appUid || "-"} mono />
              <DetailField label="应用订单号(数字)" value={order.appOrderIdInt ?? "-"} mono />
            </CardContent>
          </Card>

          <Card className="border-slate-200">
            <CardHeader>
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="space-y-1">
                  <CardTitle className="flex items-center gap-2 text-lg">
                    <Boxes className="h-4 w-4" />
                    结算过程提示视图
                  </CardTitle>
                  <CardDescription>
                    用当前算法和当前快照重新推导中间结果，帮助理解整条结算链路。
                  </CardDescription>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setSettlementDialogOpen(true)}
                >
                  <Expand className="mr-1 h-4 w-4" />
                  放大查看
                </Button>
              </div>
            </CardHeader>
            <CardContent className="min-w-0">
              <SettlementBreakdownView orderId={order.id} />
            </CardContent>
          </Card>
        </div>

        <div className="min-w-0 space-y-6">
          <OrderProfitShareOverview orderId={order.id!} />

          <Card className="border-slate-200">
            <CardHeader>
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="space-y-1">
                  <CardTitle className="flex items-center gap-2 text-lg">
                    <UserRound className="h-4 w-4" />
                    关联收益分成记录
                  </CardTitle>
                  <CardDescription>
                    查看该订单生成的所有收益分成记录，并可继续进入单条记录详情。
                  </CardDescription>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCheckoutDialogOpen(true)}
                >
                  <Expand className="mr-1 h-4 w-4" />
                  放大查看
                </Button>
              </div>
            </CardHeader>
            <CardContent className="min-w-0">
              <CheckoutDetails
                forceSearch={{ "order.id": String(order.id) }}
                pageSize={8}
                uid={`order-detail-${order.id}-profits`}
                hideHeader
              />
            </CardContent>
          </Card>
        </div>
      </div>

      <OrderDetailZoomDialog
        open={settlementDialogOpen}
        onOpenChange={setSettlementDialogOpen}
        title="结算过程提示视图"
        description="放大查看当前订单的完整结算链路，滚动仅发生在弹窗内部。"
      >
        <div className="min-w-0">
          <SettlementBreakdownView orderId={order.id} />
        </div>
      </OrderDetailZoomDialog>

      <OrderDetailZoomDialog
        open={checkoutDialogOpen}
        onOpenChange={setCheckoutDialogOpen}
        title="关联收益分成记录"
        description="放大查看该订单关联的全部收益分成记录，滚动仅发生在弹窗内部。"
      >
        <div className="min-w-0">
          <CheckoutDetails
            forceSearch={{ "order.id": String(order.id) }}
            pageSize={20}
            uid={`order-detail-dialog-${order.id}-profits`}
            hideHeader
          />
        </div>
      </OrderDetailZoomDialog>
    </div>
  );
}
