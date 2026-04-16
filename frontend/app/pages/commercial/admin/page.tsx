import React, { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from "@/components/ui/card";
import {
  ShieldAlert,
  Loader2,
  Calculator,
  RefreshCw,
  DollarSign,
  ShieldCheck,
  Users,
  Coins,
} from "lucide-react";
import { IDUNN_API } from "~/api";
import { toast } from "sonner";

// 假设 IDUNN_API 是全局可用或已导入的
// import { IDUNN_API } from '@/api';

export default function AdminDashboard() {
  document.title = "商业虚拟点数控制台";

  const [isAdmin, setIsAdmin] = useState<boolean | null>(null);
  const [loadingAction, setLoadingAction] = useState<string | null>(null);

  useEffect(() => {
    checkPermission();
  }, []);

  // 检查管理员权限
  const checkPermission = async () => {
    try {
      // 调用鉴权接口
      await IDUNN_API.apiV1CommercialAdminGet();
      // 如果接口没有抛出异常，说明有权限（具体可根据你的后端实际返回结构调整，例如 res.data === true）
      setIsAdmin(true);
    } catch (error) {
      // 捕获到错误（如 401/403）则视为无权限
      setIsAdmin(false);
    }
  };

  // 通用触发函数
  const handleTriggerAction = async (
    apiFunction: () => Promise<any>,
    actionId: string,
    actionName: string,
  ) => {
    setLoadingAction(actionId);
    try {
      await apiFunction();
      toast("✅ 操作成功", {
        description: `${actionName} 指令已成功发送。`,
      });
    } catch (error) {
      console.error(error);
      toast.warning("❌ 操作失败", {
        description: `${actionName} 触发失败，请检查网络或联系技术支持。`,
      });
    } finally {
      setLoadingAction(null);
    }
  };

  // 状态 1：正在检查权限（加载中）
  if (isAdmin === null) {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center space-y-4">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        <p className="text-sm text-muted-foreground animate-pulse">
          正在验证管理员权限...
        </p>
      </div>
    );
  }

  // 状态 2：无权限页面
  if (!isAdmin) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[70vh] text-center px-4 animate-in fade-in zoom-in duration-500">
        <div className="bg-destructive/10 p-5 rounded-full mb-6">
          <ShieldAlert className="h-16 w-16 text-destructive" />
        </div>
        <h2 className="text-3xl font-bold tracking-tight mb-3">访问受限</h2>
        <p className="text-muted-foreground max-w-md text-lg">
          抱歉，您当前使用的账号没有管理员权限。如果您认为这是一个错误，请联系系统管理员获取访问权限。
        </p>
        <Button
          variant="outline"
          className="mt-8"
          onClick={() => window.history.back()}
        >
          返回上一页
        </Button>
      </div>
    );
  }

  // 状态 3：有权限，展示管理员面板
  return (
    <div className="container mx-auto py-10 max-w-5xl animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="flex items-center space-x-3 mb-8">
        <div className="bg-primary/10 p-2 rounded-lg">
          <ShieldCheck className="h-8 w-8 text-primary" />
        </div>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">商业虚拟点数控制台</h1>
          <p className="text-muted-foreground mt-1">
            管理和手动触发核心商业计算与虚拟点数结算流程
          </p>
        </div>
      </div>

      <div className="text-2xl font-bold py-2">爬虫与同步</div>
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 pb-8">
        {/* 卡片 1：结算单实际虚拟点数结算 */}
        <Card className="flex flex-col transition-all hover:shadow-md">
          <CardHeader>
            <RefreshCw className="h-6 w-6 text-primary mb-2" />
            <CardTitle className="text-lg">同步爬虫商品</CardTitle>
            <CardDescription>触发重新从爬虫日志同步商品。</CardDescription>
          </CardHeader>
          <CardContent className="flex-1"></CardContent>
          <CardFooter>
            <Button
              className="w-full"
              variant="default"
              disabled={loadingAction === "ne-product"}
              onClick={() =>
                handleTriggerAction(
                  () => IDUNN_API.apiV1CommercialAdminSyncNeProductGet(),
                  "ne-product",
                  "同步商品",
                )
              }
            >
              {loadingAction === "ne-product" ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" /> 执行中...
                </>
              ) : (
                "同步商品"
              )}
            </Button>
          </CardFooter>
        </Card>

        {/* 卡片 2：同步订单 */}
        <Card className="flex flex-col transition-all hover:shadow-md">
          <CardHeader>
            <Coins className="h-6 w-6 text-primary mb-2" />
            <CardTitle className="text-lg">同步爬虫订单</CardTitle>
            <CardDescription>触发重新从爬虫日志同步订单。</CardDescription>
          </CardHeader>
          <CardContent className="flex-1"></CardContent>
          <CardFooter>
            <Button
              className="w-full"
              variant="default"
              disabled={loadingAction === "ne-order"}
              onClick={() =>
                handleTriggerAction(
                  () => IDUNN_API.apiV1CommercialAdminSyncNeOrderGet(),
                  "ne-order",
                  "同步订单",
                )
              }
            >
              {loadingAction === "ne-order" ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" /> 执行中...
                </>
              ) : (
                "同步订单"
              )}
            </Button>
          </CardFooter>
        </Card>
      </div>

      <div className="text-2xl font-bold py-2">虚拟点数与结算</div>
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 pb-4">
        {/* 卡片 1：结算单实际虚拟点数结算 */}
        <Card className="flex flex-col transition-all hover:shadow-md">
          <CardHeader>
            <Users className="h-6 w-6 text-green-500 mb-2" />
            <CardTitle className="text-lg">订单分成分解</CardTitle>
            <CardDescription>
              触发系统重新进行订单分成的分解与归属计算。
            </CardDescription>
          </CardHeader>
          <CardContent className="flex-1"></CardContent>
          <CardFooter>
            <Button
              className="w-full"
              variant="default"
              disabled={loadingAction === "order"}
              onClick={() =>
                handleTriggerAction(
                  () => IDUNN_API.apiV1CommercialAdminCalculateOrderGet(),
                  "order",
                  "订单分成分解计算",
                )
              }
            >
              {loadingAction === "order" ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" /> 执行中...
                </>
              ) : (
                "触发分解"
              )}
            </Button>
          </CardFooter>
        </Card>

        {/* 卡片 2：订单收益分解计算 */}
        <Card className="flex flex-col transition-all hover:shadow-md">
          <CardHeader>
            <Calculator className="h-6 w-6 text-blue-500 mb-2" />
            <CardTitle className="text-lg">结算实际虚拟点数</CardTitle>
            <CardDescription>
              重新触发结算单到实际点数的计算逻辑。
            </CardDescription>
          </CardHeader>
          <CardContent className="flex-1">
            {/* 留空以保持对齐，或者可以放一些最后更新时间等信息 */}
          </CardContent>
          <CardFooter>
            <Button
              className="w-full"
              disabled={loadingAction === "checkout"}
              onClick={() =>
                handleTriggerAction(
                  () =>
                    IDUNN_API.apiV1CommercialAdminCalculateCheckoutDetailGet(),
                  "checkout",
                  "结算单虚拟点数计算",
                )
              }
            >
              {loadingAction === "checkout" ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" /> 执行中...
                </>
              ) : (
                "触发计算"
              )}
            </Button>
          </CardFooter>
        </Card>

        {/* 卡片 3：释放冻结虚拟点数 */}
        <Card className="flex flex-col transition-all hover:shadow-md">
          <CardHeader>
            <DollarSign className="h-6 w-6 text-amber-500 mb-2" />
            <CardTitle className="text-lg">释放冻结虚拟点数</CardTitle>
            <CardDescription>
              触发计算，将符合条件的保留点数释放至可用虚拟点数。
            </CardDescription>
          </CardHeader>
          <CardContent className="flex-1"></CardContent>
          <CardFooter>
            <Button
              className="w-full bg-amber-600 hover:bg-amber-700 text-white"
              disabled={loadingAction === "release"}
              onClick={() =>
                handleTriggerAction(
                  () => IDUNN_API.apiV1CommercialAdminCalculateReleaseGet(),
                  "release",
                  "释放冻结虚拟点数",
                )
              }
            >
              {loadingAction === "release" ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" /> 执行中...
                </>
              ) : (
                "触发释放"
              )}
            </Button>
          </CardFooter>
        </Card>
      </div>
    </div>
  );
}
