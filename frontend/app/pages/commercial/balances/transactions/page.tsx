import { UserTransactionRecordList } from "~/common/balance/BalanceTables";

export default function BalanceTransactionListPage() {
  document.title = "虚拟点数变动记录";

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">虚拟点数变动记录</h1>
      <UserTransactionRecordList />
    </div>
  );
}
