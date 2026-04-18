import React from "react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { ArrowLeft, Boxes, ReceiptText, UserRound } from "lucide-react";
import { Link } from "react-router";
import apiClient from "@/lib/axios";
import type { NeteaseOrder } from "~/api/generated";
import CheckoutDetails from "../checkout-details/CheckoutDetails";
import { SettlementBreakdownView } from "../settlement/SettlementBreakdownView";
import { toPointTypeDisplay } from "../util/point-type-display";

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

export default function NeteaseOrderDetailView({ id }: { id: string }) {
  const [order, setOrder] = React.useState<NeteaseOrder | null>(null);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);

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
              <CardTitle className="flex items-center gap-2 text-lg">
                <Boxes className="h-4 w-4" />
                结算过程提示视图
              </CardTitle>
              <CardDescription>
                用当前算法和当前快照重新推导中间结果，帮助理解整条结算链路。
              </CardDescription>
            </CardHeader>
            <CardContent className="min-w-0">
              <SettlementBreakdownView orderId={order.id} />
            </CardContent>
          </Card>
        </div>

        <div className="min-w-0 space-y-6">
          <Card className="border-slate-200">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-lg">
                <UserRound className="h-4 w-4" />
                关联收益分成记录
              </CardTitle>
              <CardDescription>
                查看该订单生成的所有收益分成记录，并可继续进入单条记录详情。
              </CardDescription>
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

          <Card className="border-slate-200 bg-slate-50/70">
            <CardHeader>
              <CardTitle className="text-base">页面说明</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <div>1. 这里的结算过程是提示视图，用于解释当前算法，并不替代数据库中的最终收益记录。</div>
              <Separator />
              <div>2. 流程图和模板拆分区域的横向滚动被限制在模块内部，页面主体不会被撑宽。</div>
              <Separator />
              <div>3. 如果项目没有录入范围或没有模板快照，系统会自动只展示创作分成链路。</div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
