import { Outlet } from "react-router";
import TopBar from "~/common/topbar/topbar";

export default function RootLayout() {
    return (
        <>
            <TopBar />
            <Outlet />
        </>
    );
}