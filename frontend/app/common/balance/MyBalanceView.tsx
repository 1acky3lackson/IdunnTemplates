import React, { useEffect, useState } from "react";
import {
  Lock,
  Clock,
  ArrowRightLeft,
  History,
  Layers3,
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { toast } from "sonner";

// API 与子组件
import { IDUNN_API } from "~/api";
import type { BalanceInfo } from "~/api/generated";
import { UserTransactionRecordList } from "./BalanceTables";
import CheckoutDetails from "../checkout-details/CheckoutDetails";
import { useAuth } from "../auth/auth-provider";

// 格式化工具：统一使用 standard text
const formatMoney = (amount?: number | null) => {
  if (amount === undefined || amount === null) return "0";
  return `${Math.round(amount * 100).toLocaleString()}`;
};

export function MyBalanceView() {
  const [balanceInfo, setBalanceInfo] = useState<BalanceInfo | null>(null);
  const [loading, setLoading] = useState(true);
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
      toast.error("获取虚拟点数信息失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyBalance();
  }, [refreshKey]);

  return (
    <div className="container mx-auto p-4 space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      {/* 头部：使用标准 shadcn 字体和间距 */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-bold tracking-tight">虚拟点数中心</h1>
          <p className="text-sm text-muted-foreground">
            管理您的个人虚拟点数、收益分成记录及点数去向
          </p>
        </div>
      </div>

      {/* 数据看板：使用标准 Card 样式，通过 opacity 区分主次 */}
      <div className="grid gap-4 md:grid-cols-3">
        {/* 可用余额：主卡片使用 primary 背景 */}
        <Card className="relative overflow-hidden border-none bg-primary text-primary-foreground shadow-md">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium opacity-90">
              可用虚拟点数
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
              <Layers3 className="mr-1 h-3 w-3" /> 当前可用虚拟点数
            </div>
          </CardContent>
          <Layers3 className="absolute -right-4 -bottom-4 h-24 w-24 opacity-10 rotate-12" />
        </Card>

        {/* 冻结点数：使用默认 Card 样式 */}
        <Card>
          <CardHeader className="pb-2 flex flex-row items-center justify-between space-y-0">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              保留中虚拟点数
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

        {/* 待结算：使用默认 Card 样式 */}
        <Card>
          <CardHeader className="pb-2 flex flex-row items-center justify-between space-y-0">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              待结算虚拟点数 (预估)
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
              平台尚未完成处理的预估虚拟点数
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 底部：Tabs 使用系统默认样式，不强制颜色 */}
      <Tabs defaultValue="checkout-details" className="w-full">
        <div className="flex items-center justify-between mb-4">
          <TabsList className="grid w-full max-w-80 grid-cols-2">
            <TabsTrigger
              value="checkout-details"
            // className={authUser ? "" : "hidden"}
            >
              <History className="w-4 h-4 mr-2" /> 收益分成记录
            </TabsTrigger>
            <TabsTrigger value="transactions">
              <ArrowRightLeft className="w-4 h-4 mr-2" /> 虚拟点数变动
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
      </Tabs>
    </div>
  );
}
