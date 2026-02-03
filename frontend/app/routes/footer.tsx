import { Heart } from "lucide-react";

export function Footer() {
    const currentYear = new Date().getFullYear();

    return (
        <footer className="w-full border-t bg-background/95 backdrop-blur supports-backdrop-filter:bg-background/60">
            <div className="container flex flex-col items-center justify-between gap-4 py-10 md:h-24 md:flex-row md:py-0 mx-auto max-w-6xl px-0 md:px-2">
                <div className="flex flex-col items-center gap-4 px-8 md:flex-row md:gap-2 md:px-0">
                    <p className="text-center text-sm leading-loose text-muted-foreground md:text-left">
                        Copyright © 2025-{currentYear}{" "}
                        <span className="font-medium underline underline-offset-4">
                            Jacky_Blackson
                        </span>
                        . All rights reserved.
                    </p>
                </div>

                <div className="flex items-center gap-1 text-sm text-muted-foreground">
                    <span>Made with</span>
                    <Heart className="h-4 w-4 fill-red-500 text-red-500 animate-pulse" />
                    <span>by</span>
                    <a
                        href="https://github.com/JackyBlackson" // 替换为你的主页
                        target="_blank"
                        rel="noreferrer"
                        className="font-medium transition-colors hover:text-primary underline underline-offset-4"
                    >
                        Jacky_Blackson
                    </a>
                    <span className="mx-1 text-muted-foreground/50">@</span>
                    <span className="font-semibold text-foreground">Taixue</span>
                </div>
            </div>
        </footer>
    );
}