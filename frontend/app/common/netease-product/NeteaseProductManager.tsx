// NeteaseProductManagerPage.tsx
import React, { useState, useCallback, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useInView } from 'react-intersection-observer';

// Shadcn UI 组件
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
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from '@/components/ui/form';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Card, CardContent } from '@/components/ui/card';

// 自定义工具

import { useWaterfall, WaterfallProvider } from '../util/waterfall-provider';
import type { PageResponse, ProjectApi } from '../project/ProjectManager';
import type { Schema, SearchParam } from '../util/search-test-utils';
import type { Project } from '~/api/generated';
import { Link } from 'react-router';
import NeteasePointType from '../util/NeteasePointType';

// ---------- 类型定义 ----------
// 产品状态枚举（应与后端一致）
export enum NeteaseProductStatus {
    CREATED = 'CREATED',
    CONVERTED = 'CONVERTED',
    ONLINE = 'ONLINE',
    REJECTED = 'REJECTED',
}

// 产品实体（基于后端 NeteaseProduct，选取部分常用字段）
export interface NeteaseProduct {
    id: number;
    itemId: string;
    itemName: string;
    internalStatus: NeteaseProductStatus;
    project?: Project;                // 关联的项目对象（懒加载，可能为空）
    projectId?: number;               // 方便前端展示
    price?: number;
    priceType?: string;
    createTimeMs?: number;
    updateTimeMs?: number;
    // 其他字段可根据需要添加
}

// 分页响应（复用 Project 的分页类型，但指定内容为 NeteaseProduct）
export type ProductPageResponse = PageResponse<NeteaseProduct>;

// 产品搜索 schema（用于构建 search 字符串）
export const productSchema = {
    itemId: { search: { fuzzy: true } },
    itemName: { search: { fuzzy: true } },
    internalStatus: { search: { fuzzy: false } },
    'project.id': { search: { fuzzy: false } },
    id: { sort: true },
} as const satisfies Schema;

type ProductSearchParams = SearchParam<typeof productSchema>;

// 产品 API 接口
export interface ProductApi {
    fetchProducts: (page: number, criteria: ProductSearchParams) => Promise<ProductPageResponse>;
    updateProduct: (id: number, data: Partial<NeteaseProduct>) => Promise<NeteaseProduct>;
}

// ---------- 表单验证 Schema ----------
const statusFormSchema = z.object({
    internalStatus: z.nativeEnum(NeteaseProductStatus),
});

type StatusFormValues = z.infer<typeof statusFormSchema>;

// ---------- 主页面组件 ----------
interface NeteaseProductManagerPageProps {
    productApi: ProductApi;
    projectApi: ProjectApi; // 用于获取项目列表（下拉选择）
}

export function NeteaseProductManagerPage({ productApi, projectApi }: NeteaseProductManagerPageProps) {
    const fetchData = useCallback(
        async (page: number, criteria: ProductSearchParams) => {
            return productApi.fetchProducts(page, criteria);
        },
        [productApi]
    );

    return (
        <WaterfallProvider
            initialCriteria={{}}
            fetchData={fetchData}
            getId={(item: NeteaseProduct) => item.id}
        >
            <div className="container mx-auto p-4">
                <div className="flex justify-between items-center mb-4">
                    <h1 className="text-2xl font-bold">网易产品管理</h1>
                    {/* 这里可以添加其他操作，如批量关联等 */}
                </div>
                <ProductTable projectApi={projectApi} productApi={productApi} />
            </div>
        </WaterfallProvider>
    );
}

// ---------- 产品表格组件（使用 Waterfall 上下文）----------
function ProductTable({ productApi, projectApi }: { productApi: ProductApi; projectApi: ProjectApi }) {
    const { items, loading, hasMore, loadMore, search, error } = useWaterfall<NeteaseProduct, ProductSearchParams>();

    // 搜索输入状态
    const [searchInputs, setSearchInputs] = useState({
        itemName: '',
        itemId: '',
        internalStatus: '',
        projectId: '',
    });

    // 构建搜索条件
    const buildCriteria = useCallback((): ProductSearchParams => {
        const criteria: ProductSearchParams = {};
        if (searchInputs.itemName) criteria.itemName = { value: searchInputs.itemName, fuzzy: true };
        if (searchInputs.itemId) criteria.itemId = { value: searchInputs.itemId, fuzzy: true };
        if (searchInputs.internalStatus) criteria.internalStatus = searchInputs.internalStatus;
        if (searchInputs.projectId) criteria['project.id'] = searchInputs.projectId;
        // 默认按 id 降序
        criteria.sort = { id: 'desc' };
        return criteria;
    }, [searchInputs]);

    const handleSearch = () => {
        search(buildCriteria());
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') handleSearch();
    };

    // 无限滚动哨兵
    const { ref: sentinelRef, inView } = useInView({ threshold: 0.1, rootMargin: '100px' });

    useEffect(() => {
        if (inView && !loading && hasMore) {
            loadMore();
        }
    }, [inView, loading, hasMore, loadMore]);

    // 状态标签颜色映射
    const statusColor = (status: NeteaseProductStatus) => {
        switch (status) {
            case NeteaseProductStatus.CREATED: return 'bg-gray-500';
            case NeteaseProductStatus.CONVERTED: return 'bg-blue-500';
            case NeteaseProductStatus.ONLINE: return 'bg-green-500';
            case NeteaseProductStatus.REJECTED: return 'bg-red-500';
            default: return 'bg-gray-500';
        }
    };

    return (
        <div className="space-y-4">
            {/* 搜索栏 */}
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
                <Input
                    placeholder="项目ID"
                    value={searchInputs.projectId}
                    onChange={(e) => setSearchInputs(prev => ({ ...prev, projectId: e.target.value }))}
                    onKeyDown={handleKeyDown}
                    className="w-32"
                />
                <Button onClick={handleSearch}>搜索</Button>
            </div>

            {/* 表格 */}
            <div className="border rounded-lg overflow-hidden">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>ID</TableHead>
                            <TableHead>物品ID</TableHead>
                            <TableHead>名称</TableHead>
                            <TableHead>状态</TableHead>
                            <TableHead>项目</TableHead>
                            <TableHead>价格</TableHead>
                            <TableHead>操作</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {items.map((product) => (
                            <TableRow key={product.id}>

                                <TableCell>{product.id}</TableCell>
                                <TableCell>{product.itemId}</TableCell>
                                <TableCell className="font-bold hover:text-accent transition-all duration-300"><Link to={`./${product.id}`}>{product.itemName}</Link></TableCell>
                                <TableCell>
                                    <Badge className={statusColor(product.internalStatus)}>
                                        {product.internalStatus}
                                    </Badge>
                                </TableCell>
                                <TableCell className="font-bold hover:text-accent transition-all duration-300">
                                    <Link to={`/commercial/projects`}>
                                        {
                                            product.project
                                                ? ("[" + (product.project?.id || product.projectId || '-') + "] " + (product.project?.displayName || '未知项目名称'))
                                                : "---"
                                        }
                                    </Link>
                                </TableCell>
                                <TableCell className="inline-flex align-middle gap-1 my-auto">{product.price ?? '-'} <NeteasePointType point={product.priceType} /></TableCell>
                                <TableCell>
                                    <div className="flex gap-2">
                                        <AssignProjectDialog product={product} projectApi={projectApi} productApi={productApi} />
                                        <ChangeStatusDialog product={product} productApi={productApi} />
                                    </div>
                                </TableCell>
                            </TableRow>
                        ))}
                        {loading && (
                            <>
                                {Array.from({ length: 3 }).map((_, i) => (
                                    <TableRow key={`skeleton-${i}`}>
                                        <TableCell colSpan={8}>
                                            <Skeleton className="h-8 w-full" />
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </>
                        )}
                    </TableBody>
                </Table>
            </div>

            {/* 滚动加载哨兵 */}
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

// ---------- 关联项目对话框 ----------
interface AssignProjectDialogProps {
    product: NeteaseProduct;
    projectApi: ProjectApi;
    productApi: ProductApi;
}

function AssignProjectDialog({ product, projectApi, productApi }: AssignProjectDialogProps) {
    const [open, setOpen] = useState(false);
    const [selectedProject, setSelectedProject] = useState<Project | null>(null);
    const [projectSearch, setProjectSearch] = useState('');
    const [projects, setProjects] = useState<Project[]>([]);
    const [loadingProjects, setLoadingProjects] = useState(false);
    const { refresh } = useWaterfall<NeteaseProduct, ProductSearchParams>();

    // 加载项目列表（支持搜索）
    const loadProjects = useCallback(async (searchTerm: string) => {
        setLoadingProjects(true);
        try {
            // 构建项目搜索条件（参考 ProjectManagerPage 的搜索格式）
            const criteria = searchTerm
                ? { name: { value: searchTerm, fuzzy: true } }
                : {};
            const page = await projectApi.fetchProjects(0, criteria);
            setProjects(page.content);
        } catch (error) {
            console.error('Failed to load projects', error);
        } finally {
            setLoadingProjects(false);
        }
    }, [projectApi]);

    // 打开对话框时加载初始项目列表
    useEffect(() => {
        if (open) {
            loadProjects('');
        }
    }, [open, loadProjects]);

    // 防抖搜索
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
            await refresh(); // 刷新产品列表
        } catch (error) {
            // 错误处理
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button variant="outline" size="sm">关联项目</Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-2xl">
                <DialogHeader>
                    <DialogTitle>关联项目 - {product.itemName}</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    {/* 项目搜索 */}
                    <Input
                        placeholder="搜索项目名称..."
                        value={projectSearch}
                        onChange={(e) => setProjectSearch(e.target.value)}
                    />
                    {/* 项目下拉列表 */}
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
                    {/* 选中项目的详细信息 */}
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

// ---------- 修改状态对话框 ----------
function ChangeStatusDialog({ product, productApi }: { product: NeteaseProduct; productApi: ProductApi }) {
    const [open, setOpen] = useState(false);
    const { refresh } = useWaterfall<NeteaseProduct, ProductSearchParams>();
    const form = useForm<StatusFormValues>({
        resolver: zodResolver(statusFormSchema),
        defaultValues: {
            internalStatus: product.internalStatus,
        },
    });

    const onSubmit = async (values: StatusFormValues) => {
        try {
            await productApi.updateProduct(product.id, { internalStatus: values.internalStatus });
            setOpen(false);
            await refresh();
        } catch (error) {
            // 错误处理
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