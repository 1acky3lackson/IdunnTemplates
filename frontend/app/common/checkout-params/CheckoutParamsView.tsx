import React, { useEffect, useState } from 'react';
import { Button } from '~/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '~/components/ui/card';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '~/components/ui/dialog';
import { Input } from '~/components/ui/input';
import { Label } from '~/components/ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '~/components/ui/table';
import { toast } from "sonner"

// 全局参数实体类型（与后端一致）
export interface GlobalCheckoutParam {
  id: number;
  taixueRatio: number;
  commercialRatio: number;
  templateDefectParam: number;
  placerRatio: number;
  uploaderRatio: number;
  releaseDelayDays: number;
  createUsername: string;
  createTimeMs: number;
  disableTimeMs?: number | null;
  disableReason?: string | null;
}

// Props 接口，定义了需要外部传入的 API 函数
interface GlobalParamManagerProps {
  /** 获取当前有效配置 */
  fetchCurrentConfig: () => Promise<GlobalCheckoutParam>;
  /** 获取所有历史配置（按创建时间倒序） */
  fetchHistoryConfigs: () => Promise<GlobalCheckoutParam[]>;
  /** 更新配置：传入新参数对象、更新原因、操作人，返回更新后的新记录 */
  updateConfig: (
    newConfig: Partial<GlobalCheckoutParam>,
    updateReason: string,
  ) => Promise<GlobalCheckoutParam>;
}

export function GlobalParamManager({
  fetchCurrentConfig,
  fetchHistoryConfigs,
  updateConfig,
}: GlobalParamManagerProps) {
  const [current, setCurrent] = useState<GlobalCheckoutParam | null>(null);
  const [history, setHistory] = useState<GlobalCheckoutParam[]>([]);
  const [loading, setLoading] = useState(false);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedConfig, setSelectedConfig] = useState<Partial<GlobalCheckoutParam>>({});
  const [updateReason, setUpdateReason] = useState('');
  const [operator, setOperator] = useState('');

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      await Promise.allSettled([
        fetchCurrentConfig().then(data => setCurrent(data)),
        fetchHistoryConfigs().then(data => setHistory(data)),
      ]);
    } catch (error) {
      toast.error('加载失败', {
        description: error instanceof Error ? error.message : '未知错误',
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 打开“据此更新”对话框，预填选中行的参数
  const handleUpdateClick = (config: GlobalCheckoutParam) => {
    setSelectedConfig({
      taixueRatio: config.taixueRatio,
      commercialRatio: config.commercialRatio,
      templateDefectParam: config.templateDefectParam,
      placerRatio: config.placerRatio,
      uploaderRatio: config.uploaderRatio,
      releaseDelayDays: config.releaseDelayDays,
    });
    setUpdateReason('');
    setOperator('');
    setDialogOpen(true);
  };

  // 提交更新
  const handleSubmitUpdate = async () => {
    if (!updateReason.trim()) {
      toast.info('更新原因不能为空');
      return;
    }

    if (selectedConfig.releaseDelayDays === undefined || selectedConfig.releaseDelayDays % 1 !== 0) {
      toast.info('延迟天数必须是整数');
      return;
    }

    try {
      await updateConfig(selectedConfig, updateReason);
      toast.success('更新成功');
      setDialogOpen(false);
      await loadData(); // 刷新数据
    } catch (error) {
      toast.error('更新失败', {
        description: error instanceof Error ? error.message : '未知错误',
      });
    }
  };

  // 格式化时间戳
  const formatTime = (ms: number) => new Date(ms).toLocaleString();

  return (
    <div className="space-y-8 p-4">
      {/* 当前激活参数卡片 */}
      <Card>
        <CardHeader>
          <CardTitle>当前激活参数</CardTitle>
        </CardHeader>
        <CardContent>
          {loading && !current ? (
            <div>加载中...</div>
          ) : current ? (
            <div className="grid grid-cols-2 gap-4">
              <div>
                <span className="font-medium">太学比例：</span>
                {current.taixueRatio}
              </div>
              <div>
                <span className="font-medium">商务处比例：</span>
                {current.commercialRatio}
              </div>
              <div>
                <span className="font-medium">模板衰减参数：</span>
                {current.templateDefectParam}
              </div>
              <div>
                <span className="font-medium">放置者比例：</span>
                {current.placerRatio}
              </div>
              <div>
                <span className="font-medium">上传者比例：</span>
                {current.uploaderRatio}
              </div>
              <div>
                <span className="font-medium">收益释放延迟天数：</span>
                {current.releaseDelayDays}
              </div>
              <div>
                <span className="font-medium">创建人：</span>
                {current.createUsername}
              </div>
              <div>
                <span className="font-medium">创建时间：</span>
                {formatTime(current.createTimeMs)}
              </div>
            </div>
          ) : (
            <div>暂无有效配置</div>
          )}
        </CardContent>
      </Card>

      {/* 历史记录表格 */}
      {
        history.length > 0 &&
        <Card>
          <CardHeader>
            <CardTitle>历史配置记录</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>太学比例</TableHead>
                  <TableHead>商务处比例</TableHead>
                  <TableHead>模板衰减参数</TableHead>
                  <TableHead>放置者比例</TableHead>
                  <TableHead>上传者比例</TableHead>
                  <TableHead>收益释放延迟 (天)</TableHead>
                  <TableHead>创建人</TableHead>
                  <TableHead>创建时间</TableHead>
                  <TableHead>禁用时间</TableHead>
                  <TableHead>禁用原因</TableHead>
                  <TableHead>操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {history.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>{item.taixueRatio}</TableCell>
                    <TableCell>{item.commercialRatio}</TableCell>
                    <TableCell>{item.templateDefectParam}</TableCell>
                    <TableCell>{item.placerRatio}</TableCell>
                    <TableCell>{item.uploaderRatio}</TableCell>
                    <TableCell>{item.releaseDelayDays}</TableCell>
                    <TableCell>{item.createUsername}</TableCell>
                    <TableCell>{formatTime(item.createTimeMs)}</TableCell>
                    <TableCell>
                      {item.disableTimeMs ? formatTime(item.disableTimeMs) : '-'}
                    </TableCell>
                    <TableCell>{item.disableReason || '-'}</TableCell>
                    <TableCell>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleUpdateClick(item)}
                      >
                        据此更新
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      }

      {/* 更新对话框 */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>基于所选配置更新全局参数</DialogTitle>
            <DialogDescription>
              修改以下参数后提交，当前激活配置将被禁用并创建新版本。
            </DialogDescription>
          </DialogHeader>

          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="taixueRatio" className="text-right">
                太学比例
              </Label>
              <Input
                id="taixueRatio"
                type="number"
                step="0.01"
                value={selectedConfig.taixueRatio ?? ''}
                onChange={(e) =>
                  setSelectedConfig({
                    ...selectedConfig,
                    taixueRatio: parseFloat(e.target.value),
                  })
                }
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="commercialRatio" className="text-right">
                商务处比例
              </Label>
              <Input
                id="commercialRatio"
                type="number"
                step="0.01"
                value={selectedConfig.commercialRatio ?? ''}
                onChange={(e) =>
                  setSelectedConfig({
                    ...selectedConfig,
                    commercialRatio: parseFloat(e.target.value),
                  })
                }
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="templateDefectParam" className="text-right">
                模板衰减参数
              </Label>
              <Input
                id="templateDefectParam"
                type="number"
                step="0.01"
                value={selectedConfig.templateDefectParam ?? ''}
                onChange={(e) =>
                  setSelectedConfig({
                    ...selectedConfig,
                    templateDefectParam: parseFloat(e.target.value),
                  })
                }
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="placerRatio" className="text-right">
                放置者比例
              </Label>
              <Input
                id="placerRatio"
                type="number"
                step="0.01"
                value={selectedConfig.placerRatio ?? ''}
                onChange={(e) =>
                  setSelectedConfig({
                    ...selectedConfig,
                    placerRatio: parseFloat(e.target.value),
                  })
                }
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="placerRatio" className="text-right">
                收益释放延迟天数
              </Label>
              <Input
                id="releaseDelayDays"
                type="number"
                step="0.01"
                value={selectedConfig.releaseDelayDays ?? ''}
                onChange={(e) =>
                  setSelectedConfig({
                    ...selectedConfig,
                    releaseDelayDays: parseInt(e.target.value),
                  })
                }
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="uploaderRatio" className="text-right">
                上传者比例
              </Label>
              <Input
                id="uploaderRatio"
                type="number"
                step="0.01"
                value={selectedConfig.uploaderRatio ?? ''}
                onChange={(e) =>
                  setSelectedConfig({
                    ...selectedConfig,
                    uploaderRatio: parseFloat(e.target.value),
                  })
                }
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="updateReason" className="text-right">
                更新原因
              </Label>
              <Input
                id="updateReason"
                value={updateReason}
                onChange={(e) => setUpdateReason(e.target.value)}
                className="col-span-3"
                placeholder="必填"
              />
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>
              取消
            </Button>
            <Button onClick={handleSubmitUpdate}>确定更新</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}