import { MonitorCog, Moon, Sun } from "lucide-react"

import { Button } from "@/components/ui/button"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { useTheme } from "./theme-provider"
import { useIntlayer } from "react-intlayer"

export function ModeToggle() {
    const { setTheme, theme } = useTheme()
    const { toggle } = useIntlayer("theme");


    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon">
                    <Sun className="h-[1.2rem] w-[1.2rem] scale-100 rotate-0 transition-all dark:scale-0 dark:-rotate-90" />
                    <Moon className="absolute h-[1.2rem] w-[1.2rem] scale-0 rotate-90 transition-all dark:scale-100 dark:rotate-0" />
                    <span className="sr-only">Toggle theme</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => setTheme("light")} className={theme === "light" ? "font-medium bg-accent text-accent-foreground" : ""}>
                    <Sun className="mr-2 h-4 w-4" /> {toggle.lightMode}
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setTheme("dark")} className={theme === "dark" ? "font-medium bg-accent text-accent-foreground" : ""}>
                    <Moon className="mr-2 h-4 w-4" /> {toggle.darkMode}
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setTheme("system")} className={theme === "system" ? "font-medium bg-accent text-accent-foreground" : ""}>
                    <MonitorCog className="mr-2 h-4 w-4" />{toggle.systemMode}
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    )
}