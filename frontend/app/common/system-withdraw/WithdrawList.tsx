import React, { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  CheckCircle2,
  XCircle,
  Send,
  AlertTriangle,
  Eye,
  History,
  ArrowRight,
  Info,
  FileText,
} from "lucide-react";
import { toast } from "sonner";
import { cn } from "@/lib/utils";

// 引入 API 和类型
import { IDUNN_API } from "~/api";
import type { SystemWithdraw } from "~/api/generated";
import {
  GenericCrudTable,
  type TableActions,
} from "../generic-crud-table/generic-crud-table";

// --- 状态配置：改为动作导向 ---
const statusConfig: Record<
  string,
  { color: string; text: string; actionDesc: string }
> = {
  CREATED: {
    color: "bg-blue-500",
    text: "已申请",
    actionDesc: "等待管理员审批",
  },
  APPROVED: {
    color: "bg-yellow-500",
    text: "审批通过",
    actionDesc: "等待管理员转账",
  },
  REJECTED: { color: "bg-red-500", text: "已拒绝", actionDesc: "申请已被驳回" },
  PAID: {
    color: "bg-purple-500",
    text: "已转账",
    actionDesc: "请核对虚拟点数到账情况并确认",
  },
  FINISHED: {
    color: "bg-green-500",
    text: "流程结束",
    actionDesc: "处理流程已完成",
  },
  ERROR: {
    color: "bg-orange-600",
    text: "处理异常",
    actionDesc: "请联系管理员处理",
  },
};

export function WithdrawList({ mode }: { mode: "user" | "admin" }) {
  // 核心：存储选中的行数据以及该表格注入的 actions
  const [activeContext, setActiveContext] = useState<{
    data: SystemWithdraw;
    actions: TableActions;
  } | null>(null);

  const [actionDialog, setActionDialog] = useState<{
    open: boolean;
    targetStatus: string;
  }>({ open: false, targetStatus: "" });

  const [reason, setReason] = useState("");
  const [proof, setProof] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchData = async (
    page: number,
    size: number,
    search: string,
    sort: string,
  ) => {
    const res =
      mode === "user"
        ? await IDUNN_API.apiV1CommercialWithdrawalsMeGet(
          page,
          size,
          sort || undefined,
        )
        : await IDUNN_API.apiV1CommercialWithdrawalsGet(
          page,
          size,
          sort || undefined,
        );
    return res.data;
  };

  const handleUpdateStatus = async () => {
    if (!activeContext) return;

    setIsSubmitting(true);
    try {
      await IDUNN_API.apiV1CommercialWithdrawalsIdStatusPatch(
        String(activeContext.data.id),
        {
          status: actionDialog.targetStatus,
          reason: reason,
          transferProof: proof,
        },
      );

      toast.success("操作执行成功");

      // 使用注入的能力刷新当前页
      activeContext.actions.refreshPage();

      // 重置状态
      setActionDialog({ open: false, targetStatus: "" });
      setActiveContext(null);
      setReason("");
      setProof("");
    } catch (e) {
      toast.error("操作执行失败，请检查状态权限");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-4">
      <GenericCrudTable<SystemWithdraw>
        uid="syswthdrw"
        getRowId={(row) => row.id!}
        list={fetchData}
        pageSize={15}
        schema={{
          id: { title: "ID" },
          username: { title: "申请人" },
          amount: {
            title: "处理虚拟点数",
            sortable: true,
            render: (val) => (
              <span className="font-mono font-bold text-primary">
                {Math.round(val * 100).toLocaleString()}
              </span>
            ),
          },
          status: {
            title: "当前进度",
            render: (val) => (
              <Badge
                variant="secondary"
                className={cn(
                  "font-medium",
                  statusConfig[val].color.replace("bg-", "text-"),
                )}
              >
                {statusConfig[val].text}
              </Badge>
            ),
          },
          actions: {
            title: "操作",
            // 利用 GenericCrudTable 注入的 actions 参数
            render: (_, row, actions) => (
              <Button
                variant="outline"
                size="sm"
                className="h-8"
                onClick={() => setActiveContext({ data: row, actions })}
              >
                <Eye className="w-3.5 h-3.5 mr-1.5" /> 详情/处理
              </Button>
            ),
          },
        }}
      />

      {/* 处理详情对话框 */}
      <Dialog
        open={!!activeContext}
        onOpenChange={(o) => !o && !isSubmitting && setActiveContext(null)}
      >
          <DialogContent className="sm:max-w-155 p-0 overflow-hidden">
            <DialogHeader className="p-6 bg-muted/30 border-b text-left">
              <DialogTitle className="text-xl flex items-center gap-2">
              <FileText className="w-5 h-5 text-primary" />
              处理记录详情 #{activeContext?.data.id}
            </DialogTitle>
          </DialogHeader>

          {activeContext && (
            <div className="p-6 space-y-8">
              {/* 核心数据卡片 */}
              <div className="grid grid-cols-2 gap-6 p-4 rounded-xl border bg-card shadow-sm">
                <div className="space-y-1">
                  <Label className="text-muted-foreground font-normal">
                    处理虚拟点数
                  </Label>
                  <p className="text-2xl font-bold tracking-tight text-primary">
                    {Math.round((activeContext.data.amount || 0) * 100).toLocaleString()}
                  </p>
                </div>
                <div className="space-y-1">
                  <Label className="text-muted-foreground font-normal">
                    当前步骤
                  </Label>
                  <div className="flex items-center gap-2">
                    <Badge
                      className={statusConfig[activeContext.data.status!].color}
                    >
                      {statusConfig[activeContext.data.status!].text}
                    </Badge>
                  </div>
                </div>
                {activeContext.data.transferProof && (
                  <div className="col-span-2 space-y-2 border-t pt-4">
                    <Label className="text-xs uppercase text-muted-foreground">
                      转账虚拟点数变动/凭证
                    </Label>
                    <pre className="text-xs bg-muted p-3 rounded-md overflow-x-auto font-mono border">
                      {activeContext.data.transferProof}
                    </pre>
                  </div>
                )}
              </div>

              {/* 交互式流程图 */}
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-semibold flex items-center gap-2">
                      <History className="w-4 h-4 text-primary" />
                    流程生命周期
                  </h3>
                  <span className="text-[11px] text-muted-foreground flex items-center gap-1">
                    <Info className="w-3 h-3" />
                    高亮节点需您处理
                  </span>
                </div>

                <div className="flex items-center justify-between gap-1">
                  <StatusNode
                    status="CREATED"
                    current={activeContext.data.status!}
                    label="发起申请"
                  />
                  <FlowArrow />

                  {activeContext.data.status === "REJECTED" ? (
                    <StatusNode
                      status="REJECTED"
                      current="REJECTED"
                      label="申请拒绝"
                    />
                  ) : (
                    <>
                      <StatusNode
                        status="APPROVED"
                        current={activeContext.data.status!}
                        label="审批通过"
                        canAction={
                          mode === "admin" &&
                          activeContext.data.status === "CREATED"
                        }
                        onClick={() =>
                          setActionDialog({
                            open: true,
                            targetStatus: "APPROVED",
                          })
                        }
                      />
                      <FlowArrow />
                      <StatusNode
                        status="PAID"
                        current={activeContext.data.status!}
                        label="执行转账"
                        canAction={
                          mode === "admin" &&
                          activeContext.data.status === "APPROVED"
                        }
                        onClick={() =>
                          setActionDialog({ open: true, targetStatus: "PAID" })
                        }
                      />
                      <FlowArrow />
                      <StatusNode
                        status="FINISHED"
                        current={activeContext.data.status!}
                        label="确认收款"
                        canAction={
                          activeContext.data.status === "PAID" ||
                          activeContext.data.status === "ERROR"
                        }
                        onClick={() =>
                          setActionDialog({
                            open: true,
                            targetStatus: "FINISHED",
                          })
                        }
                      />
                    </>
                  )}
                </div>

                {/* 底部动态指引 */}
                <div className="mt-6 p-4 rounded-lg border border-primary/20 bg-primary/5 flex items-start gap-3">
                  <Info className="w-5 h-5 text-primary shrink-0 mt-0.5" />
                  <div>
                    <p className="text-sm font-bold text-primary">下一步指引</p>
                    <p className="text-xs text-muted-foreground mt-1 leading-relaxed">
                      {statusConfig[activeContext.data.status!].actionDesc}
                    </p>
                  </div>
                </div>

                {/* 拒绝/异常操作 */}
                <div className="flex items-center justify-end gap-3 pt-2">
                  {mode === "admin" &&
                    (activeContext.data.status === "CREATED" ||
                      activeContext.data.status === "ERROR") && (
                      <Button
                        variant="outline"
                        size="sm"
                        className="text-red-600 hover:bg-red-50"
                        onClick={() =>
                          setActionDialog({
                            open: true,
                            targetStatus: "REJECTED",
                          })
                        }
                      >
                        <XCircle className="w-4 h-4 mr-2" /> 拒绝/放弃申请
                      </Button>
                    )}
                  {activeContext.data.status === "PAID" && (
                    <Button
                      variant="outline"
                      size="sm"
                      className="text-orange-600 border-orange-200 hover:bg-orange-50"
                      onClick={() =>
                        setActionDialog({ open: true, targetStatus: "ERROR" })
                      }
                    >
                      <AlertTriangle className="w-4 h-4 mr-2" /> 反馈异常
                    </Button>
                  )}
                </div>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* 动作执行弹窗 */}
      <Dialog
        open={actionDialog.open}
        onOpenChange={(o) =>
          !o &&
          !isSubmitting &&
          setActionDialog((prev) => ({ ...prev, open: false }))
        }
      >
        <DialogContent className="sm:max-w-100">
          <DialogHeader className="text-left">
            <DialogTitle className="text-lg">确认操作</DialogTitle>
            <DialogDescription>
              您正在将处理状态变更为：
              <Badge variant="outline" className="ml-1">
                {statusConfig[actionDialog.targetStatus]?.text}
              </Badge>
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-2">
            {["REJECTED", "ERROR", "FINISHED"].includes(
              actionDialog.targetStatus,
            ) && (
                <div className="space-y-2">
                  <Label className="text-xs">操作说明/原因 (必填)</Label>
                  <Textarea
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder="请输入处理说明..."
                    className="min-h-25 text-sm"
                  />
                </div>
              )}
            {actionDialog.targetStatus === "PAID" && (
              <div className="space-y-2">
                <Label className="text-xs">转账凭证/单号</Label>
                <Input
                  value={proof}
                  onChange={(e) => setProof(e.target.value)}
                  placeholder="虚拟点数变动号或备注信息..."
                  className="text-sm"
                />
              </div>
            )}
          </div>
          <DialogFooter className="gap-2 sm:gap-0">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setActionDialog({ open: false, targetStatus: "" })}
              disabled={isSubmitting}
            >
              取消
            </Button>
            <Button
              size="sm"
              onClick={handleUpdateStatus}
              disabled={isSubmitting}
            >
              {isSubmitting ? "处理中..." : "提交执行"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

// --- 流程节点组件 ---
function StatusNode({
  status,
  current,
  label,
  canAction,
  onClick,
}: {
  status: string;
  current: string;
  label: string;
  canAction?: boolean;
  onClick?: () => void;
}) {
  const statusList = [
    "CREATED",
    "APPROVED",
    "PAID",
    "FINISHED",
    "ERROR",
    "REJECTED",
  ];
  const currentIndex = statusList.indexOf(current);
  const nodeIndex = statusList.indexOf(status);

  const isPast =
    nodeIndex < currentIndex && current !== "REJECTED" && current !== "ERROR";
  const isCurrent = current === status;

  return (
    <div
      onClick={canAction ? onClick : undefined}
      className={cn(
        "relative flex flex-col items-center gap-2 flex-1 transition-all duration-300 p-3 rounded-2xl border-2",
        canAction
          ? "cursor-pointer bg-primary border-primary shadow-[0_0_15px_rgba(var(--primary),0.3)]"
          : "border-transparent",
        canAction &&
        "hover:scale-110 hover:shadow-[0_0_25px_rgba(var(--primary),0.5)] active:scale-95 group",
        isCurrent &&
        !canAction &&
        "bg-primary/5 border-primary/20 shadow-inner",
      )}
    >
      <div
        className={cn(
          "w-9 h-9 rounded-full flex items-center justify-center border-2 transition-all duration-300",
          (isPast || isCurrent) && !canAction
            ? "bg-background border-primary text-primary"
            : "",
          canAction
            ? "bg-primary-foreground border-primary-foreground text-primary"
            : "",
          !isPast &&
          !isCurrent &&
          "border-dashed border-muted-foreground/30 text-muted-foreground",
        )}
      >
        {isPast ? (
          <CheckCircle2 className="w-5 h-5" />
        ) : (
          <span className="text-sm font-bold">{nodeIndex + 1}</span>
        )}
      </div>

      <div className="flex flex-col items-center">
        <span
          className={cn(
            "text-[13px] font-bold whitespace-nowrap",
            canAction
              ? "text-primary-foreground"
              : isPast || isCurrent
                ? "text-foreground"
                : "text-muted-foreground",
          )}
        >
          {label}
        </span>
        {canAction && (
          <div className="mt-1 bg-primary-foreground/20 px-1.5 py-0.5 rounded text-[10px] text-primary-foreground font-black uppercase tracking-widest animate-bounce">
            立即处理
          </div>
        )}
      </div>

      {canAction && (
        <div
          className="absolute inset-0 rounded-2xl bg-primary/20 animate-ping -z-10"
          style={{ animationDuration: "3s" }}
        />
      )}
    </div>
  );
}

function FlowArrow() {
  return (
    <div className="flex items-center justify-center w-6 shrink-0 -mt-6">
      <ArrowRight className="w-4 h-4 text-muted-foreground/20" />
    </div>
  );
}
