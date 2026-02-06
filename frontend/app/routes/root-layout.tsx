import { IntlayerProvider } from "react-intlayer";
import { Outlet } from "react-router";
import type { Route } from "./+types/root-layout";
import { Locales } from "intlayer";
import { ThemeProvider } from "~/components/theme/theme-provider";
import { AuthProvider } from "~/common/auth/auth-provider";
import { Footer } from "./footer"; // 引入下方创建的组件
import { useEffect } from "react";
import { Toaster } from "sonner";

export default function RootLayout({ params }: Route.ComponentProps) {
    let { lang } = params;

    const localstorageLang = localStorage.getItem("i18nextLng");

    useEffect(() => {
        const localstorageLang = localStorage.getItem("i18nextLng");
        const currentLang = lang || localstorageLang || Locales.CHINESE;
        
        if (lang) {
            localStorage.setItem("i18nextLng", lang);
        }
        document.documentElement.setAttribute("i18n-lang", currentLang as string);
        document.documentElement.setAttribute("lang", currentLang as string);
    }, [lang]);

    const currentLocale = localstorageLang || lang || Locales.CHINESE;

    return (
        <IntlayerProvider locale={currentLocale}>
            <ThemeProvider defaultTheme="dark" storageKey="vite-ui-theme">
                <AuthProvider>
                    {/* 使用 Flex 布局确保 Footer 始终在页面底部 */}
                    <div className="relative flex min-h-screen flex-col">
                        <main className="flex-1">
                            <Outlet />
                            <Toaster />
                        </main>
                    </div>
                </AuthProvider>
            </ThemeProvider>
        </IntlayerProvider>
    );
}