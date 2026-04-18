import React, { useEffect, useMemo, useState } from "react";
import apiClient from "@/lib/axios";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
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
import { Plus, Save, Users } from "lucide-react";
import { toast } from "sonner";

export enum RoleType {
  BUILDER = "BUILDER",
  MODIFIER = "MODIFIER",
  UPLOADER = "UPLOADER",
}

export const roleTextMap: Record<RoleType, string> = {
  [RoleType.BUILDER]: "建筑制作",
  [RoleType.MODIFIER]: "修改美化",
  [RoleType.UPLOADER]: "包装宣传",
};

type ContributionRecord = {
  id: number;
  username: string;
  role: RoleType;
  contributePoints: number;
  contributeRatio?: number;
  createUsername?: string;
  comment?: string;
  project?: { id?: number } | null;
  product?: { id?: number } | null;
};

type GroupedContributions = Record<RoleType, ContributionRecord[]>;

const COLORS = ["#0f766e", "#2563eb", "#d97706", "#dc2626", "#7c3aed", "#65a30d"];

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

function createEmptyGroupedData(): GroupedContributions {
  return {
    [RoleType.BUILDER]: [],
    [RoleType.MODIFIER]: [],
    [RoleType.UPLOADER]: [],
  };
}

function RolePieChart({ title, data }: { title: string; data: ContributionRecord[] }) {
  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="text-md text-center">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        {data.length > 0 ? (
          <ResponsiveContainer width="100%" height={280}>
            <PieChart>
              <Pie data={data} dataKey="contributeRatio" nameKey="username" cx="50%" cy="50%" outerRadius={80}>
                {data.map((_, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip formatter={((value: number) => `${(value * 100).toFixed(2)}%`) as any} />
              <Legend verticalAlign="bottom" height={36} layout="horizontal" wrapperStyle={{ paddingTop: "10px" }} />
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <div className="h-[220px] flex items-center justify-center text-sm text-muted-foreground">暂无数据</div>
        )}
      </CardContent>
    </Card>
  );
}

function ContributionTable({
  title,
  data,
  editableRoles,
  endpointBase,
  onRefresh,
}: {
  title: string;
  data: ContributionRecord[];
  editableRoles: RoleType[];
  endpointBase: string;
  onRefresh: () => void;
}) {
  if (data.length === 0) {
    return (
      <Card>
        <CardHeader className="py-3">
          <CardTitle className="text-sm font-medium">{title}</CardTitle>
        </CardHeader>
        <CardContent className="py-2">
          <div className="text-sm text-muted-foreground text-center py-4">暂无记录</div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="rounded-lg p-4 border">
      <div className="pb-3">
        <h3 className="text-lg font-semibold underline underline-offset-4">{title}</h3>
      </div>
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
          {data.map((record) => {
            const canEdit = editableRoles.includes(record.role);
            return (
              <TableRow key={record.id}>
                <TableCell>{record.id}</TableCell>
                <TableCell className="font-medium">{record.username}</TableCell>
                <TableCell>{roleTextMap[record.role] || record.role}</TableCell>
                <TableCell>{record.contributePoints}</TableCell>
                <TableCell>
                  {record.contributeRatio != null ? `${(record.contributeRatio * 100).toFixed(2)}%` : "-"}
                </TableCell>
                <TableCell>{record.createUsername || "-"}</TableCell>
                <TableCell>{record.comment || "-"}</TableCell>
                <TableCell>
                  {canEdit ? (
                    <div className="flex gap-2">
                      <EditContributionDialog record={record} endpointBase={endpointBase} onRefresh={onRefresh} />
                      <DeleteContributionDialog record={record} endpointBase={endpointBase} onRefresh={onRefresh} />
                    </div>
                  ) : (
                    <span className="text-sm text-muted-foreground">仅展示</span>
                  )}
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </div>
  );
}

export function ContributionBoard({
  endpointBase,
  title,
  boardDescription,
  displayRoles,
  editableRoles,
}: {
  endpointBase: string;
  title: string;
  boardDescription: string;
  displayRoles: RoleType[];
  editableRoles: RoleType[];
}) {
  const [groupedData, setGroupedData] = useState<GroupedContributions>(createEmptyGroupedData());
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`${endpointBase}/grouped`);
      setGroupedData({ ...createEmptyGroupedData(), ...(response.data || {}) });
    } catch (error) {
      console.error("加载贡献数据失败", error);
      toast.warning("加载贡献数据失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadData();
  }, [endpointBase]);

  const pieTitleMap = useMemo(
    () => ({
      [RoleType.BUILDER]: "建筑制作占比",
      [RoleType.MODIFIER]: "修改美化占比",
      [RoleType.UPLOADER]: "包装宣传占比",
    }),
    [],
  );

  const tableTitleMap = useMemo(
    () => ({
      [RoleType.BUILDER]: "建筑制作贡献明细",
      [RoleType.MODIFIER]: "修改美化贡献明细",
      [RoleType.UPLOADER]: "包装宣传贡献明细",
    }),
    [],
  );

  if (loading) {
    return <Skeleton className="h-96 w-full mt-6" />;
  }

  return (
    <div className="space-y-6 mt-6">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">{title}</h2>
          <p className="text-sm text-muted-foreground mt-1">{boardDescription}</p>
        </div>
        {editableRoles.length > 0 ? (
          <AddContributionDialog endpointBase={endpointBase} allowedRoles={editableRoles} onRefresh={loadData} />
        ) : null}
      </div>

      <div className={`grid grid-cols-1 gap-4 ${displayRoles.length >= 3 ? "md:grid-cols-3" : "md:grid-cols-2"}`}>
        {displayRoles.map((role) => (
          <RolePieChart key={role} title={pieTitleMap[role]} data={groupedData[role] || []} />
        ))}
      </div>

      <div className="space-y-4">
        {displayRoles.map((role) => (
          <ContributionTable
            key={role}
            title={tableTitleMap[role]}
            data={groupedData[role] || []}
            editableRoles={editableRoles}
            endpointBase={endpointBase}
            onRefresh={loadData}
          />
        ))}
      </div>
    </div>
  );
}

function AddContributionDialog({
  endpointBase,
  allowedRoles,
  onRefresh,
}: {
  endpointBase: string;
  allowedRoles: RoleType[];
  onRefresh: () => void;
}) {
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState<"single" | "team">("single");

  const singleForm = useForm<z.infer<typeof addSchema>>({
    resolver: zodResolver(addSchema as any),
    defaultValues: {
      username: "",
      role: allowedRoles[0],
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

  const visibleTeamRoles = allowedRoles.filter((role) => role !== RoleType.BUILDER || allowedRoles.length === 1);

  const onSingleSubmit = async (values: z.infer<typeof addSchema>) => {
    try {
      await apiClient.post(endpointBase, values);
      handleSuccess();
    } catch (error) {
      console.error(error);
      toast.warning("添加贡献记录失败");
    }
  };

  const onTeamSubmit = async (values: any) => {
    const payloads = [];

    if (allowedRoles.includes(RoleType.BUILDER) && values.builderUser) {
      payloads.push({
        username: values.builderUser,
        role: RoleType.BUILDER,
        contributePoints: Number(values.builderPoints) || 0,
        comment: values.comment,
      });
    }
    if (allowedRoles.includes(RoleType.MODIFIER) && values.modifierUser) {
      payloads.push({
        username: values.modifierUser,
        role: RoleType.MODIFIER,
        contributePoints: Number(values.modifierPoints) || 0,
        comment: values.comment,
      });
    }
    if (allowedRoles.includes(RoleType.UPLOADER) && values.uploaderUser) {
      payloads.push({
        username: values.uploaderUser,
        role: RoleType.UPLOADER,
        contributePoints: Number(values.uploaderPoints) || 0,
        comment: values.comment,
      });
    }

    if (payloads.length === 0) {
      toast.warning("请至少填写一个角色的用户名");
      return;
    }

    try {
      await Promise.all(payloads.map((payload) => apiClient.post(endpointBase, payload)));
      handleSuccess();
    } catch (error) {
      console.error(error);
      toast.warning("批量添加失败");
    }
  };

  const handleSuccess = () => {
    setOpen(false);
    singleForm.reset({
      username: "",
      role: allowedRoles[0],
      contributePoints: 10,
      comment: "",
    });
    teamForm.reset();
    onRefresh();
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <div className="flex gap-2">
        {allowedRoles.length > 1 ? (
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
        ) : null}
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

      <DialogContent className={mode === "team" ? "sm:max-w-[700px]" : "sm:max-w-[425px]"}>
        <DialogHeader>
          <DialogTitle>{mode === "team" ? "批量配置角色贡献" : "添加贡献记录"}</DialogTitle>
        </DialogHeader>

        {mode === "single" ? (
          <Form {...singleForm}>
            <form onSubmit={singleForm.handleSubmit(onSingleSubmit as any)} className="space-y-4">
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
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {allowedRoles.map((role) => (
                          <SelectItem key={role} value={role}>
                            {roleTextMap[role]}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
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
                    <FormMessage />
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
                    <FormMessage />
                  </FormItem>
                )}
              />
              <DialogFooter>
                <Button type="submit">保存</Button>
              </DialogFooter>
            </form>
          </Form>
        ) : (
          <form onSubmit={teamForm.handleSubmit(onTeamSubmit)} className="space-y-6">
            <div className="space-y-4">
              {visibleTeamRoles.includes(RoleType.BUILDER) ? (
                <RoleBatchRow
                  title="建筑制作"
                  titleClassName="text-blue-600"
                  placeholder="建筑制作用户名"
                  userField="builderUser"
                  pointsField="builderPoints"
                  form={teamForm}
                />
              ) : null}
              {visibleTeamRoles.includes(RoleType.MODIFIER) ? (
                <RoleBatchRow
                  title="修改美化"
                  titleClassName="text-green-600"
                  placeholder="修改美化用户名"
                  userField="modifierUser"
                  pointsField="modifierPoints"
                  form={teamForm}
                />
              ) : null}
              {visibleTeamRoles.includes(RoleType.UPLOADER) ? (
                <RoleBatchRow
                  title="包装宣传"
                  titleClassName="text-orange-600"
                  placeholder="包装宣传用户名"
                  userField="uploaderUser"
                  pointsField="uploaderPoints"
                  form={teamForm}
                  withBorder={false}
                />
              ) : null}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">统一备注</label>
              <Input {...teamForm.register("comment")} />
              <p className="text-[10px] text-muted-foreground">* 留空的用户名将不会创建该角色的记录</p>
            </div>

            <DialogFooter>
              <Button type="button" variant="ghost" onClick={() => setOpen(false)}>
                取消
              </Button>
              <Button type="submit">
                <Save className="w-4 h-4 mr-2" /> 确认批量创建
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
}

function RoleBatchRow({
  title,
  titleClassName,
  placeholder,
  userField,
  pointsField,
  form,
  withBorder = true,
}: {
  title: string;
  titleClassName: string;
  placeholder: string;
  userField: "builderUser" | "modifierUser" | "uploaderUser";
  pointsField: "builderPoints" | "modifierPoints" | "uploaderPoints";
  form: ReturnType<typeof useForm<any>>;
  withBorder?: boolean;
}) {
  return (
    <div className={`grid grid-cols-12 gap-4 items-end ${withBorder ? "border-b pb-4" : "pb-2"}`}>
      <div className={`col-span-3 text-sm font-bold self-center ${titleClassName}`}>{title}</div>
      <div className="col-span-6 space-y-1">
        <label className="text-xs text-muted-foreground">用户名</label>
        <Input placeholder={placeholder} {...form.register(userField)} />
      </div>
      <div className="col-span-3 space-y-1">
        <label className="text-xs text-muted-foreground">分数</label>
        <Input type="number" {...form.register(pointsField)} />
      </div>
    </div>
  );
}

function EditContributionDialog({
  record,
  endpointBase,
  onRefresh,
}: {
  record: ContributionRecord;
  endpointBase: string;
  onRefresh: () => void;
}) {
  const [open, setOpen] = useState(false);
  const form = useForm<z.infer<typeof editSchema>>({
    resolver: zodResolver(editSchema) as any,
    defaultValues: { contributePoints: record.contributePoints },
  });

  const onSubmit = async (values: z.infer<typeof editSchema>) => {
    try {
      await apiClient.patch(`${endpointBase}/${record.id}`, {
        ...values,
        comment: record.comment,
      });
      setOpen(false);
      onRefresh();
    } catch (error) {
      console.error(error);
      toast.warning("修改分数失败");
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
          <form onSubmit={form.handleSubmit(onSubmit as any)} className="space-y-4">
            <FormField
              control={form.control as any}
              name="contributePoints"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>新的分数</FormLabel>
                  <FormControl>
                    <Input type="number" {...field} />
                  </FormControl>
                  <FormMessage />
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

function DeleteContributionDialog({
  record,
  endpointBase,
  onRefresh,
}: {
  record: ContributionRecord;
  endpointBase: string;
  onRefresh: () => void;
}) {
  const [open, setOpen] = useState(false);
  const form = useForm<z.infer<typeof deleteSchema>>({
    resolver: zodResolver(deleteSchema),
    defaultValues: { deleteReason: "" },
  });

  const onSubmit = async (values: z.infer<typeof deleteSchema>) => {
    try {
      await apiClient.delete(`${endpointBase}/${record.id}`, { data: values });
      setOpen(false);
      onRefresh();
    } catch (error) {
      console.error(error);
      toast.warning("删除贡献记录失败");
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
                  <FormMessage />
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
