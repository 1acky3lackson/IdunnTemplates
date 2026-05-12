import React from "react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { AlertTriangle, ArrowRight, Blocks, Calculator, GitBranch, Users } from "lucide-react";
import apiClient from "@/lib/axios";

type DecimalLike = number | string | null | undefined;

interface SettlementBreakdown {
  orderId: number;
  highlightedCheckoutDetailId?: number | null;
  highlightedRole?: string | null;
  highlightedUsername?: string | null;
  orderStatus?: string | null;
  point?: number | null;
  pointType?: string | null;
  orderAmount?: DecimalLike;
  productId?: number | null;
  productName?: string | null;
  projectId?: number | null;
  projectName?: string | null;
  hasProjectRange: boolean;
  hasSettlementSnapshot: boolean;
  usedTemplateSettlement: boolean;
  params: {
    commercialRatio?: DecimalLike;
    templateDefectParam?: DecimalLike;
    placerRatio?: DecimalLike;
    uploaderRatio?: DecimalLike;
    releaseDelayDays?: number | null;
  };
  project: {
    effectiveBlocks?: number | null;
    scannedAtMs?: number | null;
    sourceServerName?: string | null;
    templateDecayedEffectiveBlocksTotal?: DecimalLike;
    templateRatio?: DecimalLike;
    creationRatio?: DecimalLike;
  };
  amounts: {
    templateGross?: DecimalLike;
    creationGross?: DecimalLike;
    builderFromTemplateGross?: DecimalLike;
    modifierFromTemplateGross?: DecimalLike;
    templateAuthorGross?: DecimalLike;
    builderFromCreationGross?: DecimalLike;
    helperFromCreationGross?: DecimalLike;
    modifierFromCreationGross?: DecimalLike;
    uploaderFromCreationGross?: DecimalLike;
    builderGross?: DecimalLike;
    modifierGross?: DecimalLike;
    uploaderGross?: DecimalLike;
  };
  templates: Array<{
    templateId: string;
    templateName?: string | null;
    authorUsername?: string | null;
    usageCount?: number | null;
    totalManagedBlocks?: number | null;
    averageEffectiveBlocks?: DecimalLike;
    geometricSeriesFactor?: DecimalLike;
    decayedEffectiveBlocks?: DecimalLike;
    shareRatio?: DecimalLike;
    gross?: DecimalLike;
    placerGross?: DecimalLike;
    authorGross?: DecimalLike;
    builderGross?: DecimalLike;
    modifierGross?: DecimalLike;
    versions?: Array<{
      versionId?: string | null;
      usageCount?: number | null;
      totalManagedBlocks?: number | null;
    }>;
  }>;
  roleGroups: Array<{
    role: string;
    label: string;
    gross?: DecimalLike;
    members: Array<{
      checkoutDetailId?: number | null;
      username?: string | null;
      contributionRatio?: DecimalLike;
      orderRatio?: DecimalLike;
      amount?: DecimalLike;
      status?: string | null;
      highlighted?: boolean;
    }>;
  }>;
  notes: string[];
}

const roleTone: Record<string, string> = {
  BUILDER: "bg-emerald-50 border-emerald-200 text-emerald-800",
  MODIFIER: "bg-sky-50 border-sky-200 text-sky-800",
  UPLOADER: "bg-amber-50 border-amber-200 text-amber-800",
  TEMPLATE_AUTHOR: "bg-fuchsia-50 border-fuchsia-200 text-fuchsia-800",
};

function toNumber(value: DecimalLike) {
  const parsed = Number(value ?? 0);
  return Number.isFinite(parsed) ? parsed : 0;
}

function formatPointAmount(value: DecimalLike) {
  return Math.round(toNumber(value) * 100).toLocaleString();
}

function formatPercent(value: DecimalLike) {
  return `${(toNumber(value) * 100).toFixed(2)}%`;
}

function formatBlocks(value?: number | null) {
  return value == null ? "-" : value.toLocaleString();
}

function StageCard({
  title,
  amount,
  hint,
  extra,
}: {
  title: string;
  amount: DecimalLike;
  hint: string;
  extra?: React.ReactNode;
}) {
  return (
    <Card className="w-[15rem] shrink-0 border-slate-200 bg-white/90 shadow-sm">
      <CardHeader className="gap-1 pb-3">
        <CardDescription>{hint}</CardDescription>
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="text-2xl font-semibold tracking-tight">
          {formatPointAmount(amount)}
          <span className="ml-2 text-sm font-normal text-muted-foreground">
            虚拟点数
          </span>
        </div>
        {extra}
      </CardContent>
    </Card>
  );
}

export function SettlementBreakdownView({
  orderId,
  checkoutDetailId,
}: {
  orderId?: number | null;
  checkoutDetailId?: number | null;
}) {
  const [data, setData] = React.useState<SettlementBreakdown | null>(null);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    const target = orderId
      ? `/api/v1/commercial/netease-orders/${orderId}/settlement-breakdown`
      : checkoutDetailId
        ? `/api/v1/commercial/balance/checkout-details/${checkoutDetailId}/settlement-breakdown`
        : null;

    if (!target) {
      setData(null);
      return;
    }

    let cancelled = false;
    setLoading(true);
    setError(null);
    apiClient
      .get(target)
      .then((response) => {
        if (!cancelled) {
          setData(response.data as SettlementBreakdown);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          console.error(err);
          setError("结算过程读取失败");
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [orderId, checkoutDetailId]);

  if (!orderId && !checkoutDetailId) return null;
  if (loading) return <div className="py-8 text-sm text-muted-foreground">正在加载结算过程...</div>;
  if (error) return <Alert><AlertDescription>{error}</AlertDescription></Alert>;
  if (!data) return null;

  return (
    <div className="space-y-5">
      <Card className="overflow-hidden border-slate-200 bg-gradient-to-br from-slate-50 via-white to-amber-50">
        <CardHeader className="pb-4">
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="outline">订单 #{data.orderId}</Badge>
            {data.productName ? <Badge variant="secondary">{data.productName}</Badge> : null}
            {data.projectName ? <Badge variant="secondary">项目：{data.projectName}</Badge> : null}
            {data.highlightedCheckoutDetailId ? (
              <Badge className="bg-amber-500 text-white hover:bg-amber-500">
                当前查看记录 #{data.highlightedCheckoutDetailId}
              </Badge>
            ) : null}
          </div>
          <CardTitle className="text-xl">结算过程公开视图</CardTitle>
          <CardDescription>
            将订单从总额、模板/创作拆分，到最终落到各角色与各用户的全部中间结果展开展示。
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-4">
            <MetricCard title="订单总额" value={formatPointAmount(data.orderAmount)} subtitle="进入结算流程的 100%" />
            <MetricCard title="模板占比 Rt" value={formatPercent(data.project.templateRatio)} subtitle={`创作占比 ${(toNumber(data.project.creationRatio) * 100).toFixed(2)}%`} />
            <MetricCard title="项目有效方块 B" value={formatBlocks(data.project.effectiveBlocks)} subtitle="排除空气等不可见方块后统计" />
            <MetricCard title="模板衰减有效方块 𝔅" value={toNumber(data.project.templateDecayedEffectiveBlocksTotal).toFixed(2)} subtitle={`λ = ${toNumber(data.params.templateDefectParam).toFixed(2)}`} />
          </div>

          {data.notes.length > 0 ? (
            <Alert className="border-amber-200 bg-amber-50 text-amber-900">
              <AlertTriangle className="h-4 w-4" />
              <AlertDescription className="space-y-1">
                {data.notes.map((note) => (
                  <div key={note}>{note}</div>
                ))}
              </AlertDescription>
            </Alert>
          ) : null}
        </CardContent>
      </Card>

      <Tabs defaultValue="overview">
        <TabsList variant="line">
          <TabsTrigger value="overview">流程总览</TabsTrigger>
          <TabsTrigger value="templates">模板拆分</TabsTrigger>
          <TabsTrigger value="allocations">最终分配</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="overflow-hidden rounded-xl border bg-slate-50/60">
            <div className="overflow-x-auto pb-2">
              <div className="flex min-w-max items-stretch gap-4 p-4">
              <StageCard
                title="订单总额"
                amount={data.orderAmount}
                hint="步骤 1"
                extra={<p className="text-sm text-muted-foreground">订单总额 100% 进入结算流程。</p>}
              />
              <ArrowChain />
              <StageCard
                title="模板部分 T"
                amount={data.amounts.templateGross}
                hint="步骤 1-2"
                extra={
                  <div className="space-y-2">
                    <Progress value={toNumber(data.project.templateRatio) * 100} />
                    <p className="text-sm text-muted-foreground">
                      T = 总额 × Rt = {formatPercent(data.project.templateRatio)}
                    </p>
                  </div>
                }
              />
              <ArrowChain />
              <StageCard
                title="创作部分 C"
                amount={data.amounts.creationGross}
                hint="步骤 1"
                extra={
                  <div className="space-y-2 text-sm text-muted-foreground">
                    <div>CB 建筑制作：{formatPointAmount(data.amounts.builderFromCreationGross)}</div>
                    <div>CF 辅助人员：{formatPointAmount(data.amounts.helperFromCreationGross)}</div>
                  </div>
                }
              />
              <ArrowChain />
              <StageCard
                title="辅助人员拆分 CF"
                amount={data.amounts.helperFromCreationGross}
                hint="步骤 8"
                extra={
                  <div className="space-y-2 text-sm text-muted-foreground">
                    <div>CFM 修改美化：{formatPointAmount(data.amounts.modifierFromCreationGross)}</div>
                    <div>CFA 包装宣传：{formatPointAmount(data.amounts.uploaderFromCreationGross)}</div>
                  </div>
                }
              />
              <ArrowChain />
              <StageCard
                title="最终角色总额"
                amount={data.orderAmount}
                hint="步骤 7-10"
                extra={
                  <div className="grid gap-2 text-sm text-muted-foreground">
                    <div>建筑制作 B：{formatPointAmount(data.amounts.builderGross)}</div>
                    <div>修改美化 M：{formatPointAmount(data.amounts.modifierGross)}</div>
                    <div>包装宣传 A：{formatPointAmount(data.amounts.uploaderGross)}</div>
                    <div>模板作者：{formatPointAmount(data.amounts.templateAuthorGross)}</div>
                  </div>
                }
              />
            </div>
            </div>
          </div>

          <div className="grid gap-4 lg:grid-cols-[1.15fr_0.85fr]">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base">
                  <Calculator className="h-4 w-4" />
                  关键公式中间量
                </CardTitle>
              </CardHeader>
              <CardContent className="grid gap-3 md:grid-cols-2">
                <FormulaLine label="B" value={formatBlocks(data.project.effectiveBlocks)} desc="项目有效方块数" />
                <FormulaLine label="𝔅" value={toNumber(data.project.templateDecayedEffectiveBlocksTotal).toFixed(2)} desc="模板衰减有效方块总数" />
                <FormulaLine label="Rt = 𝔅 / B" value={formatPercent(data.project.templateRatio)} desc="模板在总分成中的占比" />
                <FormulaLine label="1 - Rt" value={formatPercent(data.project.creationRatio)} desc="创作在总分成中的占比" />
                <FormulaLine label="λ" value={toNumber(data.params.templateDefectParam).toFixed(2)} desc="统一衰减系数" />
                <FormulaLine label="辅助人员比例" value={formatPercent(data.params.commercialRatio)} desc="创作/放置者分配中的辅助人员比例" />
                <FormulaLine label="放置者比例" value={formatPercent(data.params.placerRatio)} desc="模板份额中放置者占比" />
                <FormulaLine label="包装宣传比例" value={formatPercent(data.params.uploaderRatio)} desc="辅助人员中包装宣传占比" />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base">
                  <Blocks className="h-4 w-4" />
                  拆分结果总表
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3 text-sm">
                <AmountLine label="T 模板部分" value={data.amounts.templateGross} />
                <AmountLine label="C 创作部分" value={data.amounts.creationGross} />
                <Separator />
                <AmountLine label="sum(T_iPb) 模板建筑制作" value={data.amounts.builderFromTemplateGross} />
                <AmountLine label="sum(T_iPm) 模板修改美化" value={data.amounts.modifierFromTemplateGross} />
                <AmountLine label="sum(T_iM) 模板作者" value={data.amounts.templateAuthorGross} />
                <Separator />
                <AmountLine label="CB 创作建筑制作" value={data.amounts.builderFromCreationGross} />
                <AmountLine label="CFM 创作修改美化" value={data.amounts.modifierFromCreationGross} />
                <AmountLine label="CFA 包装宣传" value={data.amounts.uploaderFromCreationGross} />
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="templates">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <GitBranch className="h-4 w-4" />
                模板分成展开
              </CardTitle>
              <CardDescription>
                每个模板都展示使用次数、有效方块、衰减有效方块、模板占比，以及作者/放置者/建筑制作/修改美化的拆分金额。
              </CardDescription>
            </CardHeader>
            <CardContent>
              {data.templates.length === 0 ? (
                <div className="rounded-lg border border-dashed p-6 text-sm text-muted-foreground">
                  当前订单没有进入模板结算流程，或项目范围内未识别到模板使用。
                </div>
              ) : (
                <Accordion type="multiple" className="w-full">
                  {data.templates.map((template) => (
                    <AccordionItem key={template.templateId} value={template.templateId}>
                      <AccordionTrigger className="hover:no-underline">
                        <div className="flex w-full flex-wrap items-center justify-between gap-3 text-left">
                          <div>
                            <div className="font-medium">{template.templateName || template.templateId}</div>
                            <div className="text-xs text-muted-foreground">
                              作者 {template.authorUsername || "-"} · 使用 {template.usageCount ?? 0} 次
                            </div>
                          </div>
                          <div className="flex flex-wrap gap-2 text-xs">
                            <Badge variant="outline">r_i {formatPercent(template.shareRatio)}</Badge>
                            <Badge variant="outline">𝔅_i {toNumber(template.decayedEffectiveBlocks).toFixed(2)}</Badge>
                            <Badge variant="outline">T_i {formatPointAmount(template.gross)}</Badge>
                          </div>
                        </div>
                      </AccordionTrigger>
                      <AccordionContent>
                        <div className="grid gap-4 rounded-xl bg-slate-50/70 p-4 md:grid-cols-2">
                          <FormulaLine label="总覆盖方块" value={formatBlocks(template.totalManagedBlocks)} desc="项目内该模板实例覆盖的有效方块总和" />
                          <FormulaLine label="平均 b" value={toNumber(template.averageEffectiveBlocks).toFixed(2)} desc="总覆盖方块 / 使用次数" />
                          <FormulaLine label="几何级数" value={toNumber(template.geometricSeriesFactor).toFixed(4)} desc="(1 - λ^c_i) / (1 - λ)" />
                          <FormulaLine label="𝔅_i" value={toNumber(template.decayedEffectiveBlocks).toFixed(2)} desc="b × 几何级数" />
                          <FormulaLine label="模板作者 T_iM" value={formatPointAmount(template.authorGross)} desc="模板份额中归作者的部分" />
                          <FormulaLine label="放置者 T_iP" value={formatPointAmount(template.placerGross)} desc="模板份额中归放置者链路的部分" />
                          <FormulaLine label="建筑制作 T_iPb" value={formatPointAmount(template.builderGross)} desc="放置者部分中进入建筑制作的部分" />
                          <FormulaLine label="修改美化 T_iPm" value={formatPointAmount(template.modifierGross)} desc="放置者部分中进入修改美化的部分" />
                        </div>
                        {template.versions?.length ? (
                          <div className="mt-4 rounded-xl border bg-white p-4">
                            <div className="mb-3 text-sm font-medium">版本分布</div>
                            <div className="space-y-2">
                              {template.versions.map((version) => (
                                <div key={version.versionId} className="flex items-center justify-between rounded-lg bg-slate-50 px-3 py-2 text-sm">
                                  <div className="font-mono text-xs">{version.versionId || "-"}</div>
                                  <div className="text-muted-foreground">
                                    {version.usageCount ?? 0} 次 · {formatBlocks(version.totalManagedBlocks)} 方块
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        ) : null}
                      </AccordionContent>
                    </AccordionItem>
                  ))}
                </Accordion>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="allocations">
          <div className="grid gap-4 xl:grid-cols-2">
            {data.roleGroups.map((group) => (
              <Card key={group.role} className="overflow-hidden">
                <CardHeader className="pb-3">
                  <div className="flex items-center justify-between gap-2">
                    <CardTitle className="flex items-center gap-2 text-base">
                      <Users className="h-4 w-4" />
                      {group.label}
                    </CardTitle>
                    <Badge className={roleTone[group.role] || "bg-slate-100 text-slate-700"}>
                      {formatPointAmount(group.gross)} 虚拟点数
                    </Badge>
                  </div>
                  <CardDescription>
                    {group.members.length
                      ? "按照当前收益记录或贡献比例分配后的个人结果。"
                      : "当前该角色没有可展示的个人分配。"}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {group.members.map((member) => (
                      <div
                        key={`${group.role}-${member.checkoutDetailId ?? member.username}`}
                        className={[
                          "rounded-xl border p-3 transition-colors",
                          member.highlighted
                            ? "border-amber-300 bg-amber-50"
                            : "border-slate-200 bg-slate-50/70",
                        ].join(" ")}
                      >
                        <div className="flex flex-wrap items-center justify-between gap-3">
                          <div>
                            <div className="font-medium">{member.username || "-"}</div>
                            <div className="text-xs text-muted-foreground">
                              {member.checkoutDetailId ? `记录 #${member.checkoutDetailId}` : "预估分配"}
                              {member.status ? ` · ${member.status}` : ""}
                            </div>
                          </div>
                          <div className="text-right">
                            <div className="text-lg font-semibold">{formatPointAmount(member.amount)}</div>
                            <div className="text-xs text-muted-foreground">虚拟点数</div>
                          </div>
                        </div>
                        <div className="mt-3 grid gap-2 text-xs text-muted-foreground md:grid-cols-2">
                          <div>组内分配比例：{formatPercent(member.contributionRatio)}</div>
                          <div>占订单比例：{formatPercent(member.orderRatio)}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}

function MetricCard({
  title,
  value,
  subtitle,
}: {
  title: string;
  value: string;
  subtitle: string;
}) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white/80 p-4 shadow-sm">
      <div className="text-xs uppercase tracking-[0.18em] text-muted-foreground">{title}</div>
      <div className="mt-2 text-2xl font-semibold tracking-tight">{value}</div>
      <div className="mt-1 text-xs text-muted-foreground">{subtitle}</div>
    </div>
  );
}

function FormulaLine({
  label,
  value,
  desc,
}: {
  label: string;
  value: string;
  desc: string;
}) {
  return (
    <div className="rounded-lg border bg-white p-3">
      <div className="text-xs uppercase tracking-[0.18em] text-muted-foreground">{label}</div>
      <div className="mt-2 text-lg font-semibold">{value}</div>
      <div className="mt-1 text-xs text-muted-foreground">{desc}</div>
    </div>
  );
}

function AmountLine({ label, value }: { label: string; value: DecimalLike }) {
  return (
    <div className="flex items-center justify-between gap-3">
      <span className="text-muted-foreground">{label}</span>
      <span className="font-medium">{formatPointAmount(value)}</span>
    </div>
  );
}

function ArrowChain() {
  return (
    <div className="flex items-center justify-center px-1 text-muted-foreground">
      <ArrowRight className="h-5 w-5" />
    </div>
  );
}
