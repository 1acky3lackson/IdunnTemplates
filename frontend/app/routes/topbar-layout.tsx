import { Outlet } from "react-router";
import TopBar from "~/common/topbar/topbar";

export default function RootLayout() {
    return (
        <>
            <TopBar />
            <main className="max-w-6xl w-full px-0 md:px-4 mx-auto my-4">
                <Outlet />
            </main>
        </>
    );
}