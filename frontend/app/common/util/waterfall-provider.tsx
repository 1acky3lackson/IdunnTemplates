import React, { createContext, useContext, useState, useCallback, useRef, useMemo, type ReactNode } from 'react';

// --- 类型定义 ---

// 对应你的 content 里的 metadata 等结构（可按需扩展，这里简化处理）
export interface PageResponse<T> {
    content: (T | null)[];
    last: boolean;
    totalElements: number;
    number: number;
}

// Context 暴露出的能力
interface WaterfallContextType<T, S> {
    items: T[];
    loading: boolean;
    error: Error | null;
    hasMore: boolean;
    total: number;
    page: number;
    criteria: S;
    loadMore: () => Promise<void>;
    refresh: () => Promise<void>;
    search: (newCriteria: S) => Promise<void>;
    // 存储缓存组件实例的引用
    cachedNodes: Record<string, ReactNode>;
}

interface WaterfallProviderProps<T, S> {
    children: ReactNode;
    initialCriteria: S;
    // 核心加载函数：传入页码和条件，返回标准的 PageResponse
    fetchData: (page: number, criteria: S) => Promise<PageResponse<T>>;
    // 核心去重函数：获取唯一 ID
    getId: (item: T) => string | number;
    // 新增：接收 context 的当前状态，返回一组需要持久化的组件
    renderCachedComponents?: (context: { criteria: S; total: number; search: (c: S) => Promise<void> }) => Record<string, ReactNode>;
}

// 创建 Context (初始化为 undefined，在 Hook 中做非空检查)
const WaterfallContext = createContext<WaterfallContextType<any, any> | undefined>(undefined);

// --- Provider 组件 ---

export function WaterfallProvider<T, S>({
    children,
    initialCriteria,
    fetchData,
    getId,
    renderCachedComponents
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
            if (!item) return;
            map.set(getId(item), item);
        });
        return Array.from(map.values());
    }, [getId]);

    const executeFetch = useCallback(async (
        targetPage: number,
        targetCriteria: S,
        isReset: boolean
    ) => {
        const requestId = ++requestRef.current;
        setLoading(true);
        setError(null);
        try {
            const response = await fetchData(targetPage, targetCriteria);
            if (requestId !== requestRef.current) return;

            setTotal(response.totalElements);
            setHasMore(!response.last);
            setPage(targetPage); // 修正：使用请求的目标页码

            setItems(prev => {
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
    }, [fetchData, mergeItems]);

    const loadMore = useCallback(async () => {
        if (loading || !hasMore) return;
        await executeFetch(page + 1, criteria, false);
    }, [loading, hasMore, page, criteria, executeFetch]);

    const search = useCallback(async (newCriteria: S) => {
        setCriteria(newCriteria);
        setHasMore(true);
        await executeFetch(0, newCriteria, true);
    }, [executeFetch]);

    const refresh = useCallback(async () => {
        await executeFetch(0, criteria, true);
    }, [criteria, executeFetch]);

    // --- 核心：组件持久化逻辑 ---
    // 使用 useMemo 保证只要依赖项不变，返回的 ReactNode 引用永远相同
    // 只有当搜索逻辑 search 变动时才会重新生成
    const cachedNodes = useMemo(() => {
        if (!renderCachedComponents) return {};
        return renderCachedComponents({ criteria, total, search });
    }, [renderCachedComponents, criteria, total, search]);

    React.useEffect(() => {
        executeFetch(0, initialCriteria, true);
    }, []);

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
        search,
        cachedNodes
    };

    return (
        <WaterfallContext.Provider value={value}>
            {children}
        </WaterfallContext.Provider>
    );
}

// --- Hooks ---

export function useWaterfall<T, S>() {
    const context = useContext(WaterfallContext);
    if (!context) {
        throw new Error('useWaterfall must be used within a WaterfallProvider');
    }
    return context as WaterfallContextType<T, S>;
}

/**
 * 新增 Hook：专门用于提取缓存的组件实例
 */
export function useWaterfallCachedComponents() {
    const { cachedNodes } = useWaterfall();
    return cachedNodes;
}