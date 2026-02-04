"use client";

import React, { useEffect, useState } from "react";
import { cn } from "@/lib/utils";
// 👇在此处引入 CSS，不需要把它加到 tailwind.config.ts 里了
import "./falling-icons.css";

interface FallingIconsBackgroundProps {
    children: React.ReactNode;
    icons: React.ElementType[];
    iconCount?: number;
    className?: string; // 允许传入自定义类名到外层容器
}

interface IconStyle {
    id: number;
    icon: React.ElementType;
    left: string;
    delay: string;
    duration: string;
    size: number;
    colorOpacity: number;
}

export function FallingIconsBackground({
    children,
    icons,
    iconCount = 15,
    className,
}: FallingIconsBackgroundProps) {
    const [fallingIcons, setFallingIcons] = useState<IconStyle[]>([]);

    useEffect(() => {
        const newIcons = Array.from({ length: iconCount }).map((_, i) => {
            const RandomIcon = icons[Math.floor(Math.random() * icons.length)];
            return {
                id: i,
                icon: RandomIcon,
                left: `${Math.random() * 100}%`, // 0-100% 屏幕宽度
                delay: `-${Math.random() * 20}s`,
                duration: `${15 + Math.random() * 15}s`,
                size: 20 + Math.random() * 30,
                colorOpacity: 0.1 + Math.random() * 0.2,
            };
        });
        setFallingIcons(newIcons);
    }, [icons, iconCount]);

    return (
        <div className={cn("relative w-full h-full", className)}>
            {/* 背景层：使用 fixed 定位直接画在屏幕上 
                -z-10 确保在所有内容后面
                bg-background 确保有底色
            */}
            
            <div className="fixed inset-0 overflow-hidden pointer-events-none bg-background  z-10">
                {fallingIcons.map((item) => (
                    <div
                        key={item.id}
                        // 使用 fixed 使得动画相对于视口飘落，而不是相对于父容器
                        className="absolute top-[-10%] animate-falling text-primary z-114514"
                        style={{
                            left: item.left,
                            animationDelay: item.delay,
                            animationDuration: item.duration,
                            opacity: item.colorOpacity,
                        }}
                    >
                        <item.icon
                            style={{
                                width: item.size,
                                height: item.size,
                            }}
                        />
                    </div>
                ))}
            </div>
            {/* 内容层：简单的 children 渲染，不加额外布局限制 */}
            <div className="relative">
                {children}
            </div>


        </div>
    );
}