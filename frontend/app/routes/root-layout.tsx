import { IntlayerProvider } from "react-intlayer";
import { Outlet } from "react-router";

import type { Route } from "./+types/root-layout";
import { Locales } from "intlayer";
import { ThemeProvider } from "~/components/theme/theme-provider";

export default function RootLayout({ params }: Route.ComponentProps) {
    let { lang } = params;
    const localstorageLang = localStorage.getItem("i18nextLng");
    if (lang && localstorageLang !== lang) {
        localStorage.setItem("i18nextLng", lang);
    }
    if (!lang && localstorageLang) {
        lang = localstorageLang;
    }
    console.log(lang);

    return (
        <IntlayerProvider locale={lang ? lang : Locales.CHINESE}>
            <ThemeProvider defaultTheme="dark" storageKey="vite-ui-theme">
                <Outlet />
            </ThemeProvider>
        </IntlayerProvider>
    );
}