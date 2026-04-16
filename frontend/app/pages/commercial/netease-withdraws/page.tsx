import { WithdrawManagement } from "~/common/netease-withdraw/WithdrawManagement";

export default function NeteaseWithdrawPage() {
  document.title = "处理记录";

  return (
    <div className="p-6">
      <div className="flex justify-between items-center pb-4">
        <h1 className="text-2xl font-bold">处理记录</h1>
      </div>
      <WithdrawManagement />
    </div>
  );
}
