import React, { useEffect, useState } from 'react';
import { Wallet, Lock, Clock, ArrowRightLeft } from 'lucide-react';

// Shadcn UI
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';

// API 与子组件 (请根据实际路径调整)
import { IDUNN_API } from '~/api';
import type { BalanceInfo } from '~/api/generated';
import { UserTransactionRecordList } from './BalanceTables';

// ---------- 辅助格式化工具 ----------
const formatMoney = (amount?: number | null) => {
  if (amount === undefined || amount === null) return '¥0.00';
  return `¥${amount.toFixed(2)}`; // 根据实际金额单位调整
};

export function MyBalanceView() {
  const [balanceInfo, setBalanceInfo] = useState<BalanceInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const fetchMyBalance = async () => {
      try {
        setLoading(true);
        // 调用个人账户接口
        const res = await IDUNN_API.getMyBalance();
        // @ts-ignore
        setBalanceInfo(res.data);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('获取账户信息失败'));
      } finally {
        setLoading(false);
      }
    };

    fetchMyBalance();
  }, []);

  return (
    <div className="container mx-auto p-4 space-y-8 animate-in fade-in duration-500">
      
      {/* 页面标题 */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">我的财务中心</h1>
        <p className="text-muted-foreground mt-2">
          查看您的账户余额与交易明细
        </p>
      </div>

      {/* 顶部数据看板 (KPI Cards) */}
      <div className="grid gap-4 md:grid-cols-3">
        {/* 卡片 1: 账户可用余额 */}
        <Card className="shadow-sm border-emerald-100 dark:border-emerald-900/50">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              账户可用余额
            </CardTitle>
            <div className="h-8 w-8 bg-emerald-100 dark:bg-emerald-900/50 rounded-full flex items-center justify-center">
              <Wallet className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
            </div>
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-8 w-32 mt-1" />
            ) : (
              <div className="text-3xl font-bold text-emerald-600 dark:text-emerald-400">
                {formatMoney(balanceInfo?.availableBalance)}
              </div>
            )}
            <p className="text-xs text-muted-foreground mt-1">可随时申请提现的金额</p>
          </CardContent>
        </Card>

        {/* 卡片 2: 保留中 / 冻结金额 */}
        <Card className="shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              保留中金额
            </CardTitle>
            <div className="h-8 w-8 bg-amber-100 dark:bg-amber-900/50 rounded-full flex items-center justify-center">
              <Lock className="h-4 w-4 text-amber-600 dark:text-amber-400" />
            </div>
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-8 w-24 mt-1" />
            ) : (
              <div className="text-2xl font-bold">
                {formatMoney(balanceInfo?.frozenBalance)}
              </div>
            )}
            <p className="text-xs text-muted-foreground mt-1">因网易买家可能退款而保留中</p>
          </CardContent>
        </Card>

        {/* 卡片 3: 待网易提现 */}
        <Card className="shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              待网易提现 (预估)
            </CardTitle>
            <div className="h-8 w-8 bg-blue-100 dark:bg-blue-900/50 rounded-full flex items-center justify-center">
              <Clock className="h-4 w-4 text-blue-600 dark:text-blue-400" />
            </div>
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-8 w-24 mt-1" />
            ) : (
              <div className="text-2xl font-bold">
                {formatMoney(balanceInfo?.pendingBalance)}
              </div>
            )}
            <p className="text-xs text-muted-foreground mt-1">网易尚未结算的预期收益</p>
          </CardContent>
        </Card>
      </div>

      {error && (
        <div className="p-4 bg-destructive/10 text-destructive rounded-md text-sm font-medium">
          {error.message}
        </div>
      )}

      {/* 底部交易流水明细 */}
      {!loading && balanceInfo?.username && (
        <div className="space-y-4">
          <div className="flex items-center gap-2 border-b pb-2">
            <ArrowRightLeft className="h-5 w-5 text-muted-foreground" />
            <h2 className="text-xl font-semibold tracking-tight">交易流水明细</h2>
          </div>
          
          <div className="bg-card rounded-lg shadow-sm border p-4">
            {/* 复用之前的交易明细组件，通过 forceSearch 锁定当前用户 */}
            <UserTransactionRecordList 
              forceSearch={{ username: balanceInfo.username }} 
              pageSize={15} 
            />
          </div>
        </div>
      )}
      
      {/* 防止用户信息尚未加载时出现空表格区域 */}
      {loading && (
        <div className="space-y-4">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-100 w-full rounded-xl" />
        </div>
      )}

    </div>
  );
}