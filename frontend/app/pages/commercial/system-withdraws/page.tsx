import { AdminWithdrawConsole } from "~/common/system-withdraw/AdminWithdrawConsole";

export default function SystemWithdrawsAdminPage() {
  document.title = "处理流程管理";

  return (
    <div>
      <AdminWithdrawConsole />
    </div>
  );
}
