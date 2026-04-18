import { useEffect, useMemo, useState, type ReactNode } from "react";
import type { Route } from "./+types/templates-detail";
import {
  AlertCircle,
  Box,
  ChevronDown,
  ChevronRight,
  Clock3,
  Download,
  GitCommit,
  History,
  Layers3,
  MapPinned,
  Move3D,
  RotateCw,
  User,
} from "lucide-react";
import type { Template } from "~/api/generated/model/template";
import type { TemplateVersion } from "~/api/generated/model/template-version";
import type { PageResponse, TableActions } from "~/common/generic-crud-table/generic-crud-table";
import { GenericCrudTable } from "~/common/generic-crud-table/generic-crud-table";
import { Card, CardContent, CardHeader, CardTitle } from "~/components/ui/card";
import { Badge } from "~/components/ui/badge";
import { Separator } from "~/components/ui/separator";
import { Button } from "~/components/ui/button";
import { Skeleton } from "~/components/ui/skeleton";
import SchematicViewer from "~/components/schematics/SchematicViewer";
import { Switch } from "~/components/ui/switch";
import { Slider } from "~/components/ui/slider";
import { Label } from "~/components/ui/label";
import {
  downloadTemplateFile,
  getSchemLinkForTemplateVersion,
} from "~/api/overrides/template-file-download-api";
import apiClient from "~/lib/axios";
import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "~/components/ui/resizable";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "~/components/ui/tabs";
import { cn } from "~/lib/utils";
import { toast } from "sonner";
import { useTheme } from "~/components/theme/theme-provider";

const RENDER_REFUSE_THRESHOLD = 100 * 60 * 100;
const gradientAlphaDark = "80";
const gradientAlphaLight = "25";
const gradientRadius = 60;

type TemplateInstanceDto = {
  id: string;
  templateId: string;
  currentVersionId?: string | null;
  worldId: string;
  x: number;
  y: number;
  z: number;
  rotationY: number;
  flipX: boolean;
  flipY: boolean;
  flipZ: boolean;
  autoUpdate: boolean;
  placedAt: number;
  placedBy?: string | null;
  placedByName?: string | null;
  deletedTimestamp?: number | null;
  maskXNeg: number;
  maskXPos: number;
  maskYNeg: number;
  maskYPos: number;
  maskZNeg: number;
  maskZPos: number;
  embeddedInTemplateId?: string | null;
  deleted: boolean;
  wild: boolean;
};

export function meta({ params }: Route.MetaArgs) {
  return [{ title: `模板详情 ${params.uuid}` }];
}

export function clientLoader({ params }: Route.ClientLoaderArgs) {
  return { uuid: params.uuid };
}

const formatTime = (value?: number | null) =>
  value ? new Date(value).toLocaleString() : "-";

const formatBool = (value: boolean) => (value ? "是" : "否");

const getTemplateVolume = (template: Template) =>
  template.metadata.width *
  template.metadata.height *
  template.metadata.length;

function getVersionDisplay(version?: TemplateVersion | null) {
  if (!version) return "最新版";
  return version.versionId || "未命名版本";
}

const getSeed = (str: string) => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  return hash;
};

const seededRandom = (seed: number) => {
  const x = Math.sin(seed) * 10000;
  return x - Math.floor(x);
};

const generateRandomColor = (seed: number, alpha: number) => {
  const hue = Math.floor(seededRandom(seed) * 360);
  const saturation = Math.floor(seededRandom(seed) * 60) + 20;
  const lightness = Math.floor(seededRandom(seed) * 80) + 10;
  return `hsla(${hue}, ${saturation}%, ${lightness}%, ${alpha / 100})`;
};

const generateMeshGradient = (colors: string[], id: string, theme?: string) => {
  if (!colors.length) return undefined;

  const alpha =
    theme?.includes("dark")
      ? gradientAlphaDark
      : gradientAlphaLight;

  const pool = [...colors];
  while (pool.length < 6) {
    pool.push(...colors);
  }

  let seed = getSeed(id);
  let minValidIndex = -1;

  return pool
    .slice(0, 6)
    .map((color, index) => {
      let finalColorWithAlpha: string;

      if (color.toLowerCase() === "#unknown") {
        if (minValidIndex === -1) {
          minValidIndex = index;
        }
        if (pool[index - minValidIndex] !== "#unknown") {
          finalColorWithAlpha = `${pool[index - minValidIndex]}${alpha}`;
        } else {
          finalColorWithAlpha = generateRandomColor(seed++, Number.parseInt(alpha, 10));
        }
      } else {
        finalColorWithAlpha = `${color}${alpha}`;
      }

      const posX = Math.floor(seededRandom(seed++) * 100);
      const posY = Math.floor(seededRandom(seed++) * 100);

      return `radial-gradient(at ${posX}% ${posY}%, ${finalColorWithAlpha} 0%, transparent ${gradientRadius}%)`;
    })
    .join(", ");
};

function TemplateViewerPanel({
  template,
  selectedVersion,
  onDownloadLatest,
  onDownloadSelected,
}: {
  template: Template;
  selectedVersion?: TemplateVersion | null;
  onDownloadLatest: () => void;
  onDownloadSelected: (version: TemplateVersion) => void;
}) {
  const { theme } = useTheme();
  const volume = getTemplateVolume(template);
  const tooLarge = volume >= RENDER_REFUSE_THRESHOLD;
  const schematicSrc = getSchemLinkForTemplateVersion(
    template.id,
    selectedVersion?.versionId,
  );
  const [orbit, setOrbit] = useState(true);
  const [orbitSpeed, setOrbitSpeed] = useState(0.015);
  const normalizedColors = useMemo(
    () =>
      (template.colorSchemes ?? []).map((color) =>
        color.startsWith("#") ? color : `#${color}`,
      ),
    [template.colorSchemes],
  );
  const backgroundStyle = useMemo(() => {
    const gradient = generateMeshGradient(
      normalizedColors,
      `${template.id}-${selectedVersion?.versionId ?? "latest"}-detail-viewer`,
      theme,
    );
    return gradient ? { backgroundImage: gradient } : undefined;
  }, [normalizedColors, template.id, selectedVersion?.versionId, theme]);

  return (
    <Card className="h-full overflow-hidden">
      <CardHeader className="pb-3">
        <CardTitle className="flex flex-wrap items-start justify-between gap-4">
          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2">
              <span>模型预览</span>
              <Badge variant="secondary" className="font-mono">
                {template.metadata.width} × {template.metadata.height} × {template.metadata.length}
              </Badge>
            </div>
            <div className="text-xs font-normal text-muted-foreground">
              当前预览：{getVersionDisplay(selectedVersion)}
            </div>
          </div>

          <div className="flex flex-wrap items-start justify-end gap-4">
            <div className="min-w-[15rem] space-y-3 rounded-xl border bg-muted/20 px-3 py-2">
              <div className="flex items-center justify-between gap-3">
                <div className="space-y-0.5">
                  <Label htmlFor="template-orbit-switch" className="text-xs font-medium">
                    自动旋转
                  </Label>
                  <div className="text-[11px] text-muted-foreground">
                    控制模型是否持续缓慢旋转
                  </div>
                </div>
                <Switch
                  id="template-orbit-switch"
                  checked={orbit}
                  onCheckedChange={setOrbit}
                />
              </div>

              <div className="space-y-2">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-medium">旋转速度</span>
                  <span className="font-mono text-muted-foreground">
                    {orbitSpeed.toFixed(3)}
                  </span>
                </div>
                <Slider
                  value={[orbitSpeed]}
                  min={0.005}
                  max={0.05}
                  step={0.001}
                  disabled={!orbit}
                  onValueChange={(values) => {
                    const nextSpeed = values[0];
                    if (typeof nextSpeed === "number") {
                      setOrbitSpeed(nextSpeed);
                    }
                  }}
                />
              </div>
            </div>

            <div className="flex flex-wrap gap-2">
              <Button variant="outline" size="sm" onClick={onDownloadLatest}>
              <Download className="mr-2 h-4 w-4" />
              下载最新版
              </Button>
              {selectedVersion?.versionId && (
                <Button size="sm" onClick={() => onDownloadSelected(selectedVersion)}>
                  <Download className="mr-2 h-4 w-4" />
                  下载当前版本
                </Button>
              )}
            </div>
          </div>
        </CardTitle>
      </CardHeader>
      <CardContent className="h-[calc(100%-5.25rem)]">
        <div
          className="relative h-full min-h-[360px] overflow-hidden rounded-xl border bg-muted/20"
          style={backgroundStyle}
        >
          <div className="absolute inset-0 bg-gradient-to-br from-background/20 via-transparent to-background/35" />
          <div
            className="pointer-events-none absolute inset-0 opacity-[0.03]"
            style={{
              backgroundImage:
                "url(\"data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E\")",
            }}
          />
          {tooLarge ? (
            <div className="relative z-10 flex h-full flex-col items-center justify-center gap-4 p-6 text-center">
              <div className="rounded-full border border-orange-300/60 bg-orange-100/60 p-4 text-orange-600 dark:border-orange-500/30 dark:bg-orange-500/10 dark:text-orange-300">
                <AlertCircle className="h-7 w-7" />
              </div>
              <div className="space-y-1">
                <div className="text-sm font-semibold">渲染已禁用</div>
                <div className="text-xs text-muted-foreground">
                  模板体积过大，已停用实时模型渲染以保证页面稳定性。
                </div>
              </div>
              <div className="rounded-md border bg-background/70 px-3 py-2 text-xs font-mono text-muted-foreground">
                体积 {volume.toLocaleString()} / 阈值 {RENDER_REFUSE_THRESHOLD.toLocaleString()}
              </div>
            </div>
          ) : (
            <div className="relative z-10 h-full">
              <SchematicViewer
                src={schematicSrc}
                options={{
                  orbit,
                  orbitSpeed,
                  renderArrow: false,
                  renderBars: false,
                  backgroundColor: "transparent",
                  antialias: true,
                }}
                loadingElement={
                  <div className="flex h-full flex-col items-center justify-center gap-3">
                    <Box className="h-8 w-8 text-primary/60" />
                    <div className="text-xs text-muted-foreground">
                      正在加载模板模型...
                    </div>
                  </div>
                }
                errorElement={
                  <div className="flex h-full flex-col items-center justify-center gap-3">
                    <AlertCircle className="h-8 w-8 text-destructive/70" />
                    <div className="text-xs text-muted-foreground">
                      模型渲染失败，请稍后重试
                    </div>
                  </div>
                }
              />
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

function TemplateInfoPanel({
  template,
  selectedVersion,
}: {
  template: Template;
  selectedVersion?: TemplateVersion | null;
}) {
  const latestVersion = template.latestVersions?.[0];

  return (
    <Card className="h-full">
      <CardHeader className="pb-3">
        <CardTitle>模板详细信息</CardTitle>
      </CardHeader>
      <CardContent className="space-y-6 overflow-y-auto">
        <section className="space-y-3">
          <div className="flex items-start justify-between gap-4">
            <div className="space-y-1">
              <h1 className="text-2xl font-bold tracking-tight">{template.name}</h1>
              <p className="break-all font-mono text-xs text-muted-foreground">
                {template.path}
              </p>
            </div>
            <Badge variant={template.locked ? "destructive" : "secondary"}>
              {template.locked ? "已锁定" : "开放中"}
            </Badge>
          </div>
        </section>

        <Separator />

        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <InfoItem icon={<User className="h-4 w-4" />} label="创建者" value={template.metadata.creatorId} />
          <InfoItem icon={<Clock3 className="h-4 w-4" />} label="创建时间" value={formatTime(template.metadata.creationTime)} />
          <InfoItem icon={<GitCommit className="h-4 w-4" />} label="最新版本" value={template.latestVersionName || "-"} />
          <InfoItem icon={<MapPinned className="h-4 w-4" />} label="世界 ID" value={template.metadata.worldId} />
          <InfoItem icon={<Move3D className="h-4 w-4" />} label="锚点坐标" value={`${template.metadata.anchorX}, ${template.metadata.anchorY}, ${template.metadata.anchorZ}`} />
          <InfoItem icon={<Box className="h-4 w-4" />} label="模板体积" value={getTemplateVolume(template).toLocaleString()} />
        </section>

        <Separator />

        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <InfoItem label="宽度" value={String(template.metadata.width)} />
          <InfoItem label="高度" value={String(template.metadata.height)} />
          <InfoItem label="长度" value={String(template.metadata.length)} />
          <InfoItem label="版本数量" value={String(template.versionCount ?? 0)} />
        </section>

        <Separator />

        <section className="space-y-3">
          <div className="text-sm font-semibold">附加状态</div>
          <div className="flex flex-wrap gap-2">
            <Badge variant="outline">可使用：{formatBool(template.canUse)}</Badge>
            <Badge variant="outline">可提交：{formatBool(template.canCommit)}</Badge>
            <Badge variant="outline">已删除：{formatBool(template.metadata.deleted)}</Badge>
            <Badge variant="outline">已锁定：{formatBool(template.metadata.locked)}</Badge>
          </div>
        </section>

        {selectedVersion && (
          <>
            <Separator />
            <section className="space-y-2">
              <div className="text-sm font-semibold">当前预览版本</div>
              <div className="rounded-lg border bg-muted/30 p-3 text-sm">
                <div className="font-medium">{selectedVersion.message || "无提交说明"}</div>
                <div className="mt-1 text-xs text-muted-foreground">
                  版本号：{selectedVersion.versionId || "-"}
                </div>
                <div className="mt-1 text-xs text-muted-foreground">
                  提交者：{selectedVersion.submitterId || "-"}
                </div>
                <div className="mt-1 text-xs text-muted-foreground">
                  提交时间：{formatTime(selectedVersion.createdAt)}
                </div>
              </div>
            </section>
          </>
        )}

        {latestVersion && latestVersion.versionId !== selectedVersion?.versionId && (
          <>
            <Separator />
            <section className="space-y-2">
              <div className="text-sm font-semibold">最近一次提交</div>
              <div className="rounded-lg border bg-muted/30 p-3 text-sm">
                <div className="font-medium">{latestVersion.message || "无提交说明"}</div>
                <div className="mt-1 text-xs text-muted-foreground">
                  提交者：{latestVersion.submitterId || "-"}
                </div>
              </div>
            </section>
          </>
        )}
      </CardContent>
    </Card>
  );
}

function InfoItem({
  icon,
  label,
  value,
}: {
  icon?: ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div className="rounded-lg border bg-muted/20 p-3">
      <div className="mb-1 flex items-center gap-2 text-xs text-muted-foreground">
        {icon}
        <span>{label}</span>
      </div>
      <div className="break-all text-sm font-medium">{value}</div>
    </div>
  );
}

function TemplateInstancesTable({ templateId }: { templateId: string }) {
  const fetchInstances = async (
    page: number,
    size: number,
    search: string,
    sort: string,
  ) => {
    const response = await apiClient.get<PageResponse<TemplateInstanceDto>>(
      `/api/v1/templates/${templateId}/instances`,
      {
        params: {
          page,
          size,
          search: search || undefined,
          sort: sort || undefined,
        },
      },
    );
    return response.data;
  };

  return (
    <GenericCrudTable<TemplateInstanceDto>
      uid="template-inst"
      getRowId={(row) => row.id}
      list={fetchInstances}
      pageSize={20}
      searchFields={[
        { key: "id", label: "实例 ID", fuzzy: true },
        { key: "placedByName", label: "放置者", fuzzy: true },
        { key: "currentVersionId", label: "版本号", fuzzy: true },
        { key: "worldId", label: "世界 ID", fuzzy: true },
        { key: "autoUpdate", label: "自动更新", fuzzy: false },
        { key: "deleted", label: "已删除", fuzzy: false },
      ]}
      schema={{
        id: {
          title: "实例 ID",
          sortable: true,
          render: (value) => (
            <span className="max-w-[12rem] truncate font-mono text-xs" title={String(value)}>
              {value}
            </span>
          ),
        },
        placedByName: {
          title: "放置者",
          filterable: true,
          sortable: true,
          render: (value, row) => value || row.placedBy || "-",
        },
        currentVersionId: {
          title: "当前版本",
          filterable: true,
          sortable: true,
          render: (value) => value || "-",
        },
        worldId: {
          title: "世界 ID",
          filterable: true,
          sortable: true,
          render: (value) => (
            <span className="max-w-[10rem] truncate font-mono text-xs" title={String(value)}>
              {value}
            </span>
          ),
        },
        x: {
          title: "坐标",
          sortable: true,
          render: (_value, row) => (
            <span className="font-mono text-xs">
              {row.x}, {row.y}, {row.z}
            </span>
          ),
        },
        rotationY: {
          title: "旋转",
          sortable: true,
          render: (value) => (
            <span className="inline-flex items-center gap-1 text-sm">
              <RotateCw className="h-3.5 w-3.5 text-muted-foreground" />
              {value}°
            </span>
          ),
        },
        autoUpdate: {
          title: "自动更新",
          filterable: true,
          sortable: true,
          render: (value) => (
            <Badge variant={value ? "secondary" : "outline"}>
              {value ? "开启" : "关闭"}
            </Badge>
          ),
        },
        deleted: {
          title: "状态",
          filterable: true,
          sortable: true,
          render: (value, row) => (
            <Badge variant={value ? "destructive" : "secondary"}>
              {value ? "已删除" : row.wild ? "独立实例" : "嵌套实例"}
            </Badge>
          ),
        },
        placedAt: {
          title: "放置时间",
          sortable: true,
          render: (value) => formatTime(value),
        },
      }}
    />
  );
}

function TemplateVersionsTable({
  templateId,
  selectedVersionId,
  onSelectVersion,
  onDownloadVersion,
}: {
  templateId: string;
  selectedVersionId?: string | null;
  onSelectVersion: (version: TemplateVersion) => void;
  onDownloadVersion: (version: TemplateVersion) => void;
}) {
  const [expandedVersionId, setExpandedVersionId] = useState<string | null>(
    selectedVersionId ?? null,
  );

  useEffect(() => {
    if (selectedVersionId) {
      setExpandedVersionId(selectedVersionId);
    }
  }, [selectedVersionId]);

  const fetchVersions = async (
    page: number,
    size: number,
    search: string,
    sort: string,
  ) => {
    const response = await apiClient.get<PageResponse<TemplateVersion>>(
      `/api/v1/templates/${templateId}/versions`,
      {
        params: {
          page,
          size,
          search: search || undefined,
          sort: sort || undefined,
        },
      },
    );
    return response.data;
  };

  const toggleExpand = (versionId?: string | null) => {
    if (!versionId) return;
    setExpandedVersionId((prev) => (prev === versionId ? null : versionId));
  };

  return (
    <GenericCrudTable<TemplateVersion>
      uid="template-versions"
      getRowId={(row) => row.versionId || row.id || "unknown-version"}
      list={fetchVersions}
      pageSize={12}
      searchFields={[
        { key: "versionId", label: "版本号", fuzzy: true },
        { key: "message", label: "提交信息", fuzzy: true },
        { key: "submitterId", label: "提交者", fuzzy: true },
      ]}
      schema={{
        versionId: { title: "版本号", sortable: true },
        message: { title: "提交信息", sortable: false },
        submitterId: { title: "提交者", sortable: true },
        createdAt: { title: "提交时间", sortable: true },
      }}
      customRenderer={{
        renderContainer: (nodes) => (
          <div className="space-y-3">{nodes}</div>
        ),
        renderLoading: () => (
          <div className="space-y-3">
            {Array.from({ length: 4 }).map((_, index) => (
              <Card key={index} className="border-dashed">
                <CardContent className="space-y-3 p-4">
                  <Skeleton className="h-4 w-40" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-16 w-full" />
                </CardContent>
              </Card>
            ))}
          </div>
        ),
        renderEmpty: () => (
          <Card className="border-dashed">
            <CardContent className="py-10 text-center text-sm text-muted-foreground">
              暂无版本数据
            </CardContent>
          </Card>
        ),
        renderElement: (row, actions: TableActions) => {
          const versionId = row.versionId || null;
          const expanded = versionId !== null && expandedVersionId === versionId;
          const selected = versionId !== null && selectedVersionId === versionId;

          return (
            <Card
              className={cn(
                "cursor-pointer border transition-colors hover:border-primary/50",
                selected && "border-primary bg-primary/5",
              )}
              onClick={() => {
                onSelectVersion(row);
                toggleExpand(versionId);
              }}
            >
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0 flex-1 space-y-2">
                    <div className="flex items-center gap-2">
                      {expanded ? (
                        <ChevronDown className="h-4 w-4 text-muted-foreground" />
                      ) : (
                        <ChevronRight className="h-4 w-4 text-muted-foreground" />
                      )}
                      <Badge variant={selected ? "default" : "secondary"} className="font-mono">
                        {row.versionId || "未知版本"}
                      </Badge>
                      {selected && <Badge variant="outline">当前预览</Badge>}
                    </div>
                    <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
                      <span className="inline-flex items-center gap-1">
                        <Clock3 className="h-3.5 w-3.5" />
                        {formatTime(row.createdAt)}
                      </span>
                      <span className="inline-flex items-center gap-1">
                        <User className="h-3.5 w-3.5" />
                        {row.submitterId || "-"}
                      </span>
                    </div>
                    <div className="line-clamp-2 text-sm font-medium">
                      {row.message || "无提交说明"}
                    </div>
                  </div>

                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={(event) => {
                      event.stopPropagation();
                      onDownloadVersion(row);
                    }}
                  >
                    <Download className="mr-2 h-4 w-4" />
                    下载
                  </Button>
                </div>

                {expanded && (
                  <div className="mt-4 space-y-3 border-t pt-4 text-sm">
                    <div className="grid gap-3 sm:grid-cols-2">
                      <InfoItem label="版本号" value={row.versionId || "-"} />
                      <InfoItem label="内部 ID" value={String(row.id ?? "-")} />
                      <InfoItem label="提交者" value={row.submitterId || "-"} />
                      <InfoItem label="创建时间" value={formatTime(row.createdAt)} />
                    </div>
                    <div className="rounded-lg border bg-muted/20 p-3">
                      <div className="mb-1 text-xs text-muted-foreground">提交说明</div>
                      <div className="whitespace-pre-wrap break-words">
                        {row.message || "无提交说明"}
                      </div>
                    </div>
                    <div className="flex justify-end">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={(event) => {
                          event.stopPropagation();
                          actions.refreshPage();
                        }}
                      >
                        刷新列表
                      </Button>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          );
        },
      }}
    />
  );
}

export default function TemplatesDetail({ loaderData }: Route.ComponentProps) {
  const { uuid } = loaderData;
  const [template, setTemplate] = useState<Template | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedVersion, setSelectedVersion] = useState<TemplateVersion | null>(null);
  const [activeTab, setActiveTab] = useState("versions");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    apiClient
      .get<Template>(`/api/v1/templates/${uuid}`)
      .then((response) => {
        if (cancelled) return;
        const nextTemplate = response.data;
        setTemplate(nextTemplate);
        setSelectedVersion(nextTemplate.latestVersions?.[0] ?? null);
        document.title = `${nextTemplate.name} - 模板详情`;
      })
      .catch((err) => {
        if (cancelled) return;
        console.error(err);
        setError("模板加载失败");
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [uuid]);

  const pageTitle = useMemo(() => {
    if (!template) return "模板详情";
    return `${template.name} · ${selectedVersion?.versionId || "最新版"}`;
  }, [template, selectedVersion]);

  const handleDownloadLatest = async () => {
    if (!template) return;
    try {
      await downloadTemplateFile(template.id, {
        filename: `${template.name}-latest.schem`,
      });
    } catch (err) {
      console.error(err);
      toast.error("下载最新版 schematic 失败");
    }
  };

  const handleDownloadVersion = async (version: TemplateVersion) => {
    if (!template || !version.versionId) return;
    try {
      await downloadTemplateFile(template.id, {
        version: version.versionId,
        filename: `${template.name}-${version.versionId}.schem`,
      });
    } catch (err) {
      console.error(err);
      toast.error("下载指定版本 schematic 失败");
    }
  };

  if (loading) {
    return <div className="container mx-auto py-10 text-sm text-muted-foreground">正在加载模板详情...</div>;
  }

  if (error || !template) {
    return (
      <div className="container mx-auto py-10">
        <Card>
          <CardContent className="flex min-h-[12rem] flex-col items-center justify-center gap-3 text-center">
            <AlertCircle className="h-8 w-8 text-destructive/70" />
            <div className="text-sm text-muted-foreground">{error || "模板不存在"}</div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto space-y-6 px-4 py-6">
      <div className="rounded-xl border bg-card p-4">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div className="space-y-1">
            <h1 className="text-2xl font-bold tracking-tight">{pageTitle}</h1>
            <div className="font-mono text-xs text-muted-foreground">{template.path}</div>
          </div>
          <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
            <Badge variant="secondary" className="gap-1">
              <Layers3 className="h-3.5 w-3.5" />
              {template.versionCount ?? 0} 个版本
            </Badge>
            <Badge variant="outline" className="gap-1">
              <History className="h-3.5 w-3.5" />
              右侧可切换版本或实例视图
            </Badge>
          </div>
        </div>
      </div>

      <div className="h-[calc(100vh-14rem)] min-h-[720px]">
        <ResizablePanelGroup orientation="horizontal" className="h-full rounded-xl border bg-background">
          <ResizablePanel defaultSize={58} minSize={40}>
            <ResizablePanelGroup orientation="vertical">
              <ResizablePanel defaultSize={56} minSize={35}>
                <div className="h-full p-4">
                  <TemplateViewerPanel
                    template={template}
                    selectedVersion={selectedVersion}
                    onDownloadLatest={handleDownloadLatest}
                    onDownloadSelected={handleDownloadVersion}
                  />
                </div>
              </ResizablePanel>
              <ResizableHandle withHandle />
              <ResizablePanel defaultSize={44} minSize={25}>
                <div className="h-full p-4 pt-0">
                  <TemplateInfoPanel template={template} selectedVersion={selectedVersion} />
                </div>
              </ResizablePanel>
            </ResizablePanelGroup>
          </ResizablePanel>

          <ResizableHandle withHandle />

          <ResizablePanel defaultSize={42} minSize={28}>
            <div className="h-full p-4">
              <Card className="flex h-full flex-col overflow-hidden">
                <CardHeader className="pb-3">
                  <div className="flex items-center justify-between gap-3">
                    <CardTitle>版本列表与实例列表</CardTitle>
                  </div>
                </CardHeader>
                <CardContent className="min-h-0 flex-1 overflow-hidden">
                  <Tabs value={activeTab} onValueChange={setActiveTab} className="h-full">
                    <TabsList className="mb-4 grid w-full grid-cols-2">
                      <TabsTrigger value="versions">版本列表</TabsTrigger>
                      <TabsTrigger value="instances">实例列表</TabsTrigger>
                    </TabsList>

                    <TabsContent value="versions" className="h-[calc(100%-3.25rem)] overflow-y-auto pr-1">
                      <TemplateVersionsTable
                        templateId={template.id}
                        selectedVersionId={selectedVersion?.versionId ?? template.latestVersionName}
                        onSelectVersion={setSelectedVersion}
                        onDownloadVersion={handleDownloadVersion}
                      />
                    </TabsContent>

                    <TabsContent value="instances" className="h-[calc(100%-3.25rem)] overflow-y-auto pr-1">
                      <TemplateInstancesTable templateId={template.id} />
                    </TabsContent>
                  </Tabs>
                </CardContent>
              </Card>
            </div>
          </ResizablePanel>
        </ResizablePanelGroup>
      </div>
    </div>
  );
}
