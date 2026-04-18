import { useEffect, useMemo, useState, type ReactNode } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Layers3, Boxes, RefreshCcw, MapPinned } from "lucide-react";
import apiClient from "@/lib/axios";

interface ProjectSettlementSnapshotPayload {
  projectEffectiveBlocks: number;
  scannedAtMs: number;
  sourceServerName?: string;
  templates: TemplateUsageSnapshot[];
  instances: InstanceUsageSnapshot[];
}

interface TemplateUsageSnapshot {
  templateId: string;
  templateName?: string;
  usageCount: number;
  totalManagedBlocks: number;
  versions: TemplateVersionUsageSnapshot[];
}

interface TemplateVersionUsageSnapshot {
  versionId: string;
  usageCount: number;
  totalManagedBlocks: number;
}

interface InstanceUsageSnapshot {
  instanceId: string;
  templateId: string;
  templateName?: string;
  versionId: string;
  placedByName?: string;
  placedAt?: number;
  minX?: number;
  minY?: number;
  minZ?: number;
  maxX?: number;
  maxY?: number;
  maxZ?: number;
  managedBlocks: number;
}

export function ProjectTemplateUsage({
  projectId,
  hasBounds,
}: {
  projectId: number;
  hasBounds: boolean;
}) {
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [snapshot, setSnapshot] = useState<ProjectSettlementSnapshotPayload | null>(null);

  const loadSnapshot = async () => {
    if (!hasBounds) {
      setLoading(false);
      setSnapshot(null);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get<ProjectSettlementSnapshotPayload>(
        `/api/v1/commercial/projects/${projectId}/settlement-snapshot`,
      );
      setSnapshot(response.data);
    } catch (err: any) {
      if (err?.response?.status === 404) {
        setSnapshot(null);
      } else {
        setError(err?.message || "加载项目模板统计失败");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadSnapshot();
  }, [projectId, hasBounds]);

  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      await loadSnapshot();
    } finally {
      setRefreshing(false);
    }
  };

  const sortedTemplates = useMemo(
    () =>
      [...(snapshot?.templates || [])].sort(
        (a, b) => (b.totalManagedBlocks || 0) - (a.totalManagedBlocks || 0),
      ),
    [snapshot],
  );

  const sortedInstances = useMemo(
    () =>
      [...(snapshot?.instances || [])].sort(
        (a, b) => (b.managedBlocks || 0) - (a.managedBlocks || 0),
      ),
    [snapshot],
  );

  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between gap-4">
        <div className="space-y-1">
          <CardTitle className="flex items-center gap-2">
            <Layers3 className="h-5 w-5" />
            项目内模板使用
          </CardTitle>
          <p className="text-sm text-muted-foreground">
            这里展示当前项目范围内识别到的模板与实例统计，模板结算会直接使用同一份快照。
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={handleRefresh} disabled={refreshing || loading || !hasBounds}>
          <RefreshCcw className={`mr-2 h-4 w-4 ${refreshing ? "animate-spin" : ""}`} />
          重新读取快照
        </Button>
      </CardHeader>
      <CardContent className="space-y-4">
        {!hasBounds ? (
          <EmptyState
            title="这个项目还没有录入范围"
            description="未录入范围时，会自动跳过模板分成并全部计入创作分成。录入范围并刷新后，这里才会出现模板与实例统计。"
          />
        ) : loading ? (
          <div className="space-y-3">
            <Skeleton className="h-24 w-full" />
            <Skeleton className="h-56 w-full" />
          </div>
        ) : error ? (
          <EmptyState title="模板统计加载失败" description={error} />
        ) : !snapshot ? (
          <EmptyState
            title="还没有可用的模板统计快照"
            description="当前项目暂时没有同步到模板使用统计。你可以点击右上角刷新统计，稍后再查看。"
          />
        ) : (
          <>
            <div className="grid gap-3 md:grid-cols-3">
              <MetricCard label="有效方块" value={snapshot.projectEffectiveBlocks || 0} />
              <MetricCard label="模板种类" value={sortedTemplates.length} />
              <MetricCard label="实例数量" value={sortedInstances.length} />
            </div>

            <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
              <Badge variant="outline">扫描时间：{formatTime(snapshot.scannedAtMs)}</Badge>
              {snapshot.sourceServerName ? (
                <Badge variant="secondary">来源：{snapshot.sourceServerName}</Badge>
              ) : null}
            </div>

            <Tabs defaultValue="templates" className="w-full">
              <TabsList className="grid w-full max-w-sm grid-cols-2">
                <TabsTrigger value="templates">模板列表</TabsTrigger>
                <TabsTrigger value="instances">实例列表</TabsTrigger>
              </TabsList>

              <TabsContent value="templates" className="mt-4">
                <div className="grid gap-3 md:grid-cols-2">
                  {sortedTemplates.length === 0 ? (
                    <EmptyState
                      title="项目范围内暂未识别到模板"
                      description="当前快照里没有模板使用记录，结算时会全部视为创作分成。"
                    />
                  ) : (
                    sortedTemplates.map((template) => (
                      <Card key={template.templateId} className="border-dashed">
                        <CardContent className="space-y-4 p-5">
                          <div className="flex items-start justify-between gap-3">
                            <div className="space-y-1">
                              <div className="text-base font-semibold">
                                {template.templateName || template.templateId}
                              </div>
                              <div className="text-xs text-muted-foreground break-all">
                                {template.templateId}
                              </div>
                            </div>
                            <Badge variant="secondary">{template.usageCount} 次</Badge>
                          </div>

                          <div className="grid grid-cols-2 gap-3 text-sm">
                            <DataPill label="覆盖方块" value={template.totalManagedBlocks} />
                            <DataPill
                              label="平均实例方块"
                              value={Math.round(
                                template.usageCount > 0
                                  ? template.totalManagedBlocks / template.usageCount
                                  : 0,
                              )}
                            />
                          </div>

                          <div className="space-y-2">
                            <div className="text-xs font-medium text-muted-foreground">
                              版本分布
                            </div>
                            <div className="space-y-2">
                              {template.versions.map((version) => (
                                <div
                                  key={`${template.templateId}-${version.versionId}`}
                                  className="flex items-center justify-between rounded-lg bg-muted/50 px-3 py-2 text-sm"
                                >
                                  <div className="font-mono text-xs">{version.versionId}</div>
                                  <div className="flex gap-2 text-xs text-muted-foreground">
                                    <span>{version.usageCount} 次</span>
                                    <span>{version.totalManagedBlocks} 方块</span>
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    ))
                  )}
                </div>
              </TabsContent>

              <TabsContent value="instances" className="mt-4">
                {sortedInstances.length === 0 ? (
                  <EmptyState
                    title="当前没有实例明细"
                    description="项目模板快照里还没有识别到实例，刷新统计后会在这里展示每个实例的范围、版本和覆盖方块数。"
                  />
                ) : (
                  <ScrollArea className="h-[480px] rounded-xl border">
                    <div className="space-y-3 p-4">
                      {sortedInstances.map((instance) => (
                        <Card key={instance.instanceId} className="border-border/60 shadow-none">
                          <CardContent className="space-y-3 p-4">
                            <div className="flex flex-wrap items-start justify-between gap-3">
                              <div className="space-y-1">
                                <div className="font-medium">
                                  {instance.templateName || instance.templateId}
                                </div>
                                <div className="text-xs text-muted-foreground break-all">
                                  实例 #{instance.instanceId}
                                </div>
                              </div>
                              <div className="flex flex-wrap gap-2">
                                <Badge variant="secondary">{instance.managedBlocks} 方块</Badge>
                                <Badge variant="outline" className="font-mono">
                                  {instance.versionId}
                                </Badge>
                              </div>
                            </div>

                            <div className="grid gap-3 md:grid-cols-3 text-sm">
                              <DataPill label="放置者" value={instance.placedByName || "-"} />
                              <DataPill label="放置时间" value={formatTime(instance.placedAt)} />
                              <DataPill
                                label="实例范围"
                                value={formatRange(instance)}
                                icon={<MapPinned className="h-3.5 w-3.5" />}
                              />
                            </div>
                          </CardContent>
                        </Card>
                      ))}
                    </div>
                  </ScrollArea>
                )}
              </TabsContent>
            </Tabs>
          </>
        )}
      </CardContent>
    </Card>
  );
}

function MetricCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-xl border bg-muted/30 p-4">
      <div className="text-xs uppercase tracking-wide text-muted-foreground">{label}</div>
      <div className="mt-2 text-2xl font-semibold">{value.toLocaleString()}</div>
    </div>
  );
}

function DataPill({
  label,
  value,
  icon,
}: {
  label: string;
  value: string | number;
  icon?: ReactNode;
}) {
  return (
    <div className="rounded-lg bg-muted/50 px-3 py-2">
      <div className="mb-1 flex items-center gap-1 text-xs text-muted-foreground">
        {icon}
        <span>{label}</span>
      </div>
      <div className="text-sm font-medium">{value}</div>
    </div>
  );
}

function EmptyState({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <div className="rounded-xl border border-dashed bg-muted/20 px-5 py-10 text-center">
      <Boxes className="mx-auto mb-3 h-10 w-10 text-muted-foreground" />
      <div className="text-base font-medium">{title}</div>
      <p className="mx-auto mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
        {description}
      </p>
    </div>
  );
}

function formatTime(ms?: number) {
  return ms ? new Date(ms).toLocaleString() : "-";
}

function formatRange(instance: InstanceUsageSnapshot) {
  if (
    instance.minX == null ||
    instance.minY == null ||
    instance.minZ == null ||
    instance.maxX == null ||
    instance.maxY == null ||
    instance.maxZ == null
  ) {
    return "-";
  }
  return `${instance.minX},${instance.minY},${instance.minZ} -> ${instance.maxX},${instance.maxY},${instance.maxZ}`;
}
