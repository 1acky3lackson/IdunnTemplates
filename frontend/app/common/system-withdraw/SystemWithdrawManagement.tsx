import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/badge';
import { PlusCircle, Info, Settings2, Wallet } from 'lucide-react';
import { useNavigate } from 'react-router';

// 引入 API 和类型
import { SystemWithdrawApi } from '~/api/system-withdraw-api';
import type { SystemWithdraw, SystemWithdrawCreateRequest, SystemWithdrawStatusUpdateRequest } from '~/api/generated/model/system-withdraw';
import { GenericCrudTable, type PageResponse } from '../generic-crud-table/generic-crud-table';

// ---------- API 封装 ----------

export const fetchSystemWithdraws = async (
  page: number,
  size: number,
  search: string,
  sort: string,
  mode: 'all' | 'me'
): Promise<PageResponse<SystemWithdraw>> => {
  if (mode === 'all') {
    return await SystemWithdrawApi.listAllWithdrawals(page, size, search, sort);
  } else {
    return await SystemWithdrawApi.listMyWithdrawals(page, size, search, sort);
  }
};

// ---------- 主组件 ----------

interface SystemWithdrawManagementProps {
  pageSize?: number;
  mode?: 'all' | 'me';
}

const StatusBadge = ({ status }: { status: string }) => {
  switch (status) {
    case 'CREATED': return <Badge variant="secondary">待审批</Badge>;
    case 'APPROVED': return <Badge className="bg-blue-500">已审批，待转账</Badge>;
    case 'REJECTED': return <Badge variant="destructive">已拒绝</Badge>;
    case 'PAID': return <Badge className="bg-yellow-500">已转账，待确认</Badge>;
    case 'FINISHED': return <Badge className="bg-green-500">已完成</Badge>;
    case 'ERROR': return <Badge variant="destructive">异常</Badge>;
    default: return <Badge variant="outline">{status}</Badge>;
  }
};

export function SystemWithdrawManagement({ pageSize = 20, mode = 'all' }: SystemWithdrawManagementProps) {
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isManageOpen, setIsManageOpen] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<SystemWithdraw | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const handleRefresh = () => setRefreshKey(prev => prev + 1);
  const nav = useNavigate();

  return (
    <div className="space-y-4">
      <GenericCrudTable<SystemWithdraw>
        key={refreshKey}
        getRowId={(row) => row.id}
        list={(page, size, search, sort) => fetchSystemWithdraws(page, size, search, sort, mode)}
        pageSize={pageSize}

        searchFields={mode === 'all' ? [
          { key: 'username', label: '申请人', fuzzy: true },
        ] : []}

        create={mode === 'me' ? () => setIsCreateOpen(true) : undefined}

        schema={{
          'id': { title: 'ID', sortable: true },
          'username': {
            title: '申请人',
            filterable: mode === 'all',
            render: (val) => (
                <div className="flex items-center gap-2">
                    <span className="font-medium">{val}</span>
                </div>
            )
          },
          'amount': {
            title: '提现金额',
            sortable: true,
            render: (val) => (
                <div className="flex items-center gap-1 font-mono text-green-600 font-semibold">
                    <Wallet className="w-4 h-4" />
                    <span>¥{val?.toFixed(2)}</span>
                </div>
            )
          },
          'status': {
            title: '状态',
            render: (val) => <StatusBadge status={val} />
          },
          'createTimeMs': {
            title: '申请时间',
            sortable: true,
            render: (val: number) => val ? (
                <div className="text-xs text-muted-foreground">
                    {new Date(val).toLocaleString('zh-CN', { hour12: false })}
                </div>
            ) : '-'
          }
        }}

        rowActions={(row) => (
            <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => nav(mode === 'me' ? `/me/system-withdraws/${row.id}` : `/commercial/system-withdraws/${row.id}`)}
                >
                  <Info className="w-4 h-4 mr-1" />
                  详情
                </Button>
                {/* 如果是待审批，且在管理模式下，显示处理按钮 */}
                {mode === 'all' && ['CREATED', 'APPROVED', 'PAID', 'ERROR'].includes(row.status) && (
                  <Button
                    variant="default"
                    size="sm"
                    onClick={() => {
                      setSelectedRecord(row);
                      setIsManageOpen(true);
                    }}
                  >
                    <Settings2 className="w-4 h-4 mr-1" />
                    处理
                  </Button>
                )}
                {/* 用户自己可以在 PAID 和 ERROR 状态下操作 */}
                {mode === 'me' && ['PAID', 'ERROR'].includes(row.status) && (
                  <Button
                    variant="default"
                    size="sm"
                    onClick={() => {
                      setSelectedRecord(row);
                      setIsManageOpen(true);
                    }}
                  >
                    <Settings2 className="w-4 h-4 mr-1" />
                    处理
                  </Button>
                )}
            </div>
        )}
      />

      {mode === 'me' && (
        <CreateSystemWithdrawDialog
          open={isCreateOpen}
          onOpenChange={setIsCreateOpen}
          onSuccess={() => {
            setIsCreateOpen(false);
            handleRefresh();
          }}
        />
      )}

      {selectedRecord && (
        <ManageSystemWithdrawDialog
          open={isManageOpen}
          onOpenChange={(open) => {
            setIsManageOpen(open);
            if (!open) setSelectedRecord(null);
          }}
          record={selectedRecord}
          mode={mode}
          onSuccess={() => {
            setIsManageOpen(false);
            setSelectedRecord(null);
            handleRefresh();
          }}
        />
      )}
    </div>
  );
}

// ---------- 新建提现申请对话框 ----------

function CreateSystemWithdrawDialog({
  open,
  onOpenChange,
  onSuccess
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}) {
  const [loading, setLoading] = useState(false);
  const [amount, setAmount] = useState<number>(0);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (amount <= 0) {
      toast.error("提现金额必须大于0");
      return;
    }
    setLoading(true);
    try {
      await SystemWithdrawApi.createWithdrawal({ amount });
      toast.success("提现申请提交成功");
      onSuccess();
    } catch (error: any) {
      console.error(error);
      toast.error(error.response?.data?.message || "提交失败，请检查余额是否充足");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <PlusCircle className="w-5 h-5 text-primary" />
            申请提现
          </DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6 py-4">
          <div className="space-y-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="amount" className="text-right text-sm">
                提现金额 (¥)
              </Label>
              <Input
                id="amount"
                type="number"
                step="0.01"
                min="0.01"
                className="col-span-3 font-mono"
                placeholder="0.00"
                value={amount || ''}
                onChange={(e) => setAmount(parseFloat(e.target.value) || 0)}
                required
              />
            </div>
          </div>

          <div className="bg-blue-50 border border-blue-100 p-3 rounded-md text-sm text-blue-700 leading-relaxed">
            提交申请后，将从您的账户余额中扣除相应金额。管理员审批通过并转账后，请确认收款完成流程。
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                取消
            </Button>
            <Button type="submit" disabled={loading || amount <= 0} className="min-w-[80px]">
              {loading ? "提交中..." : "确认申请"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ---------- 处理提现状态对话框 ----------

function ManageSystemWithdrawDialog({
  open,
  onOpenChange,
  record,
  mode,
  onSuccess
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  record: SystemWithdraw;
  mode: 'all' | 'me';
  onSuccess: () => void;
}) {
  const [loading, setLoading] = useState(false);
  const [action, setAction] = useState<string>('');
  const [reason, setReason] = useState<string>('');
  const [transferProof, setTransferProof] = useState<string>('');

  const isAdmin = mode === 'all';

  const handleAction = async (targetStatus: string) => {

    // 基础校验
    if (targetStatus === 'REJECTED' && !reason.trim()) {
      toast.error("必须填写拒绝/放弃理由");
      return;
    }
    if (targetStatus === 'PAID' && !transferProof.trim()) {
      toast.error("必须填写转账凭证");
      return;
    }
    if (targetStatus === 'ERROR' && !reason.trim()) {
      toast.error("必须填写异常说明");
      return;
    }
    if (targetStatus === 'FINISHED' && record.status === 'ERROR' && !reason.trim()) {
      toast.error("确认异常交易成立必须填写说明");
      return;
    }

    setLoading(true);
    try {
      await SystemWithdrawApi.updateWithdrawalStatus(record.id, {
        status: targetStatus,
        reason,
        transferProof
      });
      toast.success("操作成功");
      onSuccess();
    } catch (error: any) {
      console.error(error);
      toast.error(error.response?.data?.message || "操作失败");
    } finally {
      setLoading(false);
    }
  };

  // 渲染可用的操作
  const renderActions = () => {
    if (isAdmin) {
      switch (record.status) {
        case 'CREATED':
          return (
            <div className="flex flex-col gap-4">
              <Button onClick={() => handleAction('APPROVED')} disabled={loading} className="w-full">审批通过</Button>
              <div className="space-y-2">
                <Label>拒绝理由</Label>
                <Textarea value={reason} onChange={e => setReason(e.target.value)} placeholder="请填写拒绝理由..." />
                <Button variant="destructive" onClick={() => handleAction('REJECTED')} disabled={loading} className="w-full">审批拒绝</Button>
              </div>
            </div>
          );
        case 'APPROVED':
          return (
            <div className="space-y-4">
              <div className="space-y-2">
                <Label>转账凭证 (交易单号等)</Label>
                <Input value={transferProof} onChange={e => setTransferProof(e.target.value)} placeholder="请填写转账凭证..." />
              </div>
              <Button onClick={() => handleAction('PAID')} disabled={loading} className="w-full bg-yellow-600 hover:bg-yellow-700">确认已转账</Button>
            </div>
          );
        case 'PAID':
          return (
            <div className="flex flex-col gap-4">
              <Button onClick={() => handleAction('FINISHED')} disabled={loading} className="w-full bg-green-600 hover:bg-green-700">代用户确认收款</Button>
              <div className="space-y-2">
                <Label>异常说明 (如用户反馈未收到)</Label>
                <Textarea value={reason} onChange={e => setReason(e.target.value)} placeholder="请填写异常说明..." />
                <Button variant="destructive" onClick={() => handleAction('ERROR')} disabled={loading} className="w-full">标记为异常</Button>
              </div>
            </div>
          );
        case 'ERROR':
          return (
            <div className="flex flex-col gap-4">
               <div className="space-y-2">
                <Label>处理说明 / 理由</Label>
                <Textarea value={reason} onChange={e => setReason(e.target.value)} placeholder="必填，填写沟通后的处理说明..." />
              </div>
              <div className="flex gap-2">
                <Button variant="destructive" onClick={() => handleAction('REJECTED')} disabled={loading} className="flex-1">放弃请求 (退回金额)</Button>
                <Button onClick={() => handleAction('FINISHED')} disabled={loading} className="flex-1 bg-green-600 hover:bg-green-700">交易成立 (确认完成)</Button>
              </div>
            </div>
          );
        default:
          return null;
      }
    } else {
      // User actions (mode === 'me')
      switch (record.status) {
        case 'PAID':
          return (
             <div className="flex flex-col gap-4">
              <Button onClick={() => handleAction('FINISHED')} disabled={loading} className="w-full bg-green-600 hover:bg-green-700">确认已收款</Button>
              <div className="space-y-2">
                <Label>异常说明 (若未收到款项)</Label>
                <Textarea value={reason} onChange={e => setReason(e.target.value)} placeholder="请填写未收到的说明..." />
                <Button variant="destructive" onClick={() => handleAction('ERROR')} disabled={loading} className="w-full">未收到款项，标记异常</Button>
              </div>
            </div>
          );
        case 'ERROR':
           return (
             <div className="flex flex-col gap-4">
              <div className="space-y-2">
                <Label>问题解决说明 (若已收到)</Label>
                <Textarea value={reason} onChange={e => setReason(e.target.value)} placeholder="请填写说明..." />
              </div>
              <Button onClick={() => handleAction('FINISHED')} disabled={loading} className="w-full bg-green-600 hover:bg-green-700">已解决，确认收款</Button>
              <p className="text-xs text-muted-foreground text-center">如果想要放弃该提现（退回余额），请联系管理员处理。</p>
            </div>
          );
        default:
          return <p className="text-sm text-muted-foreground text-center py-4">当前状态无需操作</p>;
      }
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>处理提现记录 #{record.id}</DialogTitle>
        </DialogHeader>

        <div className="py-4 space-y-4">
          <div className="grid grid-cols-2 gap-4 text-sm bg-muted/50 p-4 rounded-lg">
            <div>
              <span className="text-muted-foreground mr-2">申请人:</span>
              <span className="font-medium">{record.username}</span>
            </div>
            <div>
              <span className="text-muted-foreground mr-2">提现金额:</span>
              <span className="font-medium font-mono text-green-600">¥{record.amount.toFixed(2)}</span>
            </div>
            <div>
              <span className="text-muted-foreground mr-2">当前状态:</span>
              <StatusBadge status={record.status} />
            </div>
            {record.transferProof && (
              <div className="col-span-2">
                <span className="text-muted-foreground mr-2">转账凭证:</span>
                <span className="font-mono">{record.transferProof}</span>
              </div>
            )}
            {record.errorReason && (
              <div className="col-span-2 text-destructive">
                <span className="mr-2 font-semibold">异常原因:</span>
                <span>{record.errorReason}</span>
              </div>
            )}
            {record.rejectReason && (
               <div className="col-span-2 text-destructive">
                <span className="mr-2 font-semibold">拒绝原因:</span>
                <span>{record.rejectReason}</span>
              </div>
            )}
          </div>

          <div className="border-t pt-4">
            <h4 className="font-medium mb-4">执行操作</h4>
            {renderActions()}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
