import { Outlet } from "react-router";
import TopBar from "~/common/topbar/topbar";
import { Footer } from "./footer";
import { AuthProvider } from "~/common/auth/auth-provider";

export default function RootLayout() {
    return (
        <AuthProvider>
            <TopBar />
            <main className="max-w-7xl w-full px-0 md:px-4 mx-auto my-4">
                <Outlet />
                <Footer />
            </main>
        </AuthProvider>
    );
}