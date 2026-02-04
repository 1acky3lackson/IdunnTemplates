import React from "react";
import { Search, ChevronDown, FolderTree, FileType } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Badge } from "@/components/ui/badge";
import type { TemplateSearchParams } from "~/api/overrides/template-search-api";
import { useIntlayer } from "react-intlayer";
import { Separator } from "~/components/ui/separator";

interface TemplateSearchBarProps {
  values: TemplateSearchParams;
  onChange: (newValues: TemplateSearchParams) => void;
  total?: number; // 可选：显示结果数量
  className?: string;
}

export function TemplateSearchBar({
  values,
  onChange,
  total,
  className,
}: TemplateSearchBarProps) {
  // 获取翻译内容
  const { searchBar } = useIntlayer("template-browser");

  // 通用的更新辅助函数
  const updateValue = (key: string, value: string) => {
    onChange({
      ...values,
      [key]: value || undefined, // 如果是空字符串，设为 undefined 以保持整洁
    });
  };

  // 计算有多少个"高级筛选"处于激活状态 (用于 UI 提示)
  const activeAdvancedFilters = [values.pathPrefix, values.nameLike].filter(Boolean).length;

  return (
    <div className={`flex items-center gap-2 flex-1 max-w-xl ${className}`}>
      {/* 组合输入框容器 */}
      <div className="relative flex flex-1 items-center">

        {/* 左侧：高级搜索展开按钮 */}
        <Popover>
          <PopoverTrigger asChild>
            <Button
              variant={activeAdvancedFilters > 0 ? "secondary" : "ghost"}
              size="sm"
              className="absolute left-1 h-8 px-2 text-muted-foreground hover:text-foreground z-10 gap-1"
            >
              <span className="text-xs font-medium">{searchBar.filterBtn}</span>
              <ChevronDown className="h-3 w-3" />
              {activeAdvancedFilters > 0 && (
                <span className="absolute -top-1 -right-1 flex h-2 w-2">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary/40 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
                </span>
              )}
            </Button>
          </PopoverTrigger>

          <PopoverContent align="start" className="w-[320px] p-4 space-y-4">
            <h4 className="font-medium leading-none mb-2 text-sm text-muted-foreground">
              {searchBar.advancedTitle}
            </h4>
            <Separator />
            {/* 1. Path Prefix 输入 */}
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <FolderTree className="h-4 w-4 text-muted-foreground" />
                <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                  {searchBar.pathPrefixLabel}
                </label>
              </div>
              <Input
                placeholder="e.g., trees/nobi/"
                value={(values.pathPrefix as string) || ""}
                onChange={(e) => updateValue("pathPrefix", e.target.value)}
                className="h-8"
              />
              <p className="text-[10px] text-muted-foreground">{searchBar.pathPrefixDesc}</p>
            </div>
            <Separator />
            {/* 2. Name Like 输入 */}
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <FileType className="h-4 w-4 text-muted-foreground" />
                <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                  {searchBar.nameMatchLabel}
                </label>
              </div>
              <Input
                placeholder="e.g., 松"
                value={(values.nameLike as string) || ""}
                onChange={(e) => updateValue("nameLike", e.target.value)}
                className="h-8"
              />
              <p className="text-[10px] text-muted-foreground">{searchBar.nameMatchDesc}</p>
            </div>
            <Separator />
            {/* 清除按钮 */}
            {(values.pathPrefix || values.nameLike) && (
              <Button
                variant="ghost"
                size="sm"
                className="w-full h-7 text-xs text-muted-foreground"
                onClick={() => onChange({ ...values, pathPrefix: undefined, nameLike: undefined })}
              >
                {searchBar.clearAdvanced}
              </Button>
            )}

          </PopoverContent>
        </Popover>

        {/* 主输入框：默认为 Path Like (Fuzzy) */}
        <div className="relative w-full">
          <Search className="absolute left-19 top-2.5 h-4 w-4 text-muted-foreground pointer-events-none" />
          <Input
            type="search"
            placeholder={searchBar.mainPlaceholder.value}
            className="pl-24.75 bg-muted/50 w-full"
            value={(values.pathLike as string) || ""}
            onChange={(e) => updateValue("pathLike", e.target.value)}
          />
        </div>
      </div>

      {/* 结果计数 */}
      {total !== undefined && (
        <div className="text-sm text-muted-foreground hidden sm:block whitespace-nowrap">
          {total} {searchBar.resultsCount}
        </div>
      )}
    </div>
  );
}