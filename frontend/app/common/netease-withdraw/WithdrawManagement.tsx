import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import {
  CheckCircle2,
  History,
  BadgeInfo,
  TrendingDown,
  PlusCircle,
} from "lucide-react";

// 引入 API 和类型
import { IDUNN_API } from "~/api";
import type { NeteaseWithdraw, NeteaseWithdrawInput } from "~/api/generated";
import {
  GenericCrudTable,
  type PageResponse,
} from "../generic-crud-table/generic-crud-table";
import { useNavigate } from "react-router";

// ---------- API 封装 ----------

export const fetchWithdraws = async (
  page: number,
  size: number,
  search: string,
  sort: string,
): Promise<PageResponse<NeteaseWithdraw>> => {
  const response = await IDUNN_API.listWithdraws(
    search || undefined,
    page,
    size,
    sort || undefined,
  );
  return response.data;
};

// ---------- 主组件 ----------

interface WithdrawManagementProps {
  pageSize?: number;
}

export function WithdrawManagement({ pageSize = 20 }: WithdrawManagementProps) {
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  const handleRefresh = () => setRefreshKey((prev) => prev + 1);

  const nav = useNavigate();

  return (
    <div className="space-y-4">
      <GenericCrudTable<NeteaseWithdraw>
        uid="newthdrw"
        key={refreshKey}
        getRowId={(row) => row.id!}
        list={fetchWithdraws}
        pageSize={pageSize}
        searchFields={[{ key: "username", label: "操作人", fuzzy: true }]}
        create={() => setIsCreateOpen(true)}
        schema={{
          id: { title: "ID" },
          username: {
            title: "操作人",
            filterable: true,
            render: (val) => (
              <div className="flex items-center gap-2">
                <span className="font-medium">{val}</span>
              </div>
            ),
          },
          originalValue: {
            title: "原始虚拟点数",
            sortable: true,
            render: (val) => (
              <div className="flex items-center gap-1 font-mono text-blue-600">
                <BadgeInfo className="w-3.5 h-3.5" />
                <span>{Math.round((val || 0) * 100).toLocaleString()}</span>
              </div>
            ),
          },
          withdrawValue: {
            title: "实际处理点数",
            render: (val) => (
              <div className="font-mono text-green-600 font-semibold">
                {Math.round((val || 0) * 100).toLocaleString()}
              </div>
            ),
          },
          ratio: {
            title: "费率 (Ratio)",
            sortable: true,
            render: (val) => (
              <div className="flex items-center gap-1 text-muted-foreground italic text-xs">
                <TrendingDown className="w-3 h-3 text-orange-400" />
                <span>{(val * 100).toFixed(2)}%</span>
              </div>
            ),
          },
          usedOriginalValue: {
            title: "消耗进度 (已用/剩余)",
            render: (val, row) => {
              const used = val || 0;
              const total = row.originalValue || 0;
              const remaining = Math.max(0, total - used);
              const percentage = Math.min(100, (used / total) * 100);

              return (
                <div className="w-48 space-y-1.5">
                  {/* 进度条展示 */}
                  <div className="h-2 w-full bg-secondary rounded-full overflow-hidden flex">
                    <div
                      className="h-full bg-blue-500 transition-all"
                      style={{ width: `${percentage}%` }}
                    />
                  </div>

                  {/* 详细数据文字与图标 */}
                  <div className="flex justify-between items-center text-[10px] font-medium uppercase tracking-wider">
                    <div className="flex items-center gap-1 text-blue-600">
                      <CheckCircle2 className="w-3 h-3" />
                      <span>{Math.round(used * 100).toLocaleString()}</span>
                    </div>
                    <div className="flex items-center gap-1 text-muted-foreground">
                      <History className="w-3 h-3" />
                      <span>剩 {Math.round(remaining * 100).toLocaleString()}</span>
                    </div>
                  </div>
                </div>
              );
            },
          },
          saveTimeMs: {
            title: "记录时间",
            sortable: true,
            render: (val: number) =>
              val ? (
                <div className="text-xs text-muted-foreground">
                  {new Date(val).toLocaleString("zh-CN", { hour12: false })}
                </div>
              ) : (
                "-"
              ),
          },
        }}
        rowActions={(row) => (
          <div>
            <Button
              variant="outline"
              onClick={() => nav(`/commercial/netease-withdraws/${row.id}`)}
            >
              查看详情
            </Button>
          </div>
        )}
      />

      <CreateWithdrawDialog
        open={isCreateOpen}
        onOpenChange={setIsCreateOpen}
        onSuccess={() => {
          setIsCreateOpen(false);
          handleRefresh();
        }}
      />
    </div>
  );
}

// ---------- 新建对话框组件 ----------

function CreateWithdrawDialog({
  open,
  onOpenChange,
  onSuccess,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}) {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<Partial<NeteaseWithdrawInput>>({
    originalValue: 0,
    withdrawValue: 0,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await IDUNN_API.createWithdraw(formData as NeteaseWithdrawInput);
      toast.success("处理记录已成功添加");
      onSuccess();
    } catch (error) {
      console.error(error);
      toast.error("提交失败，请检查网络或输入数值");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-106.25">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <PlusCircle className="w-5 h-5 text-primary" />
            新建处理记录
          </DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6 py-4">
          <div className="space-y-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="originalValue" className="text-right text-xs">
                原始虚拟点数 (PE)
              </Label>
              <Input
                id="originalValue"
                type="number"
                step="0.000001"
                className="col-span-3 font-mono"
                placeholder="0.00"
                value={formData.originalValue}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    originalValue: parseFloat(e.target.value) || 0,
                  })
                }
                required
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="withdrawValue" className="text-right text-xs">
                实际处理点数
              </Label>
              <Input
                id="withdrawValue"
                type="number"
                step="0.000001"
                className="col-span-3 font-mono"
                placeholder="0.00"
                value={formData.withdrawValue}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    withdrawValue: parseFloat(e.target.value) || 0,
                  })
                }
                required
              />
            </div>
          </div>

          <div className="bg-amber-50 border border-amber-100 p-3 rounded-md text-[11px] text-amber-700 leading-relaxed">
            <strong>自动计算：</strong> 折算比例（Ratio）将由后台根据{" "}
            <code>实际处理点数 / 原始虚拟点数</code> 自动生成，无需手动维护。
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
            >
              取消
            </Button>
            <Button type="submit" disabled={loading} className="min-w-20">
              {loading ? "提交中..." : "确认添加"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
