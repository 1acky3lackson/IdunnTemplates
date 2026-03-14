import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { 
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow 
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { 
  ArrowDown, ArrowUp, ArrowUpDown, Plus, Search, Trash, Edit, X, Filter, Eraser 
} from "lucide-react";

// --- 基础类型定义 ---

export interface PageResponse<T> {
  content: (T | null)[];
  last: boolean;
  totalElements: number;
  number: number;
}

// 搜索字段定义
export interface SearchFieldDef {
  key: string;       
  label: string;     
  fuzzy?: boolean;   
}

export interface SortState {
  field: string;
  direction: 'asc' | 'desc';
}

export interface ColumnSchema<T> {
  title: string;
  sortable?: boolean;
  filterable?: boolean; 
  render?: (value: any, row: T) => React.ReactNode; 
}

export interface GenericCrudTableProps<T> {
  list: (page: number, size: number, search: string, sort: string) => Promise<PageResponse<T>>;
  create?: () => void; 
  modify?: (row: T) => void; 
  deleteAction?: (row: T) => Promise<void>; 
  
  getRowId: (row: T) => string | number;
  schema: Record<string, ColumnSchema<T>>;
  searchFields?: SearchFieldDef[];
  forcedSearchValues?: Record<string, string>;
  rowActions?: (row: T) => React.ReactNode;
  pageSize?: number;
}

// --- 工具函数 ---

function getNestedValue(obj: any, path: string): any {
  if (!obj || !path) return undefined;
  return path.split('.').reduce((acc, part) => (acc && acc[part] !== undefined) ? acc[part] : undefined, obj);
}

function buildSearchString(activeValues: Record<string, string>, fields: SearchFieldDef[]): string {
  const conditions: string[] = [];
  Object.entries(activeValues).forEach(([key, val]) => {
    if (val !== undefined && val !== null && String(val).trim() !== '') {
      const fieldDef = fields.find(f => f.key === key);
      const op = fieldDef?.fuzzy ? '~' : '';
      conditions.push(`${key}${op}:${String(val).trim()}`);
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
  forcedSearchValues = {}, 
  rowActions,
  pageSize = 20
}: GenericCrudTableProps<T>) {
  // 状态管理
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  
  const [page, setPage] = useState(0); 
  const [sortState, setSortState] = useState<SortState | null>(null);
  
  const [draftSearchValues, setDraftSearchValues] = useState<Record<string, string>>({});
  const [activeSearchValues, setActiveSearchValues] = useState<Record<string, string>>({});

  const [pageInput, setPageInput] = useState("1");
  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  // --- 核心防御性依赖管理：防止死循环 ---
  
  // 1. 缓存 list 函数引用，避免父组件没有包裹 useCallback 导致无限请求
  const listRef = useRef(list);
  useEffect(() => {
    listRef.current = list;
  }, [list]);

  // 2. 将复杂对象/数组的安全字符串化，作为底层依赖，防止内存地址变化引发重渲染
  const forcedSearchStr = JSON.stringify(forcedSearchValues);
  const searchFieldsStr = JSON.stringify(searchFields);

  // 3. 提前计算出查询和排序字符串，只有它们真实发生改变时，才触发请求
  const appliedSearchStr = useMemo(() => {
    const combinedSearchValues = { ...activeSearchValues, ...JSON.parse(forcedSearchStr) };
    return buildSearchString(combinedSearchValues, JSON.parse(searchFieldsStr));
  }, [activeSearchValues, forcedSearchStr, searchFieldsStr]);

  const sortStr = useMemo(() => {
    return sortState ? `${sortState.field},${sortState.direction}` : "";
  }, [sortState]);


  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      // 这里的 listRef.current 永远指向最新的 list，而且不会破坏 useCallback 的依赖
      const res = await listRef.current(page, pageSize, appliedSearchStr, sortStr);
      
      const validData = (res.content.filter(Boolean) as T[]) || [];
      setData(validData);
      setTotal(res.totalElements);
    } catch (error) {
      console.error("Failed to fetch table data:", error);
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, appliedSearchStr, sortStr]); // 依赖项彻底净化，只有基础数据类型

  // 唯一触发数据获取的口子，绝对安全
  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // 当实际页码改变时，同步输入框的值
  useEffect(() => {
    setPageInput(String(page + 1));
  }, [page]);


  // --- 操作处理逻辑 ---

  const handleSearch = () => {
    setActiveSearchValues(draftSearchValues);
    setPage(0);
  };

  const removeSearchFilter = (key: string) => {
    const newActive = { ...activeSearchValues };
    delete newActive[key];
    setActiveSearchValues(newActive);

    const newDraft = { ...draftSearchValues };
    delete newDraft[key];
    setDraftSearchValues(newDraft);
    
    setPage(0);
  };

  const removeSort = () => {
    setSortState(null);
    setPage(0);
  };

  const clearAllFilters = () => {
    setDraftSearchValues({});
    setActiveSearchValues({});
    setSortState(null);
    setPage(0);
  };

  const applyQuickFilter = (key: string, value: any) => {
    if (key in forcedSearchValues) return;

    const strVal = String(value);
    const newActive = { ...activeSearchValues, [key]: strVal };
    
    setActiveSearchValues(newActive);
    setDraftSearchValues(prev => ({ ...prev, [key]: strVal }));
    setPage(0);
  };

  const handleSort = (fieldKey: string) => {
    setSortState(prev => {
      if (prev?.field === fieldKey) {
        return prev.direction === 'asc' 
          ? { field: fieldKey, direction: 'desc' } 
          : null;
      }
      return { field: fieldKey, direction: 'asc' };
    });
    setPage(0);
  };

  const handlePageSubmit = () => {
    const p = parseInt(pageInput, 10);
    if (!isNaN(p) && p > 0 && p <= totalPages) {
      setPage(p - 1);
    } else {
      setPageInput(String(page + 1));
    }
  };

  const columnsKeys = Object.keys(schema);
  const hasActionsColumn = Boolean(modify || deleteAction || rowActions);

  const activeKeys = Object.keys(activeSearchValues).filter(k => activeSearchValues[k]?.trim() !== '');
  const hasActiveFilters = activeKeys.length > 0 || sortState !== null;

  return (
    <div className="space-y-4">
      {/* 顶部工具栏：搜索与创建 */}
      <div className="flex flex-col gap-3">
        <div className="flex flex-wrap items-center justify-between gap-4">
          {searchFields.length > 0 && (
            <div className="flex items-center gap-2 flex-1 flex-wrap">
              {searchFields.map((field) => {
                if (field.key in forcedSearchValues) return null;
                
                return (
                  <Input
                    key={field.key}
                    placeholder={`🔍︎ ${field.label}...`}
                    className="w-48"
                    value={draftSearchValues[field.key] || ''}
                    onChange={(e) => setDraftSearchValues(prev => ({ ...prev, [field.key]: e.target.value }))}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  />
                );
              })}
              <Button variant="secondary" onClick={handleSearch}>
                <Search className="mr-2 h-4 w-4" /> 搜索
              </Button>
              
              {hasActiveFilters && (
                <Button variant="ghost" onClick={clearAllFilters} className="text-muted-foreground hover:text-red-500">
                  <Eraser className="mr-2 h-4 w-4" /> 清空条件
                </Button>
              )}
            </div>
          )}

          {create && (
            <Button onClick={create} className="ml-auto">
              <Plus className="mr-2 h-4 w-4" /> 新建
            </Button>
          )}
        </div>

        {/* 激活的筛选/排序条件标签 (Tags) */}
        {hasActiveFilters && (
          <div className="flex flex-wrap gap-2 items-center min-h-[28px]">
            <span className="text-xs text-muted-foreground mr-1">活动筛选:</span>
            
            {activeKeys.map(key => {
              const label = searchFields.find(f => f.key === key)?.label || schema[key]?.title || key;
              return (
                <span 
                  key={`filter-${key}`} 
                  className="inline-flex items-center gap-1 px-2 py-1 bg-blue-50 text-blue-700 text-xs rounded-md border border-blue-200 transition-colors hover:bg-blue-100"
                >
                  <span className="font-medium">{label}:</span> {activeSearchValues[key]}
                  <X 
                    className="h-3 w-3 ml-1 cursor-pointer hover:text-blue-900" 
                    onClick={() => removeSearchFilter(key)} 
                  />
                </span>
              );
            })}

            {sortState && (
              <span className="inline-flex items-center gap-1 px-2 py-1 bg-amber-50 text-amber-700 text-xs rounded-md border border-amber-200 transition-colors hover:bg-amber-100">
                <span className="font-medium">排序:</span> {schema[sortState.field]?.title || sortState.field} 
                ({sortState.direction === 'asc' ? '升序' : '降序'})
                <X 
                  className="h-3 w-3 ml-1 cursor-pointer hover:text-amber-900" 
                  onClick={removeSort} 
                />
              </span>
            )}
          </div>
        )}
      </div>

      {/* 表格主体 */}
      <div className="rounded-md border bg-card">
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
                <TableCell colSpan={columnsKeys.length + (hasActionsColumn ? 1 : 0)} className="h-24 text-center text-muted-foreground">
                  暂无数据
                </TableCell>
              </TableRow>
            ) : (
              data.map(row => (
                <TableRow key={getRowId(row)}>
                  {columnsKeys.map(key => {
                    const col = schema[key];
                    const rawValue = getNestedValue(row, key);
                    const isFilterableVal = rawValue !== undefined && rawValue !== null && rawValue !== '';
                    const isForced = key in forcedSearchValues;

                    return (
                      <TableCell key={key} className="group relative">
                        <div className="flex items-center gap-1.5 min-h-[1.5rem]">
                          <div>{col.render ? col.render(rawValue, row) : (rawValue as React.ReactNode)}</div>
                          
                          {col.filterable && isFilterableVal && !isForced && (
                            <Button
                              variant="ghost"
                              size="icon"
                              title="以此值筛选"
                              className="h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity"
                              onClick={() => applyQuickFilter(key, rawValue)}
                            >
                              <Filter className="h-3.5 w-3.5 text-muted-foreground hover:text-primary" />
                            </Button>
                          )}
                        </div>
                      </TableCell>
                    );
                  })}
                  
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

      {/* 增强版分页组件 */}
      <div className="flex items-center justify-between">
        <div className="text-sm text-muted-foreground">
          共 {total} 条数据
        </div>
        <div className="flex items-center space-x-4">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0 || loading}
          >
            上一页
          </Button>
          
          <div className="flex items-center gap-2 text-sm font-medium">
            <span>第</span>
            <Input 
              className="h-8 w-14 text-center px-1"
              value={pageInput}
              onChange={(e) => setPageInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handlePageSubmit()}
              onBlur={handlePageSubmit} 
            />
            <span>页 / 共 {totalPages} 页</span>
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