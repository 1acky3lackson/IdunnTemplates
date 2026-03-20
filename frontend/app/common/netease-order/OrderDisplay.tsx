import React, { useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

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

  return (
    <div className="space-y-4">
      <GenericCrudTable<NeteaseOrder>
        uid={uid}
        getRowId={(row) => row.id}
        list={fetchOrders}
        pageSize={pageSize}
        forcedSearchValues={forceSearch}
        // 配置搜索字段 (基于你原先的 placeholder 需求)
        searchFields={[
          // 注意：这里的 key 需要和你后端的 Specification 对应
          { key: "orderId", label: "订单号", fuzzy: true },
          { key: "userId", label: "用户ID", fuzzy: true },
          { key: "productName", label: "产品名称", fuzzy: true },
          { key: "internalStatus", label: "状态 (如 PENDING)", fuzzy: false },
        ]}
        // 配置表格列
        schema={{
          id: { title: "ID" },
          appOrderId: { title: "订单编号" },
          productName: {
            title: "产品名称",
            filterable: true,
            render: (val, row) => (
              <Link to={`/commercial/netease-products/${row.productId}`}>
                <span className="font-medium">{val || "-"}</span>
              </Link>
            ),
          },
          appUid: { title: "用户ID" },
          point: {
            title: "价格",
            filterable: true,
            sortable: true,
            // render: (val, row) => `${val} ${row.pointType}`,
          },
          pointType: {
            title: "类型",
            filterable: true,
            render: (val: string) =>
              val.includes("付费") || val.includes("钻石") ? (
                <div className="flex gap-2 align-middle text-sm rounded-4xl border border-blue-600 bg-blue-300/50 px-2 py-1 text-blue-950 dark:text-blue-300">
                  <DollarSign size="1em" /> {val}
                </div>
              ) : (
                <div className="flex gap-2 align-middle text-sm rounded-4xl border border-green-600 bg-green-400/50 px-2 py-1 text-green-800 dark:text-green-300">
                  <Coins size="1em" /> {val}
                </div>
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
          <Button
            variant="outline"
            size="sm"
            onClick={() => setViewingOrder(row)}
          >
            查看详情
          </Button>
        )}
      />

      {/* 订单详情弹窗 */}
      <OrderDetailsDialog
        order={viewingOrder}
        onClose={() => setViewingOrder(null)}
      />
    </div>
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
            <span className="text-muted-foreground block mb-1">订单金额</span>
            <div className="font-medium text-lg text-blue-600">
              {(order.point ? `¥${(order.point / 100).toFixed(2)}` : "0") +
                order.pointType}
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
