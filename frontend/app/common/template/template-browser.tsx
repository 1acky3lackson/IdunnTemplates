import React, { useState, useEffect, useCallback } from 'react';
import { Search, Filter, SlidersHorizontal, Loader2, ArrowDownUp } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Sheet, SheetContent, SheetTrigger, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Slider } from '@/components/ui/slider';
import { Select, SelectContent, SelectItem, SelectSeparator, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ScrollArea } from '@/components/ui/scroll-area';

// --- 假设的卡片组件 (来自你的需求) ---
import type { Template } from '~/api/generated/model';
import { useWaterfall, WaterfallProvider } from '../util/waterfall-provider';
import { searchTemplatesObjectParam, templateSearchBatchBuildSortParams, templateSearchSortBuilder, type TemplateSearchParams } from '~/api/overrides/template-search-api';
import { IDUNN_API } from '~/api';
import { TemplateCard } from './template-card';
import { TemplateSearchBar } from './template-search-bar';
import { useIntlayer } from 'react-intlayer';
import { DirectoryTree } from '../folder/directory-view';
import { Separator } from '~/components/ui/separator';

// --- 1. 筛选组件 (Filter Component) ---
// 提取出来以便在 Desktop Sidebar 和 Mobile Sheet 中复用
const TemplateFilters = () => {
    // 获取翻译内容
    const { filters } = useIntlayer("template-browser");
    
    // 假设这些是从自定义 hook 获取的
    const { criteria, search } = useWaterfall<Template, TemplateSearchParams>();

    const [localMinWidth, setLocalMinWidth] = useState(criteria.minWidth || 0);
    const [localMinLength, setLocalMinLength] = useState(criteria.minLength || 0);
    const [localMinHeight, setLocalMinHeight] = useState(criteria.minHeight || 0);

    const handleFilterChange = (updates: Partial<TemplateSearchParams>) => {
        search({ ...criteria, ...updates });
    };

    return (
        <div className="space-y-6 p-1">
            {/* 排序 */}
            <div className="flex flex-row items-center justify-between flex-wrap gap-2">
                <div className="flex items-center gap-2 text-muted-foreground flex-row">
                    <ArrowDownUp className="h-4 w-4" />
                    <Label className="font-bold text-foreground">{filters.sortBy}</Label>
                </div>
                <Select
                    value={criteria.sort || 'metadata.creationTime,desc'}
                    onValueChange={(val) => handleFilterChange({ sort: val })}
                >
                    <SelectTrigger>
                        <SelectValue placeholder={filters.sortBy} />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="metadata.creationTime,desc">{filters.sortOrders.newest}</SelectItem>
                        <SelectItem value="metadata.creationTime,asc">{filters.sortOrders.oldest}</SelectItem>
                        <SelectSeparator />
                        <SelectItem value="metadata.lockedTimestamp,desc">{filters.sortOrders.newestLocked}</SelectItem>
                        <SelectItem value="metadata.lockedTimestamp,asc">{filters.sortOrders.oldestLocked}</SelectItem>
                        <SelectSeparator />
                        <SelectItem value="metadata.height,desc">{filters.sortOrders.tallest}</SelectItem>
                        <SelectItem value="metadata.width,desc">{filters.sortOrders.widest}</SelectItem>
                        <SelectItem value="metadata.length,desc">{filters.sortOrders.longest}</SelectItem>
                        <SelectSeparator />
                        <SelectItem value="name,asc">{filters.sortOrders.nameAZ}</SelectItem>
                        <SelectItem value="name,desc">{filters.sortOrders.nameZA}</SelectItem>
                    </SelectContent>
                </Select>
            </div>

            <Separator />

            {/* 目录树区域 */}
            <div className="space-y-2">
                <Label className="font-bold">{filters.categories}</Label>
                {/* 使用 ScrollArea 确保树太长时可以滚动，而不影响外层布局 */}
                <div className="min-h-25 max-h-75 overflow-y-auto pr-2 border rounded-md bg-background/50 p-2">
                    <DirectoryTree 
                        currentPath={criteria.pathPrefix}
                        onSelect={(path) => handleFilterChange({ pathPrefix: 
                            path === "" || path === "/" ? undefined : ( 
                                path.endsWith('/') ? path : (path + '/')
                            )
                        })}
                    />
                </div>
            </div>

            <Separator />

            {/* 锁定状态 */}
            <div className="flex items-center justify-between space-x-2">
                <Label htmlFor="locked-mode" className="font-bold">{filters.lockedOnly}</Label>
                <Switch
                    id="locked-mode"
                    checked={criteria.locked === true}
                    onCheckedChange={(checked) => handleFilterChange({ locked: checked ? true : undefined })}
                />
            </div>

            <Separator />

            {/* 尺寸过滤 */}
            {[
                { label: filters.minWidth, val: localMinWidth, setLocal: setLocalMinWidth, key: 'minWidth' },
                { label: filters.minLength, val: localMinLength, setLocal: setLocalMinLength, key: 'minLength' },
                { label: filters.minHeight, val: localMinHeight, setLocal: setLocalMinHeight, key: 'minHeight' }
            ].map((item) => (
                <div className="space-y-4" key={item.key}>
                    <div className="flex justify-between">
                        <Label className="font-bold">{item.label}</Label>
                        <span className="text-xs text-muted-foreground">
                            {item.val} {filters.blocksUnit}
                        </span>
                    </div>
                    <Slider
                        value={[item.val]}
                        max={100}
                        step={1}
                        onValueChange={(val) => item.setLocal(val[0])}
                        onValueCommit={(val) => handleFilterChange({ [item.key]: val[0] })}
                    />
                </div>
            ))}
        </div>
    );
};

// --- 2. 核心视图组件 (Main View) ---
const TemplateBrowserView = () => {
    // 1. 获取翻译内容
    const { view } = useIntlayer("template-browser");

    // 2. 获取核心数据钩子
    const {
        items,
        loading,
        hasMore,
        loadMore,
        criteria, // 当前生效的搜索条件 (服务端确认后的)
        search,   // 触发实际搜索的方法
        total
    } = useWaterfall<Template, TemplateSearchParams>();

    // 3. 本地状态管理 (Local State)
    const [localParams, setLocalParams] = useState<TemplateSearchParams>(criteria);

    // 监听 criteria 的外部变化，同步回 localParams
    useEffect(() => {
        setLocalParams(prev => {
            const isSame = JSON.stringify(prev) === JSON.stringify(criteria);
            return isSame ? prev : criteria;
        });
    }, [criteria]);

    // 4. 防抖搜索逻辑 (Debounce)
    useEffect(() => {
        if (JSON.stringify(localParams) === JSON.stringify(criteria)) {
            return;
        }

        const timer = setTimeout(() => {
            search(localParams);
        }, 500);

        return () => clearTimeout(timer);
    }, [localParams, criteria, search]);

    // 处理搜索栏变更
    const handleSearchChange = (newParams: TemplateSearchParams) => {
        setLocalParams(newParams);
    };

    // 5. 滚动监听
    const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
        const { scrollTop, scrollHeight, clientHeight } = e.currentTarget;
        if (scrollHeight - scrollTop - clientHeight < 100 && !loading && hasMore) {
            loadMore();
        }
    };

    return (
        <div className="flex h-full w-full bg-background">

            {/* --- 左侧侧边栏 (LG+ 显示) --- */}
            <aside className="hidden lg:block w-72 xl:w-96 border-r bg-muted/10 shrink-0">
                <div className="h-full flex flex-col">
                    <div className="p-4 border-b h-14 flex items-center">
                        <h2 className="font-semibold text-lg">{view.filterTitle}</h2>
                    </div>
                    <ScrollArea className="flex-1 p-4">
                        <TemplateFilters />
                    </ScrollArea>
                </div>
            </aside>

            {/* --- 右侧内容区域 --- */}
            <div className="flex-1 flex flex-col h-full overflow-hidden">

                {/* 顶部搜索栏 (Sticky Header) */}
                <div className="h-14 border-b px-4 flex items-center gap-2 bg-background/95 backdrop-blur z-10 shrink-0 justify-between">

                    {/* 左侧：移动端侧边栏 Trigger */}
                    <div className="lg:hidden mr-2">
                        <Sheet>
                            <SheetTrigger asChild>
                                <Button variant="outline" size="icon">
                                    <Filter className="h-4 w-4" />
                                </Button>
                            </SheetTrigger>
                            <SheetContent side="left" className="w-[80%] sm:w-75">
                                <SheetHeader>
                                    <SheetTitle>{view.filterTitle}</SheetTitle>
                                </SheetHeader>
                                <div className="mt-4">
                                    <TemplateFilters />
                                </div>
                            </SheetContent>
                        </Sheet>
                    </div>

                    {/* 中间：集成搜索组件 */}
                    <TemplateSearchBar 
                        values={localParams}
                        onChange={handleSearchChange}
                        total={total}
                        className="flex-1"
                    />
                </div>

                {/* 瀑布流滚动区域 */}
                <div
                    className="flex-1 overflow-y-auto p-4"
                    onScroll={handleScroll}
                >
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {items.map((tpl) => (
                            <div key={tpl.id} className="break-inside-avoid">
                                <TemplateCard template={tpl} className="w-full" />
                            </div>
                        ))}
                    </div>

                    {/* Loading 状态 */}
                    {loading && (
                        <div className="w-full py-8 flex justify-center items-center text-muted-foreground gap-2">
                            <Loader2 className="h-4 w-4 animate-spin" />
                            <span>{view.loadingMore}</span>
                        </div>
                    )}

                    {/* 空状态 */}
                    {!loading && items.length === 0 && (
                        <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
                            <SlidersHorizontal className="h-12 w-12 mb-2 opacity-20" />
                            <p>{view.noTemplates}</p>
                            <Button 
                                variant="link" 
                                onClick={() => search({})} 
                                className="mt-2"
                            >
                                {view.clearFilters}
                            </Button>
                        </div>
                    )}

                    {!hasMore && items.length > 0 && (
                        <div className="w-full py-8 text-center text-sm text-muted-foreground border-t mt-4">
                            {view.endOfResults}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

IDUNN_API.apiV1PathsPathsGet

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