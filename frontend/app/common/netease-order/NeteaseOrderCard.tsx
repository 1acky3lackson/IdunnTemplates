import { Fragment, useState } from "react";
import { ChevronDown, ChevronUp } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

// --- 辅助函数 ---

/** 格式化日期时间 (输入 ISO 字符串，输出本地可读字符串) */
function formatDateTime(isoString: string): string {
  try {
    const date = new Date(isoString);
    return date.toLocaleString("zh-CN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: false,
    });
  } catch {
    return isoString; // fallback
  }
}

/** 针对 internalStatus 返回对应的 Badge 变体 */
function getStatusVariant(
  status: string,
): "default" | "secondary" | "destructive" | "outline" {
  switch (status) {
    case "ENTERED":
      return "default"; // 通常绿色
    case "PENDING":
      return "secondary"; // 灰色
    case "CANCELLED":
      return "destructive"; // 红色
    default:
      return "outline";
  }
}

function getStatusText(status: string): string {
  switch (status) {
    case "ENTERED":
      return "已录入";
    case "CALCULATED":
      return "已分成";
    case "INCOME":
      return "已结算";
    case "AFTER_M":
      return "处理中";
    case "AFTER_N":
      return "已完成";
    case "REFUNDED":
      return "已退款";
    case "PENDING":
      return "待处理";
    case "CANCELLED":
      return "已取消";
    default:
      return status;
  }
}

/** 将空字符串或 null 转为占位符 '-' */
function renderValue(value: string | number | null | undefined): string {
  if (value === "" || value === null || value === undefined) return "-";
  return String(value);
}

// --- 单个订单卡片组件 (可展开/折叠) ---
interface OrderCardProps {
  data: NeteaseOrder;
}

export function OrderCard({ data }: OrderCardProps) {
  const [expanded, setExpanded] = useState(false);

  // 解构数据以便使用
  const {
    productName,
    internalStatus,
    appOrderId,
    appUid,
    point,
    pointType,
    shipTime,
    id,
    nePeProductLogId,
    appOrderIdInt,
    appUidInt,
    discount,
    officialChannel,
    price,
    priceType,
    purchaseLimit,
    refundStatus,
    shipTimeMs,
    refundInTimeMs,
    productId,
  } = data;

  // 用于详情区域的通用标签-值对，避免重复代码
  const detailItems: Array<{ label: string; value: React.ReactNode }> = [
    { label: "记录ID", value: id },
    { label: "产品日志ID", value: nePeProductLogId },
    { label: "应用订单号", value: appOrderId },
    { label: "应用订单号(数字)", value: appOrderIdInt },
    { label: "用户ID", value: appUid },
    { label: "用户ID(数字)", value: appUidInt },
    {
      label: "折扣",
      value: discount ? discount : "-",
    },
    {
      label: "官方渠道",
      value: officialChannel === 1 ? "官方" : `渠道 ${officialChannel}`,
    },
    { label: "积分", value: `${point} ${pointType}` },
    {
      label: "价格",
      value: priceType ? `${price} ${priceType}` : `${price}`,
    },
    { label: "产品名称", value: productName },
    { label: "购买限制", value: purchaseLimit ?? "-" },
    { label: "退款状态", value: refundStatus ? refundStatus : "-" },
    { label: "发货时间", value: formatDateTime(shipTime) },
    { label: "发货时间戳(ms)", value: shipTimeMs },
    {
      label: "可退款时间戳",
      value: refundInTimeMs !== null ? refundInTimeMs : "-",
    },
    { label: "内部状态", value: getStatusText(internalStatus) },
    { label: "产品ID", value: productId },
  ];

  return (
    <Card className="w-full shadow-sm hover:shadow transition-shadow py-2">
      <CardHeader className="flex flex-row items-start align-middle justify-between space-y-0">
        <div className="flex items-center align-middle gap-2 flex-wrap min-w-0">
          <Badge variant={getStatusVariant(internalStatus)}>
            {getStatusText(internalStatus)}
          </Badge>
          <Badge
            variant="outline"
            className="text-xs font-normal whitespace-nowrap"
          >
            {point} {pointType}
          </Badge>
          <CardTitle className="text-base font-semibold truncate max-w-50 sm:max-w-xs">
            {productName}
          </CardTitle>

          {/* 折叠状态下的快捷标签 */}
          {!expanded && (
            <div className="flex flex-wrap items-center gap-1 ml-1">
              <Badge
                variant="outline"
                className="text-xs font-normal font-mono truncate"
              >
                订单号: {appOrderId}
              </Badge>
              <Badge
                variant="outline"
                className="text-xs font-normal font-mono truncate"
              >
                ID: {appUid}
              </Badge>
              <Badge
                variant="outline"
                className="text-xs font-normal whitespace-nowrap"
              >
                {formatDateTime(shipTime)}
              </Badge>
            </div>
          )}
        </div>

        <Button
          variant="ghost"
          size="sm"
          onClick={() => setExpanded(!expanded)}
          className="ml-2 shrink-0"
          aria-label={expanded ? "折叠详情" : "展开详情"}
        >
          {expanded ? (
            <ChevronUp className="h-4 w-4" />
          ) : (
            <ChevronDown className="h-4 w-4" />
          )}
        </Button>
      </CardHeader>

      {/* 展开时显示详细信息网格，折叠时完全不渲染 CardContent 以压缩高度 */}
      {expanded && (
        <CardContent>
          <div className="grid grid-cols-2 gap-x-4 gap-y-3 text-sm">
            {detailItems.map((item, index) => (
              <Fragment key={index}>
                <div className="text-muted-foreground">{item.label}</div>
                <div className="truncate" title={String(item.value)}>
                  {item.value}
                </div>
              </Fragment>
            ))}
          </div>
        </CardContent>
      )}
    </Card>
  );
}

// 由于需要在 map 中使用 Fragment，导入 React
import React from "react";
import type { NeteaseOrder } from "~/api/generated";

// --- 列表组件 ---
interface OrderListProps {
  orders: NeteaseOrder[];
  className?: string;
}

export function OrderList({ orders, className }: OrderListProps) {
  if (!orders || orders.length === 0) {
    return (
      <div className="text-center py-12 text-muted-foreground border rounded-lg">
        暂无订单数据
      </div>
    );
  }

  return (
    <div className={cn("space-y-4", className)}>
      {orders.map((order) => (
        <OrderCard key={order.id} data={order} />
      ))}
    </div>
  );
}

// 示例用法 (注释掉，仅用于展示)
/*
const sampleOrder: OrderData = {
  id: 5,
  nePeProductLogId: 77,
  appOrderId: "4686279564581166114",
  appOrderIdInt: 4686279564581166114,
  appUid: "919132906",
  appUidInt: 919132906,
  discount: "",
  officialChannel: 1,
  point: 300,
  pointType: "付费钻石",
  price: 0,
  priceType: "",
  productName: "【中式/唐风/古风】栖霞寺",
  purchaseLimit: 0,
  refundStatus: "",
  shipTime: "2026-03-07T11:10:40+08:00",
  shipTimeMs: 1772853040000,
  refundInTimeMs: null,
  internalStatus: "ENTERED",
  productId: 3,
};

// 渲染列表
<OrderList orders={[sampleOrder, sampleOrder]} />
*/
