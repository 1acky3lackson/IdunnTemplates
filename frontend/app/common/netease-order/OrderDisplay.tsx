export type OrderCriteria = OrderQueryParams;

// 假设使用 axios 调用后端接口

import { useEffect, useRef, useState, useCallback } from 'react';
import { useInView } from 'react-intersection-observer'; // 推荐使用该库简化 IntersectionObserver
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import type { NeteaseOrder } from '~/api/generated';
import { useWaterfall, WaterfallProvider, type PageResponse } from '../util/waterfall-provider';
import { OrderList } from './NeteaseOrderCard';
import { Loader2 } from 'lucide-react';
import { neteaseOrderSchema, type OrderQueryParams } from './config';
import { IDUNN_API } from '~/api';
import { buildSearchString, buildSortString } from '../util/search-test-utils';

// 外部必须实现的 fetch 函数签名
export type FetchOrders = (
  page: number,
  criteria: OrderCriteria
) => Promise<PageResponse<NeteaseOrder>>;

export const fetchOrdersDefault: FetchOrders = async (page, criteria) => {

  // 2. 调用生成的 API
  // 注意：生成函数可能返回 AxiosResponse 或直接返回数据，此处假设返回 response.data 为 PageResponse
  return await IDUNN_API.apiV1CommercialNeteaseOrdersGet(
    buildSearchString(criteria, neteaseOrderSchema), // 将对象转换为后端可识别的搜索字符串
    page,                                           // 当前页码（从0开始）
    20,                                             // 每页大小（可配置）
    criteria.sort ? buildSortString(criteria.sort) : undefined // 排序字符串
  ).then((data) => data.data);
};

interface OrderDisplayProps {
  fetchOrders?: FetchOrders;                // 由调用方实现的接口函数
  initialCriteria?: OrderCriteria;          // 初始筛选条件
  pageSize?: number;                        // 可选，后端可能固定
}

// 内部负责筛选 UI 和列表渲染的子组件（使用 Waterfall 的上下文）
function OrderListWithWaterfall() {
  const { items, loading, hasMore, loadMore, search, criteria } = useWaterfall<
    NeteaseOrder,
    OrderCriteria
  >();

  // 本地状态用于绑定筛选表单
  const [localStatus, setLocalStatus] = useState<string>(criteria.internalStatus || 'all');
  const [localSearch, setLocalSearch] = useState<string>(criteria.rawSearch || '');

  // 触发搜索：将表单值合并后调用 context 的 search
  const handleSearch = () => {
    search({
      internalStatus: localStatus === 'all' ? undefined : localStatus,
    //   searchText: localSearch || undefined,
    });
  };

  // 监听回车键
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  // --- 无限滚动检测 ---
  const { ref: sentinelRef, inView } = useInView({
    threshold: 0.1,
    rootMargin: '100px', // 提前加载
  });

  useEffect(() => {
    if (inView && !loading && hasMore) {
      loadMore();
    }
  }, [inView, loading, hasMore, loadMore]);

  // 可选的缓存组件示例（如统计信息），这里暂不使用
  // const cached = useWaterfallCachedComponents();

  return (
    <div className="space-y-4">
      {/* 筛选栏 */}
      <div className="flex flex-wrap items-end gap-2">
        <div className="grid w-full max-w-sm items-center gap-1.5">
          <Input
            placeholder="搜索订单号/用户ID/产品名..."
            value={localSearch}
            onChange={(e) => setLocalSearch(e.target.value)}
            onKeyDown={handleKeyDown}
          />
        </div>
        <Select value={localStatus} onValueChange={setLocalStatus}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="全部状态" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部状态</SelectItem>
            <SelectItem value="ENTERED">已录入</SelectItem>
            <SelectItem value="PENDING">处理中</SelectItem>
            <SelectItem value="CANCELLED">已取消</SelectItem>
          </SelectContent>
        </Select>
        <Button onClick={handleSearch}>筛选</Button>
      </div>

      {/* 订单列表 */}
      <OrderList orders={items} />

      {/* 加载指示器和哨兵 */}
      <div ref={sentinelRef} className="py-4 flex justify-center">
        {loading && (
          <div className="flex items-center text-muted-foreground">
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            加载中...
          </div>
        )}
        {!hasMore && items.length > 0 && (
          <p className="text-sm text-muted-foreground">没有更多数据了</p>
        )}
      </div>

      {/* 错误提示（可从 context 中获取 error） */}
      {useWaterfall().error && (
        <div className="p-4 text-red-500 bg-red-50 rounded">
          加载失败: {useWaterfall().error?.message}
        </div>
      )}
    </div>
  );
}

// 主组件：提供 WaterfallProvider 上下文
export function OrderDisplay({
  fetchOrders = fetchOrdersDefault,
  initialCriteria = {},
  pageSize = 20, // 可根据实际覆盖
}: OrderDisplayProps) {
  // 为了确保 fetchData 符合 Waterfall 要求的签名，进行一层包装
  const wrappedFetch = useCallback(
    async (page: number, criteria: OrderCriteria): Promise<PageResponse<NeteaseOrder>> => {
      // 这里可以补充默认参数，比如后端期望的 pageSize
      const response = await fetchOrders(page, criteria);
      // 如果后端返回的格式与 PageResponse 略有不同，在此处做映射
      // 例如后端可能返回 { content, last, totalElements, number } 即可
      return response;
    },
    [fetchOrders]
  );

  return (
    <WaterfallProvider
      initialCriteria={initialCriteria}
      fetchData={wrappedFetch}
      getId={(item) => item.id} // 使用订单ID作为唯一标识
      // 可选：提供缓存组件（如统计卡片）
      // renderCachedComponents={({ criteria, total, search }) => ({
      //   stats: <StatsCard total={total} criteria={criteria} onReset={() => search({})} />
      // })}
    >
      <OrderListWithWaterfall />
    </WaterfallProvider>
  );
}