import { WithdrawManagement } from "~/common/netease-withdraw/WithdrawManagement";

export default function NeteaseWithdrawPage() {
    return <div>
        <div className="flex justify-between items-center pb-4">
            <h1 className="text-2xl font-bold">网易提现记录</h1>
        </div>
        <WithdrawManagement />
    </div>
}