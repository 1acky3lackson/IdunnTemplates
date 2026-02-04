import React, { createContext, useContext, useState, useCallback, useRef, type ReactNode } from 'react';

// --- 类型定义 ---

// 对应你的 content 里的 metadata 等结构（可按需扩展，这里简化处理）
export interface PageResponse<T> {
    content: (T | null)[]; // 明确 content 可能包含 null
    last: boolean;
    totalElements: number;
    number: number; // 当前页码
    // 其他字段如 pageable, sort 等在前端逻辑中通常非必须，可按需添加
}

// Context 暴露出的能力
interface WaterfallContextType<T, S> {
    items: T[];           // 经过清洗、去重后的最终列表
    loading: boolean;     // 是否正在加载
    error: Error | null;  // 错误状态
    hasMore: boolean;     // 是否还有更多数据
    total: number;        // 总条数
    page: number;         // 当前页码
    criteria: S;          // 当前搜索条件

    // 动作
    loadMore: () => Promise<void>;            // 加载下一页
    refresh: () => Promise<void>;             // 刷新当前条件（重置数据）
    search: (newCriteria: S) => Promise<void>;// 改变搜索条件并搜索
}

// 组件 Props
interface WaterfallProviderProps<T, S> {
    children: ReactNode;
    initialCriteria: S;
    // 核心加载函数：传入页码和条件，返回标准的 PageResponse
    fetchData: (page: number, criteria: S) => Promise<PageResponse<T>>;
    // 核心去重函数：获取唯一 ID
    getId: (item: T) => string | number;
}

// 创建 Context (初始化为 undefined，在 Hook 中做非空检查)
const WaterfallContext = createContext<WaterfallContextType<any, any> | undefined>(undefined);

// --- Provider 组件 ---

export function WaterfallProvider<T, S>({
    children,
    initialCriteria,
    fetchData,
    getId
}: WaterfallProviderProps<T, S>) {

    // 状态维护
    const [items, setItems] = useState<T[]>([]);
    const [page, setPage] = useState<number>(0);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<Error | null>(null);
    const [hasMore, setHasMore] = useState<boolean>(true);
    const [total, setTotal] = useState<number>(0);

    // 搜索条件
    const [criteria, setCriteria] = useState<S>(initialCriteria);

    // 引用，用于处理竞态条件（比如快速切换搜索条件）
    const requestRef = useRef<number>(0);

    // 核心数据合并逻辑：利用 Map 特性实现 O(n) 的去重和原位覆盖
    const mergeItems = useCallback((currentItems: T[], newItems: (T | null)[]) => {
        // 1. 创建 Map，保持原有顺序
        const map = new Map<string | number, T>();
        currentItems.forEach(item => map.set(getId(item), item));

        // 2. 遍历新数据
        newItems.forEach(item => {
            if (!item) return; // 过滤 null
            const id = getId(item);
            // Map.set 特性：Key 存在则更新 Value（覆盖），Key 不存在则追加到末尾
            // 这完美符合“ID去重且覆盖”以及“瀑布流追加”的需求
            map.set(id, item);
        });

        return Array.from(map.values());
    }, [getId]);

    // 通用请求处理函数
    const executeFetch = useCallback(async (
        targetPage: number,
        targetCriteria: S,
        isReset: boolean
    ) => {
        const requestId = ++requestRef.current;
        setLoading(true);
        setError(null);
        console.log(`Fetching page ${targetPage} with criteria`, targetCriteria);
        try {
            const response = await fetchData(targetPage, targetCriteria);

            // 竞态检查：如果这个请求回来时，已经发起了新的请求（requestId变了），则丢弃结果
            if (requestId !== requestRef.current) return;

            setTotal(() => response.totalElements);
            setHasMore(() => !response.last);
            setPage(() => response.number); // 使用服务端返回的页码以防万一

            setItems(prev => {
                // 如果是重置/搜索，基准数据是空数组；如果是加载更多，基准是 prev
                const baseItems = isReset ? [] : prev;
                return mergeItems(baseItems, response.content);
            });

        } catch (err) {
            if (requestId === requestRef.current) {
                setError(err instanceof Error ? err : new Error('Unknown error'));
            }
        } finally {
            if (requestId === requestRef.current) {
                setLoading(false);
            }
        }
    }, [fetchData, mergeItems, setPage, setHasMore, setTotal, setItems, setError, setLoading]);

    // 1. 加载下一页
    const loadMore = useCallback(async () => {
        if (loading || !hasMore) return;
        await executeFetch(page + 1, criteria, false);
    }, [loading, hasMore, page, criteria, executeFetch]);

    // 2. 改变搜索条件（重置页码为0，清空数据）
    const search = useCallback(async (newCriteria: S) => {
        setCriteria(newCriteria);
        // 重置状态
        setHasMore(true);
        // 立即发起请求，page=0, isReset=true
        await executeFetch(0, newCriteria, true);
    }, [executeFetch]);

    // 3. 刷新当前列表（保留条件，但数据重抓，通常用于下拉刷新）
    const refresh = useCallback(async () => {
        await executeFetch(0, criteria, true);
    }, [criteria, executeFetch]);

    // 首次挂载是否自动加载？
    // 通常瀑布流组件挂载时需要自动加载第一页。
    // 可以在这里使用 useEffect，或者让 UI 层决定何时调用 search/refresh。
    // 这里做一个简单的初始化加载：
    React.useEffect(() => {
        // 仅在组件挂载且没有数据时触发一次，或者由父组件控制。
        // 为了更可控，这里建议让使用者在 useEffect 中调用 search，或者在这里加一个 init Ref。
        // 这里采取最常见模式：挂载即加载第一页。
        executeFetch(0, initialCriteria, true);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []); // 依赖项空，只由 initialCriteria 决定初始

    const value: WaterfallContextType<T, S> = {
        items,
        loading,
        error,
        hasMore,
        total,
        page,
        criteria,
        loadMore,
        refresh,
        search
    };

    return (
        <WaterfallContext.Provider value={value}>
            {children}
        </WaterfallContext.Provider>
    );
}

// --- Hook ---

export function useWaterfall<T, S>() {
    const context = useContext(WaterfallContext);
    if (!context) {
        throw new Error('useWaterfall must be used within a WaterfallProvider');
    }
    // 强制转换类型，因为 Context 内部存储的是泛型
    return context as WaterfallContextType<T, S>;
}