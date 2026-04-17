// NeteaseProductManagerPage.tsx
import React, { useState, useCallback, useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Link } from "react-router"; // React Router v7 / v6

// Shadcn UI 组件
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Card, CardContent } from "@/components/ui/card";

// 自定义工具与组件
import type { ProjectApi } from "../project/ProjectManager";
import type { Schema, SearchParam } from "../util/search-test-utils";
import type { Project } from "~/api/generated";
import NeteasePointType from "../util/NeteasePointType";
import {
  GenericCrudTable,
  type PageResponse,
} from "../generic-crud-table/generic-crud-table";
import { DEFAULT_PRODUCT_API, DEFAULT_PROJECT_API } from "./default-api";
import { Copyable } from "../util/Copyable";

// ---------- 类型定义 ----------

export enum NeteaseProductStatus {
  CREATED = "CREATED",
  CONVERTED = "CONVERTED",
  ONLINE = "ONLINE",
  REJECTED = "REJECTED",
}

export interface NeteaseProduct {
  id: number;
  itemId: string;
  itemName: string;
  internalStatus: NeteaseProductStatus;
  project?: Project;
  projectId?: number;
  price?: number;
  priceType?: string;
  createTimeMs?: number;
  updateTimeMs?: number;
}

export type ProductPageResponse = PageResponse<NeteaseProduct>;

export const productSchema = {
  itemId: { search: { fuzzy: true } },
  itemName: { search: { fuzzy: true } },
  internalStatus: { search: { fuzzy: false } },
  "project.id": { search: { fuzzy: false } },
  id: { sort: true },
} as const satisfies Schema;

type ProductSearchParams = SearchParam<typeof productSchema>;

export interface ProductApi {
  fetchProducts: (
    page: number,
    criteria: ProductSearchParams,
  ) => Promise<ProductPageResponse>;
  updateProduct: (
    id: number,
    data: Partial<NeteaseProduct>,
  ) => Promise<NeteaseProduct>;
}

// ---------- 表单验证 Schema ----------
const statusFormSchema = z.object({
  internalStatus: z.nativeEnum(NeteaseProductStatus),
});
type StatusFormValues = z.infer<typeof statusFormSchema>;

// ---------- 状态标签颜色映射工具 ----------
const statusColor = (status: NeteaseProductStatus) => {
  switch (status) {
    case NeteaseProductStatus.CREATED:
      return "bg-gray-500";
    case NeteaseProductStatus.CONVERTED:
      return "bg-blue-500";
    case NeteaseProductStatus.ONLINE:
      return "bg-green-500";
    case NeteaseProductStatus.REJECTED:
      return "bg-red-500";
    default:
      return "bg-gray-500";
  }
};

const statusText = (status: NeteaseProductStatus) => {
  switch (status) {
    case NeteaseProductStatus.CREATED:
      return "已创建";
    case NeteaseProductStatus.CONVERTED:
      return "已转换";
    case NeteaseProductStatus.ONLINE:
      return "已添加";
    case NeteaseProductStatus.REJECTED:
      return "已拒绝";
    default:
      return status;
  }
};

// ---------- 辅助转换函数 ----------
// 将 GenericCrudTable 产生的 "itemName~:测试,internalStatus:ONLINE" 转回旧 API 需要的 Criteria 对象
const parseSearchStringToCriteria = (search: string): ProductSearchParams => {
  const criteria: any = {};
  if (!search) return criteria;

  search.split(",").forEach((part) => {
    const [fieldWithOp, value] = part.split(":");
    if (!fieldWithOp || !value) return;

    if (fieldWithOp.endsWith("~")) {
      const field = fieldWithOp.slice(0, -1);
      criteria[field] = { value, fuzzy: true };
    } else {
      criteria[fieldWithOp] = value;
    }
  });
  return criteria as ProductSearchParams;
};

// ---------- 主页面组件 ----------
interface NeteaseProductManagerPageProps {
  productApi?: ProductApi;
  projectApi?: ProjectApi;
  forceSearch?: Record<string, string>;
  title?: string;
  uid?: string;
}

export function NeteaseProductManagerPage({
  productApi = DEFAULT_PRODUCT_API,
  projectApi = DEFAULT_PROJECT_API,
  forceSearch,
  title,
  uid = "prdct",
}: NeteaseProductManagerPageProps) {
  // 使用一个计数器来强制刷新表格。
  // 当我们把它作为 fetchTableData 的依赖项时，只要它改变，fetchTableData 引用就会改变，触发 GenericCrudTable 内部的 useEffect 重新请求。
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const triggerRefresh = useCallback(
    () => setRefreshTrigger((prev) => prev + 1),
    [],
  );

  // 适配层：将 GenericCrudTable 的传参转换为旧接口所需的结构
  const fetchTableData = useCallback(
    async (page: number, size: number, search: string, sort: string) => {
      const criteria = parseSearchStringToCriteria(search);

      // 排序处理
      if (sort) {
        const [field, direction] = sort.split(",");
        criteria.sort = { [field]: direction };
      } else {
        criteria.sort = { id: "desc" }; // 默认排序
      }

      // 注意：如果你后端的 API 支持传入 size (pageSize)，请在 fetchProducts 接口中扩展。
      // 如果旧接口仅支持 page 和 criteria，你可能需要去稍微改一下 productApi 的定义。
      return await productApi.fetchProducts(page, criteria);
    },
    [productApi, refreshTrigger],
  ); // refreshTrigger 变化会导致这里重新生成，进而刷新表格

  return (
    <div className="container mx-auto p-4 space-y-4">
      {title && (
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-bold">{title}</h1>
        </div>
      )}

      <GenericCrudTable<NeteaseProduct>
        uid={uid}
        getRowId={(row) => row.id}
        list={fetchTableData}
        // 搜索栏配置，和后端的约定以及前面的解析函数完美对应
        searchFields={[
          { key: "itemName", label: "产品名称", fuzzy: true },
          { key: "itemId", label: "商品ID", fuzzy: true },
          { key: "internalStatus", label: "状态", fuzzy: false },
          { key: "project.id", label: "项目ID", fuzzy: false },
        ]}
        forcedSearchValues={forceSearch}
        // 表格列渲染配置
        schema={{
          id: { title: "ID", sortable: true },
          itemId: {
            title: "商品ID",
            render: (val) => (
              <Copyable value={val}>
                {String(val).substring(0, 6) + "..."}
              </Copyable>
            ),
          },
          itemName: {
            title: "名称",
            render: (val, row) => (
              <Link
                to={`/commercial/products/${row.id}`}
                className="font-bold hover:text-accent transition-all duration-300"
              >
                {val}
              </Link>
            ),
          },
          internalStatus: {
            title: "状态",
            filterable: true,
            render: (val: NeteaseProductStatus) => (
              <Badge className={statusColor(val)}>{statusText(val)}</Badge>
            ),
          },
          project: {
            // 这里直接拿 project 对象来渲染，不涉及深度索引取值，在 render 里自己解构即可
            title: "项目",
            filterable: true,
            render: (_, row) => (
              <Link
                to={`/commercial/projects`}
                className="font-bold hover:text-accent transition-all duration-300"
              >
                {row.project ? (
                  <Copyable
                    value={row.project.displayName}
                  >{`[${row.project.id || row.projectId || "-"}] ${row.project.displayName.substring(0, 8) || "未知项目名称"}...`}</Copyable>
                ) : (
                  "---"
                )}
              </Link>
            ),
          },
          price: {
            title: "价格",
            filterable: true,
            sortable: true,
          },
          priceType: {
            title: "类型",
            filterable: true,
            render: (val) => <NeteasePointType point={val} />,
          },
        }}
        // 自定义行操作列
        rowActions={(row) => (
          <div className="flex gap-2">
            <AssignProjectDialog
              product={row}
              projectApi={projectApi}
              productApi={productApi}
              onSuccess={triggerRefresh}
            />
            <ChangeStatusDialog
              product={row}
              productApi={productApi}
              onSuccess={triggerRefresh}
            />
          </div>
        )}
      />
    </div>
  );
}

// ---------- 关联项目对话框 ----------
interface AssignProjectDialogProps {
  product: NeteaseProduct;
  projectApi: ProjectApi;
  productApi: ProductApi;
  onSuccess: () => void; // 替换了原本的 context.refresh
}

function AssignProjectDialog({
  product,
  projectApi,
  productApi,
  onSuccess,
}: AssignProjectDialogProps) {
  const [open, setOpen] = useState(false);
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);
  const [projectSearch, setProjectSearch] = useState("");
  const [projects, setProjects] = useState<Project[]>([]);
  const [loadingProjects, setLoadingProjects] = useState(false);

  const loadProjects = useCallback(
    async (searchTerm: string) => {
      setLoadingProjects(true);
      try {
        const criteria = searchTerm
          ? { name: { value: searchTerm, fuzzy: true } }
          : {};
        const page = await projectApi.fetchProjects(0, criteria);
        setProjects(page.content.filter((p): p is Project => p !== null));
      } catch (error) {
        console.error("Failed to load projects", error);
      } finally {
        setLoadingProjects(false);
      }
    },
    [projectApi],
  );

  useEffect(() => {
    if (open) loadProjects("");
  }, [open, loadProjects]);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (open) loadProjects(projectSearch);
    }, 300);
    return () => clearTimeout(timer);
  }, [projectSearch, open, loadProjects]);

  const handleConfirm = async () => {
    if (!selectedProject) return;
    try {
      await productApi.updateProduct(product.id, {
        projectId: selectedProject.id,
      });
      setOpen(false);
      onSuccess(); // 触发上层组件表格刷新
    } catch (error) {
      console.error("Update failed", error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm">
          关联项目
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>关联项目 - {product.itemName}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <Input
            placeholder="搜索项目名称..."
            value={projectSearch}
            onChange={(e) => setProjectSearch(e.target.value)}
          />
          <ScrollArea className="h-60 border rounded-md p-2">
            {loadingProjects ? (
              <div className="flex justify-center p-4">
                <Skeleton className="h-8 w-full" />
              </div>
            ) : (
              <div className="space-y-1">
                {projects.map((project) => (
                  <div
                    key={project.id}
                    className={`p-2 rounded cursor-pointer hover:bg-accent ${selectedProject?.id === project.id ? "bg-accent" : ""
                      }`}
                    onClick={() => setSelectedProject(project)}
                  >
                    {project.displayName} (ID: {project.id})
                  </div>
                ))}
              </div>
            )}
          </ScrollArea>
          {selectedProject && (
            <Card>
              <CardContent className="p-4 space-y-2 text-sm">
                <p>
                  <strong>项目名称：</strong>
                  {selectedProject.displayName}
                </p>
                <p>
                  <strong>内部名称：</strong>
                  {selectedProject.name}
                </p>
              </CardContent>
            </Card>
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)}>
            取消
          </Button>
          <Button onClick={handleConfirm} disabled={!selectedProject}>
            确认关联
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ---------- 修改状态对话框 ----------
function ChangeStatusDialog({
  product,
  productApi,
  onSuccess,
}: {
  product: NeteaseProduct;
  productApi: ProductApi;
  onSuccess: () => void;
}) {
  const [open, setOpen] = useState(false);

  const form = useForm<StatusFormValues>({
    resolver: zodResolver(statusFormSchema),
    defaultValues: {
      internalStatus: product.internalStatus,
    },
  });

  const onSubmit = async (values: StatusFormValues) => {
    try {
      await productApi.updateProduct(product.id, {
        internalStatus: values.internalStatus,
      });
      setOpen(false);
      onSuccess(); // 触发上层组件表格刷新
    } catch (error) {
      console.error("Failed to change status", error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm">
          修改状态
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>修改状态 - {product.itemName}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="internalStatus"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>状态</FormLabel>
                  <Select
                    onValueChange={field.onChange}
                    defaultValue={field.value}
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="选择状态" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {Object.values(NeteaseProductStatus).map((status) => (
                        <SelectItem key={status} value={status}>
                          {statusText(status)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <DialogFooter>
              <Button
                variant="outline"
                type="button"
                onClick={() => setOpen(false)}
              >
                取消
              </Button>
              <Button type="submit">保存</Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
