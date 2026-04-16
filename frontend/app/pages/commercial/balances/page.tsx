import { UserBalanceList } from "~/common/balance/BalanceTables";

export default function BalanceListPage() {
  document.title = "用户虚拟点数";

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">用户虚拟点数</h1>
      <UserBalanceList />
    </div>
  );
}
