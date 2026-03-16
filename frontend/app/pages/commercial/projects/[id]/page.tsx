// ~/common/project/ProjectDetail.tsx
import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { Link } from 'react-router';
import {
    Card,
    CardContent,
    CardHeader,
    CardTitle,
} from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DialogFooter,
} from '@/components/ui/dialog';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useInView } from 'react-intersection-observer';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer,
} from 'recharts';

import { IDUNN_API } from '~/api';
import type { Project, NeteaseProduct } from '~/api/generated';
import { NeteaseProductManagerPage, type ProductApi, type ProductPageResponse } from '~/common/netease-product/NeteaseProductManager';
import { productSchema } from '~/common/netease-product/NeteaseProductManager';
import { WaterfallProvider, useWaterfall } from '~/common/util/waterfall-provider';
import { buildSearchString, buildSortString } from '~/common/util/search-test-utils';
import { deepNullToUndefined } from '~/common/util/null-to-undefined';
import { ProjectContributions } from '~/common/project/ProjectContributions';
import type { Route } from './+types/page';
import { PlusCircle, Search, Package, CheckCircle2 } from 'lucide-react';

// ---------- 类型定义 ----------
interface ProjectDetailProps {
    projectApi: {
        getProject: (id: number) => Promise<Project>;
    };
    productApi: ProductApi;
}

export function clientLoader({ params }: Route.ClientLoaderArgs) {
    return { id: Number.parseInt(params.id) };
}

const defaultProjectAPI: ProjectDetailProps['projectApi'] = {
    getProject: async (id: number) => {
        const response = await IDUNN_API.apiV1CommercialProjectsIdGet(id);
        return response.data;
    }
};

const convertToNeteaseProduct = (raw: any): NeteaseProduct => ({
    ...raw,
    project: raw.project,
});

const defaultProductAPI: ProductApi = {
    fetchProducts: async (page, criteria) => {
        const search = buildSearchString(criteria, productSchema);
        const sort = criteria.sort ? buildSortString(criteria.sort) : undefined;
        const response = await IDUNN_API.apiV1CommercialNeteaseProductsGet(
            search,
            page,
            20,
            sort
        );
        const rawPage = deepNullToUndefined(response.data);
        return {
            ...rawPage,
            content: rawPage.content.map((item: any) => convertToNeteaseProduct(item)),
        } as ProductPageResponse;
    },

    updateProduct: async (id, data) => {
        const response = await IDUNN_API.apiV1CommercialNeteaseProductsIdPut(id, data);
        return convertToNeteaseProduct(deepNullToUndefined(response.data)) as any;
    },
};

// ---------- 主组件 ----------
export default function ProjectDetail({
    projectApi = defaultProjectAPI,
    productApi = defaultProductAPI,
    loaderData
}: ProjectDetailProps & Route.ComponentProps) {
    const projectId = loaderData.id;
    const [project, setProject] = useState<Project | null>(null);
    const [parentProject, setParentProject] = useState<Project | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [refreshKey, setRefreshKey] = useState(0);

    useEffect(() => {
        if (!projectId) return;
        setLoading(true);
        projectApi.getProject(projectId)
            .then((data) => {
                setProject(data);
                if (data.parentProjectId) {
                    return projectApi.getProject(data.parentProjectId);
                }
            })
            .then((parent) => parent && setParentProject(parent))
            .catch((err) => setError(err.message || '加载项目失败'))
            .finally(() => setLoading(false));
    }, [projectId, projectApi, refreshKey]);

    const fetchProductsForProject = useCallback(async (page: number, criteria: any) => {
        return productApi.fetchProducts(page, { ...criteria, 'project.id': String(projectId) });
    }, [productApi, projectId]);

    if (loading) return <DetailSkeleton />;

    return (
        <div className="container mx-auto p-6 space-y-6">
            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <div>
                        <CardTitle className="text-2xl">{project?.displayName}</CardTitle>
                        <p className="text-sm text-muted-foreground mt-1">{project?.description || '暂无描述'}</p>
                    </div>
                    <div className="flex gap-2">
                        <Badge variant="secondary">{project?.kind}</Badge>
                        <LinkProductDialog 
                            projectId={projectId} 
                            productApi={productApi} 
                            onSuccess={() => setRefreshKey(k => k + 1)} 
                        />
                    </div>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <InfoItem label="项目ID" value={project?.id} />
                        <InfoItem label="路径" value={project?.pathName} />
                        <InfoItem label="创建时间" value={formatTime(project?.createTimeMs)} />
                        <InfoItem label="父项目" value={
                            project?.parentProjectId 
                                ? <Link to={`/commercial/projects/${project.parentProjectId}`} className="text-blue-500 hover:underline">
                                    {parentProject?.displayName || project.parentProjectId}
                                  </Link>
                                : '无'
                        } />
                    </div>
                </CardContent>
            </Card>

            <ProjectContributions projectId={projectId}/>

            <WaterfallProvider
                key={refreshKey}
                initialCriteria={{}}
                fetchData={fetchProductsForProject as any}
                getId={(item: NeteaseProduct) => item.id}
            >
                <ProjectProductsStats productApi={productApi} projectId={projectId} />
            </WaterfallProvider>
        </div>
    );
}

// ---------- 关联产品对话框 ----------
function LinkProductDialog({ projectId, productApi, onSuccess }: { projectId: number, productApi: ProductApi, onSuccess: () => void }) {
    const [open, setOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [items, setItems] = useState<NeteaseProduct[]>([]);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [loading, setLoading] = useState(false);
    const [selected, setSelected] = useState<NeteaseProduct | null>(null);

    const { ref, inView } = useInView();

    const loadProducts = useCallback(async (isNewSearch = false) => {
        if (loading || (!hasMore && !isNewSearch)) return;
        setLoading(true);
        try {
            const currentPage = isNewSearch ? 0 : page;
            const res = await productApi.fetchProducts(currentPage, { itemName: { value: searchQuery, fuzzy: true } });
            setItems(prev => ((isNewSearch ? res.content : [...prev, ...res.content]) as any));
            setPage(currentPage + 1);
            setHasMore(!res.last);
        } finally {
            setLoading(false);
        }
    }, [searchQuery, page, hasMore, loading, productApi]);

    useEffect(() => {
        if (open) loadProducts(true);
    }, [open]);

    useEffect(() => {
        if (inView && hasMore && !loading) loadProducts();
    }, [inView]);

    const handleConfirm = async () => {
        if (!selected) return;
        await productApi.updateProduct(selected.id, { projectId });
        setOpen(false);
        onSuccess();
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button variant="default" size="sm"><PlusCircle className="w-4 h-4 mr-2" />关联产品</Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[600px]">
                <DialogHeader><DialogTitle>关联已有产品至本项目</DialogTitle></DialogHeader>
                <div className="relative mt-2">
                    <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                    <Input 
                        placeholder="输入产品名称搜索..." 
                        className="pl-8" 
                        value={searchQuery}
                        onChange={(e) => { setSearchQuery(e.target.value); loadProducts(true); }}
                    />
                </div>
                <ScrollArea className="h-[300px] mt-4 border rounded-md p-2">
                    {items.map(item => (
                        <div 
                            key={item.id} 
                            onClick={() => setSelected(item)}
                            className={`p-3 mb-2 rounded-lg border cursor-pointer transition-colors flex justify-between items-center ${selected?.id === item.id ? 'bg-primary/10 border-primary' : 'hover:bg-muted'}`}
                        >
                            <div>
                                <div className="font-medium">{item.itemName}</div>
                                <div className="text-xs text-muted-foreground">ID: {item.id} | ItemID: {item.itemId}</div>
                            </div>
                            {selected?.id === item.id && <CheckCircle2 className="w-5 h-5 text-primary" />}
                        </div>
                    ))}
                    <div ref={ref} className="h-10 flex items-center justify-center">
                        {loading && <Skeleton className="h-4 w-full" />}
                    </div>
                </ScrollArea>
                {selected && (
                    <Card className="bg-muted/50 border-none shadow-none mt-2">
                        <CardContent className="p-3 text-sm grid grid-cols-2 gap-2">
                            <div><span className="text-muted-foreground">当前项目ID:</span> {selected?.project?.id ?? '空'}</div>
                            <div><span className="text-muted-foreground">价格:</span> {selected.price} {selected.priceType}</div>
                        </CardContent>
                    </Card>
                )}
                <DialogFooter>
                    <Button variant="outline" onClick={() => setOpen(false)}>取消</Button>
                    <Button disabled={!selected} onClick={handleConfirm}>确认关联</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

// ---------- 统计显示组件 ----------
function ProjectProductsStats({ productApi, projectId }: { productApi: ProductApi; projectId: number }) {
    const { items, loading } = useWaterfall<NeteaseProduct, any>();
    const hasData = items.length > 0;

    const orderSummary = useMemo(() => {
        let totals = { orders: 0, diamonds: 0, points: 0 };
        items.forEach(p => {
            if (!p.orderPayload) return;
            const data = JSON.parse(p.orderPayload).data || {};
            totals.orders += data.count || 0;
            totals.diamonds += (data.total_diamonds || 0) / 100;
            totals.points += data.total_points || 0;
        });
        return totals;
    }, [items]);

    return (
        <div className="space-y-6">
            <div className={`grid grid-cols-1 md:grid-cols-3 gap-4 ${!hasData ? 'opacity-40 grayscale pointer-events-none' : ''}`}>
                <DashboardCard label="总计订单" value={orderSummary.orders} />
                <DashboardCard label="总销售额 (元)" value={orderSummary.diamonds.toFixed(2)} />
                <DashboardCard label="总积分" value={orderSummary.points} />
            </div>

            {!hasData && !loading && (
                <div className="flex flex-col items-center justify-center p-12 border-2 border-dashed rounded-xl bg-muted/20">
                    <Package className="w-12 h-12 text-muted-foreground mb-4" />
                    <h3 className="text-lg font-medium">暂无关联产品数据</h3>
                    <p className="text-sm text-muted-foreground mb-4">关联产品后即可查看销售看板与趋势图</p>
                </div>
            )}

            <Card className={!hasData ? 'hidden' : ''}>
                <CardHeader><CardTitle>关联产品列表</CardTitle></CardHeader>
                <CardContent>
                    <NeteaseProductManagerPage forceSearch={{ 'project.id': String(projectId) }} />
                </CardContent>
            </Card>
        </div>
    );
}

// ---------- 辅助组件 ----------
function InfoItem({ label, value }: { label: string; value: any }) {
    return (
        <div className="flex flex-col">
            <span className="text-muted-foreground font-normal">{label}</span>
            <span className="font-semibold">{value ?? '-'}</span>
        </div>
    );
}

function DashboardCard({ label, value }: { label: string; value: any }) {
    return (
        <Card>
            <CardContent className="pt-6 text-center">
                <div className="text-3xl font-bold">{value}</div>
                <p className="text-xs text-muted-foreground uppercase tracking-wider mt-1">{label}</p>
            </CardContent>
        </Card>
    );
}

function formatTime(ms?: number) {
    return ms ? new Date(ms).toLocaleString() : '-';
}

function DetailSkeleton() {
    return (
        <div className="container mx-auto p-6 space-y-6">
            <Skeleton className="h-40 w-full" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-64 w-full" />
        </div>
    );
}