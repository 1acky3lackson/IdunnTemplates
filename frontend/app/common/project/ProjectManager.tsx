// ProjectManagerPage.tsx
import React, { useState, useCallback, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useInView } from 'react-intersection-observer'; // 引入 useInView

// Shadcn UI 组件
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from '@/components/ui/sheet';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import type { Schema, SearchParam } from '../util/search-test-utils';
import { useWaterfall, useWaterfallCachedComponents, WaterfallProvider, type PageResponse } from '../util/waterfall-provider';
import type { Project } from '~/api/generated';
import { Link } from 'react-router';

// ---------- 类型定义 ----------

export const projectSchema = {
  name: { search: { fuzzy: true }, sort: true },
  displayName: { search: { fuzzy: true } },
  kind: { search: { fuzzy: false } },
  'world.id': { search: { fuzzy: false } },
  id: { sort: true },
} as const;

type ProjectSearchParams = SearchParam<typeof projectSchema>;

export interface ProjectApi {
  fetchProjects: (page: number, criteria: ProjectSearchParams) => Promise<PageResponse<Project>>;
  createProject: (data: Partial<Project>) => Promise<Project>;
  updateProject: (id: number, data: Partial<Project>) => Promise<Project>;
}

const projectFormSchema = z.object({
  name: z.string().min(1, '名称不能为空'),
  displayName: z.string().min(1, '显示名称不能为空'),
  description: z.string().optional(),
  pathName: z.string().min(1, '路径不能为空'),
  kind: z.string().min(1, '类型不能为空'),
  modelKind: z.string().optional(),
  worldId: z.number().optional(),
  minX: z.number().optional(),
  minY: z.number().optional(),
  minZ: z.number().optional(),
  maxX: z.number().optional(),
  maxY: z.number().optional(),
  maxZ: z.number().optional(),
  tpX: z.number().optional(),
  tpY: z.number().optional(),
  tpZ: z.number().optional(),
  tpYaw: z.number().optional(),
  tpPitch: z.number().optional(),
  parentProjectId: z.number().optional(),
});

type ProjectFormValues = z.infer<typeof projectFormSchema>;

// ---------- 主页面组件 ----------
interface ProjectManagerPageProps {
  api: ProjectApi;
}

export function ProjectManagerPage({ api }: ProjectManagerPageProps) {
  const fetchData = useCallback(
    async (page: number, criteria: ProjectSearchParams) => {
      return api.fetchProjects(page, criteria);
    },
    [api]
  );

  return (
    <WaterfallProvider
      initialCriteria={{}} // 初始无搜索条件
      fetchData={fetchData}
      getId={(item: Project) => item.id}
    >
      <div className="container mx-auto p-4">
        <div className="flex justify-between items-center mb-4">
          <h1 className="text-2xl font-bold">项目管理</h1>
          <AddProjectDialog api={api} />
        </div>
        <ProjectListWithWaterfall api={api} />
      </div>
    </WaterfallProvider>
  );
}

// ---------- 项目列表及搜索组件（使用 Waterfall 上下文）----------
function ProjectListWithWaterfall({ api }: { api: ProjectApi }) {
  const { items, loading, hasMore, loadMore, search, error } = useWaterfall<Project, ProjectSearchParams>();

  // 搜索输入框本地状态
  const [searchInputs, setSearchInputs] = useState({
    name: '',
    displayName: '',
    kind: '',
    worldId: '',
  });

  // 构建搜索条件
  const buildCriteria = useCallback((): ProjectSearchParams => {
    const criteria: ProjectSearchParams = {};
    if (searchInputs.name) criteria.name = { value: searchInputs.name, fuzzy: true };
    if (searchInputs.displayName) criteria.displayName = { value: searchInputs.displayName, fuzzy: true };
    if (searchInputs.kind) criteria.kind = searchInputs.kind;
    if (searchInputs.worldId) criteria['world.id'] = searchInputs.worldId;
    return criteria;
  }, [searchInputs]);

  const handleSearch = () => {
    search(buildCriteria());
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  // 无限滚动哨兵
  const { ref: sentinelRef, inView } = useInView({ threshold: 0.1, rootMargin: '100px' });

  useEffect(() => {
    if (inView && !loading && hasMore) {
      loadMore();
    }
  }, [inView, loading, hasMore, loadMore]);

  return (
    <div className="space-y-4">
      {/* 搜索栏 */}
      <div className="flex flex-wrap gap-2 mb-4">
        <Input
          placeholder="名称（模糊）"
          value={searchInputs.name}
          onChange={(e) => setSearchInputs(prev => ({ ...prev, name: e.target.value }))}
          onKeyDown={handleKeyDown}
          className="w-48"
        />
        <Input
          placeholder="显示名称（模糊）"
          value={searchInputs.displayName}
          onChange={(e) => setSearchInputs(prev => ({ ...prev, displayName: e.target.value }))}
          onKeyDown={handleKeyDown}
          className="w-48"
        />
        <Input
          placeholder="类型"
          value={searchInputs.kind}
          onChange={(e) => setSearchInputs(prev => ({ ...prev, kind: e.target.value }))}
          onKeyDown={handleKeyDown}
          className="w-32"
        />
        <Input
          placeholder="世界 ID"
          value={searchInputs.worldId}
          onChange={(e) => setSearchInputs(prev => ({ ...prev, worldId: e.target.value }))}
          onKeyDown={handleKeyDown}
          className="w-32"
        />
        <Button onClick={handleSearch}>搜索</Button>
      </div>

      {/* 项目列表 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {items.map((project) => (
          <ProjectCard key={project.id} project={project} api={api} />
        ))}
        {loading && <LoadingSkeletons count={3} />}
      </div>

      {/* 滚动加载哨兵 */}
      {hasMore && <div ref={sentinelRef} className="h-4" />}

      {!hasMore && items.length > 0 && (
        <p className="text-center text-muted-foreground">没有更多项目了</p>
      )}

      {/* 错误提示 */}
      {error && (
        <div className="p-4 text-red-500 bg-red-50 rounded">
          加载失败: {error.message}
        </div>
      )}
    </div>
  );
}

// 项目卡片（保持不变，仅导入 useWaterfall 用于刷新）
function ProjectCard({ project, api }: { project: Project; api: ProjectApi }) {
  const [editOpen, setEditOpen] = useState(false);
  const { refresh } = useWaterfall<Project, ProjectSearchParams>();

  return (
    <Card className="cursor-pointer hover:shadow-lg transition-shadow">
      <CardHeader>
        <CardTitle><Link to={`./${project.id}`}>{project.displayName}</Link></CardTitle>
      </CardHeader>
      <CardContent onClick={() => setEditOpen(true)}>
        <p className="text-sm text-muted-foreground">名称: {project.name}</p>
        <p className="text-sm text-muted-foreground">类型: {project.kind}</p>
        {project?.world && project.world !== null && <p className="text-sm text-muted-foreground">世界ID: {project.world.id}</p>}
      </CardContent>

      <Sheet open={editOpen} onOpenChange={setEditOpen}>
        <SheetContent className="sm:max-w-lg">
          <SheetHeader>
            <SheetTitle>编辑项目</SheetTitle>
          </SheetHeader>
          <ProjectForm
            project={project}
            onSubmit={async (data) => {
              try {
                await api.updateProject(project.id, data);
                setEditOpen(false);
                await refresh(); // 刷新列表
              } catch (error) {
                // 错误处理
              }
            }}
          />
        </SheetContent>
      </Sheet>
    </Card>
  );
}

// 新增项目对话框（保持不变）
function AddProjectDialog({ api }: { api: ProjectApi }) {
  const [open, setOpen] = useState(false);
  const { refresh } = useWaterfall<Project, ProjectSearchParams>();

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>新增项目</Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>新建项目</DialogTitle>
        </DialogHeader>
        <ProjectForm
          onSubmit={async (data) => {
            try {
              await api.createProject(data);
              setOpen(false);
              await refresh();
            } catch (error) {
              // 错误处理
            }
          }}
        />
      </DialogContent>
    </Dialog>
  );
}

// 项目表单（保持不变）
interface ProjectFormProps {
  project?: Project;
  onSubmit: (data: ProjectFormValues) => Promise<void>;
}

function ProjectForm({ project, onSubmit }: ProjectFormProps) {
  const form = useForm<ProjectFormValues>({
    resolver: zodResolver(projectFormSchema),
    defaultValues: {
      name: project?.name || '',
      displayName: project?.displayName || '',
      description: project?.description || '',
      pathName: project?.pathName || '',
      kind: project?.kind || '',
      modelKind: project?.modelKind || '',
      worldId: (project && project?.world === null ? undefined : project?.world?.id),
      minX: project?.minX,
      minY: project?.minY,
      minZ: project?.minZ,
      maxX: project?.maxX,
      maxY: project?.maxY,
      maxZ: project?.maxZ,
      tpX: project?.tpX,
      parentProjectId: project?.parentProjectId || undefined,
    },
  });

  const handleSubmit = async (values: ProjectFormValues) => {
    await onSubmit(values);
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
        <FormField
          control={form.control}
          name="name"
          render={({ field }) => (
            <FormItem>
              <FormLabel>名称 *</FormLabel>
              <FormControl>
                <Input {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="displayName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>显示名称 *</FormLabel>
              <FormControl>
                <Input {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="description"
          render={({ field }) => (
            <FormItem>
              <FormLabel>描述</FormLabel>
              <FormControl>
                <Input {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="pathName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>路径 *</FormLabel>
              <FormControl>
                <Input {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="kind"
          render={({ field }) => (
            <FormItem>
              <FormLabel>类型 *</FormLabel>
              <FormControl>
                <Input {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="modelKind"
          render={({ field }) => (
            <FormItem>
              <FormLabel>模型类型</FormLabel>
              <FormControl>
                <Input {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="worldId"
          render={({ field }) => (
            <FormItem>
              <FormLabel>世界ID</FormLabel>
              <FormControl>
                <Input
                  type="number"
                  {...field}
                  value={field.value ?? ''}
                  onChange={(e) => field.onChange(e.target.value ? Number(e.target.value) : undefined)}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        {/* 坐标范围等字段省略，可按需添加 */}
        <div className="flex justify-end gap-2">
          <Button type="submit">保存</Button>
        </div>
      </form>
    </Form>
  );
}

// 加载骨架（保持不变）
function LoadingSkeletons({ count }: { count: number }) {
  return (
    <>
      {Array.from({ length: count }).map((_, i) => (
        <Card key={i}>
          <CardHeader>
            <Skeleton className="h-6 w-3/4" />
          </CardHeader>
          <CardContent className="space-y-2">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-2/3" />
          </CardContent>
        </Card>
      ))}
    </>
  );
}