import React, { useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";

// 引入通用表格组件
import { IDUNN_API } from "~/api";
import type { NeteaseOrder } from "~/api/generated";
import {
  GenericCrudTable,
  type PageResponse,
} from "../generic-crud-table/generic-crud-table";
import { Link } from "react-router";
import CheckoutDetails from "../checkout-details/CheckoutDetails";
import { CircleDollarSign, Coins, DollarSign } from "lucide-react";
import { toast } from "sonner";
import apiClient from "@/lib/axios";

// ---------- 辅助工具 ----------

/*
public enum NeteaseOrderStatus {
  ENTERED,
  CALCULATED,
  INCOME,
  AFTER_M,
  AFTER_N,
  REFUNDED
}
*/

// 状态标签颜色映射
const orderStatusColor = (status: string) => {
  switch (status) {
    case "ENTERED":
      return "bg-blue-500 dark:bg-blue-500/50";
    case "CALCULATED":
      return "bg-yellow-500 dark:bg-yellow-500/50";
    case "INCOME":
      return "bg-lime-300 dark:bg-lime-300/50";
    case "AFTER_N":
      return "bg-green-500 dark:bg-green-500/50";
    case "REFUNDED":
      return "bg-red-300 dark:bg-red-300/50";
    default:
      return "bg-gray-300 dark:bg-gray-300/50";
  }
};

const orderStatusText = (status: string) => {
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
      return status;
  }
};

// ---------- API 封装 ----------

// 让外部可以直接适配 GenericCrudTable 所需的接口签名
export type FetchOrders = (
  page: number,
  size: number,
  search: string,
  sort: string,
) => Promise<PageResponse<NeteaseOrder>>;

export const fetchOrdersDefault: FetchOrders = async (
  page,
  size,
  search,
  sort,
) => {
  // 直接调用生成的 API，假设后端已经支持解析 search 和 sort 字符串
  const response = await IDUNN_API.apiV1CommercialNeteaseOrdersGet(
    search || undefined,
    page,
    size,
    sort || undefined,
  );
  return response.data;
};

// ---------- 主组件 ----------

interface OrderDisplayProps {
  fetchOrders?: FetchOrders;
  pageSize?: number;
  forceSearch?: Record<string, string>;
  uid?: string;
}

export function OrderDisplay({
  fetchOrders = fetchOrdersDefault,
  pageSize = 20,
  forceSearch,
  uid = "odr",
}: OrderDisplayProps) {
  // 详情弹窗的状态管理
  const [viewingOrder, setViewingOrder] = useState<NeteaseOrder | null>(null);
  const [editingPointOrder, setEditingPointOrder] = useState<NeteaseOrder | null>(
    null,
  );
  const [refundingOrder, setRefundingOrder] = useState<NeteaseOrder | null>(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const fetchOrdersWithRefresh = useCallback(
    (page: number, size: number, search: string, sort: string) =>
      fetchOrders(page, size, search, sort),
    [fetchOrders, refreshTrigger],
  );

  return (
    <div className="space-y-4">
      <GenericCrudTable<NeteaseOrder>
        uid={uid}
        getRowId={(row) => row.id}
        list={fetchOrdersWithRefresh}
        pageSize={pageSize}
        forcedSearchValues={forceSearch}
        // 配置搜索字段 (基于你原先的 placeholder 需求)
        searchFields={[
          // 注意：这里的 key 需要和你后端的 Specification 对应
          { key: "appOrderId", label: "订单号", fuzzy: true },
          { key: "appUid", label: "用户ID", fuzzy: true },
          { key: "productName", label: "产品名称", fuzzy: true },
          { key: "internalStatus", label: "状态 (如 PENDING)", fuzzy: false },
        ]}
        // 配置表格列
        schema={{
          id: { title: "ID" },
          appOrderId: {
            title: "订单编号",
            render: (val) => <CompactCopyValue value={val} />,
          },
          productName: {
            title: "产品名称",
            filterable: true,
            render: (val, row) => (
              <Link to={`/commercial/products/${row.productId}`}>
                <span className="font-medium">{val || "-"}</span>
              </Link>
            ),
          },
          appUid: {
            title: "用户ID",
            render: (val) => <CompactCopyValue value={val} />,
          },
          point: {
            title: "价格",
            filterable: true,
            sortable: true,
            render: (val, row) =>
              val != null ? `${Number(val).toLocaleString()} ${row.pointType || ""}` : "待填写",
          },
          pointType: {
            title: "类型",
            filterable: true,
            render: (val: string) =>
              val.includes("付费") || val.includes("钻石") ? (
                <Badge className="inline-flex items-center gap-1 rounded-full border border-blue-600 bg-blue-300/50 px-2 py-0.5 text-[11px] font-medium leading-4 text-blue-950 dark:text-blue-300">
                  <DollarSign size={12} /> {val}
                </Badge>
              ) : (
                <Badge className="inline-flex items-center gap-1 rounded-full border border-green-600 bg-green-400/50 px-2 py-0.5 text-[11px] font-medium leading-4 text-green-800 dark:text-green-300">
                  <Coins size={12} /> {val}
                </Badge>
              ),
            // sortable: true
          },
          internalStatus: {
            title: "状态",
            filterable: true,
            render: (val: string) => (
              <Badge
                className={orderStatusColor(val) + " text-foreground font-bold"}
              >
                {orderStatusText(val)}
              </Badge>
            ),
          },
          shipTimeMs: {
            title: "创建时间",
            sortable: true,
            render: (val: number) =>
              val ? new Date(val).toLocaleString() : "-",
          },
        }}
        // 注入自定义操作列：查看详情
        rowActions={(row) => (
          <div className="flex gap-2 justify-end">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setViewingOrder(row)}
            >
              查看详情
            </Button>
            <Button
              variant={row.point == null ? "default" : "secondary"}
              size="sm"
              onClick={() => setEditingPointOrder(row)}
            >
              {row.point == null ? "填写点数" : "修改点数"}
            </Button>
            {row.internalStatus !== "REFUNDED" && (
              <Button
                variant="destructive"
                size="sm"
                onClick={() => setRefundingOrder(row)}
              >
                退款
              </Button>
            )}
          </div>
        )}
      />

      {/* 订单详情弹窗 */}
      <OrderDetailsDialog
        order={viewingOrder}
        onClose={() => setViewingOrder(null)}
      />
      <UpdateOrderPointDialog
        order={editingPointOrder}
        onClose={() => setEditingPointOrder(null)}
        onSuccess={() => {
          setEditingPointOrder(null);
          setRefreshTrigger((prev) => prev + 1);
        }}
      />
      <RefundOrderDialog
        order={refundingOrder}
        onClose={() => setRefundingOrder(null)}
        onSuccess={() => {
          setRefundingOrder(null);
          setRefreshTrigger((prev) => prev + 1);
        }}
      />
    </div>
  );
}

function CompactCopyValue({ value }: { value?: string | number | null }) {
  const fullValue = value == null ? "-" : String(value);
  const shortValue =
    fullValue.length > 6 ? `${fullValue.slice(0, 6)}...` : fullValue;

  const handleContextMenu = async (event: React.MouseEvent) => {
    event.preventDefault();
    if (fullValue === "-") return;
    try {
      await navigator.clipboard.writeText(fullValue);
      toast.success("已复制完整内容");
    } catch (error) {
      console.error(error);
      toast.warning("复制失败");
    }
  };

  return (
    <TooltipProvider delayDuration={200}>
      <Tooltip>
        <TooltipTrigger asChild>
          <button
            type="button"
            className="max-w-[8rem] cursor-copy truncate rounded px-1 text-left font-mono hover:bg-muted"
            onContextMenu={handleContextMenu}
          >
            {shortValue}
          </button>
        </TooltipTrigger>
        <TooltipContent>{fullValue}</TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}

// ---------- 订单详情弹窗组件 ----------

function OrderDetailsDialog({
  order,
  onClose,
}: {
  order: NeteaseOrder | null;
  onClose: () => void;
}) {
  if (!order) return null;

  return (
    <Dialog open={!!order} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>订单详情 - {order.id}</DialogTitle>
        </DialogHeader>

        {/* 使用 grid 布局紧凑展示详细信息 */}
        <div className="grid grid-cols-2 gap-y-4 gap-x-8 py-4 text-sm">
          <div>
            <span className="text-muted-foreground block mb-1">数据库 ID</span>
            <div className="font-medium">{order.id}</div>
          </div>
          <div>
            <span className="text-muted-foreground block mb-1">系统状态</span>
            <Badge className={orderStatusColor(order.internalStatus)}>
              {orderStatusText(order.internalStatus)}
            </Badge>
          </div>

          <div>
            <span className="text-muted-foreground block mb-1">产品名称</span>
            <div className="font-medium">{order.productName || "-"}</div>
          </div>
          <div>
            <span className="text-muted-foreground block mb-1">APP UID</span>
            <div className="font-medium">{order.appUid || "-"}</div>
          </div>

          <div>
            <span className="text-muted-foreground block mb-1">订单虚拟点数</span>
            <div className="font-medium text-lg text-blue-600">
              {order.point ? order.point.toLocaleString() : "0"}
            </div>
          </div>
          <div>
            <span className="text-muted-foreground block mb-1">创建时间</span>
            <div className="font-medium">
              {order.shipTimeMs
                ? new Date(order.shipTimeMs).toLocaleString()
                : "-"}
            </div>
          </div>

          {/* 你可以在这里扩展更多不适合放在表格里的长文本或附加信息 */}
          {order && (
            <div className="col-span-2">
              <span className="text-muted-foreground block mb-1">备注信息</span>
              <CheckoutDetails
                forceSearch={{ "order.id": String(order.id) }}
                pageSize={5}
              />
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}

function UpdateOrderPointDialog({
  order,
  onClose,
  onSuccess,
}: {
  order: NeteaseOrder | null;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const [point, setPoint] = useState("");
  const [pointType, setPointType] = useState("");
  const [submitting, setSubmitting] = useState(false);

  React.useEffect(() => {
    setPoint(order?.point != null ? String(order.point) : "");
    setPointType(order?.pointType || "");
  }, [order]);

  if (!order) return null;

  const handleSubmit = async () => {
    const parsedPoint = Number(point);
    if (!Number.isFinite(parsedPoint) || parsedPoint <= 0) {
      toast.warning("请输入大于 0 的虚拟点数");
      return;
    }

    setSubmitting(true);
    try {
      await apiClient.patch(`/api/v1/commercial/netease-orders/${order.id}/point`, {
        point: parsedPoint,
        pointType: pointType || undefined,
      });
      toast.success("虚拟点数已更新，系统已触发即时结算");
      onSuccess();
    } catch (error) {
      console.error(error);
      toast.warning("更新虚拟点数失败");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={!!order} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>填写订单虚拟点数</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="text-sm text-muted-foreground">
            订单 #{order.id}，填写后会立即触发分成与结算链路。
          </div>
          <div className="space-y-2">
            <div className="text-sm font-medium">虚拟点数</div>
            <Input
              type="number"
              min={1}
              value={point}
              onChange={(e) => setPoint(e.target.value)}
              placeholder="请输入订单虚拟点数"
            />
          </div>
          <div className="space-y-2">
            <div className="text-sm font-medium">点数类型</div>
            <Input
              value={pointType}
              onChange={(e) => setPointType(e.target.value)}
              placeholder="可选，不填则沿用现有类型"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            取消
          </Button>
          <Button onClick={handleSubmit} disabled={submitting}>
            保存并结算
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function RefundOrderDialog({
  order,
  onClose,
  onSuccess,
}: {
  order: NeteaseOrder | null;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const [submitting, setSubmitting] = useState(false);

  if (!order) return null;

  const handleRefund = async () => {
    setSubmitting(true);
    try {
      await apiClient.patch(`/api/v1/commercial/netease-orders/${order.id}/refund`, {
        refundStatus: "MANUAL_REFUND",
      });
      toast.success("订单已触发退款流程");
      onSuccess();
    } catch (error) {
      console.error(error);
      toast.warning("订单退款失败");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={!!order} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>确认退款</DialogTitle>
        </DialogHeader>
        <div className="space-y-3 text-sm text-muted-foreground">
          <div>订单 #{order.id}</div>
          <div>退款会立即回滚该订单对应的收益分成与虚拟点数变动。</div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            取消
          </Button>
          <Button variant="destructive" onClick={handleRefund} disabled={submitting}>
            确认退款
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
