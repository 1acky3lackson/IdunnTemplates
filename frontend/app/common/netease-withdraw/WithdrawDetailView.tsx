import React, { useMemo, useState, useEffect, useCallback } from "react";
import { useParams, Link } from "react-router";
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
  Legend,
} from "recharts";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import {
  ArrowLeft,
  Wallet,
  PieChart as PieIcon,
  Coins,
  Info,
  CheckCircle2,
  ListCheck,
  Search,
  User,
  Calendar,
} from "lucide-react";
import { Button } from "@/components/ui/button";

// 引入 API 和类型;
import { IDUNN_API } from "~/api";
import type {
  NeteaseWithdraw,
  CheckoutWithdrawAllocation,
} from "~/api/generated";
import {
  GenericCrudTable,
  type PageResponse,
} from "../generic-crud-table/generic-crud-table";

// 定义结账单类型（基于你提供的JSON结构）
interface CheckoutDetailInfo {
  id?: number;
  orderId?: number;
  role?: string;
  username?: string;
  ratio?: number;
  netProfit?: number;
  actualProfit?: number;
  status?: string;
  createTimeMs?: number;
  confirmTimeMs?: number;
  releaseTimeMs?: number;
  finishTimeMs?: number;
}

export default function WithdrawDetailView({
  id = "1",
  uid = "wthdrw",
}: {
  id: string;
  uid?: string;
}) {
  const withdrawId = Number(id);
  const [withdraw, setWithdraw] = useState<NeteaseWithdraw | null>(null);

  // 详情弹窗状态
  const [selectedDetail, setSelectedDetail] =
    useState<CheckoutDetailInfo | null>(null);
  const [isLoadingDetail, setIsLoadingDetail] = useState(false);

  // 1. 获取提现单基础数据
  useEffect(() => {
    const loadWithdraw = async () => {
      const res = await IDUNN_API.listWithdraws(`id:${withdrawId}`);
      if (res.data.content.length > 0) {
        setWithdraw(res.data.content[0] as NeteaseWithdraw);
      }
    };
    loadWithdraw();
  }, [withdrawId]);

  // 2. 准备图表数据
  const chartData = useMemo(() => {
    if (!withdraw) return [];
    const used = withdraw.usedOriginalValue || 0;
    const remaining = Math.max(0, (withdraw.originalValue || 0) - used);
    return [
      { name: "已分配原始额", value: used, color: "#3b82f6" },
      { name: "剩余可用原始额", value: remaining, color: "#e2e8f0" },
    ];
  }, [withdraw]);

  // 3. 详情获取逻辑
  const handleViewDetail = async (detailId: number) => {
    setIsLoadingDetail(true);
    try {
      // 假设这是你提到的 /api/v1/commercial/balance/checkout-details 接口
      const res = await IDUNN_API.listBalanceCheckoutDetails(`id:${detailId}`);
      if (res.data.content && res.data.content.length > 0) {
        setSelectedDetail(res.data.content[0]);
      }
    } catch (error) {
      console.error("Failed to fetch checkout detail", error);
    } finally {
      setIsLoadingDetail(false);
    }
  };

  const fetchAllocations = async (
    page: number,
    size: number,
    search: string,
  ) => {
    const res =
      await IDUNN_API.apiV1CommercialNeteaseWithdrawsWithdrawIdAllocationsGet(
        withdrawId,
        search || undefined,
        page,
        size,
      );
    return res.data as PageResponse<CheckoutWithdrawAllocation>;
  };

  if (!withdraw) return <div className="p-8 text-center">加载中...</div>;

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* 顶部导航 */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" asChild>
          <Link to="/commercial/netease-withdraws">
            <ArrowLeft className="w-4 h-4 mr-2" />
            返回列表
          </Link>
        </Button>
        <h1 className="text-2xl font-bold tracking-tight">提现单使用明细</h1>
        <Badge variant="outline" className="font-mono">
          ID: {withdrawId}
        </Badge>
      </div>

      {/* 第一部分：统计卡片 (保持不变) */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              原始总点数 (PE)
            </CardTitle>
            <Wallet className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold font-mono text-blue-600">
              {Math.round((withdraw.originalValue || 0) * 100).toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              费率: {(withdraw.ratio! * 100).toFixed(2)}%
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              已分配点数
            </CardTitle>
            <CheckCircle2 className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold font-mono">
              {Math.round((withdraw.usedOriginalValue || 0) * 100).toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              占比:{" "}
              {(
                (withdraw.usedOriginalValue! / withdraw.originalValue!) *
                100
              ).toFixed(1)}
              %
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              剩余可用点数
            </CardTitle>
            <Coins className="h-4 w-4 text-amber-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold font-mono text-amber-600">
              {Math.round((withdraw.originalValue! - withdraw.usedOriginalValue!) * 100).toLocaleString()}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              实际到账点数
            </CardTitle>
            <Info className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold font-mono">
              {Math.round((withdraw.withdrawValue || 0) * 100).toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              记录时间: {new Date(withdraw.saveTimeMs!).toLocaleDateString()}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 第二部分：图表展示 (保持不变) */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card className="md:col-span-1">
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <PieIcon className="w-5 h-5" /> 资金消耗比
            </CardTitle>
          </CardHeader>
          <CardContent className="h-62.5">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={chartData}
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend verticalAlign="bottom" height={36} />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle className="text-lg">使用说明</CardTitle>
            <CardDescription>
              该提现记录的所有资金流向如下列表所示
            </CardDescription>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground leading-relaxed">
            <p>
              1. 每一行记录代表一笔结算单（CheckoutDetail）对该提现单的占用。
            </p>
            <p className="mt-2">
              2. <strong>扣除原始额</strong> 是指从网易后台显示的 PE
              总额中扣除的部分。
            </p>
            <p className="mt-2">
              3. <strong>实际折算点数</strong>{" "}
              是根据当前提现单的汇率（Ratio）自动计算的最终支付数字。
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 第三部分：明细列表 - 修改了跳转逻辑 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg flex items-center gap-2">
            <ListCheck className="w-5 h-5" /> 分配明细记录
          </CardTitle>
        </CardHeader>
        <CardContent>
          <GenericCrudTable<CheckoutWithdrawAllocation>
            uid={uid}
            getRowId={(row) => row.id!}
            list={fetchAllocations}
            pageSize={10}
            schema={{
              id: { title: "分配ID" },
              checkoutDetailId: {
                title: "关联结账单",
                render: (val) => (
                  <Button
                    variant="link"
                    className="h-auto p-0 text-blue-600 font-medium"
                    onClick={() => handleViewDetail(val)}
                  >
                    #{val}
                  </Button>
                ),
              },
              allocatedOriginal: {
                title: "扣除原始额 (PE)",
                sortable: true,
                render: (val) => (
                  <span className="font-mono text-blue-600">
                    {Math.round((val || 0) * 100).toLocaleString()}
                  </span>
                ),
              },
              actualAmount: {
                title: "实际折算点数",
                render: (val) => (
                  <span className="font-mono font-semibold text-green-600">
                    {Math.round((val || 0) * 100).toLocaleString()}
                  </span>
                ),
              },
              createTimeMs: {
                title: "分配时间",
                render: (val) => new Date(val).toLocaleString(),
              },
            }}
          />
        </CardContent>
      </Card>

      {/* 结账单详情对话框 */}
      <Dialog
        open={!!selectedDetail}
        onOpenChange={(open) => !open && setSelectedDetail(null)}
      >
        <DialogContent className="sm:max-w-125">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Search className="w-5 h-5 text-primary" />
              结账单详情 #{selectedDetail?.id}
            </DialogTitle>
            <DialogDescription>
              该结账单对应订单 #{selectedDetail?.orderId} 的分账明细
            </DialogDescription>
          </DialogHeader>

          {selectedDetail && (
            <div className="grid grid-cols-2 gap-4 py-4 text-sm">
              <div className="space-y-1">
                <span className="text-muted-foreground flex items-center gap-1 text-xs">
                  <User className="w-3 h-3" /> 参与用户
                </span>
                <p className="font-medium">{selectedDetail.username}</p>
              </div>
              <div className="space-y-1">
                <span className="text-muted-foreground flex items-center gap-1 text-xs">
                  角色类型
                </span>
                <Badge variant="secondary">{selectedDetail.role}</Badge>
              </div>
              <div className="space-y-1 border-t pt-2">
                <span className="text-muted-foreground text-xs">分账比例</span>
                <p className="font-mono">
                  {(selectedDetail.ratio || 0 * 100).toFixed(2)}%
                </p>
              </div>
              <div className="space-y-1 border-t pt-2">
                <span className="text-muted-foreground text-xs">当前状态</span>
                <Badge className="bg-green-600">{selectedDetail.status}</Badge>
              </div>
              <div className="space-y-1 border-t pt-2">
                <span className="text-muted-foreground text-xs">
                  净利润 (点数)
                </span>
                <p className="font-mono text-blue-600 font-bold">
                  {Math.round((selectedDetail.netProfit ?? 0) * 100).toLocaleString()}
                </p>
              </div>
              <div className="space-y-1 border-t pt-2">
                <span className="text-muted-foreground text-xs">
                  实际利润 (点数)
                </span>
                <p className="font-mono text-green-600 font-bold">
                  {Math.round((selectedDetail.actualProfit ?? 0) * 100).toLocaleString()}
                </p>
              </div>
              <div className="col-span-2 space-y-1 border-t pt-2">
                <span className="text-muted-foreground flex items-center gap-1 text-xs">
                  <Calendar className="w-3 h-3" /> 创建时间
                </span>
                <p className="text-xs">
                  {new Date(selectedDetail.createTimeMs ?? 0).toLocaleString()}
                </p>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
