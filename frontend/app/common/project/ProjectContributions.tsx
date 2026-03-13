import React, { useEffect, useState, useMemo } from 'react';
import {
    Card,
    CardContent,
    CardHeader,
    CardTitle,
} from '@/components/ui/card';
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
import { Skeleton } from '@/components/ui/skeleton';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import {
    PieChart,
    Pie,
    Cell,
    Tooltip,
    Legend,
    ResponsiveContainer,
} from 'recharts';

import { IDUNN_API } from '~/api';

// ========== 类型和枚举定义 ==========
enum RoleType {
    BUILDER = 'BUILDER',
    MODIFIER = 'MODIFIER',
    UPLOADER = 'UPLOADER',
}

// 分组数据的类型：记录每个角色对应的贡献列表
type GroupedContributions = Record<RoleType, any[]>;

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d'];

// ========== 表单验证 Schema ==========
const addSchema = z.object({
    username: z.string().min(1, "用户名不能为空"),
    role: z.nativeEnum(RoleType, { error: "请选择角色" }),
    contributePoints: z.coerce.number().min(0, "贡献分数不能小于0"),
    comment: z.string().optional(),
});

const editSchema = z.object({
    contributePoints: z.coerce.number().min(0, "贡献分数不能小于0"),
});

const deleteSchema = z.object({
    deleteReason: z.string().min(1, "删除原因不能为空"),
});

// ========== 饼图子组件 ==========
// ========== 饼图子组件 ==========
function RolePieChart({ title, data }: { title: string, data: any[] }) {
    return (
        <Card>
            <CardHeader className="pb-2">
                <CardTitle className="text-md text-center">{title}</CardTitle>
            </CardHeader>
            <CardContent>
                {data.length > 0 ? (
                    <ResponsiveContainer width="100%" height={280}>
                        <PieChart>
                            <Pie
                                data={data}
                                dataKey="contributeRatio"
                                nameKey="username"
                                cx="50%"
                                cy="50%"
                                outerRadius={80}
                            >
                                {data.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip 
                                formatter={((value: number) => `${(value * 100).toFixed(2)}%`) as any}
                            />
                            <Legend 
                                verticalAlign="bottom"
                                height={36}
                                layout="horizontal"
                                wrapperStyle={{ paddingTop: '10px' }}
                            />
                        </PieChart>
                    </ResponsiveContainer>
                ) : (
                    <div className="h-[220px] flex items-center justify-center text-sm text-muted-foreground">
                        暂无数据
                    </div>
                )}
            </CardContent>
        </Card>
    );
}

// ========== 贡献表格子组件（复用） ==========
function ContributionTable({
    title,
    data,
    bgGray = false,
    onRefresh,
}: {
    title: string,
    data: any[],
    bgGray?: boolean,
    onRefresh: () => void,
}) {
    if (data.length === 0) {
        return (
            <Card className={bgGray ? 'bg-gray-50' : ''}>
                <CardHeader className="py-3">
                    <CardTitle className="text-sm font-medium">{title}</CardTitle>
                </CardHeader>
                <CardContent className="py-2">
                    <div className="text-sm text-muted-foreground text-center py-4">
                        暂无记录
                    </div>
                </CardContent>
            </Card>
        );
    }

    return (
        // <div className="">
        <div className={(bgGray ? 'bg-gray-200' : '') + " rounded-lg p-4 border"}>

            <div className="pb-3">
                <h3 className="text-lg font-semibold underline underline-offset-4">{title}</h3>
            </div>
            <div className="p-0">
                <div className=" overflow-hidden">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>ID</TableHead>
                                <TableHead>用户名</TableHead>
                                <TableHead>角色</TableHead>
                                <TableHead>分数</TableHead>
                                <TableHead>占比</TableHead>
                                <TableHead>添加人</TableHead>
                                <TableHead>备注</TableHead>
                                <TableHead>操作</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {data.map((record) => (
                                <TableRow key={record.id}>
                                    <TableCell>{record.id}</TableCell>
                                    <TableCell className="font-medium">{record.username}</TableCell>
                                    <TableCell>{record.role}</TableCell>
                                    <TableCell>{record.contributePoints}</TableCell>
                                    <TableCell>
                                        {record.contributeRatio != null
                                            ? `${(record.contributeRatio * 100).toFixed(2)}%`
                                            : '-'}
                                    </TableCell>
                                    <TableCell>{record.createUsername}</TableCell>
                                    <TableCell>{record.comment || '-'}</TableCell>
                                    <TableCell>
                                        <div className="flex gap-2">
                                            <EditContributionDialog
                                                record={record}
                                                onRefresh={onRefresh}
                                            />
                                            <DeleteContributionDialog
                                                record={record}
                                                onRefresh={onRefresh}
                                            />
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            </div>
        </div>
        // </div>
    );
}

// ========== 主组件 ==========
export function ProjectContributions({ projectId }: { projectId: number }) {
    const [project, setProject] = useState<any>(null);               // 当前项目详情
    const [contributions, setContributions] = useState<any[]>([]);   // 本项目数据（列表用）
    const [groupedData, setGroupedData] = useState<GroupedContributions>({
        [RoleType.BUILDER]: [],
        [RoleType.MODIFIER]: [],
        [RoleType.UPLOADER]: [],
    });
    const [loading, setLoading] = useState(true);

    // 加载项目详情
    const loadProject = async () => {
        const res = await IDUNN_API.apiV1CommercialProjectsIdGet(projectId);
        setProject(res.data);
    };

    // 加载本项目数据（用于列表）
    const loadProjectContributions = async () => {
        const res = await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsGet(projectId);
        setContributions(res.data || []);
    };

    // 加载分组数据（用于饼图）
    const loadGroupedContributions = async () => {
        const res = await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsGroupedGet(projectId);
        setGroupedData(prev => ({
            ...prev,
            ...(res.data || {}),
        }));
    };

    const loadData = async () => {
        setLoading(true);
        try {
            await Promise.all([
                loadProject(),
                loadProjectContributions(),
                loadGroupedContributions(),
            ]);
        } catch (error) {
            console.error("加载贡献数据失败", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (projectId) {
            loadData();
        }
    }, [projectId]);

    // 从 groupedData 中提取饼图所需的数据
    const buildersForPie = groupedData[RoleType.BUILDER] || [];
    const modifiersForPie = groupedData[RoleType.MODIFIER] || [];
    const uploadersForPie = groupedData[RoleType.UPLOADER] || [];

    // 从 contributions 中提取本项目各角色的数据
    const localBuilders = contributions.filter(c => c.role === RoleType.BUILDER);
    const localModifiers = contributions.filter(c => c.role === RoleType.MODIFIER);
    const localUploaders = contributions.filter(c => c.role === RoleType.UPLOADER);

    if (loading) return <Skeleton className="h-96 w-full mt-6" />;

    return (
        <div className="space-y-6 mt-6">
            <div className="flex flex-row justify-between align-middle">
                <h2 className="text-2xl font-bold tracking-tight">项目贡献看板</h2>
                {/* 添加记录按钮 */}
            </div>


            {/* 1. 三个饼状图面板，使用 groupedData */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <RolePieChart title="BUILDER (建筑师) 占比" data={buildersForPie} />
                <RolePieChart title="MODIFIER (修改者) 占比" data={modifiersForPie} />
                <RolePieChart title="UPLOADER (上传者) 占比" data={uploadersForPie} />
            </div>

            {/* 2. 垂直排列的四个表格 */}
            <div className="space-y-4">
                <div className='flex flex-row justify-between align-middle'>
                    <h3 className="text-lg font-semibold">本项目贡献明细</h3>
                    <AddContributionDialog projectId={projectId} onRefresh={loadData} />
                </div>
                

                {/* 本项目 BUILDER 表格（灰色背景） */}
                <ContributionTable
                    title="BUILDER (本项目)"
                    data={localBuilders}
                    bgGray={true}
                    onRefresh={loadData}
                />

                {/* 本项目 MODIFIER 表格 */}
                <ContributionTable
                    title="MODIFIER"
                    data={localModifiers}
                    onRefresh={loadData}
                />

                {/* 本项目 UPLOADER 表格 */}
                <ContributionTable
                    title="UPLOADER"
                    data={localUploaders}
                    onRefresh={loadData}
                />

                {/* 父项目 BUILDER 表格（始终显示，数据来自 groupedData） */}
                <ContributionTable
                    title="父项目 BUILDER（实际计算时使用父项目的 BUILDER 记录）"
                    data={groupedData[RoleType.BUILDER] || []}
                    onRefresh={loadData}
                />
            </div>
        </div>
    );
}

// ========== 操作弹窗：添加记录 ==========
function AddContributionDialog({ projectId, onRefresh }: { projectId: number, onRefresh: () => void }) {
    const [open, setOpen] = useState(false);
    const form = useForm<z.infer<typeof addSchema>>({
        resolver: zodResolver(addSchema) as any,
        defaultValues: { username: '', role: RoleType.BUILDER, contributePoints: 0, comment: '' },
    });

    const onSubmit = async (values: z.infer<typeof addSchema>) => {
        try {
            await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsPost(projectId, values);
            setOpen(false);
            form.reset();
            onRefresh();
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="sm">添加贡献记录</Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader><DialogTitle>添加贡献记录</DialogTitle></DialogHeader>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                        <FormField control={form.control} name="username" render={({ field }) => (
                            <FormItem><FormLabel>用户名</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
                        )} />
                        <FormField control={form.control} name="role" render={({ field }) => (
                            <FormItem>
                                <FormLabel>角色</FormLabel>
                                <Select onValueChange={field.onChange} defaultValue={field.value}>
                                    <FormControl><SelectTrigger><SelectValue /></SelectTrigger></FormControl>
                                    <SelectContent>
                                        {Object.values(RoleType).map(role => (
                                            <SelectItem key={role} value={role}>{role}</SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                <FormMessage />
                            </FormItem>
                        )} />
                        <FormField control={form.control} name="contributePoints" render={({ field }) => (
                            <FormItem><FormLabel>贡献分数</FormLabel><FormControl><Input type="number" {...field} /></FormControl><FormMessage /></FormItem>
                        )} />
                        <FormField control={form.control} name="comment" render={({ field }) => (
                            <FormItem><FormLabel>备注说明</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
                        )} />
                        <DialogFooter>
                            <Button type="button" variant="outline" onClick={() => setOpen(false)}>取消</Button>
                            <Button type="submit">保存</Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}

// ========== 操作弹窗：修改分数（使用记录自身的项目ID） ==========
function EditContributionDialog({ record, onRefresh }: { record: any, onRefresh: () => void }) {
    const [open, setOpen] = useState(false);
    const form = useForm<z.infer<typeof editSchema>>({
        resolver: zodResolver(editSchema) as any,
        defaultValues: { contributePoints: record.contributePoints },
    });

    useEffect(() => {
        if (open) form.reset({ contributePoints: record.contributePoints });
    }, [open, record, form]);

    const onSubmit = async (values: z.infer<typeof editSchema>) => {
        try {
            // 使用记录所属的项目ID，确保路径正确
            const targetProjectId = record.project?.id;
            if (!targetProjectId) {
                console.error('记录缺少 project 信息');
                return;
            }
            await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsContributionIdPatch(
                String(targetProjectId),
                String(record.id),
                {
                    ...values,
                    comment: record.comment
                }
            );
            setOpen(false);
            onRefresh();
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button variant="outline" size="sm">修改分数</Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader><DialogTitle>修改分数 - {record.username}</DialogTitle></DialogHeader>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit as any)} className="space-y-4">
                        <FormField control={form.control as any} name="contributePoints" render={({ field }) => (
                            <FormItem><FormLabel>新的分数</FormLabel><FormControl><Input type="number" {...field} /></FormControl><FormMessage /></FormItem>
                        )} />
                        <DialogFooter>
                            <Button type="button" variant="outline" onClick={() => setOpen(false)}>取消</Button>
                            <Button type="submit">确认修改</Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}

// ========== 操作弹窗：软删除记录（使用记录自身的项目ID） ==========
function DeleteContributionDialog({ record, onRefresh }: { record: any, onRefresh: () => void }) {
    const [open, setOpen] = useState(false);
    const form = useForm<z.infer<typeof deleteSchema>>({
        resolver: zodResolver(deleteSchema),
        defaultValues: { deleteReason: '' },
    });

    const onSubmit = async (values: z.infer<typeof deleteSchema>) => {
        try {
            const targetProjectId = record.project?.id;
            if (!targetProjectId) {
                console.error('记录缺少 project 信息');
                return;
            }
            await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsContributionIdDelete(
                targetProjectId,
                record.id,
                values
            );
            setOpen(false);
            form.reset();
            onRefresh();
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button variant="destructive" size="sm">删除</Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader><DialogTitle>删除贡献记录</DialogTitle></DialogHeader>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                        <FormField control={form.control} name="deleteReason" render={({ field }) => (
                            <FormItem><FormLabel>删除原因</FormLabel><FormControl><Input placeholder="请输入删除原因" {...field} /></FormControl><FormMessage /></FormItem>
                        )} />
                        <DialogFooter>
                            <Button type="button" variant="outline" onClick={() => setOpen(false)}>取消</Button>
                            <Button type="submit" variant="destructive">确认删除</Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}