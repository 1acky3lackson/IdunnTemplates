import { TrustedServerList } from "~/common/trusted-server";

export default function TrustedServersPage() {
    document.title = "服务端凭证管理";
    return (
        <TrustedServerList />
    );
}
