import { SystemWithdrawManagement } from "~/common/system-withdraw/SystemWithdrawManagement";

export default function SystemWithdrawsAdminPage() {
    return <div>
        <div className="flex justify-between items-center pb-4">
            <h1 className="text-2xl font-bold">系统提现管理</h1>
        </div>
        <SystemWithdrawManagement mode="all" />
    </div>
}