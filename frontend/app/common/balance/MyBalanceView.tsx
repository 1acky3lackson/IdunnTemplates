import React, { useEffect, useState } from "react";
import {
  Wallet,
  Lock,
  Clock,
  ArrowRightLeft,
  PlusCircle,
  History,
  Landmark,
  Coins,
} from "lucide-react";

// Shadcn UI
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";

// API 与子组件
import { IDUNN_API } from "~/api";
import type { BalanceInfo } from "~/api/generated";
import { UserTransactionRecordList } from "./BalanceTables";
import { WithdrawList } from "../system-withdraw/WithdrawList";
import CheckoutDetails from "../checkout-details/CheckoutDetails";
import { useAuth } from "../auth/auth-provider";

// 格式化工具：统一使用 standard text
const formatMoney = (amount?: number | null) => {
  if (amount === undefined || amount === null) return "¥0.00";
  return `¥${amount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

export function MyBalanceView() {
  const [balanceInfo, setBalanceInfo] = useState<BalanceInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [isWithdrawOpen, setIsWithdrawOpen] = useState(false);
  const [withdrawAmount, setWithdrawAmount] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  const {
    isAuthenticated,
    user: authUser,
    login: loginFunc,
    logout,
  } = useAuth();

  const fetchMyBalance = async () => {
    try {
      setLoading(true);
      const res = await IDUNN_API.getMyBalance();
      // @ts-ignore
      setBalanceInfo(res.data);
    } catch (err) {
      toast.error("获取账户信息失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyBalance();
  }, [refreshKey]);

  const handleRequestWithdraw = async () => {
    const amount = parseFloat(withdrawAmount);
    if (isNaN(amount) || amount <= 0) {
      toast.error("请输入有效的提现金额");
      return;
    }
    if (amount > (balanceInfo?.availableBalance || 0)) {
      toast.error("可用余额不足");
      return;
    }

    setSubmitting(true);
    try {
      await IDUNN_API.apiV1CommercialWithdrawalsPost({ amount });
      toast.success("提现申请已提交，请等待管理员审批");
      setIsWithdrawOpen(false);
      setWithdrawAmount("");
      setRefreshKey((p) => p + 1);
    } catch (e) {
      toast.error("提现申请失败，请稍后重试");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container mx-auto p-4 space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      {/* 头部：使用标准 shadcn 字体和间距 */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-bold tracking-tight">财务中心</h1>
          <p className="text-sm text-muted-foreground">
            管理您的个人资产、提现记录及资金流向
          </p>
        </div>

        <Dialog open={isWithdrawOpen} onOpenChange={setIsWithdrawOpen}>
          <DialogTrigger asChild>
            <Button size="lg" className="shadow-sm">
              <PlusCircle className="mr-2 h-4 w-4" /> 发起提现申请
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-100">
            <DialogHeader>
              <DialogTitle>申请提现</DialogTitle>
              <DialogDescription>
                资金将在审批通过后转入您的预留结算账户
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <Label htmlFor="amount">提现金额</Label>
                  <span className="text-xs text-muted-foreground">
                    可用: {formatMoney(balanceInfo?.availableBalance)}
                  </span>
                </div>
                <div className="relative">
                  <span className="absolute left-3 top-2.5 text-muted-foreground font-medium">
                    ¥
                  </span>
                  <Input
                    id="amount"
                    type="number"
                    placeholder="0.00"
                    className="pl-7 font-mono"
                    value={withdrawAmount}
                    onChange={(e) => setWithdrawAmount(e.target.value)}
                  />
                </div>
              </div>
              <div className="rounded-md border border-input bg-muted/50 p-3">
                <p className="text-[11px] leading-relaxed text-muted-foreground">
                  提示：提交后将锁定对应余额。若审批拒绝，金额将自动退回您的账户。
                </p>
              </div>
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => setIsWithdrawOpen(false)}
              >
                取消
              </Button>
              <Button
                onClick={handleRequestWithdraw}
                disabled={submitting || !withdrawAmount}
              >
                {submitting ? "提交中..." : "确认提交"}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* 数据看板：使用标准 Card 样式，通过 opacity 区分主次 */}
      <div className="grid gap-4 md:grid-cols-3">
        {/* 可用余额：主卡片使用 primary 背景 */}
        <Card className="relative overflow-hidden border-none bg-primary text-primary-foreground shadow-md">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium opacity-90">
              可用余额
            </CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-9 w-32 bg-primary-foreground/20" />
            ) : (
              <div className="text-3xl font-bold tracking-tighter">
                {formatMoney(balanceInfo?.availableBalance)}
              </div>
            )}
            <div className="mt-4 flex items-center text-xs opacity-70">
              <Landmark className="mr-1 h-3 w-3" /> 账户可提现资产
            </div>
          </CardContent>
          <Wallet className="absolute -right-4 -bottom-4 h-24 w-24 opacity-10 rotate-12" />
        </Card>

        {/* 冻结金额：使用默认 Card 样式 */}
        <Card>
          <CardHeader className="pb-2 flex flex-row items-center justify-between space-y-0">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              保留中金额
            </CardTitle>
            <Lock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-8 w-24" />
            ) : (
              <div className="text-2xl font-bold">
                {formatMoney(balanceInfo?.frozenBalance)}
              </div>
            )}
            <p className="text-[10px] text-muted-foreground mt-1">
              因平台规则临时保留的风险金
            </p>
          </CardContent>
        </Card>

        {/* 待提现：使用默认 Card 样式 */}
        <Card>
          <CardHeader className="pb-2 flex flex-row items-center justify-between space-y-0">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              待提现 (预估)
            </CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-8 w-24" />
            ) : (
              <div className="text-2xl font-bold">
                {formatMoney(balanceInfo?.pendingBalance)}
              </div>
            )}
            <p className="text-[10px] text-muted-foreground mt-1">
              网易平台尚未结算的预期收益
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 底部：Tabs 使用系统默认样式，不强制颜色 */}
      <Tabs defaultValue="checkout-details" className="w-full">
        <div className="flex items-center justify-between mb-4">
          <TabsList className="grid w-full max-w-100 grid-cols-3">
            <TabsTrigger
              value="checkout-details"
              // className={authUser ? "" : "hidden"}
            >
              <History className="w-4 h-4 mr-2" /> 分成记录
            </TabsTrigger>
            <TabsTrigger value="withdrawals">
              <History className="w-4 h-4 mr-2" /> 提现记录
            </TabsTrigger>
            <TabsTrigger value="transactions">
              <ArrowRightLeft className="w-4 h-4 mr-2" /> 交易流水
            </TabsTrigger>
          </TabsList>
        </div>

        <TabsContent value="checkout-details" className="space-y-4">
          <CheckoutDetails
            forceSearch={authUser ? { username: authUser.username } : undefined}
          />
        </TabsContent>

        <TabsContent value="transactions" className="space-y-4">
          {!loading && balanceInfo?.username ? (
            <Card>
              <CardContent className="pt-6">
                <UserTransactionRecordList
                  forceSearch={{ username: balanceInfo.username }}
                  pageSize={10}
                />
              </CardContent>
            </Card>
          ) : (
            <Skeleton className="h-100 w-full" />
          )}
        </TabsContent>

        <TabsContent value="withdrawals" className="space-y-4">
          <Card>
            <CardContent className="pt-6">
              <WithdrawList mode="user" />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
