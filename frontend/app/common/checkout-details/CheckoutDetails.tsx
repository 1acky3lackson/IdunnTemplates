import React, { useState } from 'react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

// 引入通用表格组件和 API 客户端
import { IDUNN_API } from '~/api'; 
import type { CheckoutDetailDto } from '~/api/generated';
import { GenericCrudTable, type PageResponse } from '~/common/generic-crud-table/generic-crud-table';

// ---------- 辅助工具 ----------

// 状态标签颜色映射 (根据你实际的枚举值进行调整)
const statusColorMap: Record<string, string> = {
  'CREATED': 'bg-gray-500 hover:bg-gray-600',
  'CONFIRMED': 'bg-blue-500 hover:bg-blue-600',
  'RELEASED': 'bg-green-500 hover:bg-green-600',
  'FINISHED': 'bg-emerald-600 hover:bg-emerald-700',
  'REFUNDED': 'bg-red-500 hover:bg-red-600',
};

// 角色标签颜色映射
const roleColorMap: Record<string, string> = {
  'CREATOR': 'bg-purple-100 text-purple-800 border-purple-200',
  'PLATFORM': 'bg-blue-100 text-blue-800 border-blue-200',
  'AGENCY': 'bg-orange-100 text-orange-800 border-orange-200',
};

// 时间格式化工具
const formatTime = (ms?: number | null) => {
  if (!ms) return '-';
  return new Date(ms).toLocaleString();
};

// 金额格式化工具 (假设后端传来的直接是元或者具体数值)
const formatMoney = (amount?: number | null) => {
  if (amount === undefined || amount === null) return '-';
  return `¥${amount.toFixed(4)}`;
};

// ---------- 主页面组件 ----------

export default function CheckoutDetails({
    forceSearch,
    pageSize = 20
}: { 
    forceSearch?: Record<string, string>,
    pageSize?: number
}) {
  // 详情弹窗的状态管理
  const [viewingDetail, setViewingDetail] = useState<CheckoutDetailDto | null>(null);

  // 适配 GenericCrudTable 的接口
  const fetchCheckoutDetails = async (page: number, size: number, search: string, sort: string) => {
    // 调用由 OpenAPI 生成的 API 客户端
    const response = await IDUNN_API.listBalanceCheckoutDetails(
      search || undefined,
      page,
      size,
      sort || undefined
    );
    // 假设 Axios 返回的结构中，数据在 data 属性里
    return response.data as PageResponse<CheckoutDetailDto>;
  };

  return (
    <div className="container mx-auto p-4 space-y-4">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">结账单列表</h1>
      </div>

      <GenericCrudTable<CheckoutDetailDto>
        getRowId={(row) => row.id!}
        list={fetchCheckoutDetails}
        pageSize={pageSize}
        
        forcedSearchValues={forceSearch}

        // 配置搜索栏字段
        searchFields={[
          { key: 'username', label: '用户名', fuzzy: true },
          { key: 'orderId', label: '订单ID', fuzzy: false },
          { key: 'withdrawId', label: '提现ID', fuzzy: false },
          { key: 'status', label: '状态 (如 CREATED)', fuzzy: false },
          { key: 'role', label: '角色 (如 CREATOR)', fuzzy: false },
        ]}
        
        // 配置表格列
        schema={{
          'id': { 
            title: 'ID', 
            sortable: true,
          },
          'orderId': { 
            title: '订单ID',
            sortable: true,
            render: (val) => val ? <span className="font-mono">{val}</span> : '-'
          },
          'username': { 
            title: '用户名',
            filterable: true,
            render: (val) => <span className="font-medium">{val || '-'}</span>
          },
          'role': { 
            title: '角色',
            filterable: true,
            render: (val: string) => val ? (
              <span className={`px-2 py-1 rounded-md text-xs border ${roleColorMap[val] || 'bg-gray-100 text-gray-800'}`}>
                {val}
              </span>
            ) : '-'
          },
          'ratio': {
            title: '分成比例',
            sortable: true,
            render: (val: number) => val !== undefined ? `${(val * 100).toFixed(2)}%` : '-'
          },
          'actualProfit': {
            title: '实际收益',
            sortable: true,
            render: (val) => <span className="text-blue-600 font-semibold">{formatMoney(val)}</span>
          },
          'status': {
            title: '状态',
            filterable: true,
            render: (val: string) => val ? (
              <Badge className={statusColorMap[val] || 'bg-gray-400'}>
                {val}
              </Badge>
            ) : '-'
          },
          'createTimeMs': {
            title: '创建时间',
            sortable: true,
            render: (val) => formatTime(val)
          }
        }}

        // 注入自定义操作列：查看详情
        rowActions={(row) => (
          <Button variant="outline" size="sm" onClick={() => setViewingDetail(row)}>
            查看详情
          </Button>
        )}
      />

      {/* 结算单详情弹窗 */}
      <CheckoutDetailDialog 
        detail={viewingDetail} 
        onClose={() => setViewingDetail(null)} 
      />
    </div>
  );
}

// ---------- 详情弹窗组件 ----------

function CheckoutDetailDialog({ 
  detail, 
  onClose 
}: { 
  detail: CheckoutDetailDto | null; 
  onClose: () => void;
}) {
  if (!detail) return null;

  return (
    <Dialog open={!!detail} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>结账单详情 - #{detail.id}</DialogTitle>
        </DialogHeader>
        
        <div className="grid grid-cols-2 gap-y-6 gap-x-8 py-4 text-sm">
          {/* 基础信息 */}
          <div className="space-y-4 col-span-1 border-r pr-4">
            <h4 className="font-semibold text-base border-b pb-2">基础信息</h4>
            <div>
              <span className="text-muted-foreground block mb-1">系统状态</span>
              <Badge className={statusColorMap[detail.status!] || 'bg-gray-400'}>
                {detail.status || '-'}
              </Badge>
            </div>
            <div>
              <span className="text-muted-foreground block mb-1">关联订单 ID</span>
              <div className="font-medium font-mono">{detail.orderId || '-'}</div>
            </div>
            <div>
              <span className="text-muted-foreground block mb-1">目标用户 / 角色</span>
              <div className="font-medium">
                {detail.username || '-'} 
                {detail.role && <span className="text-xs text-muted-foreground ml-2">({detail.role})</span>}
              </div>
            </div>
          </div>

          {/* 财务数据 */}
          <div className="space-y-4 col-span-1">
            <h4 className="font-semibold text-base border-b pb-2">财务数据</h4>
            <div>
              <span className="text-muted-foreground block mb-1">分成比例</span>
              <div className="font-medium">{detail.ratio !== undefined ? `${(detail.ratio * 100).toFixed(2)}%` : '-'}</div>
            </div>
            <div>
              <span className="text-muted-foreground block mb-1">应结利润 (Net Profit)</span>
              <div className="font-medium">{formatMoney(detail.netProfit)}</div>
            </div>
            <div>
              <span className="text-muted-foreground block mb-1">实际收益 (Actual Profit)</span>
              <div className="font-medium text-lg text-blue-600">{formatMoney(detail.actualProfit)}</div>
            </div>
          </div>

          {/* 时间流转记录 */}
          <div className="col-span-2 space-y-4 mt-2">
            <h4 className="font-semibold text-base border-b pb-2">时间流转</h4>
            <div className="grid grid-cols-3 gap-4 bg-muted/50 p-4 rounded-lg">
              <div>
                <span className="text-muted-foreground block mb-1 text-xs">创建时间</span>
                <div className="font-medium text-xs">{formatTime(detail.createTimeMs)}</div>
              </div>
              <div>
                <span className="text-muted-foreground block mb-1 text-xs">确认时间</span>
                <div className="font-medium text-xs">{formatTime(detail.confirmTimeMs)}</div>
              </div>
              <div>
                <span className="text-muted-foreground block mb-1 text-xs">释放时间</span>
                <div className="font-medium text-xs">{formatTime(detail.releaseTimeMs)}</div>
              </div>
              <div>
                <span className="text-muted-foreground block mb-1 text-xs">完成时间</span>
                <div className="font-medium text-xs">{formatTime(detail.finishTimeMs)}</div>
              </div>
              <div>
                <span className="text-muted-foreground block mb-1 text-xs">退款时间</span>
                <div className="font-medium text-xs">{formatTime(detail.refundTimeMs)}</div>
              </div>
            </div>
          </div>

        </div>
      </DialogContent>
    </Dialog>
  );
}