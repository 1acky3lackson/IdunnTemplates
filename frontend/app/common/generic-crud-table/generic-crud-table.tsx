import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { 
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow 
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { ArrowDown, ArrowUp, ArrowUpDown, Plus, Search, Trash, Edit } from "lucide-react";

// --- 基础类型定义 ---

export interface PageResponse<T> {
  content: (T | null)[];
  last: boolean;
  totalElements: number;
  number: number;
}

// 搜索字段定义
export interface SearchFieldDef {
  key: string;       // 对应的后端字段名，例如 itemName
  label: string;     // 前端显示的占位符或标签
  fuzzy?: boolean;   // 是否为模糊查询（如果是，后端拼接 ~）
}

// 排序状态定义
export interface SortState {
  field: string;
  direction: 'asc' | 'desc';
}

// 表格列 Schema 定义
export interface ColumnSchema<T> {
  title: string;
  sortable?: boolean;
  // 自定义渲染函数，如果未提供，则按照 key 直接输出文本
  render?: (value: any, row: T) => React.ReactNode; 
}

// 主组件 Props
export interface GenericCrudTableProps<T> {
  // 必须的获取列表接口
  list: (page: number, size: number, search: string, sort: string) => Promise<PageResponse<T>>;
  // 可选的 CRUD 接口
  create?: () => void; // 触发创建的逻辑（如打开弹窗）
  modify?: (row: T) => void; // 触发修改的逻辑
  deleteAction?: (row: T) => Promise<void>; // 触发删除的逻辑
  
  // 核心数据标识
  getRowId: (row: T) => string | number;
  
  // 表格结构
  schema: Record<string, ColumnSchema<T>>;
  
  // 搜索配置
  searchFields?: SearchFieldDef[];
  
  // 额外的自定义行控件
  rowActions?: (row: T) => React.ReactNode;
  
  pageSize?: number;
}

// --- 工具函数 ---

/**
 * 解析深度路径，例如将 "project.id" 解析为 obj.project.id
 */
function getNestedValue(obj: any, path: string): any {
  if (!obj || !path) return undefined;
  return path.split('.').reduce((acc, part) => (acc && acc[part] !== undefined) ? acc[part] : undefined, obj);
}

/**
 * 根据搜索状态和配置，构建 Spring Boot 后端所需的 search 字符串
 * 格式：field:value,field~:value
 */
function buildSearchString(searchValues: Record<string, string>, fields: SearchFieldDef[]): string {
  const conditions: string[] = [];
  fields.forEach(field => {
    const val = searchValues[field.key];
    if (val && val.trim() !== '') {
      const op = field.fuzzy ? '~' : '';
      conditions.push(`${field.key}${op}:${val.trim()}`);
    }
  });
  return conditions.join(',');
}

export function GenericCrudTable<T>({
  list,
  create,
  modify,
  deleteAction,
  getRowId,
  schema,
  searchFields = [],
  rowActions,
  pageSize = 20
}: GenericCrudTableProps<T>) {
  // 状态管理
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  
  // 分页与排序
  const [page, setPage] = useState(0); // Spring Boot page starts at 0
  const [sortState, setSortState] = useState<SortState | null>(null);
  
  // 搜索条件暂存 (Input 绑定的值)
  const [searchValues, setSearchValues] = useState<Record<string, string>>({});
  // 实际应用于请求的 search 字符串
  const [appliedSearch, setAppliedSearch] = useState("");

  // 构建请求
  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      let sortStr = "";
      if (sortState) {
        sortStr = `${sortState.field},${sortState.direction}`;
      }
      
      const res = await list(page, pageSize, appliedSearch, sortStr);
      // 过滤掉可能的 null 数据
      const validData = (res.content.filter(Boolean) as T[]) || [];
      setData(validData);
      setTotal(res.totalElements);
    } catch (error) {
      console.error("Failed to fetch table data:", error);
      // 实际项目中这里可以接入 Toast 通知组件
    } finally {
      setLoading(false);
    }
  }, [list, page, pageSize, appliedSearch, sortState]);

  // 依赖变更时自动获取数据
  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // 处理搜索
  const handleSearch = () => {
    const searchString = buildSearchString(searchValues, searchFields);
    setAppliedSearch(searchString);
    setPage(0); // 搜索时重置回第一页
  };

  // 处理排序切换
  const handleSort = (fieldKey: string) => {
    setSortState(prev => {
      if (prev?.field === fieldKey) {
        return prev.direction === 'asc' 
          ? { field: fieldKey, direction: 'desc' } 
          : null; // 第三次点击取消排序
      }
      return { field: fieldKey, direction: 'asc' };
    });
    setPage(0); // 排序变动通常也回到第一页
  };

  const columnsKeys = Object.keys(schema);
  const hasActionsColumn = Boolean(modify || deleteAction || rowActions);

  return (
    <div className="space-y-4">
      {/* 顶部工具栏：搜索与创建 */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        {searchFields.length > 0 && (
          <div className="flex items-center gap-2 justify-between flex-1">
            <div className="flex items-center gap-2">
            {searchFields.map((field) => (
              <Input
                key={field.key}
                placeholder={`🔍︎ ${field.label}...`}
                className="w-48"
                value={searchValues[field.key] || ''}
                onChange={(e) => setSearchValues(prev => ({ ...prev, [field.key]: e.target.value }))}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              />
            ))}
            </div>
            <Button variant="secondary" onClick={handleSearch}>
              <Search className="mr-2 h-4 w-4" /> 搜索
            </Button>
          </div>
        )}

        {create && (
          <Button onClick={create}>
            <Plus className="mr-2 h-4 w-4" /> 新建
          </Button>
        )}
      </div>

      {/* 表格主体 */}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              {columnsKeys.map(key => {
                const col = schema[key];
                return (
                  <TableHead key={key}>
                    {col.sortable ? (
                      <Button 
                        variant="ghost" 
                        onClick={() => handleSort(key)}
                        className="-ml-4 h-8 data-[state=open]:bg-accent"
                      >
                        {col.title}
                        {sortState?.field === key ? (
                          sortState.direction === 'asc' ? <ArrowUp className="ml-2 h-4 w-4" /> : <ArrowDown className="ml-2 h-4 w-4" />
                        ) : (
                          <ArrowUpDown className="ml-2 h-4 w-4 text-muted-foreground" />
                        )}
                      </Button>
                    ) : (
                      col.title
                    )}
                  </TableHead>
                );
              })}
              {hasActionsColumn && <TableHead className="text-right">操作</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              // 加载占位符
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  {columnsKeys.map(k => (
                    <TableCell key={k}><Skeleton className="h-4 w-full" /></TableCell>
                  ))}
                  {hasActionsColumn && <TableCell><Skeleton className="h-4 w-12 ml-auto" /></TableCell>}
                </TableRow>
              ))
            ) : data.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columnsKeys.length + (hasActionsColumn ? 1 : 0)} className="h-24 text-center">
                  暂无数据
                </TableCell>
              </TableRow>
            ) : (
              // 实际数据渲染
              data.map(row => (
                <TableRow key={getRowId(row)}>
                  {columnsKeys.map(key => {
                    const col = schema[key];
                    const rawValue = getNestedValue(row, key);
                    return (
                      <TableCell key={key}>
                        {col.render ? col.render(rawValue, row) : (rawValue as React.ReactNode)}
                      </TableCell>
                    );
                  })}
                  
                  {/* 动作列渲染 */}
                  {hasActionsColumn && (
                    <TableCell className="text-right space-x-2">
                      {rowActions && rowActions(row)}
                      {modify && (
                        <Button variant="ghost" size="icon" onClick={() => modify(row)}>
                          <Edit className="h-4 w-4 text-blue-500" />
                        </Button>
                      )}
                      {deleteAction && (
                        <Button variant="ghost" size="icon" onClick={() => deleteAction(row)}>
                          <Trash className="h-4 w-4 text-red-500" />
                        </Button>
                      )}
                    </TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* 简易分页组件 */}
      <div className="flex items-center justify-between">
        <div className="text-sm text-muted-foreground">
          共 {total} 条数据
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0 || loading}
          >
            上一页
          </Button>
          <div className="text-sm font-medium">
            页码 {page + 1}
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage(p => p + 1)}
            disabled={(page + 1) * pageSize >= total || loading}
          >
            下一页
          </Button>
        </div>
      </div>
    </div>
  );
}