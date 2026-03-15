import React from 'react';
import {
    ShieldCheck,
    AlertCircle,
    Search,
    Info,
    CheckCircle2,   // 新增
    AlertTriangle,  // 新增
    ArrowRight,     // 新增
    XCircle,        // 新增
    RefreshCcw      // 新增
} from 'lucide-react';

// 引入 Badge 组件 (请确保你有这个组件，如果没有可以用 span + class 替代)
import { Badge } from "@/components/ui/badge";

// Shadcn UI 组件
import {
    Card,
    CardContent,
    CardHeader,
    CardTitle,
    CardDescription
} from '@/components/ui/card';
import {
    Alert,
    AlertDescription,
    AlertTitle
} from "@/components/ui/alert";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

// 业务组件
import { WithdrawList } from './WithdrawList'; // 引用之前编写的提现列表组件

export function AdminWithdrawConsole() {
    return (
        <div className="container mx-auto p-6 space-y-8 pb-16">

            {/* 头部：标题与安全警告 */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b pb-6">
                <div>
                    <div className="flex items-center gap-2">
                        <ShieldCheck className="h-8 w-8 text-primary" />
                        <h1 className="text-3xl font-bold tracking-tight">提现管理控制台</h1>
                    </div>
                    <p className="text-muted-foreground mt-2">
                        审核系统用户的提现申请、执行打款操作并处理异常申诉。
                    </p>
                </div>

                <Alert className="max-w-md bg-blue-50 border-blue-200 dark:bg-blue-950/20 dark:border-blue-900">
                    <AlertCircle className="h-4 w-4 text-blue-600" />
                    <AlertTitle className="text-blue-700 dark:text-blue-400">操作合规性提示</AlertTitle>
                    <AlertDescription className="text-xs text-blue-600/80">
                        所有提现审批动作均会被审计记录。在执行 <strong>PAID</strong> 操作前，请务必核对第三方流水单号。
                    </AlertDescription>
                </Alert>
            </div>

            {/* 第一部分：管理列表主体（全宽） */}
            <div className="w-full space-y-4">
                <Tabs defaultValue="all" className="w-full">
                    <div className="flex items-center justify-between mb-2">
                        <TabsList>
                            <TabsTrigger value="all" className="text-xs">
                                全量记录
                            </TabsTrigger>
                            <TabsTrigger value="active" className="text-xs">
                                需我处理
                            </TabsTrigger>
                        </TabsList>

                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            <Search className="h-3 w-3" />
                            <span>支持通过 Crud 表格内部字段排序检索</span>
                        </div>
                    </div>

                    <TabsContent value="all" className="mt-0">

                        {/* 调用 WithdrawList，指定为 admin 模式 */}
                        <WithdrawList mode="admin" />

                    </TabsContent>

                    <TabsContent value="active" className="mt-0">
                        {/* 传递过滤参数，只显示 CREATED/APPROVED/ERROR */}

                        <WithdrawList mode="admin" />

                    </TabsContent>
                </Tabs>
            </div>

            {/* 第二部分：业务说明 */}
            {/* 第二部分：业务说明与流程可视化 */}
            <Card className="bg-slate-50 border-slate-200 dark:bg-slate-900 dark:border-slate-800">
                <CardHeader>
                    <CardTitle className="text-lg flex items-center gap-2">
                        <Info className="h-5 w-5 text-blue-500" />
                        提现业务全流程说明
                    </CardTitle>
                    <CardDescription>
                        全局状态流转与操作规范指南（左侧为主流程，右侧为异常处理）
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">

                        {/* 左列：主流程 (时间轴视图) */}
                        <div className="space-y-4">
                            <h3 className="font-semibold text-base flex items-center gap-2 text-primary border-b pb-2">
                                <CheckCircle2 className="h-4 w-4 text-green-500" /> 正常提现流转 (Main Flow)
                            </h3>
                            <div className="relative border-l-2 border-slate-200 dark:border-slate-700 ml-3 space-y-6 pb-4 pt-2">

                                {/* 节点 1 */}
                                <div className="relative pl-6">
                                    <span className="absolute -left-2.25 top-1 h-4 w-4 rounded-full bg-slate-200 dark:bg-slate-700 border-2 border-white dark:border-slate-900" />
                                    <div className="flex items-center gap-2 mb-1">
                                        <span className="text-sm font-semibold">1. 发起申请</span>
                                        <Badge className="bg-slate-500 hover:bg-slate-600">CREATED</Badge>
                                    </div>
                                    <p className="text-xs text-muted-foreground leading-relaxed">
                                        用户提出申请生成提现记录。系统自动扣除用户余额，并生成出账流水（备注：用户提现）。
                                    </p>
                                </div>

                                {/* 节点 2 */}
                                <div className="relative pl-6">
                                    <span className="absolute -left-2.25 top-1 h-4 w-4 rounded-full bg-blue-300 border-2 border-white dark:border-slate-900" />
                                    <div className="flex items-center gap-2 mb-1">
                                        <span className="text-sm font-semibold">2. 管理员审批</span>
                                        <Badge className="bg-blue-500 hover:bg-blue-600">APPROVED</Badge>
                                    </div>
                                    <p className="text-xs text-muted-foreground leading-relaxed">
                                        管理员核对无误后通过审批，记录正式进入待打款队列。
                                    </p>
                                </div>

                                {/* 节点 3 */}
                                <div className="relative pl-6">
                                    <span className="absolute -left-2.25 top-1 h-4 w-4 rounded-full bg-yellow-400 border-2 border-white dark:border-slate-900" />
                                    <div className="flex items-center gap-2 mb-1">
                                        <span className="text-sm font-semibold">3. 执行转账</span>
                                        <Badge className="bg-yellow-500 hover:bg-yellow-600">PAID</Badge>
                                    </div>
                                    <p className="text-xs text-muted-foreground leading-relaxed">
                                        管理员向用户账户实际转账。在系统中上传转账凭证与交易单号，记录打款时间。
                                    </p>
                                </div>

                                {/* 节点 4 */}
                                <div className="relative pl-6">
                                    <span className="absolute -left-2.25 top-1 h-4 w-4 rounded-full bg-green-400 border-2 border-white dark:border-slate-900" />
                                    <div className="flex items-center gap-2 mb-1">
                                        <span className="text-sm font-semibold">4. 用户确认</span>
                                        <Badge className="bg-green-500 hover:bg-green-600">FINISHED</Badge>
                                    </div>
                                    <p className="text-xs text-muted-foreground leading-relaxed">
                                        用户确认资金到账，在系统中点击确认收款，整个提现流程圆满结束。
                                    </p>
                                </div>

                            </div>
                        </div>

                        {/* 右列：异常与阻断流程 (卡片流转视图) */}
                        <div className="space-y-4">
                            <h3 className="font-semibold text-base flex items-center gap-2 text-destructive border-b pb-2">
                                <AlertTriangle className="h-4 w-4 text-orange-500" /> 异常与退回处理 (Exceptions)
                            </h3>

                            <div className="space-y-4 pt-2">
                                {/* 分支 1：审批拒绝 */}
                                <div className="bg-white dark:bg-slate-950 p-3.5 rounded-lg border shadow-sm">
                                    <div className="flex items-center gap-2 mb-2">
                                        <Badge variant="outline" className="text-slate-500">CREATED</Badge>
                                        <ArrowRight className="h-4 w-4 text-muted-foreground" />
                                        <Badge variant="destructive">REJECTED</Badge>
                                    </div>
                                    <p className="text-sm font-medium mb-1">审批不通过 / 拒绝提现</p>
                                    <p className="text-xs text-muted-foreground leading-relaxed">
                                        管理员拒绝申请（需填理由）。金额将<strong>自动退回</strong>用户系统账户，并生成入账流水（备注：提现退回+理由）。
                                    </p>
                                </div>

                                {/* 分支 2：打款异常 */}
                                <div className="bg-white dark:bg-slate-950 p-3.5 rounded-lg border shadow-sm border-orange-100 dark:border-orange-900">
                                    <div className="flex items-center gap-2 mb-2">
                                        <Badge className="bg-yellow-500 hover:bg-yellow-600">PAID</Badge>
                                        <ArrowRight className="h-4 w-4 text-muted-foreground" />
                                        <Badge variant="outline" className="text-orange-600 border-orange-200 bg-orange-50">ERROR</Badge>
                                    </div>
                                    <p className="text-sm font-medium mb-1">转账后未到账 / 支付异常</p>
                                    <p className="text-xs text-muted-foreground leading-relaxed">
                                        用户反馈未收到款项时，管理员将状态标记为异常（需附带说明），转入线下人工核实阶段。
                                    </p>
                                </div>

                                {/* 分支 3：异常最终决议 */}
                                <div className="bg-orange-50/50 dark:bg-orange-950/20 p-3.5 rounded-lg border border-orange-100 dark:border-orange-900">
                                    <p className="text-sm font-medium mb-3 flex items-center gap-2 text-orange-800 dark:text-orange-300">
                                        <RefreshCcw className="h-3.5 w-3.5" /> 异常状态 (ERROR) 决议方案
                                    </p>
                                    <div className="space-y-3">
                                        <div className="flex items-start gap-2">
                                            <XCircle className="h-4 w-4 text-destructive mt-0.5 shrink-0" />
                                            <div>
                                                <div className="flex items-center gap-2 mb-1">
                                                    <span className="text-xs font-medium">交易取消/失败</span>
                                                    <ArrowRight className="h-3 w-3 text-muted-foreground" />
                                                    <Badge variant="destructive" className="h-5 text-[10px]">REJECTED</Badge>
                                                </div>
                                                <p className="text-xs text-muted-foreground">管理员执行，附错误说明。资金自动退回用户账户。</p>
                                            </div>
                                        </div>
                                        <div className="h-px bg-orange-200/50 dark:bg-orange-800/50 w-full" />
                                        <div className="flex items-start gap-2">
                                            <CheckCircle2 className="h-4 w-4 text-green-500 mt-0.5 shrink-0" />
                                            <div>
                                                <div className="flex items-center gap-2 mb-1">
                                                    <span className="text-xs font-medium">误报/补发成功</span>
                                                    <ArrowRight className="h-3 w-3 text-muted-foreground" />
                                                    <Badge className="bg-green-500 hover:bg-green-600 h-5 text-[10px]">FINISHED</Badge>
                                                </div>
                                                <p className="text-xs text-muted-foreground">管理员或用户执行，附处理说明。提现流程完结。</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>

                    {/* 底部：硬性约束警告 */}
                    <Alert variant="destructive" className="mt-8 bg-red-50 dark:bg-red-950/20 border-red-200 dark:border-red-900">
                        <AlertCircle className="h-4 w-4" />
                        <AlertTitle>合规性强约束</AlertTitle>
                        <AlertDescription className="text-xs mt-1">
                            系统中除上述说明的流转路径外，<strong>其他任何状态转换途径均被视为非法操作</strong>，系统底层 API 应予以强制拦截。
                        </AlertDescription>
                    </Alert>

                </CardContent>
            </Card>

        </div>
    );
}