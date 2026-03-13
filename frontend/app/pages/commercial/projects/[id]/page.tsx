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
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DialogFooter,
} from '@/components/ui/dialog';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from '@/components/ui/form';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useInView } from 'react-intersection-observer';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
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
import type { Project, NeteaseProduct } from '~/api/generated'; // 从生成的 API 导入
import { NeteaseProductStatus, type ProductApi, type ProductPageResponse } from '~/common/netease-product/NeteaseProductManager';
import { productSchema } from '~/common/netease-product/NeteaseProductManager';
import { WaterfallProvider, useWaterfall } from '~/common/util/waterfall-provider';
import { buildSearchString, buildSortString } from '~/common/util/search-test-utils';
import { deepNullToUndefined } from '~/common/util/null-to-undefined';
import NeteasePointType from '~/common/util/NeteasePointType';
import { projectSchema } from '~/common/project/ProjectManager';
import type { Route } from './+types/page';
import { ProjectContributions } from '~/common/project/ProjectContributions';

// ---------- 类型定义 ----------
interface ProjectDetailProps {
    projectApi: {
        getProject: (id: number) => Promise<Project>;
    };
    productApi: ProductApi;
}

// 状态表单验证（使用枚举，但实际接收字符串）
const statusFormSchema = z.object({
    internalStatus: z.nativeEnum(NeteaseProductStatus),
});
type StatusFormValues = z.infer<typeof statusFormSchema>;

export function meta({ params }: Route.MetaArgs) {
    return [
        { title: `项目详情 ${params.id}` },
    ];
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

// 辅助函数：将 API 返回的原始数据转换为组件所需的 NeteaseProduct 类型
const convertToNeteaseProduct = (raw: any): NeteaseProduct => ({
    id: raw.id,
    internalStatus: raw.internalStatus,
    updateTimeMs: raw.updateTimeMs,
    neUserLogId: raw.neUserLogId,
    applyReviewTime: raw.applyReviewTime,
    applyReviewTimeMs: raw.applyReviewTimeMs,
    canManageServer: raw.canManageServer,
    canSilentOnline: raw.canSilentOnline,
    canSynchronizePc: raw.canSynchronizePc,
    canUpdatePc: raw.canUpdatePc,
    collectionId: raw.collectionId,
    createTime: raw.createTime,
    createTimeMs: raw.createTimeMs,
    discount: raw.discount,
    exemptPerfReviewNum: raw.exemptPerfReviewNum,
    interceptFields: raw.interceptFields,
    isEa: raw.isEa,
    isOriginal: raw.isOriginal,
    isSilentOnline: raw.isSilentOnline,
    isSuitablePc: raw.isSuitablePc,
    isSync: raw.isSync,
    isTestServer: raw.isTestServer,
    itemId: raw.itemId,
    itemName: raw.itemName,
    lobbyConfigOpLog: raw.lobbyConfigOpLog,
    lobbySortKey: raw.lobbySortKey,
    onlineTime: raw.onlineTime,
    onlineTimeMs: raw.onlineTimeMs,
    oriWeakOffline: raw.oriWeakOffline,
    oriWeakOfflineReason: raw.oriWeakOfflineReason,
    peIsAddPlayPlan: raw.peIsAddPlayPlan,
    perfData: raw.perfData,
    performanceServiceAvailable: raw.performanceServiceAvailable,
    performanceServiceStatus: raw.performanceServiceStatus,
    playPlanExpireMonth: raw.playPlanExpireMonth,
    priType: raw.priType,
    price: raw.price,
    priceRank: raw.priceRank,
    priceType: raw.priceType,
    queuePosition: raw.queuePosition,
    ratingLevel: raw.ratingLevel,
    remindable: raw.remindable,
    res: raw.res,
    status: raw.status,
    syncItemInfo: raw.syncItemInfo,
    syncPcFlag: raw.syncPcFlag,
    urgentStatus: raw.urgentStatus,
    weakOffline: raw.weakOffline,
    weakOfflineReason: raw.weakOfflineReason,
    orderPayload: raw.orderPayload,
    statPayload: raw.statPayload,
    project: raw.project,
});

const defaultProductAPI: ProjectDetailProps['productApi'] = {
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
        const response = await IDUNN_API.apiV1CommercialNeteaseProductsIdPut(
            id,
            data
        );
        const raw = deepNullToUndefined(response.data);
        return convertToNeteaseProduct(raw) as any;
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

    // 加载项目信息
    useEffect(() => {
        if (!projectId) return;
        setLoading(true);
        projectApi.getProject(projectId)
            .then((data) => {
                setProject(data);
                setError(null);
                return data.parentProjectId;
            }).then((parentId) => {
                if (!parentId) return;
                projectApi.getProject(parentId).then((data) => {
                    setParentProject(data);
                })
            })
            .catch((err) => {
                setError(err.message || '加载项目失败');
            })
            .finally(() => {
                setLoading(false);
            });
    }, [projectId, projectApi]);

    // 产品列表数据提供者（固定按 project.id 筛选）
    const fetchProductsForProject = useCallback(async (page: number, criteria: any) => {
        const finalCriteria = {
            ...criteria,
            'project.id': String(projectId),
        };
        return productApi.fetchProducts(page, finalCriteria);
    }, [productApi, projectId]);

    if (loading) {
        return <DetailSkeleton />;
    }

    if (error || !project) {
        return (
            <div className="container mx-auto p-6">
                <div className="p-4 text-red-500 bg-red-50 rounded">
                    加载失败: {error}
                </div>
            </div>
        );
    }

    return (
        <div className="container mx-auto p-6 space-y-6">
            {/* 项目信息卡片 */}
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center justify-between">
                        <span className="truncate">{project.displayName}</span>
                        <Badge variant="outline">{project.kind}</Badge>
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        <InfoItem label="ID" value={project.id} />
                        <InfoItem label="名称" value={project.name} />
                        <InfoItem label="路径" value={project.pathName} />
                        <InfoItem label="世界ID" value={project.world?.id} />
                        <InfoItem label="世界名称" value={project.world?.displayName} />
                        <InfoItem label="描述" value={project.description} />
                        <InfoItem label="创建时间" value={formatTime(project.createTimeMs)} />
                        <InfoItem label="父项目ID" value={
                            project.parentProjectId
                            ? <Link to={`/commercial/projects/${project.parentProjectId}`} className='font-bold hover:underline underline-offset-2'>{`[${parentProject?.id}]: ${parentProject?.displayName}`}</Link>
                            : '无父项目'
                        } />
                    </div>
                </CardContent>
            </Card>

            {/* 贡献数据 */}
            <ProjectContributions projectId={projectId} />

            {/* 产品数据区域：销售看板和统计图表（基于所有已加载产品） */}
            <WaterfallProvider
                initialCriteria={{}}
                fetchData={fetchProductsForProject as any}
                getId={(item: NeteaseProduct) => item.id}
            >
                {/* 使用内部组件访问 items 并渲染汇总卡片 */}
                <ProjectProductsStats productApi={productApi} projectId={projectId} />
            </WaterfallProvider>
        </div>
    );
}

// ---------- 项目内产品统计组件（位于 WaterfallProvider 内部）----------
function ProjectProductsStats({ productApi, projectId }: { productApi: ProductApi; projectId: number }) {
    const { items, loading, hasMore, loadMore, search, error } = useWaterfall<NeteaseProduct, any>();

    // 汇总 orderPayload（处理 null）
    const orderSummary = useMemo(() => {
        let totalOrders = 0;
        let totalDiamonds = 0;
        let totalPoints = 0;
        let totalOrderItems = 0;

        items.forEach(product => {
            if (!product.orderPayload) return;
            try {
                const parsed = JSON.parse(product.orderPayload);
                const data = parsed.data || {};
                totalOrders += data.count || 0;
                totalDiamonds += data.total_diamonds / 100 || 0;
                totalPoints += data.total_points || 0;
                totalOrderItems += (data.orders?.length) || 0;
            } catch (e) {
                // 忽略解析失败的
            }
        });

        return { totalOrders, totalDiamonds, totalPoints, totalOrderItems };
    }, [items]);

    // 汇总 statPayload：合并所有产品的每日数据，按日期聚合（处理 null）
    const chartData = useMemo(() => {
        const dateMap = new Map<string, { DAU: number; download_num: number; cnt_buy: number; diamond: number }>();

        items.forEach(product => {
            if (!product.statPayload) return;
            try {
                const parsed = JSON.parse(product.statPayload);
                const dailyData = parsed.data?.data || [];
                dailyData.forEach((day: any) => {
                    if (!day.dateid) return;
                    const dateStr = formatDate(day.dateid);
                    const existing = dateMap.get(dateStr) || { DAU: 0, download_num: 0, cnt_buy: 0, diamond: 0 };
                    existing.DAU += day.DAU || 0;
                    existing.download_num += day.download_num || 0;
                    existing.cnt_buy += day.cnt_buy || 0;
                    existing.diamond += day.diamond / 100 || 0;
                    dateMap.set(dateStr, existing);
                });
            } catch (e) {
                // 忽略解析失败的
            }
        });

        // 转换为数组并按日期排序
        return Array.from(dateMap.entries())
            .map(([date, values]) => ({ date, ...values }))
            .sort((a, b) => a.date.localeCompare(b.date));
    }, [items]);

    return (
        <>
            <h2 className="text-2xl font-bold tracking-tight">项目销售看板</h2>
            {/* 销售看板 */}
            <Card>
                <CardHeader>
                    <CardTitle>项目销售汇总</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <DashboardCard label="总订单数" value={orderSummary.totalOrders} />
                        <DashboardCard label="销售（100钻石=1元）" value={orderSummary.totalDiamonds} />
                        <DashboardCard label="总积分" value={orderSummary.totalPoints} />
                        <DashboardCard label="订单记录数" value={orderSummary.totalOrderItems} />
                    </div>
                </CardContent>
            </Card>

            {/* 统计图表 */}
            {chartData.length > 0 ? (
                <Card>
                    <CardHeader>
                        <CardTitle>项目数据趋势</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ResponsiveContainer width="100%" height={400}>
                            <LineChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="date" />
                                <YAxis yAxisId="left" />
                                <YAxis yAxisId="right" orientation="right" />
                                <Tooltip />
                                <Legend />
                                <Line type="monotone" dataKey="DAU" stroke="#8884d8" name="日活" yAxisId="left" />
                                <Line type="monotone" dataKey="download_num" stroke="#82ca9d" name="下载量" yAxisId="right" />
                                <Line type="monotone" dataKey="cnt_buy" stroke="#ffc658" name="购买数" yAxisId="left" />
                                <Line type="monotone" dataKey="diamond" stroke="#ff7300" name="销售额（100钻石=1元）" yAxisId="left" />
                            </LineChart>
                        </ResponsiveContainer>
                    </CardContent>
                </Card>
            ) : (
                <Card>
                    <CardHeader>
                        <CardTitle>项目数据趋势</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-muted-foreground">暂无统计数据</p>
                    </CardContent>
                </Card>
            )}

            {/* 产品表格 */}
            <Card>
                <CardHeader>
                    <CardTitle>关联产品</CardTitle>
                </CardHeader>
                <CardContent>
                    <ProductTableForProject productApi={productApi} projectId={projectId} />
                </CardContent>
            </Card>
        </>
    );
}

// ---------- 项目内产品表格组件 ----------
function ProductTableForProject({ productApi, projectId }: { productApi: ProductApi; projectId: number }) {
    const { items, loading, hasMore, loadMore, search, error } = useWaterfall<NeteaseProduct, any>();

    const [searchInputs, setSearchInputs] = useState({
        itemName: '',
        itemId: '',
        internalStatus: '',
    });

    const buildCriteria = useCallback((): any => {
        const criteria: any = {};
        if (searchInputs.itemName) criteria.itemName = { value: searchInputs.itemName, fuzzy: true };
        if (searchInputs.itemId) criteria.itemId = { value: searchInputs.itemId, fuzzy: true };
        if (searchInputs.internalStatus) criteria.internalStatus = searchInputs.internalStatus;
        criteria.sort = { id: 'desc' };
        return criteria;
    }, [searchInputs]);

    const handleSearch = () => {
        search(buildCriteria());
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') handleSearch();
    };

    const { ref: sentinelRef, inView } = useInView({ threshold: 0.1, rootMargin: '100px' });

    useEffect(() => {
        if (inView && !loading && hasMore) {
            loadMore();
        }
    }, [inView, loading, hasMore, loadMore]);

    // 状态标签颜色映射（基于字符串）
    const statusColor = (status: string) => {
        switch (status) {
            case 'CREATED': return 'bg-gray-500';
            case 'CONVERTED': return 'bg-blue-500';
            case 'ONLINE': return 'bg-green-500';
            case 'REJECTED': return 'bg-red-500';
            default: return 'bg-gray-500';
        }
    };

    return (
        <div className="space-y-4">
            <div className="flex flex-wrap gap-2 mb-4">
                <Input
                    placeholder="产品名称（模糊）"
                    value={searchInputs.itemName}
                    onChange={(e) => setSearchInputs(prev => ({ ...prev, itemName: e.target.value }))}
                    onKeyDown={handleKeyDown}
                    className="w-48"
                />
                <Input
                    placeholder="物品ID"
                    value={searchInputs.itemId}
                    onChange={(e) => setSearchInputs(prev => ({ ...prev, itemId: e.target.value }))}
                    onKeyDown={handleKeyDown}
                    className="w-32"
                />
                <Input
                    placeholder="状态"
                    value={searchInputs.internalStatus}
                    onChange={(e) => setSearchInputs(prev => ({ ...prev, internalStatus: e.target.value }))}
                    onKeyDown={handleKeyDown}
                    className="w-32"
                />
                <Button onClick={handleSearch}>筛选</Button>
            </div>

            <div className="border rounded-lg overflow-hidden">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>ID</TableHead>
                            <TableHead>物品ID</TableHead>
                            <TableHead>名称</TableHead>
                            <TableHead>状态</TableHead>
                            <TableHead>价格</TableHead>
                            <TableHead>操作</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {items.map((product) => (
                            <TableRow key={product.id}>
                                <TableCell>{product.id}</TableCell>
                                <TableCell>{product.itemId}</TableCell>
                                <TableCell className="font-bold hover:text-accent transition-all duration-300">
                                    <Link to={`/commercial/netease-products/${product.id}`}>{product.itemName}</Link>
                                </TableCell>
                                <TableCell>
                                    <Badge className={statusColor(product.internalStatus)}>
                                        {product.internalStatus}
                                    </Badge>
                                </TableCell>
                                <TableCell className="inline-flex align-middle gap-1 my-auto">
                                    {product.price ?? '-'} <NeteasePointType point={product.priceType} />
                                </TableCell>
                                <TableCell>
                                    <div className="flex gap-2">
                                        <ChangeStatusDialog product={product} productApi={productApi} />
                                        <AssignProjectDialog product={product} productApi={productApi} projectId={projectId} />
                                    </div>
                                </TableCell>
                            </TableRow>
                        ))}
                        {loading && (
                            <>
                                {Array.from({ length: 3 }).map((_, i) => (
                                    <TableRow key={`skeleton-${i}`}>
                                        <TableCell colSpan={6}>
                                            <Skeleton className="h-8 w-full" />
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </>
                        )}
                    </TableBody>
                </Table>
            </div>

            {hasMore && <div ref={sentinelRef} className="h-4" />}
            {!hasMore && items.length > 0 && (
                <p className="text-center text-muted-foreground">没有更多产品了</p>
            )}
            {error && (
                <div className="p-4 text-red-500 bg-red-50 rounded">
                    加载失败: {error.message}
                </div>
            )}
        </div>
    );
}

// ---------- 修改状态对话框 ----------
function ChangeStatusDialog({ product, productApi }: { product: NeteaseProduct; productApi: ProductApi }) {
    const [open, setOpen] = useState(false);
    const { refresh } = useWaterfall<NeteaseProduct, any>();
    const form = useForm<StatusFormValues>({
        resolver: zodResolver(statusFormSchema),
        defaultValues: {
            internalStatus: product.internalStatus as NeteaseProductStatus, // 强制转换
        },
    });

    const onSubmit = async (values: StatusFormValues) => {
        try {
            await productApi.updateProduct(product.id, { internalStatus: values.internalStatus });
            setOpen(false);
            await refresh();
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button variant="outline" size="sm">修改状态</Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle>修改状态 - {product.itemName}</DialogTitle>
                </DialogHeader>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                        <FormField
                            control={form.control}
                            name="internalStatus"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>状态</FormLabel>
                                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                                        <FormControl>
                                            <SelectTrigger>
                                                <SelectValue placeholder="选择状态" />
                                            </SelectTrigger>
                                        </FormControl>
                                        <SelectContent>
                                            {Object.values(NeteaseProductStatus).map((status) => (
                                                <SelectItem key={status} value={status}>
                                                    {status}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <DialogFooter>
                            <Button variant="outline" onClick={() => setOpen(false)}>取消</Button>
                            <Button type="submit">保存</Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}

// ---------- 关联项目对话框 ----------
function AssignProjectDialog({ product, productApi, projectId }: { product: NeteaseProduct; productApi: ProductApi; projectId: number }) {
    const [open, setOpen] = useState(false);
    const [selectedProject, setSelectedProject] = useState<Project | null>(null);
    const [projectSearch, setProjectSearch] = useState('');
    const [projects, setProjects] = useState<Project[]>([]);
    const [loadingProjects, setLoadingProjects] = useState(false);
    const { refresh } = useWaterfall<NeteaseProduct, any>();

    const loadProjects = useCallback(async (searchTerm: string) => {
        setLoadingProjects(true);
        try {
            const criteria = searchTerm ? { name: { value: searchTerm, fuzzy: true } } : {};
            const search = buildSearchString(criteria, projectSchema);
            const response = await IDUNN_API.apiV1CommercialProjectsGet(search, 0, 20, "");
            setProjects(response.data.content);
        } catch (error) {
            console.error('Failed to load projects', error);
        } finally {
            setLoadingProjects(false);
        }
    }, []);

    useEffect(() => {
        if (open) {
            loadProjects('');
        }
    }, [open, loadProjects]);

    useEffect(() => {
        const timer = setTimeout(() => {
            if (open) {
                loadProjects(projectSearch);
            }
        }, 300);
        return () => clearTimeout(timer);
    }, [projectSearch, open, loadProjects]);

    const handleConfirm = async () => {
        if (!selectedProject) return;
        try {
            await productApi.updateProduct(product.id, { projectId: selectedProject.id });
            setOpen(false);
            await refresh();
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button variant="outline" size="sm">重新关联</Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-2xl">
                <DialogHeader>
                    <DialogTitle>重新关联项目 - {product.itemName}</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <Input
                        placeholder="搜索项目名称..."
                        value={projectSearch}
                        onChange={(e) => setProjectSearch(e.target.value)}
                    />
                    <ScrollArea className="h-60 border rounded-md p-2">
                        {loadingProjects ? (
                            <div className="flex justify-center p-4">
                                <Skeleton className="h-8 w-full" />
                            </div>
                        ) : (
                            <div className="space-y-1">
                                {projects.map((project) => (
                                    <div
                                        key={project.id}
                                        className={`p-2 rounded cursor-pointer hover:bg-accent ${selectedProject?.id === project.id ? 'bg-accent' : ''
                                            }`}
                                        onClick={() => setSelectedProject(project)}
                                    >
                                        {project.displayName} (ID: {project.id})
                                    </div>
                                ))}
                            </div>
                        )}
                    </ScrollArea>
                    {selectedProject && (
                        <Card>
                            <CardContent className="p-4 space-y-2">
                                <p><strong>项目名称：</strong>{selectedProject.displayName}</p>
                                <p><strong>内部名称：</strong>{selectedProject.name}</p>
                                <p><strong>类型：</strong>{selectedProject.kind}</p>
                                {selectedProject.world && <p><strong>世界ID：</strong>{selectedProject.world.id}</p>}
                                {selectedProject.description && <p><strong>描述：</strong>{selectedProject.description}</p>}
                            </CardContent>
                        </Card>
                    )}
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={() => setOpen(false)}>取消</Button>
                    <Button onClick={handleConfirm} disabled={!selectedProject}>确认关联</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

// ---------- 辅助组件 ----------
function InfoItem({ label, value }: { label: string; value: any }) {
    return (
        <div>
            <div className="text-sm text-muted-foreground">{label}</div>
            <div className="font-medium truncate">{value ?? '-'}</div>
        </div>
    );
}

function DashboardCard({ label, value }: { label: string; value: any }) {
    return (
        <div className="bg-muted p-4 rounded-lg text-center">
            <div className="text-2xl font-bold">{value ?? 0}</div>
            <div className="text-sm text-muted-foreground">{label}</div>
        </div>
    );
}

function formatTime(ms?: number): string {
    if (!ms) return '-';
    return new Date(ms).toLocaleString();
}

function formatDate(dateid: string): string {
    if (!dateid || dateid.length !== 8) return dateid;
    return `${dateid.slice(4, 6)}-${dateid.slice(6, 8)}`;
}

function DetailSkeleton() {
    return (
        <div className="container mx-auto p-6 space-y-6">
            <Skeleton className="h-32 w-full" />
            <Skeleton className="h-96 w-full" />
        </div>
    );
}