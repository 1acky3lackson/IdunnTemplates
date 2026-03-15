import { SystemWithdrawManagement } from "~/common/system-withdraw/SystemWithdrawManagement";

export default function MySystemWithdrawsPage() {
    return <div>
        <div className="flex justify-between items-center pb-4">
            <h1 className="text-2xl font-bold">我的提现记录</h1>
        </div>
        <SystemWithdrawManagement mode="me" />
    </div>
}