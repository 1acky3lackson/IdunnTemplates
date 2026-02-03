import React, { useState, useEffect, useCallback } from 'react';
import { Search, Filter, SlidersHorizontal, Loader2 } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Sheet, SheetContent, SheetTrigger, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Slider } from '@/components/ui/slider';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ScrollArea } from '@/components/ui/scroll-area';

// --- 假设的卡片组件 (来自你的需求) ---
import type { Template } from '~/api/generated/model';
import { useWaterfall, WaterfallProvider } from '../util/waterfall-provider';
import { searchTemplatesObjectParam, templateSearchBatchBuildSortParams, templateSearchSortBuilder, type TemplateSearchParams } from '~/api/overrides/template-search-api';
import { IDUNN_API } from '~/api';
import { TemplateCard } from './template-card';

// --- 1. 筛选组件 (Filter Component) ---
// 提取出来以便在 Desktop Sidebar 和 Mobile Sheet 中复用
const TemplateFilters = () => {
    const { criteria, search } = useWaterfall<Template, TemplateSearchParams>();

    // 本地状态用于 Slider 等控件的流畅拖动，松开时再触发 search
    const [localMinWidth, setLocalMinWidth] = useState(criteria.minWidth || 0);

    const handleFilterChange = (updates: Partial<TemplateSearchParams>) => {
        search({ ...criteria, ...updates });
    };

    return (
        <div className="space-y-6 p-1">
            {/* 排序 */}
            <div className="space-y-2">
                <Label>Sort By</Label>
                <Select
                    value={criteria.sort || 'metadata.creationTime,desc'}
                    onValueChange={(val) => handleFilterChange({ sort: val })}
                >
                    <SelectTrigger>
                        <SelectValue placeholder="Sort order" />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="metadata.creationTime,desc">Newest First</SelectItem>
                        <SelectItem value="metadata.width,desc">Widest First</SelectItem>
                        <SelectItem value="path,asc">Name (A-Z)</SelectItem>
                    </SelectContent>
                </Select>
            </div>

            {/* 锁定状态 */}
            <div className="flex items-center justify-between space-x-2">
                <Label htmlFor="locked-mode">Show Locked Only</Label>
                <Switch
                    id="locked-mode"
                    checked={criteria.locked === true}
                    onCheckedChange={(checked) => handleFilterChange({ locked: checked ? true : undefined })}
                />
            </div>

            {/* 常用分类 (通过 Path Prefix 模拟) */}
            <div className="space-y-2">
                <Label>Categories</Label>
                <div className="flex flex-col gap-1">
                    {['minecraft/village', 'minecraft/features', 'custom'].map((path) => (
                        <Button
                            key={path}
                            variant={criteria.pathPrefix?.startsWith(path) ? "secondary" : "ghost"}
                            className="justify-start h-8 px-2"
                            onClick={() => handleFilterChange({ pathPrefix: path })}
                        >
                            {path}
                        </Button>
                    ))}
                    <Button
                        variant={!criteria.pathPrefix ? "secondary" : "ghost"}
                        className="justify-start h-8 px-2"
                        onClick={() => handleFilterChange({ pathPrefix: '' })}
                    >
                        All Categories
                    </Button>
                </div>
            </div>

            {/* 尺寸过滤 (示例: 最小宽度) */}
            <div className="space-y-4">
                <div className="flex justify-between">
                    <Label>Min Width</Label>
                    <span className="text-xs text-muted-foreground">{localMinWidth} blocks</span>
                </div>
                <Slider
                    value={[localMinWidth]}
                    max={100}
                    step={1}
                    onValueChange={(val) => setLocalMinWidth(val[0])}
                    onValueCommit={(val) => handleFilterChange({ minWidth: val[0] })}
                />
            </div>
        </div>
    );
};

// --- 2. 核心视图组件 (Main View) ---
const TemplateBrowserView = () => {
    const {
        items,
        loading,
        hasMore,
        loadMore,
        criteria,
        search,
        total
    } = useWaterfall<Template, TemplateSearchParams>();

    // 搜索框防抖逻辑
    const [searchTerm, setSearchTerm] = useState(criteria.pathPrefix || '');

    useEffect(() => {
        // 只有当输入值与当前 criteria 不一致时才触发防抖搜索
        if (searchTerm !== (criteria.pathPrefix || '')) {
            const timer = setTimeout(() => {
                search({ ...criteria, pathPrefix: searchTerm });
            }, 500); // 500ms 防抖
            return () => clearTimeout(timer);
        }
    }, [searchTerm, criteria, search]);

    // 滚动监听 (简单的到底加载)
    // 实际项目中推荐使用 IntersectionObserver 监听底部的一个 div
    const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
        const { scrollTop, scrollHeight, clientHeight } = e.currentTarget;
        // 距离底部 100px 时加载
        if (scrollHeight - scrollTop - clientHeight < 100 && !loading && hasMore) {
            loadMore();
        }
    };

    return (
        <div className="flex h-full w-full bg-background">

            {/* --- 左侧侧边栏 (LG+ 显示) --- */}
            <aside className="hidden lg:block w-64 border-r bg-muted/10 flex-shrink-0">
                <div className="h-full flex flex-col">
                    <div className="p-4 border-b h-14 flex items-center">
                        <h2 className="font-semibold text-lg">Filters</h2>
                    </div>
                    <ScrollArea className="flex-1 p-4">
                        <TemplateFilters />
                    </ScrollArea>
                </div>
            </aside>

            {/* --- 右侧内容区域 --- */}
            <main className="flex-1 flex flex-col h-full overflow-hidden">

                {/* 顶部搜索栏 (Sticky Header) */}
                <header className="h-14 border-b px-4 flex items-center gap-2 bg-background/95 backdrop-blur z-10 flex-shrink-0">

                    {/* 移动端筛选按钮 */}
                    <div className="lg:hidden">
                        <Sheet>
                            <SheetTrigger asChild>
                                <Button variant="outline" size="icon">
                                    <Filter className="h-4 w-4" />
                                </Button>
                            </SheetTrigger>
                            <SheetContent side="left" className="w-[80%] sm:w-[300px]">
                                <SheetHeader>
                                    <SheetTitle>Filters</SheetTitle>
                                </SheetHeader>
                                <div className="mt-4">
                                    <TemplateFilters />
                                </div>
                            </SheetContent>
                        </Sheet>
                    </div>

                    {/* 搜索输入框 */}
                    <div className="relative flex-1 max-w-md">
                        <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                        <Input
                            type="search"
                            placeholder="Search templates..."
                            className="pl-8 bg-muted/50"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>

                    <div className="text-sm text-muted-foreground hidden sm:block">
                        {total} results
                    </div>
                </header>

                {/* 瀑布流滚动区域 */}
                <div
                    className="flex-1 overflow-y-auto p-4"
                    onScroll={handleScroll}
                >
                    {/* 使用 CSS Columns 实现两列瀑布流 */}
                    {/* gap-4 对应 tailwind 的间距，columns-2 强制两列 */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {items.map((tpl) => (
                            <div key={tpl.id} className="break-inside-avoid mb-4">
                                <TemplateCard template={tpl} className="w-full" />
                            </div>
                        ))}
                    </div>

                    {/* Loading 状态指示器 */}
                    {loading && (
                        <div className="w-full py-8 flex justify-center items-center text-muted-foreground gap-2">
                            <Loader2 className="h-4 w-4 animate-spin" />
                            <span>Loading more...</span>
                        </div>
                    )}

                    {/* 空状态 / 底部 */}
                    {!loading && items.length === 0 && (
                        <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
                            <SlidersHorizontal className="h-12 w-12 mb-2 opacity-20" />
                            <p>No templates found.</p>
                        </div>
                    )}

                    {!hasMore && items.length > 0 && (
                        <div className="w-full py-8 text-center text-sm text-muted-foreground border-t mt-4">
                            End of results
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
};

// --- 3. 入口组件 (Wrapper) ---
export const TemplateBrowser = () => {
    // 定义 Fetch 函数，连接 IDUNN_API
    const fetchTemplates = useCallback(async (page: number, criteria: TemplateSearchParams) => {
        // 将 criteria 映射到 API 参数
        // apiV1TemplatesGet(pathPrefix, locked, minWidth, maxWidth, worldId, page, size, sort)
        const res = await searchTemplatesObjectParam(criteria);

        // 假设 IDUNN_API 返回的是 AxiosResponse，数据在 data 中
        // 如果直接返回 data，请去掉 .data
        return res.data;
    }, []);

    // 定义如何获取 ID
    const getId = useCallback((t: Template) => t.id, []);

    return (
        <div className="w-full">
            <WaterfallProvider<Template, TemplateSearchParams>
                initialCriteria={{
                    sort: templateSearchSortBuilder('metadata.creationTime', 'desc'),
                    pathPrefix: ''
                }}
                fetchData={fetchTemplates}
                getId={getId}
            >
                <TemplateBrowserView />
            </WaterfallProvider>
        </div>
    );
};

export default TemplateBrowser;