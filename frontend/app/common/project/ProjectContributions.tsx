import React, { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Skeleton } from "@/components/ui/skeleton";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

import { IDUNN_API } from "~/api";
import { Plus, Users, Save } from "lucide-react";

// ========== 类型和枚举定义 ==========
enum RoleType {
  BUILDER = "BUILDER",
  MODIFIER = "MODIFIER",
  UPLOADER = "UPLOADER",
}

const roleTextMap: Record<RoleType, string> = {
  [RoleType.BUILDER]: "建筑制作",
  [RoleType.MODIFIER]: "修改美化",
  [RoleType.UPLOADER]: "包装宣传",
};

type GroupedContributions = Record<RoleType, any[]>;

const COLORS = [
  "#0088FE",
  "#00C49F",
  "#FFBB28",
  "#FF8042",
  "#8884d8",
  "#82ca9d",
];

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
function RolePieChart({ title, data }: { title: string; data: any[] }) {
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
                {data.map((_, index) => (
                  <Cell
                    key={`cell-${index}`}
                    fill={COLORS[index % COLORS.length]}
                  />
                ))}
              </Pie>
              <Tooltip
                formatter={
                  ((value: number) => `${(value * 100).toFixed(2)}%`) as any
                }
              />
              <Legend
                verticalAlign="bottom"
                height={36}
                layout="horizontal"
                wrapperStyle={{ paddingTop: "10px" }}
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

// ========== 贡献表格子组件 ==========
function ContributionTable({
  title,
  data,
  bgGray = false,
  onRefresh,
}: {
  title: string;
  data: any[];
  bgGray?: boolean;
  onRefresh: () => void;
}) {
  if (data.length === 0) {
    return (
      <Card className={bgGray ? "bg-gray-50" : ""}>
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
    <div className={(bgGray ? "bg-gray-200" : "") + " rounded-lg p-4 border"}>
      <div className="pb-3">
        <h3 className="text-lg font-semibold underline underline-offset-4">
          {title}
        </h3>
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
                  <TableCell className="font-medium">
                    {record.username}
                  </TableCell>
                  <TableCell>{roleTextMap[record.role as RoleType] || record.role}</TableCell>
                  <TableCell>{record.contributePoints}</TableCell>
                  <TableCell>
                    {record.contributeRatio != null
                      ? `${(record.contributeRatio * 100).toFixed(2)}%`
                      : "-"}
                  </TableCell>
                  <TableCell>{record.createUsername}</TableCell>
                  <TableCell>{record.comment || "-"}</TableCell>
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
  );
}

// ========== 主组件 ==========
export function ProjectContributions({ projectId }: { projectId: number }) {
  const [contributions, setContributions] = useState<any[]>([]);
  const [groupedData, setGroupedData] = useState<GroupedContributions>({
    [RoleType.BUILDER]: [],
    [RoleType.MODIFIER]: [],
    [RoleType.UPLOADER]: [],
  });
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const [resDetails, resGrouped] = await Promise.all([
        IDUNN_API.apiV1CommercialProjectsProjectIdContributionsGet(projectId),
        IDUNN_API.apiV1CommercialProjectsProjectIdContributionsGroupedGet(
          projectId,
        ),
      ]);
      setContributions(resDetails.data || []);
      setGroupedData((prev) => ({ ...prev, ...(resGrouped.data || {}) }));
    } catch (error) {
      console.error("加载贡献数据失败", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (projectId) loadData();
  }, [projectId]);

  const buildersForPie = groupedData[RoleType.BUILDER] || [];
  const modifiersForPie = groupedData[RoleType.MODIFIER] || [];
  const uploadersForPie = groupedData[RoleType.UPLOADER] || [];

  const localModifiers = contributions.filter(
    (c) => c.role === RoleType.MODIFIER,
  );
  const localUploaders = contributions.filter(
    (c) => c.role === RoleType.UPLOADER,
  );

  if (loading) return <Skeleton className="h-96 w-full mt-6" />;

  return (
    <div className="space-y-6 mt-6">
      <h2 className="text-2xl font-bold tracking-tight">项目贡献看板</h2>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <RolePieChart title="建筑制作占比" data={buildersForPie} />
        <RolePieChart title="修改美化占比" data={modifiersForPie} />
        <RolePieChart title="包装宣传占比" data={uploadersForPie} />
      </div>

      <div className="space-y-4">
        <div className="flex flex-row justify-between align-middle">
          <h3 className="text-lg font-semibold">本项目贡献明细</h3>
          <AddContributionDialog projectId={projectId} onRefresh={loadData} />
        </div>

        <ContributionTable
          title="父项目建筑制作（关联同步）"
          data={groupedData[RoleType.BUILDER] || []}
          onRefresh={loadData}
        />
        <ContributionTable
          title="修改美化"
          data={localModifiers}
          onRefresh={loadData}
        />
        <ContributionTable
          title="包装宣传"
          data={localUploaders}
          onRefresh={loadData}
        />
      </div>
    </div>
  );
}

// ========== 操作弹窗：添加记录 (核心修改部分) ==========
function AddContributionDialog({
  projectId,
  onRefresh,
}: {
  projectId: number;
  onRefresh: () => void;
}) {
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState<"single" | "team">("single");

  const singleForm = useForm<z.infer<typeof addSchema>>({
    resolver: zodResolver(addSchema as any),
    defaultValues: {
      username: "",
      role: RoleType.BUILDER,
      contributePoints: 10,
      comment: "",
    },
  });

  const teamForm = useForm({
    defaultValues: {
      builderUser: "",
      builderPoints: 10,
      modifierUser: "",
      modifierPoints: 10,
      uploaderUser: "",
      uploaderPoints: 10,
      comment: "团队协作初始化",
    },
  });

  const onSingleSubmit = async (values: z.infer<typeof addSchema>) => {
    try {
      await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsPost(
        projectId,
        values,
      );
      handleSuccess();
    } catch (error) {
      console.error(error);
    }
  };

  const onTeamSubmit = async (values: any) => {
    try {
      const payloads = [];
      if (values.builderUser) {
        payloads.push({
          username: values.builderUser,
          role: RoleType.BUILDER,
          contributePoints: values.builderPoints,
          comment: values.comment,
        });
      }
      if (values.modifierUser) {
        payloads.push({
          username: values.modifierUser,
          role: RoleType.MODIFIER,
          contributePoints: values.modifierPoints,
          comment: values.comment,
        });
      }
      if (values.uploaderUser) {
        payloads.push({
          username: values.uploaderUser,
          role: RoleType.UPLOADER,
          contributePoints: values.uploaderPoints,
          comment: values.comment,
        });
      }

      if (payloads.length === 0) return alert("请至少填写一个角色的用户名");

      await Promise.all(
        payloads.map((p) =>
          IDUNN_API.apiV1CommercialProjectsProjectIdContributionsPost(
            projectId,
            p,
          ),
        ),
      );
      handleSuccess();
    } catch (error) {
      console.error("批量添加失败", error);
    }
  };

  const handleSuccess = () => {
    setOpen(false);
    singleForm.reset();
    teamForm.reset();
    onRefresh();
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <div className="flex gap-2">
        <Button
          size="sm"
          variant="outline"
          onClick={() => {
            setMode("team");
            setOpen(true);
          }}
        >
          <Users className="w-4 h-4 mr-1" /> 批量添加角色
        </Button>
        <Button
          size="sm"
          onClick={() => {
            setMode("single");
            setOpen(true);
          }}
        >
          <Plus className="w-4 h-4 mr-1" /> 添加单条
        </Button>
      </div>

      <DialogContent
        className={mode === "team" ? "sm:max-w-[700px]" : "sm:max-w-[425px]"}
      >
        <DialogHeader>
          <DialogTitle>
            {mode === "team" ? "批量配置各角色贡献" : "添加贡献记录"}
          </DialogTitle>
        </DialogHeader>

        {mode === "single" ? (
          <Form {...singleForm}>
            <form
              onSubmit={singleForm.handleSubmit(onSingleSubmit as any)}
              className="space-y-4"
            >
              <FormField
                control={singleForm.control as any}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>用户名</FormLabel>
                    <FormControl>
                      <Input placeholder="输入用户名" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={singleForm.control as any}
                name="role"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>角色</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {Object.values(RoleType).map((role) => (
                          <SelectItem key={role} value={role}>
                            {roleTextMap[role]}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </FormItem>
                )}
              />
              <FormField
                control={singleForm.control as any}
                name="contributePoints"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>贡献分数</FormLabel>
                    <FormControl>
                      <Input type="number" {...field} />
                    </FormControl>
                  </FormItem>
                )}
              />
              <FormField
                control={singleForm.control as any}
                name="comment"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>备注</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                  </FormItem>
                )}
              />
              <DialogFooter>
                <Button type="submit">保存</Button>
              </DialogFooter>
            </form>
          </Form>
        ) : (
          <form
            onSubmit={teamForm.handleSubmit(onTeamSubmit)}
            className="space-y-6"
          >
            <div className="space-y-4">
              {/* Builder Row */}
              <div className="grid grid-cols-12 gap-4 items-end border-b pb-4">
                <div className="col-span-3 text-sm font-bold text-blue-600 self-center">
                  建筑制作
                </div>
                <div className="col-span-6 space-y-1">
                  <label className="text-xs text-muted-foreground">
                    用户名
                  </label>
                  <Input
                    placeholder="建筑制作用户名"
                    {...teamForm.register("builderUser")}
                  />
                </div>
                <div className="col-span-3 space-y-1">
                  <label className="text-xs text-muted-foreground">分数</label>
                  <Input
                    type="number"
                    {...teamForm.register("builderPoints")}
                  />
                </div>
              </div>

              {/* Modifier Row */}
              <div className="grid grid-cols-12 gap-4 items-end border-b pb-4">
                <div className="col-span-3 text-sm font-bold text-green-600 self-center">
                  修改美化
                </div>
                <div className="col-span-6 space-y-1">
                  <label className="text-xs text-muted-foreground">
                    用户名
                  </label>
                  <Input
                    placeholder="修改美化用户名"
                    {...teamForm.register("modifierUser")}
                  />
                </div>
                <div className="col-span-3 space-y-1">
                  <label className="text-xs text-muted-foreground">分数</label>
                  <Input
                    type="number"
                    {...teamForm.register("modifierPoints")}
                  />
                </div>
              </div>

              {/* Uploader Row */}
              <div className="grid grid-cols-12 gap-4 items-end pb-2">
                <div className="col-span-3 text-sm font-bold text-orange-600 self-center">
                  包装宣传
                </div>
                <div className="col-span-6 space-y-1">
                  <label className="text-xs text-muted-foreground">
                    用户名
                  </label>
                  <Input
                    placeholder="包装宣传用户名"
                    {...teamForm.register("uploaderUser")}
                  />
                </div>
                <div className="col-span-3 space-y-1">
                  <label className="text-xs text-muted-foreground">分数</label>
                  <Input
                    type="number"
                    {...teamForm.register("uploaderPoints")}
                  />
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">统一备注</label>
              <Input {...teamForm.register("comment")} />
              <p className="text-[10px] text-muted-foreground">
                * 留空的用户名将不会创建该角色的记录
              </p>
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="ghost"
                onClick={() => setOpen(false)}
              >
                取消
              </Button>
              <Button
                type="submit"
                className="bg-indigo-600 hover:bg-indigo-700"
              >
                <Save className="w-4 h-4 mr-2" /> 确认批量创建
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
}

// ========== 操作弹窗：修改分数 (保持不变) ==========
function EditContributionDialog({
  record,
  onRefresh,
}: {
  record: any;
  onRefresh: () => void;
}) {
  const [open, setOpen] = useState(false);
  const form = useForm<z.infer<typeof editSchema>>({
    resolver: zodResolver(editSchema) as any,
    defaultValues: { contributePoints: record.contributePoints },
  });

  const onSubmit = async (values: z.infer<typeof editSchema>) => {
    try {
      await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsContributionIdPatch(
        String(record.project?.id),
        String(record.id),
        { ...values, comment: record.comment },
      );
      setOpen(false);
      onRefresh();
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <Button variant="outline" size="sm" onClick={() => setOpen(true)}>
        修改分数
      </Button>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>修改分数 - {record.username}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit as any)}
            className="space-y-4"
          >
            <FormField
              control={form.control as any}
              name="contributePoints"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>新的分数</FormLabel>
                  <FormControl>
                    <Input type="number" {...field} />
                  </FormControl>
                </FormItem>
              )}
            />
            <DialogFooter>
              <Button type="submit">确认</Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

// ========== 操作弹窗：软删除记录 (保持不变) ==========
function DeleteContributionDialog({
  record,
  onRefresh,
}: {
  record: any;
  onRefresh: () => void;
}) {
  const [open, setOpen] = useState(false);
  const form = useForm<z.infer<typeof deleteSchema>>({
    resolver: zodResolver(deleteSchema),
    defaultValues: { deleteReason: "" },
  });

  const onSubmit = async (values: z.infer<typeof deleteSchema>) => {
    try {
      await IDUNN_API.apiV1CommercialProjectsProjectIdContributionsContributionIdDelete(
        record.project?.id,
        record.id,
        values,
      );
      setOpen(false);
      onRefresh();
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <Button variant="destructive" size="sm" onClick={() => setOpen(true)}>
        删除
      </Button>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>确认删除</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="deleteReason"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>删除原因</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                </FormItem>
              )}
            />
            <DialogFooter>
              <Button type="submit" variant="destructive">
                确认删除
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
