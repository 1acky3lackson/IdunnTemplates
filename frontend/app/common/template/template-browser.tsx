import React, { useState, useEffect, useCallback, type ReactNode } from 'react';
import { Search, Filter, SlidersHorizontal, Loader2, ArrowDownUp, LockKeyhole, FolderTree, Tags, UserPen, PencilRuler } from 'lucide-react';
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
import { useWaterfall, useWaterfallCachedComponents, WaterfallProvider } from '../util/waterfall-provider';
import { MAX_SIZE, searchTemplatesObjectParam, templateSearchBatchBuildSortParams, templateSearchSortBuilder, type TemplateSearchParams } from '~/api/overrides/template-search-api';
import { IDUNN_API } from '~/api';
import { TemplateCard } from './template-card';
import { TemplateSearchBar } from './template-search-bar';
import { useIntlayer, type IntlayerNode } from 'react-intlayer';
import { DirectoryTree } from '../folder/directory-view';
import { Separator } from '~/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '~/components/ui/tabs';
import { CreatorPicker } from '../userinfo/creator-picker';
import { HoverCard, HoverCardContent, HoverCardTrigger } from '~/components/ui/hover-card';
import { Badge } from '~/components/ui/badge';
import { toast } from 'sonner';



function formSingleMetricsDesc(
    min: number | undefined,
    max: number | undefined,
    unitMono: string | IntlayerNode,
    unitPoly: string | IntlayerNode,
    name: string | IntlayerNode
): ReactNode {
    if ((min === undefined || min <= 0) && (max === undefined || max >= MAX_SIZE)) {
        return null;
    }
    if (min === undefined || min <= 0) {
        // name + " ≥ " + max + (max === 1 ? unitMono : unitPoly);
        return <span>
            {name} ≥ {max} {max === 1 ? unitMono : unitPoly}
        </span>
    }
    if (max === undefined || max >= MAX_SIZE) {
        // name + " ≤ " + min + (min === 1 ? unitMono : unitPoly);
        return <span>
            {name} ≤ {min} {min === 1 ? unitMono : unitPoly}
        </span>

    }
    if (min === max) {
        // name + " = " + min + (min === 1 ? unitMono : unitPoly)
        return <span>
            {name} = {min} {min === 1 ? unitMono : unitPoly}
        </span>

    }
    // min + " " + (min === 1 ? unitMono : unitPoly) + " ≤ " + name + " ≤ " + max + (max === 1 ? unitMono : unitPoly)
    return <span>
        {min} {min === 1 ? unitMono : unitPoly} ≤ {name} ≤ {max} {max === 1 ? unitMono : unitPoly}
    </span>
}

function generateMetricsDescString(
    criteria: TemplateSearchParams,
    unitMono: string | IntlayerNode,
    unitPoly: string | IntlayerNode,
    and: string | IntlayerNode
): ReactNode {
    return <span>
        {
            ([
                formSingleMetricsDesc(criteria.minLength, criteria.maxLength, unitMono, unitPoly, "X"),
                formSingleMetricsDesc(criteria.minHeight, criteria.maxHeight, unitMono, unitPoly, "Y"),
                formSingleMetricsDesc(criteria.minWidth, criteria.maxWidth, unitMono, unitPoly, "Z"),
            ] as ReactNode[])
                .filter(i => i !== null)
                .map(i => <Badge>{i}</Badge>)
                .reduce((prev, curr, i) => (
                    i === 0 ? [curr] : [...(prev as ReactNode[]), <span key={`sep-${i}`} className='mx-2'>{and}</span>, curr]
                ), [] as ReactNode[])
        }
    </span>

}

// --- 1. 筛选组件 (Filter Component) ---
// 提取出来以便在 Desktop Sidebar 和 Mobile Sheet 中复用
export const TemplateFilters: React.FC<{
    defaultFilterTab:   FilterType;
    disabledCatetories: Array<FilterType>;
}> = ({
    defaultFilterTab    = 'folders',
    disabledCatetories  = [],
}) => {
    // 获取翻译内容
    const { filters } = useIntlayer("template-browser");

    // 假设这些是从自定义 hook 获取的
    const { criteria, search } = useWaterfall<Template, TemplateSearchParams>();

    const [localMinWidth, setLocalMinWidth] = useState(criteria.minWidth || 0);
    const [localMinLength, setLocalMinLength] = useState(criteria.minLength || 0);
    const [localMinHeight, setLocalMinHeight] = useState(criteria.minHeight || 0);

    const [localMaxWidth, setLocalMaxWidth] = useState(criteria.maxWidth || MAX_SIZE);
    const [localMaxHeight, setLocalMaxHeight] = useState(criteria.maxHeight || MAX_SIZE);
    const [localMaxLength, setLocalMaxLength] = useState(criteria.maxLength || MAX_SIZE);

    const [filterTabsValue, setFilterTabsValue] = useState<'folders' | 'tags' | 'creators' | 'metrics'>(defaultFilterTab || 'folders');

    const [creatorInfoList, setCreatorInfoList] = useState<Array<{ uuid?: string; name?: string }>>([]);

    const disabledSet = new Set(disabledCatetories);

    // 同步搜索状态
    useEffect(
        () => {
            setLocalMinWidth(criteria.minWidth || 0);
            setLocalMinLength(criteria.minLength || 0);
            setLocalMinHeight(criteria.minHeight || 0);
            setLocalMaxWidth(criteria.maxWidth || MAX_SIZE);
            setLocalMaxHeight(criteria.maxHeight || MAX_SIZE);
            setLocalMaxLength(criteria.maxLength || MAX_SIZE);
        },
        [criteria]
    )

    const handleFilterChange = (updates: Partial<TemplateSearchParams>) => {
        search({ ...criteria, ...updates });
    };

    const clearMetricsConditions = useCallback(() => {
        setLocalMinWidth(0);
        setLocalMinLength(0);
        setLocalMinHeight(0);
        setLocalMaxWidth(MAX_SIZE);
        setLocalMaxHeight(MAX_SIZE);
        setLocalMaxLength(MAX_SIZE);
        handleFilterChange({
            minWidth: 0,
            minLength: 0,
            minHeight: 0,
            maxWidth: MAX_SIZE,
            maxLength: MAX_SIZE,
            maxHeight: MAX_SIZE,
        });
    }, [handleFilterChange]);

    const clearCreatorConditions = useCallback(() => {
        // setFilterTabsValue('folders');
        handleFilterChange({ creatorId: "" });
    }, [handleFilterChange]);

    const clearFolderConditions = useCallback(() => {
        handleFilterChange({ pathPrefix: "" });
    }, [handleFilterChange]);

    useEffect(() => {
        IDUNN_API.apiV1UserinfoCreatorsGet().then((res) => {
            setCreatorInfoList(res.data);
        });
    }, []);


    const isFolderActive =
        criteria.pathPrefix !== undefined
        && criteria.pathPrefix
        && criteria.pathPrefix !== ''
        && criteria.pathPrefix !== '/'
        && criteria.pathPrefix.endsWith('/');

    const isMetricsActive =
        // min
        (criteria.minWidth !== undefined && criteria.minWidth > 0)
        || (criteria.minLength !== undefined && criteria.minLength > 0)
        || (criteria.minHeight !== undefined && criteria.minHeight > 0)
        // max
        || (criteria.maxWidth !== undefined && criteria.maxWidth < MAX_SIZE)
        || (criteria.maxLength !== undefined && criteria.maxLength < MAX_SIZE)
        || (criteria.maxHeight !== undefined && criteria.maxHeight < MAX_SIZE)
        ;

    const isCreatorActive =
        criteria.creatorId !== undefined
        && criteria.creatorId !== '';


    return (
        <ScrollArea className="w-full h-[calc(100vh-200px)] p-3">
            <div className="space-y-6">
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

                {/* 锁定状态 */}
                <div className="flex items-center justify-between space-x-2">
                    <div className="flex items-center gap-2 text-muted-foreground">
                        <LockKeyhole className="h-4 w-4" />
                        <Label htmlFor="locked-mode" className="font-bold text-foreground">{filters.lockedOnly}</Label>
                    </div>
                    <Switch
                        id="locked-mode"
                        checked={criteria.locked === true}
                        onCheckedChange={(checked) => handleFilterChange({ locked: checked ? true : undefined })}
                    />
                </div>

                <Separator />

                <Tabs defaultValue={defaultFilterTab} value={filterTabsValue} className="w-full" onValueChange={(val) => setFilterTabsValue(val as any)}>
                    <TabsList className="w-full mb-2 gap-2">
                        {/* 文件夹选项 */}
                        {!disabledSet.has('folders') && 
                            <TabsTrigger value="folders" className='cursor-pointer hover:translate-px hover:shadow-md transition-all'>
                                <HoverCard openDelay={10} closeDelay={100}>
                                    <HoverCardTrigger asChild>
                                        <div className="relative">
                                            <FolderTree className="h-4 w-4" />
                                            {isFolderActive && (
                                                <span className="absolute -top-1 -right-1 flex h-2 w-2">
                                                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary/40 opacity-75"></span>
                                                    <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
                                                </span>
                                            )}
                                        </div>
                                    </HoverCardTrigger>
                                    <HoverCardContent className="flex w-full flex-col gap-0.5 bg-background/50 backdrop-blur">
                                        <div className="font-semibold text-sm flex flex-row justify-between items-center">
                                            <div>{filters.tabs.folders.hoverTitle}</div>
                                            {isFolderActive && <Button size="xs" variant="destructive" onClick={clearFolderConditions} className='text-xs'>
                                                {filters.tabs.folders.clearBtn}
                                            </Button>}
                                        </div>
                                        <div className="text-sm">{filters.tabs.folders.hoverDesc}</div>
                                        {isFolderActive && (<div className="text-muted-foreground text-sm mt-1">
                                            {filters.tabs.folders.currentValue}<Badge>{criteria.pathPrefix}</Badge>
                                        </div>)}
                                    </HoverCardContent>
                                </HoverCard>
                            </TabsTrigger>
                        }
                        {/* 标签选项 */}
                        {!disabledSet.has('tags') &&
                            <TabsTrigger value="tags" className='cursor-pointer hover:translate-px hover:shadow-md transition-all'>
                                <HoverCard openDelay={10} closeDelay={100}>
                                    <HoverCardTrigger asChild>
                                        <div className="relative">
                                            <Tags className="h-4 w-4" />
                                            {false && (
                                                <span className="absolute -top-1 -right-1 flex h-2 w-2">
                                                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary/40 opacity-75"></span>
                                                    <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
                                                </span>
                                            )}
                                        </div>
                                    </HoverCardTrigger>
                                    <HoverCardContent className="flex w-full flex-col gap-0.5 bg-background/50 backdrop-blur">
                                        <div className="font-semibold text-sm">{filters.tabs.tags.hoverTitle}</div>
                                        <div className="text-sm">{filters.tabs.tags.hoverDesc}</div>
                                        {isFolderActive && (<div className="text-muted-foreground text-sm mt-1">
                                            {filters.tabs.tags.currentValue}<Badge>{criteria.pathPrefix}</Badge>
                                        </div>)}
                                    </HoverCardContent>
                                </HoverCard>
                            </TabsTrigger>
                        }
                        {/* 创作者选项 */}
                        {!disabledSet.has('creators') &&
                            <TabsTrigger value="creators" className='cursor-pointer hover:translate-px hover:shadow-md transition-all'>
                                <HoverCard openDelay={10} closeDelay={100}>
                                    <HoverCardTrigger asChild>
                                        <span className="relative">
                                            <UserPen className="h-4 w-4" />
                                            {isCreatorActive && (
                                                <span className="absolute -top-1 -right-1 flex h-2 w-2">
                                                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary/40 opacity-75"></span>
                                                    <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
                                                </span>
                                            )}
                                        </span>
                                    </HoverCardTrigger>
                                    <HoverCardContent className="flex w-full flex-col gap-0.5 bg-background/50 backdrop-blur">
                                        <div className="font-semibold text-sm flex flex-row justify-between items-center">
                                            <div>{filters.tabs.creators.hoverTitle}</div>
                                            {isCreatorActive && <Button size="xs" variant="destructive" onClick={clearCreatorConditions} className='text-xs'>
                                                {filters.tabs.creators.clearBtn}
                                            </Button>}
                                        </div>
                                        <div className="text-sm">{filters.tabs.creators.hoverDesc}</div>
                                        {isCreatorActive && (<div className="text-muted-foreground text-sm mt-1">
                                            {filters.tabs.creators.currentValue}<Badge>{creatorInfoList.filter(i => i.uuid === criteria.creatorId).at(0)?.name}</Badge>
                                        </div>)}
                                    </HoverCardContent>
                                </HoverCard>
                            </TabsTrigger>
                        }
                        {/* 尺寸选项 */}
                        {!disabledSet.has('metrics') &&
                            <TabsTrigger value="metrics" className='cursor-pointer hover:translate-px hover:shadow-md transition-all'>
                                <HoverCard openDelay={10} closeDelay={100}>
                                    <HoverCardTrigger asChild>
                                        <span className="relative">
                                            <PencilRuler className="h-4 w-4" />
                                            {isMetricsActive && (
                                                <span className="absolute -top-1 -right-1 flex h-2 w-2">
                                                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary/40 opacity-75"></span>
                                                    <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
                                                </span>
                                            )}
                                        </span>
                                    </HoverCardTrigger>
                                    <HoverCardContent className="flex w-full flex-col gap-0.5 bg-background/50 backdrop-blur">
                                        <div className="font-semibold text-sm flex flex-row justify-between items-center">
                                            <div>{filters.tabs.metrics.hoverTitle}</div>
                                            {isMetricsActive && <Button size="xs" variant="destructive" onClick={clearMetricsConditions} className='text-xs'>
                                                {filters.tabs.metrics.clearBtn}
                                            </Button>}
                                        </div>
                                        <div className="text-sm">{filters.tabs.metrics.hoverDesc}</div>
                                        {isMetricsActive && (<div className="text-muted-foreground text-sm mt-1">
                                            <span className='mr-2'>{filters.tabs.metrics.currentValue}</span>{generateMetricsDescString(
                                                criteria,
                                                filters.tabs.metrics.unitMono,
                                                filters.tabs.metrics.unitPoly,
                                                filters.tabs.metrics.logicAnd
                                            )}
                                        </div>)}
                                    </HoverCardContent>
                                </HoverCard>
                            </TabsTrigger>
                        }
                    </TabsList>
                    {/* 目录树区域 */}
                    {!disabledSet.has('folders') && 
                        <TabsContent value="folders">
                            <div className="space-y-6 p-1">
                                <div className="space-y-2">
                                    <div className="flex items-center gap-2 text-muted-foreground">
                                        {/* <FolderTree className="h-4 w-4" /> */}
                                        <Label className="font-bold text-foreground">{filters.categories}</Label>
                                    </div>
                                    {/* 使用 ScrollArea 确保树太长时可以滚动，而不影响外层布局 */}
                                    <div className="min-h-25 pr-2 border rounded-md bg-background/50 p-2">
                                        <DirectoryTree
                                            currentPath={criteria.pathPrefix}
                                            onSelect={(path) => handleFilterChange({
                                                pathPrefix:
                                                    path === "" || path === "/" ? undefined : (
                                                        path.endsWith('/') ? path : (path + '/')
                                                    )
                                            })}
                                        />
                                    </div>
                                </div>
                            </div>
                        </TabsContent>
                    }
                    {!disabledSet.has('tags') && 
                        <TabsContent value="tags">
                            <div>Yet to come...</div>
                        </TabsContent>
                    }
                    {/* 创作者过滤 */}
                    {!disabledSet.has('creators') && 
                        <TabsContent value="creators">
                            <div className="space-y-6 p-1">
                                <div className="space-y-2">
                                    <div className="flex items-center gap-2 text-muted-foreground">
                                        {/* <FolderTree className="h-4 w-4" /> */}
                                        <Label className="font-bold text-foreground">{filters.creators}</Label>
                                    </div>
                                    <div className="w-full px-2 py-4 border rounded-md bg-background/50">
                                        <CreatorPicker
                                            creators={creatorInfoList}
                                            value={criteria.creatorId}
                                            onValueChange={(val) => handleFilterChange({ creatorId: val })}
                                        // multiple={true}
                                        // title={filters.creators}
                                        />
                                    </div>
                                </div>
                            </div>
                        </TabsContent>
                    }
                    {/* 尺寸过滤 */}
                    {!disabledSet.has('metrics') && 
                        <TabsContent value="metrics">
                            <div className="space-y-6 p-1">
                                {[
                                    { label: filters.minLength, val: localMinLength, setLocal: setLocalMinLength, key: 'minLength', type: "min" },
                                    { label: filters.maxLength, val: localMaxLength, setLocal: setLocalMaxLength, key: 'maxLength', type: "max" },
                                    { label: filters.minHeight, val: localMinHeight, setLocal: setLocalMinHeight, key: 'minHeight', type: "min" },
                                    { label: filters.maxHeight, val: localMaxHeight, setLocal: setLocalMaxHeight, key: 'maxHeight', type: "max" },
                                    { label: filters.minWidth, val: localMinWidth, setLocal: setLocalMinWidth, key: 'minWidth', type: "min" },
                                    { label: filters.maxWidth, val: localMaxWidth, setLocal: setLocalMaxWidth, key: 'maxWidth', type: "max" },
                                ].map((item) => (
                                    <div className="space-y-4" key={item.key}>
                                        <div className="flex justify-between">
                                            <Label className="font-bold">{item.label}</Label>
                                            {(
                                                (item.type === "min" && item.val > 0)
                                                || (item.type === "max" && item.val < MAX_SIZE)

                                            ) ? <span className="text-xs text-muted-foreground">
                                                {item.type === "min" ? "≥" : "≤"} {item.val} {filters.blocksUnit}
                                            </span> : <span className="text-xs text-muted-foreground">{filters.notSpecified}</span>}
                                        </div>
                                        <Slider
                                            value={[item.val]}
                                            max={MAX_SIZE}
                                            step={1}
                                            onValueChange={(val) => item.setLocal(val[0])}
                                            onValueCommit={(val) => handleFilterChange({ [item.key]: val[0] })}
                                        />
                                    </div>
                                ))}
                            </div>
                        </TabsContent>
                    }
                </Tabs>
            </div>
        </ScrollArea>
    );
};

// --- 2. 核心视图组件 (Main View) ---
const TemplateBrowserView: React.FC<{
    sidebar?: boolean;
    browserId?: string;
}> = ({ sidebar = true, browserId = "default" }) => {
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
        total,

    } = useWaterfall<Template, TemplateSearchParams>();
    const cachedComponents = useWaterfallCachedComponents();
    const TemplateFilters = cachedComponents.templateFilters;

    // 当 criteria 变化时，写入 localStorage 缓存
    useEffect(() => {
        localStorage.setItem(`template-browser-criteria-${browserId}`, JSON.stringify(criteria));
    }, [criteria, browserId]);

    // 初次渲染，或ID变化，则弹窗询问用户是否加载旧的搜索设置，是的话，就加载
    useEffect(() => {
        const saved = localStorage.getItem(`template-browser-criteria-${browserId}`);
        if (saved) {
            try {
                const parsed = JSON.parse(saved) as TemplateSearchParams;
                // 只有当保存的条件与当前初始条件不一致时，才询问用户
                if (JSON.stringify(parsed) !== JSON.stringify(criteria)) {
                    toast(view.restoreFiltersTitle || "Restore previous filters?", {
                        description: view.restoreFiltersDesc || "We found your last search settings. Would you like to apply them?",
                        action: {
                            label: view.restoreFiltersAction || "Restore",
                            onClick: () => search(parsed),
                        },
                    });
                }
            } catch (e) {
                console.error("Failed to parse cached criteria", e);
            }
        }
    }, [browserId]);
    


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
    // 1. 更新 handleScroll 逻辑
    const handleScroll = useCallback(() => {
        // 获取页面滚动的关键数值
        const scrollHeight = document.documentElement.scrollHeight;
        const scrollTop = window.scrollY;
        const clientHeight = window.innerHeight;

        // 调试日志，可以根据需要保留或删除
        // console.log('Global Scroll:', scrollTop, scrollHeight, clientHeight);

        // 判断逻辑：距离底部小于 100px 且不在加载中
        if (scrollHeight - scrollTop - clientHeight < 100 && !loading && hasMore) {
            loadMore();
        }
    }, [loading, hasMore, loadMore]);

    // 2. 注册全局监听
    useEffect(() => {
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, [handleScroll]);

    return (
        <div className="flex h-full w-full bg-background">

            {/* --- 左侧侧边栏 (LG+ 显示) --- */}
            {sidebar && 
                <aside className="hidden lg:block w-72 xl:w-96 border-r bg-muted/10 shrink-0 sticky top-0 h-screen">
                    <div className="h-full flex flex-col">
                        <div className="p-4 border-b h-14 flex items-center justify-between align-middle">
                            <h2 className="font-semibold text-lg">
                                {view.filterTitle}
                            </h2>
                            <Button
                                variant="link"
                                onClick={() => {search({}); toast.info(view.clearFilters)}}
                                className=""
                            >
                                {view.clearFilters}
                            </Button>
                        </div>
                        <ScrollArea className="flex-1 p-1">
                            {TemplateFilters}
                        </ScrollArea>
                    </div>
                </aside>
            }

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
                            <SheetContent side="left" className="w-[90vw] sm:w-95" forceMount>
                                <SheetHeader>
                                    <SheetTitle>{view.filterTitle}</SheetTitle>
                                </SheetHeader>
                                <div className="mt-4">
                                    {TemplateFilters}
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
                    {/* 底部标志，监听页面滚动 */}
                    <div>
                        {/* --- 新增：加载更多按钮 --- */}
                        {/* 逻辑：还有更多内容 (hasMore)、且当前不在加载中 (!loading)、且列表不为空 */}
                        {hasMore && !loading && items.length > 0 && (
                            <div className="w-full py-6 flex justify-center">
                                <Button
                                    variant="outline"
                                    onClick={() => loadMore()}
                                    className="w-full max-w-xs font-medium transition-all hover:bg-secondary/50"
                                >
                                    {/* 这里可以使用你 i18n 配置中的词条，如果没有可以暂时写死 */}
                                    {view.loadMoreBtn || "加载更多"}
                                </Button>
                            </div>
                        )}

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
        </div>
    );
};

type FilterType = 'folders' | 'tags' | 'creators' | 'metrics';

// --- 3. 入口组件 (Wrapper) ---
export const TemplateBrowser: React.FC<{
    initialCriteria?: Partial<TemplateSearchParams>;
    defaultFilterTab?: FilterType;
    sidebar?: boolean;
    disabledCategories?: Array<FilterType>;
}> = ({ 
    initialCriteria, 
    defaultFilterTab = 'folders',
    sidebar = true,
    disabledCategories = ([] as Array<FilterType>)
}) => {
    // 定义 Fetch 函数，连接 IDUNN_API
    const fetchTemplates = useCallback(async (page: number, criteria: TemplateSearchParams) => {
        // 将 criteria 映射到 API 参数
        // apiV1TemplatesGet(pathPrefix, locked, minWidth, maxWidth, worldId, page, size, sort)
        const res = await searchTemplatesObjectParam({ ...criteria, page });

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
                    pathPrefix: '',
                    ...initialCriteria
                }}
                fetchData={fetchTemplates}
                getId={getId}
                renderCachedComponents={() => {
                    return {
                        templateFilters: <TemplateFilters defaultFilterTab={defaultFilterTab} disabledCatetories={disabledCategories}/>
                    }
                }}
            >
                <TemplateBrowserView sidebar={sidebar}/>
            </WaterfallProvider>
        </div>
    );
};

export default TemplateBrowser;