import React from "react";
import { Badge } from "@/components/ui/badge";
import { IDUNN_API } from "~/api";
import type { BalanceInfo, UserBalanceRecordDto } from "~/api/generated";
import { GenericCrudTable } from "../generic-crud-table/generic-crud-table";

// ---------- 共享 Props 定义 ----------
export interface CommonListProps {
  forceSearch?: Record<string, string>;
  pageSize?: number;
  uid?: string;
}

// ---------- 辅助格式化工具 ----------
const formatMoney = (amount?: number | null) => {
  if (amount === undefined || amount === null) return "-";
  return `${Math.round(amount * 100)}`; // 将数值 *100 显示为虚拟点数（不需要带单位）
};

const formatTime = (ms?: number | null) => {
  if (!ms) return "-";
  return new Date(ms).toLocaleString();
};

const TransactionTypeBadge = ({ type }: { type: string }) => {
  switch (type) {
    case "EXPENSE":
      return <Badge className="bg-orange-500">支出</Badge>;
    case "INCOME":
      return <Badge className="bg-emerald-500">收入</Badge>;
    default:
      return <Badge className="bg-gray-400">{type}</Badge>;
  }
};

// ==========================================
// 1. 用户账户余额列表组件
// ==========================================
export function UserBalanceList({
  forceSearch = {},
  pageSize = 20,
  uid = "ublc",
}: CommonListProps) {
  const fetchBalances = async (
    page: number,
    size: number,
    search: string,
    sort: string,
  ) => {
    const response = await IDUNN_API.listUserBalances(
      search || undefined,
      page,
      size,
      sort || undefined,
    );
    // @ts-ignore (视实际生成的类型签名而定，通常 .data 包含 PageResponse 结构)
    return response.data as PageResponse<BalanceInfo>;
  };

  return (
    <GenericCrudTable<BalanceInfo>
      uid={uid}
      // BalanceInfo 没有 id 字段，username 在系统中是唯一的
      getRowId={(row) => row.username!}
      list={fetchBalances}
      pageSize={pageSize}
      forcedSearchValues={forceSearch}
      searchFields={[{ key: "username", label: "用户名", fuzzy: true }]}
      schema={{
        username: {
          title: "用户名",
          sortable: true,
          filterable: true,
          render: (val) => <span className="font-bold">{val}</span>,
        },
        availableBalance: {
          title: "账户虚拟点数",
          sortable: true,
          render: (val) => (
            <span className="text-emerald-600 font-semibold">
              {formatMoney(val)}
            </span>
          ),
        },
        frozenBalance: {
          title: "保留中",
          render: (val) => (
            <span className="text-yellow-500">{formatMoney(val)}</span>
          ),
        },
        pendingBalance: {
          title: "待网易提现（预估）",
          render: (val) => (
            <span className="text-gray-600">{formatMoney(val)}</span>
          ),
        },
      }}
    />
  );
}

// ==========================================
// 2. 用户交易流水记录组件
// ==========================================
export function UserTransactionRecordList({
  forceSearch = {},
  pageSize = 20,
  uid = "trsctn",
}: CommonListProps) {
  const fetchRecords = async (
    page: number,
    size: number,
    search: string,
    sort: string,
  ) => {
    const response = await IDUNN_API.listBalanceRecords(
      search || undefined,
      page,
      size,
      sort || undefined,
    );
    // @ts-ignore
    return response.data as PageResponse<UserBalanceRecordDto>;
  };

  return (
    <GenericCrudTable<UserBalanceRecordDto>
      uid={uid}
      getRowId={(row) => row.id!}
      list={fetchRecords}
      pageSize={pageSize}
      forcedSearchValues={forceSearch}
      searchFields={[
        { key: "username", label: "用户名", fuzzy: true },
        { key: "type", label: "流水类型 (如 PROFIT)", fuzzy: false },
        { key: "relatedId", label: "关联ID", fuzzy: false },
      ]}
      schema={{
        id: {
          title: "流水号",
          sortable: true,
        },
        username: {
          title: "用户名",
          filterable: true,
          render: (val) => <span className="font-medium">{val}</span>,
        },
        type: {
          title: "类型",
          filterable: true,
          render: (val: string) => <TransactionTypeBadge type={val} />,
        },
        amount: {
          title: "变动虚拟点数",
          sortable: true,
          render: (val: number, row) => {
            const isIncome = row.type === "INCOME";
            return (
              <span
                className={`font-bold ${val === 0 ? "text-gray-800" : isIncome ? "text-green-600" : "text-red-500"}`}
              >
                {isIncome ? "+" : "-"}
                {formatMoney(val)}
              </span>
            );
          },
        },
        balanceBefore: {
          title: "变动前虚拟点数",
          render: (val) => (
            <span className="text-muted-foreground text-sm">
              {formatMoney(val)}
            </span>
          ),
        },
        balanceAfter: {
          title: "变动后虚拟点数",
          render: (val) => (
            <span className="text-muted-foreground text-sm">
              {formatMoney(val)}
            </span>
          ),
        },
        relatedId: {
          title: "关联业务ID",
          filterable: true,
          render: (val) =>
            val ? (
              <span className="font-mono text-xs bg-gray-100 p-1 rounded">
                {val}
              </span>
            ) : (
              "-"
            ),
        },
        description: {
          title: "备注说明",
          render: (val) => (
            <span
              className="text-gray-600 text-sm max-w-50 truncate block"
              title={val}
            >
              {val || "-"}
            </span>
          ),
        },
        createTimeMs: {
          title: "记录时间",
          sortable: true,
          render: (val) => formatTime(val),
        },
      }}
    />
  );
}
