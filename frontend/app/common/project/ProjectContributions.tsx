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
function RolePieChart({ title, data }: { title: string, data: any[] }) {
    return (
        <Card>
            <CardHeader className="pb-2">
                <CardTitle className="text-md text-center">{title}</CardTitle>
            </CardHeader>
            <CardContent>
                {data.length > 0 ? (
                    <ResponsiveContainer width="100%" height={220}>
                        <PieChart>
                            <Pie
                                data={data}
                                dataKey="contributeRatio"
                                nameKey="username"
                                cx="50%"
                                cy="50%"
                                outerRadius={60}
                                labelLine={false}
                                label={({ name, percent }) => `${name} ${((percent || 0) * 100).toFixed(0)}%`}
                            >
                                {data.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
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

// ========== 主组件 ==========
export function ProjectContributions({ projectId }: { projectId: number }) {
    const [contributions, setContributions] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    const loadData = async () => {
        // debugger;
        setLoading(true);
        try {
            const res = await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsGet(projectId);
            setContributions(() => res.data || []);
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

    // 对数据进行分组用于图表展示
    const { builders, modifiers, uploaders } = useMemo(() => {
        return {
            builders: contributions.filter(c => c.role === RoleType.BUILDER),
            modifiers: contributions.filter(c => c.role === RoleType.MODIFIER),
            uploaders: contributions.filter(c => c.role === RoleType.UPLOADER),
        };
    }, [contributions]);

    if (loading) return <Skeleton className="h-96 w-full mt-6" />;

    return (
        <div className="space-y-6 mt-6">
            <h2 className="text-2xl font-bold tracking-tight">项目贡献看板</h2>

            {/* 1. 三个饼状图面板 */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <RolePieChart title="BUILDER (建筑师) 占比" data={builders} />
                <RolePieChart title="MODIFIER (修改者) 占比" data={modifiers} />
                <RolePieChart title="UPLOADER (上传者) 占比" data={uploaders} />
            </div>

            {/* 2. 数据表格面板 */}
            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <CardTitle>贡献明细</CardTitle>
                    <AddContributionDialog projectId={projectId} onRefresh={loadData} />
                </CardHeader>
                <CardContent>
                    <div className="border rounded-lg overflow-hidden">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>ID</TableHead>
                                    <TableHead>用户名</TableHead>
                                    <TableHead>角色 (Role)</TableHead>
                                    <TableHead>分数 (Points)</TableHead>
                                    <TableHead>占比 (Ratio)</TableHead>
                                    <TableHead>备注</TableHead>
                                    <TableHead>操作</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {contributions.map((record) => (
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
                                        <TableCell>{record.comment || '-'}</TableCell>
                                        <TableCell>
                                            <div className="flex gap-2">
                                                <EditContributionDialog 
                                                    projectId={projectId} 
                                                    record={record} 
                                                    onRefresh={loadData} 
                                                />
                                                <DeleteContributionDialog 
                                                    projectId={projectId} 
                                                    recordId={record.id} 
                                                    onRefresh={loadData} 
                                                />
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                ))}
                                {contributions.length === 0 && (
                                    <TableRow>
                                        <TableCell colSpan={7} className="text-center text-muted-foreground h-24">
                                            暂无贡献记录
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </div>
                </CardContent>
            </Card>
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

// ========== 操作弹窗：修改分数 ==========
function EditContributionDialog({ projectId, record, onRefresh }: { projectId: number, record: any, onRefresh: () => void }) {
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
            // 注意：API 要求第二个参数 contributionId 是字符串格式，使用 String() 转换
            await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsContributionIdPatch(
                String(projectId), 
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

// ========== 操作弹窗：软删除记录 ==========
function DeleteContributionDialog({ projectId, recordId, onRefresh }: { projectId: number, recordId: number, onRefresh: () => void }) {
    const [open, setOpen] = useState(false);
    const form = useForm<z.infer<typeof deleteSchema>>({
        resolver: zodResolver(deleteSchema),
        defaultValues: { deleteReason: '' },
    });

    const onSubmit = async (values: z.infer<typeof deleteSchema>) => {
        try {
            await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsContributionIdDelete(
                projectId, 
                recordId, 
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