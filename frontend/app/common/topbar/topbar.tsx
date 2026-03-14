import * as React from "react"
import { LogIn, Menu, User, X } from "lucide-react"

import { Button } from "@/components/ui/button"
import { ModeToggle } from "@/components/theme/mode-toggle"
import { NavigationBar } from "./nav/navigation-bar"
import logo from "@/common/topbar/logo.svg"
import darkLogo from "@/common/topbar/logo-dark.svg"
import lightLogo from "@/common/topbar/logo-light.svg"
import { useTheme } from "@/components/theme/theme-provider"
import { LocaleSwitcher } from "../i18n/locale-switcher"
import { useAuth } from "../auth/auth-provider"
import { is } from "zod/v4/locales"
import { Link, useNavigate } from "react-router"
import { useIntlayer } from "react-intlayer"
import { HoverCard, HoverCardContent, HoverCardTrigger } from "~/components/ui/hover-card"

export default function TopBar() {
    const { siteTitle, login, logout: logoutBtn, meBtn } = useIntlayer("topbar");
    const [isMobileMenuOpen, setIsMobileMenuOpen] = React.useState(false)
    const { theme } = useTheme()
    const nav = useNavigate();

    const { isAuthenticated, user, login: loginFunc, logout } = useAuth();
    const toggleMobileMenu = () => {
        setIsMobileMenuOpen(!isMobileMenuOpen)
    }

    // Simple Logo Component that switches based on theme or shows a fallback
    const Logo = () => (
        <div className="flex items-center gap-1 font-bold text-xl">
            {/* Assuming the svgs are importable as strings or components. 
                If they are URLs, we use img tags. 
                Tailwind dark mode strategy usually requires 'dark:' class.
                Here we explicitly check theme if needed or use CSS hiding. */}
            <img
                src={lightLogo}
                alt="Logo"
                className="h-12 w-auto dark:hidden"
            />
            <img
                src={darkLogo}
                alt="Logo"
                className="h-12 w-auto hidden dark:block"
            />
            <span className="hidden md:inline-block">{siteTitle}</span>
        </div>
    )

    return (
        <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-backdrop-filter:bg-background/60">
            <div className="mx-auto flex h-16 max-w-6xl items-center justify-between md:px-4 px-0">
                {/* Left: Logo */}
                <div className="flex items-center">
                    <Link to="/" className="flex items-center gap-2">
                        <Logo />
                    </Link>
                </div>

                {/* Center: Navigation (Desktop) */}
                <div className="hidden md:flex flex-1 justify-center">
                    <NavigationBar />
                </div>

                {/* Right: Actions */}
                <div className="flex items-center gap-4">

                    {/* 1. 语言切换器 */}
                    <LocaleSwitcher />

                    {/* 2. 主题切换器 */}
                    <ModeToggle />

                    <div className="h-6 w-px bg-border mx-1 hidden sm:block" /> {/* 视觉分割线 */}

                    {/* 3. 用户按钮 */}
                    {
                        isAuthenticated
                            ?
                            <HoverCard openDelay={10} closeDelay={100}>
                                <HoverCardTrigger asChild>
                                    <div className="flex flex-row gap-2 items-center hover:bg-accent hover:text-accent-foreground transition-colors px-3 py-1 rounded-md hover:cursor-pointer">
                                        <div className="text-sm hidden lg:block">{user?.username}</div>
                                        <Button variant="outline" size="icon">
                                            <User className="h-5 w-5" />
                                            <span className="sr-only">User profile</span>
                                        </Button>
                                    </div>
                                </HoverCardTrigger>
                                <HoverCardContent className="flex w-64 flex-col gap-0.5">
                                    <Button variant="default" onClick={() => nav("/me")}>{meBtn}</Button>
                                    <Button onClick={logout} variant="link">{logoutBtn}</Button>
                                </HoverCardContent>
                            </HoverCard>

                            :
                            <Link to="/login">
                                <Button variant="default">
                                    <span className="pr-0.5">{login}</span>
                                    <LogIn className="h-5 w-5" />
                                </Button>
                            </Link>
                    }

                    {/* Mobile Menu Trigger */}
                    <Button
                        variant="ghost"
                        size="icon"
                        className="md:hidden"
                        onClick={toggleMobileMenu}
                    >
                        {isMobileMenuOpen ? (
                            <X className="h-5 w-5" />
                        ) : (
                            <Menu className="h-5 w-5" />
                        )}
                        <span className="sr-only">Toggle menu</span>
                    </Button>
                </div>
            </div>

            {/* Mobile Menu (Collapsible) */}
            {isMobileMenuOpen && (
                <div className="border-b bg-background md:hidden">
                    <div className="container py-4 space-y-4 px-7">
                        {/* Re-using NavigationBar structure or creating a simpler vertical list for mobile */}
                        {/* Since NavigationBar uses NavigationMenu which might be tricky in mobile stack, 
                            we can wrap it or just show it. NavigationMenu usually works best on desktop.
                            For mobile, we might want a simple list. For now, let's try to render the NavigationBar 
                            but typically Shadcn NavigationMenu is horizontal. 
                            Let's create a simple vertical placeholder for mobile links to be safe. */}

                        <nav className="flex flex-col space-y-4">
                            <a href="/" className="text-sm font-medium transition-colors hover:text-primary">
                                Getting Started
                            </a>
                            <a href="/docs" className="text-sm font-medium transition-colors hover:text-primary">
                                Components
                            </a>
                            <a href="/docs" className="text-sm font-medium transition-colors hover:text-primary">
                                Documentation
                            </a>
                        </nav>
                    </div>
                </div>
            )}
        </header>
    )
}
