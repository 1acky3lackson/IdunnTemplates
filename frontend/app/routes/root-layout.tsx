import { IntlayerProvider } from "react-intlayer";
import { Outlet } from "react-router";

import type { Route } from "./+types/root-layout";
import { Locales } from "intlayer";
import { ThemeProvider } from "~/components/theme/theme-provider";
import { AuthProvider } from "~/common/auth/auth-provider";

export default function RootLayout({ params }: Route.ComponentProps) {
    let { lang } = params;
    let localstorageLang = localStorage.getItem("i18nextLng");
    if (lang && localstorageLang !== lang) {
        localStorage.setItem("i18nextLng", lang);
    }
    if (!lang && localstorageLang) {
        lang = localstorageLang;
    }
    console.log(lang);

    if (lang === undefined) {
        lang = Locales.CHINESE;
        console.log("no lang found, set to default:", lang);
    }

    // 修改 html 的 lang 属性
    document.documentElement.setAttribute("i18n-lang", lang);


    return (
        <IntlayerProvider locale={lang ? lang : Locales.CHINESE}>
            <ThemeProvider defaultTheme="dark" storageKey="vite-ui-theme">
                <AuthProvider>
                    <Outlet />
                </AuthProvider>
            </ThemeProvider>
        </IntlayerProvider>
    );
}