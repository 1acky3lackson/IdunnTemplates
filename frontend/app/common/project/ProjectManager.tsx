// ProjectManagerPage.tsx
import React, { useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Link } from 'react-router'; 

// Shadcn UI 组件
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';

// 自定义工具与组件
import type { Schema, SearchParam } from '../util/search-test-utils';
import type { Project } from '~/api/generated';
import { GenericCrudTable, type PageResponse } from '../generic-crud-table/generic-crud-table';
import { Copyable } from '../util/Copyable';

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

// ---------- 辅助转换函数 ----------
const parseSearchStringToCriteria = (search: string): ProjectSearchParams => {
  const criteria: any = {};
  if (!search) return criteria;
  
  search.split(',').forEach(part => {
      const [fieldWithOp, value] = part.split(':');
      if (!fieldWithOp || !value) return;
      
      if (fieldWithOp.endsWith('~')) {
          const field = fieldWithOp.slice(0, -1);
          criteria[field] = { value, fuzzy: true };
      } else {
          criteria[fieldWithOp] = value;
      }
  });
  return criteria as ProjectSearchParams;
};

// ---------- 主页面组件 ----------
interface ProjectManagerPageProps {
  api: ProjectApi;
}

export function ProjectManagerPage({ api }: ProjectManagerPageProps) {
  // 控制表格强制刷新的触发器
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const triggerRefresh = useCallback(() => setRefreshTrigger(prev => prev + 1), []);

  // 弹窗与抽屉的状态管理
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingProject, setEditingProject] = useState<Project | null>(null);

  // 适配 GenericCrudTable 的取数接口
  const fetchTableData = useCallback(async (page: number, size: number, search: string, sort: string) => {
    const criteria = parseSearchStringToCriteria(search);
    
    // 排序处理
    if (sort) {
        const [field, direction] = sort.split(',');
        criteria.sort = { [field]: direction };
    } else {
        criteria.sort = { id: 'desc' }; // 默认排序
    }

    return await api.fetchProjects(page, criteria);
  }, [api, refreshTrigger]);

  return (
    <div className="container mx-auto p-4 space-y-4">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">项目管理</h1>
      </div>

      <GenericCrudTable<Project>
        getRowId={(row) => row.id}
        list={fetchTableData}
        
        // 开启创建和编辑入口
        create={() => setIsCreateOpen(true)}
        modify={(row) => setEditingProject(row)}

        // 配置搜索字段
        searchFields={[
          { key: 'displayName', label: '显示名称', fuzzy: true },
          { key: 'name', label: '名称', fuzzy: true },
          { key: 'kind', label: '类型', fuzzy: false },
          { key: 'world.id', label: '世界ID', fuzzy: false },
        ]}
        
        // 配置数据列（替代了原先只有几个字段的 Card）
        schema={{
          'id': { title: 'ID', sortable: true },
          'displayName': { 
            title: '显示名称',
            render: (val, row) => (
              <Link to={`./${row.id}`} className="font-bold hover:text-accent transition-all duration-300">
                {val}
              </Link>
            )
          },
          'name': {
            title: '内部名称', sortable: true,
            render: (val: string) => <Copyable value={val}>{val.substring(0, 16) + (val.length > 16 ? "..." : "")}</Copyable>
          },
          'pathName': {
            title: '项目路径',
            render: (val: string) => <Copyable value={val}>{val.substring(0, 16) + (val.length > 16 ? "..." : "")}</Copyable>
          },
          'kind': { 
            title: '类型',
            filterable: true,
            render: (val) => <span className="text-gray-600 bg-gray-100 px-2 py-1 rounded-md text-xs">{val}</span> 
          },
          'world.id': { 
            title: '世界ID',
            filterable: true,
            render: (val) => val ? val : <span className="text-gray-400">-</span>
          },
        }}
      />

      {/* 新建项目弹窗 */}
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent className="sm:max-w-xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>新建项目</DialogTitle>
          </DialogHeader>
          <ProjectForm
            onSubmit={async (data) => {
              try {
                await api.createProject(data);
                setIsCreateOpen(false);
                triggerRefresh();
              } catch (error) {
                console.error("创建失败", error);
              }
            }}
            onCancel={() => setIsCreateOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* 编辑项目抽屉 */}
      <Sheet open={!!editingProject} onOpenChange={(open) => !open && setEditingProject(null)}>
        <SheetContent className="sm:max-w-xl overflow-y-auto">
          <SheetHeader>
            <SheetTitle>编辑项目: {editingProject?.displayName}</SheetTitle>
          </SheetHeader>
          <div className="mt-6">
            {editingProject && (
              <ProjectForm
                project={editingProject}
                onSubmit={async (data) => {
                  try {
                    await api.updateProject(editingProject.id, data);
                    setEditingProject(null);
                    triggerRefresh();
                  } catch (error) {
                    console.error("更新失败", error);
                  }
                }}
                onCancel={() => setEditingProject(null)}
              />
            )}
          </div>
        </SheetContent>
      </Sheet>
    </div>
  );
}

// ---------- 项目表单组件 ----------
interface ProjectFormProps {
  project?: Project;
  onSubmit: (data: ProjectFormValues) => Promise<void>;
  onCancel: () => void;
}

function ProjectForm({ project, onSubmit, onCancel }: ProjectFormProps) {
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
        <div className="grid grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="name"
            render={({ field }) => (
              <FormItem>
                <FormLabel>内部名称 *</FormLabel>
                <FormControl><Input {...field} /></FormControl>
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
                <FormControl><Input {...field} /></FormControl>
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
                <FormControl><Input {...field} /></FormControl>
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
                <FormControl><Input {...field} /></FormControl>
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
          <FormField
            control={form.control}
            name="modelKind"
            render={({ field }) => (
              <FormItem>
                <FormLabel>模型类型</FormLabel>
                <FormControl><Input {...field} /></FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>
        
        <FormField
          control={form.control}
          name="description"
          render={({ field }) => (
            <FormItem>
              <FormLabel>描述</FormLabel>
              <FormControl><Input {...field} /></FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="flex justify-end gap-2 pt-4">
          <Button type="button" variant="outline" onClick={onCancel}>取消</Button>
          <Button type="submit">保存</Button>
        </div>
      </form>
    </Form>
  );
}