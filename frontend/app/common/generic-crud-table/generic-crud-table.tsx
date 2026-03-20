import React, {
  useState,
  useEffect,
  useCallback,
  useMemo,
  useRef,
  useImperativeHandle,
  forwardRef,
  type ReactNode,
} from "react";
import { useLocation, useNavigate } from "react-router";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  ArrowDown,
  ArrowUp,
  ArrowUpDown,
  Plus,
  Search,
  Trash,
  Edit,
  X,
  Filter,
  Eraser,
} from "lucide-react";

// --- 基础类型定义 ---

export interface PageResponse<T> {
  content: (T | null)[];
  last: boolean;
  totalElements: number;
  number: number;
}

export interface SearchFieldDef {
  key: string;
  label: string;
  fuzzy?: boolean;
}

export interface SortState {
  field: string;
  direction: "asc" | "desc";
}

export interface TableActions {
  refreshPage: () => void;
  refreshGlobal: () => void;
  clearFilters: () => void;
}

export interface ColumnSchema<T> {
  title: string | ReactNode;
  sortable?: boolean;
  filterable?: boolean;
  render?: (value: any, row: T, actions: TableActions) => React.ReactNode;
}

export interface GenericCrudTableHandle {
  refreshPage: () => void;
  refreshGlobal: () => void;
  clearFilters: () => void;
}

export interface GenericCrudTableProps<T> {
  uid?: string;
  list: (
    page: number,
    size: number,
    search: string,
    sort: string,
  ) => Promise<PageResponse<T>>;
  create?: () => void;
  modify?: (row: T, actions: TableActions) => void;
  deleteAction?: (row: T, actions: TableActions) => Promise<void>;

  getRowId: (row: T) => string | number;
  schema: Record<string, ColumnSchema<T>>;
  searchFields?: SearchFieldDef[];
  forcedSearchValues?: Record<string, string>;
  rowActions?: (row: T, actions: TableActions) => React.ReactNode;
  headerActions?: React.ReactNode;
  pageSize?: number;
  customRenderer?: {
    renderContainer: (nodes: ReactNode[]) => ReactNode;
    renderElement: (row: T, actions: TableActions) => ReactNode;
  };
}

// --- 工具函数 ---

function getNestedValue(obj: any, path: string): any {
  if (!obj || !path) return undefined;
  return path
    .split(".")
    .reduce(
      (acc, part) => (acc && acc[part] !== undefined ? acc[part] : undefined),
      obj,
    );
}

/**
 * 比较两个 Record<string, string> 是否完全相等
 */
function areRecordsEqual(
  recordA: Record<string, string>,
  recordB: Record<string, string>,
): boolean {
  const keysA = Object.keys(recordA);
  const keysB = Object.keys(recordB);

  // 1. 首先检查键的数量是否一致
  if (keysA.length !== keysB.length) {
    return false;
  }

  // 2. 遍历其中一个 Record 的键，比较值
  // 既然长度相等，只要 A 中的所有键在 B 中都有且值相等，则两者完全一致
  return keysA.every((key) => {
    return (
      Object.prototype.hasOwnProperty.call(recordB, key) &&
      recordA[key] === recordB[key]
    );
  });
}

function buildSearchString(
  activeValues: Record<string, string>,
  fields: SearchFieldDef[],
): string {
  const conditions: string[] = [];
  Object.entries(activeValues).forEach(([key, val]) => {
    if (val !== undefined && val !== null && String(val).trim() !== "") {
      const fieldDef = fields.find((f) => f.key === key);
      const op = fieldDef?.fuzzy ? "~" : "";
      conditions.push(`${key}${op}:${String(val).trim()}`);
    }
  });
  return conditions.join(",");
}

const GenericCrudTableComponent = forwardRef(
  <T,>(
    {
      uid,
      list,
      create,
      modify,
      deleteAction,
      getRowId,
      schema,
      searchFields = [],
      forcedSearchValues = {},
      rowActions,
      headerActions,
      pageSize = 20,
      customRenderer,
    }: GenericCrudTableProps<T>,
    ref: React.ForwardedRef<GenericCrudTableHandle>,
  ) => {
    const location = useLocation();
    const navigate = useNavigate();

    // --- URL 参数处理逻辑 ---
    const getParamKey = useCallback(
      (key: string) => (uid ? `${uid}_${key}` : null),
      [uid],
    );

    // 从 URL 解析状态
    const parseStateFromUrl = useCallback(() => {
      if (!uid) return { page: 0, sortState: null, activeSearchValues: {} };

      const params = new URLSearchParams(location.search);
      const page = parseInt(params.get(getParamKey("page")!) || "0", 10);

      const sortRaw = params.get(getParamKey("sort")!);
      let sortState: SortState | null = null;
      if (sortRaw && sortRaw.includes(",")) {
        const [field, direction] = sortRaw.split(",");
        sortState = { field, direction: direction as "asc" | "desc" };
      }

      // 修复：动态匹配 URL 中带 s_ 前缀的搜索参数，而不受 searchFields 限制
      const activeSearchValues: Record<string, string> = {};
      const searchPrefix = getParamKey("s_");
      if (searchPrefix) {
        params.forEach((val, key) => {
          if (key.startsWith(searchPrefix)) {
            const actualKey = key.substring(searchPrefix.length);
            activeSearchValues[actualKey] = val;
          }
        });
      }

      return { page, sortState, activeSearchValues };
    }, [location.search, uid, getParamKey]);

    const urlState = useMemo(() => parseStateFromUrl(), [parseStateFromUrl]);

    // 状态管理
    const [data, setData] = useState<T[]>([]);
    const [loading, setLoading] = useState(false);
    const [total, setTotal] = useState(0);

    // 核心状态由 URLState 驱动或作为初始值
    const [page, setPage] = useState(urlState.page);
    const [sortState, setSortState] = useState<SortState | null>(
      urlState.sortState,
    );
    const [activeSearchValues, setActiveSearchValues] = useState<
      Record<string, string>
    >(urlState.activeSearchValues);
    const [draftSearchValues, setDraftSearchValues] = useState<
      Record<string, string>
    >(urlState.activeSearchValues);

    const [pageInput, setPageInput] = useState(String(page + 1));
    const totalPages = Math.max(1, Math.ceil(total / pageSize));

    // 修复：当 URL 改变时，同步内部状态（消除无限渲染并修复比较逻辑）
    useEffect(() => {
      if (uid) {
        setPage((prev) => (prev !== urlState.page ? urlState.page : prev));

        setSortState((prev) => {
          if (
            prev?.direction !== urlState.sortState?.direction ||
            prev?.field !== urlState.sortState?.field
          ) {
            return urlState.sortState;
          }
          return prev;
        });

        setActiveSearchValues((prev) =>
          !areRecordsEqual(prev, urlState.activeSearchValues)
            ? urlState.activeSearchValues
            : prev,
        );

        setDraftSearchValues((prev) =>
          !areRecordsEqual(prev, urlState.activeSearchValues)
            ? urlState.activeSearchValues
            : prev,
        );
      }
    }, [urlState, uid]);

    // 更新 URL 的统一函数
    const updateUrl = useCallback(
      (
        newPage: number,
        newSort: SortState | null,
        newSearch: Record<string, string>,
      ) => {
        if (!uid) return;

        const params = new URLSearchParams(location.search);

        // 设置分页
        params.set(getParamKey("page")!, String(newPage));

        // 设置排序
        const sortKey = getParamKey("sort")!;
        if (newSort)
          params.set(sortKey, `${newSort.field},${newSort.direction}`);
        else params.delete(sortKey);

        // 修复：动态清除该 UID 下所有的搜索参数，而不只清除 searchFields
        const searchPrefix = getParamKey("s_");
        if (searchPrefix) {
          const keysToDelete: string[] = [];
          params.forEach((_, key) => {
            if (key.startsWith(searchPrefix)) {
              keysToDelete.push(key);
            }
          });
          keysToDelete.forEach((k) => params.delete(k));
        }

        // 设置新搜索参数
        Object.entries(newSearch).forEach(([k, v]) => {
          if (v) params.set(getParamKey(`s_${k}`)!, v);
        });

        navigate(`${location.pathname}?${params.toString()}`, {
          replace: true,
        });
      },
      [uid, location.pathname, location.search, navigate, getParamKey],
    );

    // --- 引用缓存 ---
    const listRef = useRef(list);
    useEffect(() => {
      listRef.current = list;
    }, [list]);

    const forcedSearchStr = JSON.stringify(forcedSearchValues);
    const searchFieldsStr = JSON.stringify(searchFields);

    const appliedSearchStr = useMemo(() => {
      const combinedSearchValues = {
        ...activeSearchValues,
        ...JSON.parse(forcedSearchStr),
      };
      return buildSearchString(
        combinedSearchValues,
        JSON.parse(searchFieldsStr),
      );
    }, [activeSearchValues, forcedSearchStr, searchFieldsStr]);

    const sortStr = useMemo(() => {
      return sortState ? `${sortState.field},${sortState.direction}` : "";
    }, [sortState]);

    const fetchData = useCallback(async () => {
      setLoading(true);
      try {
        const res = await listRef.current(
          page,
          pageSize,
          appliedSearchStr,
          sortStr,
        );
        const validData = (res.content.filter(Boolean) as T[]) || [];
        setData(validData);
        setTotal(res.totalElements);
      } catch (error) {
        console.error("Failed to fetch table data:", error);
      } finally {
        setLoading(false);
      }
    }, [page, pageSize, appliedSearchStr, sortStr]);

    useEffect(() => {
      fetchData();
    }, [fetchData]);

    // --- 动作拦截：所有改变状态的操作改为触发 URL 更新 ---
    const tableActions: TableActions = useMemo(
      () => ({
        refreshPage: () => fetchData(),
        refreshGlobal: () => {
          if (uid) updateUrl(0, sortState, activeSearchValues);
          else setPage(0);
        },
        clearFilters: () => {
          if (uid) updateUrl(0, null, {});
          else {
            setDraftSearchValues({});
            setActiveSearchValues({});
            setSortState(null);
            setPage(0);
          }
        },
      }),
      [fetchData, uid, updateUrl, sortState, activeSearchValues],
    );

    useImperativeHandle(ref, () => tableActions);

    useEffect(() => {
      setPageInput(String(page + 1));
    }, [page]);

    const handleSearch = () => {
      if (uid) updateUrl(0, sortState, draftSearchValues);
      else {
        setActiveSearchValues(draftSearchValues);
        setPage(0);
      }
    };

    const removeSearchFilter = (key: string) => {
      const newSearch = { ...activeSearchValues };
      delete newSearch[key];
      if (uid) updateUrl(0, sortState, newSearch);
      else {
        setActiveSearchValues(newSearch);
        setDraftSearchValues(newSearch);
        setPage(0);
      }
    };

    const handleSort = (fieldKey: string) => {
      let nextSort: SortState | null = null;
      if (sortState?.field === fieldKey) {
        if (sortState.direction === "asc")
          nextSort = { field: fieldKey, direction: "desc" };
        else nextSort = null;
      } else {
        nextSort = { field: fieldKey, direction: "asc" };
      }

      if (uid) updateUrl(0, nextSort, activeSearchValues);
      else {
        setSortState(nextSort);
        setPage(0);
      }
    };

    const handlePageChange = (nextPage: number) => {
      if (uid) updateUrl(nextPage, sortState, activeSearchValues);
      else setPage(nextPage);
    };

    const handlePageSubmit = () => {
      const p = parseInt(pageInput, 10);
      if (!isNaN(p) && p > 0 && p <= totalPages) {
        handlePageChange(p - 1);
      } else {
        setPageInput(String(page + 1));
      }
    };

    const columnsKeys = Object.keys(schema);
    const hasActionsColumn = Boolean(modify || deleteAction || rowActions);
    const activeKeys = Object.keys(activeSearchValues).filter(
      (k) => activeSearchValues[k]?.trim() !== "",
    );
    const hasActiveFilters =
      activeKeys.length > 0 || sortState !== null || page > 0;

    return (
      <div className="space-y-4">
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
                      value={draftSearchValues[field.key] || ""}
                      onChange={(e) =>
                        setDraftSearchValues((prev) => ({
                          ...prev,
                          [field.key]: e.target.value,
                        }))
                      }
                      onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                    />
                  );
                })}
                <Button variant="secondary" onClick={handleSearch}>
                  <Search className="mr-2 h-4 w-4" /> 搜索
                </Button>
                {hasActiveFilters && (
                  <Button
                    variant="ghost"
                    onClick={tableActions.clearFilters}
                    className="text-muted-foreground hover:text-red-500"
                  >
                    <Eraser className="mr-2 h-4 w-4" /> 清空条件
                  </Button>
                )}
              </div>
            )}
            <div className="ml-auto flex items-center gap-2 flex-shrink-0">
              {headerActions}
              {create && (
                <Button onClick={create}>
                  <Plus className="mr-2 h-4 w-4" /> 新建
                </Button>
              )}
            </div>
          </div>

          {hasActiveFilters && (
            <div className="flex flex-wrap gap-2 items-center min-h-7">
              <span className="text-xs text-muted-foreground mr-1">
                活动筛选:
              </span>
              {page > 0 && (
                <span className="inline-flex items-center gap-1 px-2 py-1 bg-slate-100 text-slate-700 text-xs rounded-md border border-slate-200 transition-colors hover:bg-slate-200">
                  <span className="font-medium">页码:</span> 第 {page + 1} 页
                  <X
                    className="h-3 w-3 ml-1 cursor-pointer hover:text-slate-900"
                    onClick={() => handlePageChange(0)}
                  />
                </span>
              )}
              {activeKeys.map((key) => {
                const label =
                  searchFields.find((f) => f.key === key)?.label ||
                  schema[key]?.title ||
                  key;
                return (
                  <span
                    key={`filter-${key}`}
                    className="inline-flex items-center gap-1 px-2 py-1 bg-blue-50 text-blue-700 text-xs rounded-md border border-blue-200 transition-colors hover:bg-blue-100"
                  >
                    <span className="font-medium">{label}:</span>{" "}
                    {activeSearchValues[key]}
                    <X
                      className="h-3 w-3 ml-1 cursor-pointer hover:text-blue-900"
                      onClick={() => removeSearchFilter(key)}
                    />
                  </span>
                );
              })}
              {sortState && (
                <span className="inline-flex items-center gap-1 px-2 py-1 bg-amber-50 text-amber-700 text-xs rounded-md border border-amber-200 transition-colors hover:bg-amber-100">
                  <span className="font-medium">排序:</span>{" "}
                  {schema[sortState.field]?.title || sortState.field} (
                  {sortState.direction === "asc" ? "升序" : "降序"})
                  <X
                    className="h-3 w-3 ml-1 cursor-pointer hover:text-amber-900"
                    onClick={() => {
                      if (uid) updateUrl(page, null, activeSearchValues);
                      else setSortState(null);
                    }}
                  />
                </span>
              )}
            </div>
          )}
        </div>

        {customRenderer ? (
          customRenderer.renderContainer(
            data.map((row) => (
              <React.Fragment key={getRowId(row)}>
                {customRenderer.renderElement(row, tableActions)}
              </React.Fragment>
            ))
          )
        ) : (
          <div className="rounded-md border bg-card overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow className="hover:bg-transparent">
                  {columnsKeys.map((key) => (
                    <TableHead key={key}>
                      {schema[key].sortable ? (
                        <Button
                          variant="ghost"
                          onClick={() => handleSort(key)}
                          className="-ml-4 h-8"
                        >
                          {schema[key].title}
                          {sortState?.field === key ? (
                            sortState.direction === "asc" ? (
                              <ArrowUp className="ml-2 h-4 w-4" />
                            ) : (
                              <ArrowDown className="ml-2 h-4 w-4" />
                            )
                          ) : (
                            <ArrowUpDown className="ml-2 h-4 w-4 text-muted-foreground" />
                          )}
                        </Button>
                      ) : (
                        schema[key].title
                      )}
                    </TableHead>
                  ))}
                  {hasActionsColumn && (
                    <TableHead className="text-right">操作</TableHead>
                  )}
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  Array.from({ length: 5 }).map((_, i) => (
                    <TableRow key={i}>
                      {columnsKeys.map((k) => (
                        <TableCell key={k}>
                          <Skeleton className="h-4 w-full" />
                        </TableCell>
                      ))}
                      {hasActionsColumn && (
                        <TableCell>
                          <Skeleton className="h-4 w-12 ml-auto" />
                        </TableCell>
                      )}
                    </TableRow>
                  ))
                ) : data.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={columnsKeys.length + (hasActionsColumn ? 1 : 0)}
                      className="h-24 text-center text-muted-foreground"
                    >
                      暂无数据
                    </TableCell>
                  </TableRow>
                ) : (
                  data.map((row) => (
                    <TableRow key={getRowId(row)}>
                      {columnsKeys.map((key) => {
                        const col = schema[key];
                        const rawValue = getNestedValue(row, key);
                        return (
                          <TableCell key={key} className="group relative">
                            <div className="flex items-center gap-1.5 min-h-6">
                              {col.render
                                ? col.render(rawValue, row, tableActions)
                                : (rawValue as any)}
                              {col.filterable &&
                                rawValue &&
                                !(key in forcedSearchValues) && (
                                  <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity"
                                    onClick={() => {
                                      const newSearch = {
                                        ...activeSearchValues,
                                        [key]: String(rawValue),
                                      };
                                      if (uid) updateUrl(0, sortState, newSearch);
                                      else {
                                        setActiveSearchValues(newSearch);
                                        setDraftSearchValues(newSearch);
                                        setPage(0);
                                      }
                                    }}
                                  >
                                    <Filter className="h-3.5 w-3.5 text-muted-foreground" />
                                  </Button>
                                )}
                            </div>
                          </TableCell>
                        );
                      })}
                      {hasActionsColumn && (
                        <TableCell className="text-right space-x-2">
                          {rowActions && rowActions(row, tableActions)}
                          {modify && (
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => modify(row, tableActions)}
                            >
                              <Edit className="h-4 w-4 text-blue-500" />
                            </Button>
                          )}
                          {deleteAction && (
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => deleteAction(row, tableActions)}
                            >
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
        )}

        <div className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">共 {total} 条数据</div>
          <div className="flex items-center space-x-4">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(Math.max(0, page - 1))}
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
                onKeyDown={(e) => e.key === "Enter" && handlePageSubmit()}
                onBlur={handlePageSubmit}
              />
              <span>页 / 共 {totalPages} 页</span>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(page + 1)}
              disabled={(page + 1) * pageSize >= total || loading}
            >
              下一页
            </Button>
          </div>
        </div>
      </div>
    );
  },
) as <T>(
  props: GenericCrudTableProps<T> & {
    ref?: React.ForwardedRef<GenericCrudTableHandle>;
  },
) => React.ReactElement;

export const GenericCrudTable = GenericCrudTableComponent;
(GenericCrudTable as any).displayName = "GenericCrudTable";
