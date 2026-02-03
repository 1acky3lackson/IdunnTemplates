import type { FC } from "react";
import { Languages, Check } from "lucide-react";
import { getLocaleName, getLocalizedUrl, getPathWithoutLocale } from "intlayer";
import { useIntlayer, useLocale } from "react-intlayer";
import { Link, useLocation } from "react-router";

import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";

export const LocaleSwitcher: FC = () => {
    const { localeSwitcherLabel } = useIntlayer("locale-switcher");
    const { pathname } = useLocation();
    const { availableLocales, locale } = useLocale();

    const pathWithoutLocale = getPathWithoutLocale(pathname);

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon">
                    <Languages className="h-5 w-5" />
                    <span className="sr-only">{localeSwitcherLabel?.value || "Switch Language"}</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-45">
                {availableLocales.map((localeItem) => {
                    const isActive = localeItem === locale;

                    return (
                        <DropdownMenuItem
                            key={localeItem}
                            asChild
                            onClick={() => {
                                localStorage.setItem("i18nextLng", localeItem);
                                // re render page
                                window.location.reload();
                            }}
                        >
                            <div
                                className="flex items-center justify-between w-full cursor-pointer"
                            >
                                <div className="flex flex-col">
                                    {/* 显示语言自身语言环境的名称，例如：简体中文 */}
                                    <span className="text-sm font-medium">
                                        {getLocaleName(localeItem, localeItem)}
                                    </span>
                                    {/* 显示英文名称作为辅助备注，例如：Chinese (Simplified) */}
                                    <span className="text-[10px] text-muted-foreground uppercase">
                                        {getLocaleName(localeItem, "en")}
                                    </span>
                                </div>
                                {isActive && <Check className="h-4 w-4 ml-2 opacity-70" />}
                            </div>
                        </DropdownMenuItem>
                    );
                })}
            </DropdownMenuContent>
        </DropdownMenu>
    );
};