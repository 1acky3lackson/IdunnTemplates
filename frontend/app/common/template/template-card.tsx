import {
    ChevronLeft,
    ChevronRight,
    Box,
    Lock,
    Unlock,
    Clock,
    Ruler,
    ImageIcon,
    HelpCircle
} from 'lucide-react';
import { useTheme } from "~/components/theme/theme-provider";
import { useEffect, useMemo, useState } from "react";
import { getThumbnailUrlForTemplate } from "~/api";
import type { Template } from "~/api/generated/model/template";
import { cn } from "~/lib/utils";
import { SmartTemplatePreview } from './smart-template-preview';
import { Link } from 'react-router';

interface TemplateCardProps {
    template: Template;
    className?: string;
}

export const EXAMPLE_TEMPLATE_1: Template = JSON.parse(`
    {
        "id": "be44b74f-a9f3-4a24-bde8-7d8b3f692c3e",
        "path": "minecraft/village/plains/houses/butcher_shop_1",
        "name": "butcher_shop_1",
        "metadata": {
            "templateId": "be44b74f-a9f3-4a24-bde8-7d8b3f692c3e",
            "creatorId": "500d59d1-84a8-3207-932c-e62f8a543ef7",
            "creationTime": 1769973879770,
            "worldId": "c95b4000-db96-48dc-b754-40e910e89f29",
            "anchorX": -1622,
            "anchorY": 85,
            "anchorZ": -47,
            "width": 19,
            "height": 15,
            "length": 20,
            "locked": false,
            "lockedTimestamp": null,
            "childTemplateInstances": {},
            "parentTemplateInstances": {},
            "deleted": false,
            "stagedChanges": {
                "addedInstances": [],
                "removedInstanceIds": [],
                "empty": true
            },
            "versions": []
        },
        "usePermissionNode": "idunn.template.use.minecraft.village.plains.houses.butcher_shop_1",
        "latestVersion": null,
        "locked": false,
        "colorSchemes": [
            "b89058",
            "886840",
            "584828",
            "606060",
            "382818",
            "888088"
        ]
    }
`);

export const EXAMPLE_TEMPLATE_2: Template = JSON.parse(`
    {
        "id": "e9f424e4-c0ab-49f6-b341-b600faa38ffa",
        "path": "minecraft/village/plains/houses/cartographer_1",
        "name": "cartographer_1",
        "metadata": {
            "templateId": "e9f424e4-c0ab-49f6-b341-b600faa38ffa",
            "creatorId": "500d59d1-84a8-3207-932c-e62f8a543ef7",
            "creationTime": 1769973956829,
            "worldId": "c95b4000-db96-48dc-b754-40e910e89f29",
            "anchorX": -1625,
            "anchorY": 85,
            "anchorZ": -89,
            "width": 19,
            "height": 16,
            "length": 16,
            "locked": false,
            "lockedTimestamp": null,
            "childTemplateInstances": {},
            "parentTemplateInstances": {},
            "deleted": false,
            "stagedChanges": {
                "addedInstances": [],
                "removedInstanceIds": [],
                "empty": true
            },
            "versions": []
        },
        "usePermissionNode": "idunn.template.use.minecraft.village.plains.houses.cartographer_1",
        "latestVersion": null,
        "locked": false,
        "colorSchemes": [
            "886840",
            "b89058",
            "483820",
            "606060",
            "684838",
            "506830"
        ]
    }
`);

export const EXAMPLE_TEMPLATE_3: Template = JSON.parse(`
    {
        "id": "d97ecd43-3a99-4820-b202-fab4e3f5bb1d",
        "path": "users/Jacky_Blackson/lantern",
        "name": "lantern",
        "metadata": {
            "templateId": "d97ecd43-3a99-4820-b202-fab4e3f5bb1d",
            "creatorId": "500d59d1-84a8-3207-932c-e62f8a543ef7",
            "creationTime": 1769856943040,
            "worldId": "c95b4000-db96-48dc-b754-40e910e89f29",
            "anchorX": 1249,
            "anchorY": 109,
            "anchorZ": -2225,
            "width": 3,
            "height": 4,
            "length": 3,
            "locked": false,
            "lockedTimestamp": null,
            "childTemplateInstances": {},
            "parentTemplateInstances": {},
            "deleted": false,
            "stagedChanges": {
                "addedInstances": [],
                "removedInstanceIds": [],
                "empty": true
            },
            "versions": []
        },
        "usePermissionNode": "idunn.template.use.users.Jacky_Blackson.lantern",
        "latestVersion": null,
        "locked": false,
        "colorSchemes": [
            "283038",
            "909090",
            "e8e8e8",
            "a8a8a8",
            "d0d0d0",
            "302010"
        ]
    }
`);

// 调高透明度至 '33' (约 20%)，缩小半径至 40%-60% 使颜色更凝聚
const gradientAlphaDark = "80";
const gradientAlphaLight = "25";
const radius = 60;

// 2. 简单的 Hash 函数，根据 ID 生成种子
const getSeed = (str: string) => {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return hash;
};

// 3. 基于种子的伪随机数生成器 (Linear Congruential Generator)
const seededRandom = (seed: number) => {
    const x = Math.sin(seed) * 10000;
    return x - Math.floor(x);
};

/**
 * 基于种子生成随机颜色，直接包含 alpha 通道
 * @param seed 随机种子
 * @param alpha 透明度值 (0-100)
 */
const generateRandomColor = (seed: number, alpha: number) => {
    const hue = Math.floor(seededRandom(seed) * 360);
    const saturation  = Math.floor(seededRandom(seed) * 60) + 20; // 饱和度在40%-60%之间
    const lightness = Math.floor(seededRandom(seed) * 80) + 10; // 亮度在30%-70%之间

    // 使用新的 hsl 语法：hsla(hue, saturation, lightness, alpha)
    // alpha 需要转换为 0-1 之间的小数
    return `hsla(${hue}, ${saturation}%, ${lightness}%, ${alpha / 100})`;
};

/**
 * 生成基于 ID 稳定的随机 Mesh 渐变
 * @param colors 颜色数组（建议传入 6 个颜色）
 * @param id 用于生成稳定随机位置的唯一标识
 * @param theme 当前主题（'light' 或 'dark'）
 * @returns CSS 渐变字符串
 */
const generateMeshGradient = (colors: string[], id: string, theme: string = 'light') => {
    if (!colors || colors.length === 0) return undefined;

    // 根据主题确定当前的 alpha 值
    const alpha = (theme && theme.includes('dark')) ? gradientAlphaDark : gradientAlphaLight;

    const pool = [...colors];
    while (pool.length < 6) {
        pool.push(...colors);
    }
    
    let seed = getSeed(id);

    const gradients = pool.slice(0, 6).map((color) => {
        let finalColorWithAlpha: string;

        if (color.toLowerCase() === "#unknown") {
            // 随机生成的颜色直接携带 alpha
            finalColorWithAlpha = generateRandomColor(seed++, Number.parseInt(alpha));
        } else {
            // 处理传入的预设颜色：如果是 hex 格式，直接拼接
            // 如果你传入的是其他格式，建议在此处统一转换为包含 alpha 的字符串
            finalColorWithAlpha = `${color}${alpha}`; 
        }

        // 生成坐标
        const posX = Math.floor(seededRandom(seed++) * 100);
        const posY = Math.floor(seededRandom(seed++) * 100);

        // 返回径向渐变，无需再在外部拼接 alpha
        return `radial-gradient(at ${posX}% ${posY}%, ${finalColorWithAlpha} 0%, transparent ${radius}%)`;
    });

    return gradients.join(', ');
};

export const TemplateCard: React.FC<TemplateCardProps> = ({ template, className }) => {
    template.colorSchemes = template.colorSchemes?.map(color => color.startsWith("#") ? color : "#" + color);
    // console.log("Rendering TemplateCard for template:", template);

    // 状态管理
    const [angle, setAngle] = useState<0 | 1 | 2 | 3>(0);
    const [isHovering, setIsHovering] = useState(false);
    const { setTheme, theme } = useTheme();

    // --- 新增：图片错误状态 ---
    const [imgError, setImgError] = useState(false);

    // --- 新增：当角度或模板ID改变时，重置错误状态，尝试加载新图片 ---
    useEffect(() => {
        setImgError(false);
    }, [angle, template.id]);

    // 1. 处理主题色
    // 取第一个颜色作为主色，如果没有则回退到灰色
    // 我们需要确保颜色格式是 hex 或 rgb 才能用于 css 变量，这里假设后端返回的是 hex
    const primaryColor = template.colorSchemes?.[0] || '#71717a';

    // 角度切换逻辑
    const nextAngle = (e: React.MouseEvent) => {
        e.stopPropagation();
        setAngle((prev) => (prev + 1) % 4 as 0 | 1 | 2 | 3);
    };

    const prevAngle = (e: React.MouseEvent) => {
        e.stopPropagation();
        setAngle((prev) => (prev - 1 + 4) % 4 as 0 | 1 | 2 | 3);
    };

    const formattedDate = useMemo(() => {
        return new Date(template.metadata.creationTime).toLocaleDateString();
    }, [template.metadata.creationTime]);

    // --- 核心变化：计算 Mesh Gradient ---
    const backgroundStyle = useMemo(() => {
        const gradient = generateMeshGradient(template.colorSchemes, template.id + theme + ((angle+1)*147), theme);
        return gradient ? { backgroundImage: gradient } : {};
    }, [template.colorSchemes, template.id, theme, angle]);

    return (
        <div
            className={cn(
                "group relative flex flex-col overflow-hidden rounded-xl border transition-all duration-300 hover:shadow-lg",
                // 基础背景色：必须设置 bg-card (白色/深灰色)，否则透明渐变会透到底部的页面背景
                // "bg-card text-card-foreground",
                "hover:-translate-y-1",
                className
            )}
            style={backgroundStyle} // 应用生成的渐变
        >
            {/* --- Top: Thumbnail Area --- */}
            <div
                className="relative aspect-video w-full overflow-hidden bg-muted/20" // 改淡了一点默认背景
                onMouseEnter={() => setIsHovering(true)}
                onMouseLeave={() => setIsHovering(false)}
            >
                {/* 条件渲染：如果出错显示可爱图标，否则显示图片 */}
                
                <SmartTemplatePreview
                    template={template}
                    angle={angle}
                    getThumbnailUrl={getThumbnailUrlForTemplate}
                />

                {/* 悬停时的遮罩：为了让白色箭头更清晰，可以加一个非常淡的暗色渐变 */}
                <div className={cn(
                    "absolute inset-0 bg-linear-to-t from-black/20 via-transparent to-transparent transition-opacity duration-300",
                    isHovering ? "opacity-100" : "opacity-0"
                )} />

                {/* 左右切换按钮 */}
                <div className={cn(
                    "absolute inset-0 flex items-center justify-between transition-opacity duration-200",
                    isHovering ? "opacity-100" : "opacity-0"
                )}>
                    {/* 使用 backdrop-blur 增加毛玻璃感，显得更高级 */}
                    <button
                        onClick={prevAngle}
                        className="rounded-full bg-black/20 p-1.5 text-white backdrop-blur-md hover:bg-black/40 transition-colors"
                    >
                        <ChevronLeft className="h-4 w-4" />
                    </button>
                    <button
                        onClick={nextAngle}
                        className="rounded-full bg-black/20 p-1.5 text-white backdrop-blur-md hover:bg-black/40 transition-colors"
                    >
                        <ChevronRight className="h-4 w-4" />
                    </button>
                </div>

                {/* 角度指示点 */}
                <div className="absolute bottom-2 left-1/2 flex -translate-x-1/2 gap-1.5 p-1 rounded-full bg-black/10 backdrop-blur-[2px]">
                    {[0, 1, 2, 3].map((i) => (
                        <div
                            key={i}
                            className={cn(
                                "h-1.5 w-1.5 rounded-full transition-all duration-300",
                                i === angle ? "bg-white scale-110 shadow-[0_0_4px_rgba(255,255,255,0.8)]" : "bg-white/40 hover:bg-white/60"
                            )}
                        />
                    ))}
                </div>
            </div>

            {/* --- Middle: Logo Area (Color Bubbles) --- */}
            <div className="relative z-20 top-5 md:top-5 left-3">
                {/* 使用 -mt-5 让圆圈组向上浮动，一半压在图片上，一半在内容区 */}
                <div className="flex items-center h-0 z-10 -mt-5">
                    {template.colorSchemes && template.colorSchemes.length > 0 ? (
                        <div className="flex items-center">
                            {template.colorSchemes.slice(0, 6).filter(c => c !== null && !c.endsWith("unknown")).map((color, idx) => {
                                const isUnknown = color.endsWith("unknown");

                                return (
                                    <div
                                        key={idx}
                                        className={`
                                            relative h-6 w-6 md:h-8 md:w-8 rounded-full border-[3px] border-card shadow-sm 
                                            transition-all duration-300 ease-out 
                                            hover:scale-125 hover:z-50 hover:border-background
                                            flex items-center justify-center overflow-hidden
                                            ${isUnknown ? "bg-linear-to-tr from-slate-200 via-gray-300 to-slate-400" : ""}
                                        `}
                                        style={{
                                            backgroundColor: isUnknown ? undefined : color,
                                            marginLeft: idx === 0 ? 0 : "-14px",
                                            zIndex: 10 - idx,
                                        }}
                                    >
                                        {isUnknown && (
                                            <HelpCircle className="w-1/2 h-1/2 text-muted-foreground/80" strokeWidth={2.5} />
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    ) : (
                        // Fallback: 如果没有颜色，显示一个默认的圆圈图标
                        <div className="flex h-6 w-6 md:h-8 md:w-8 items-center justify-center rounded-full border-[3px] border-card bg-muted shadow-sm">
                            <Box className="h-5 w-5 text-muted-foreground/50" />
                        </div>
                    )}
                </div>
            </div>

            {/* --- Bottom: Info Body --- */}
            <div className="flex flex-1 flex-col p-4 pt-2 z-10 dark:bg-primary-foreground/85 bg-primary-foreground/65">
                <Link to={`/templates/${template.id}`}>
                    <div className="mb-3 mt-4">
                        <h3 className="line-clamp-1 text-base font-bold tracking-tight text-foreground/90 group-hover:text-primary transition-colors">
                            {template.name}
                        </h3>
                        <p className="mt-1 line-clamp-1 text-[10px] font-mono text-muted-foreground/70 break-all" title={template.path}>
                            {template.path}
                        </p>
                    </div>
                </Link>

                {/* Metadata Tags */}
                <div className="mt-auto grid grid-cols-2 gap-2 text-xs">
                    {/* 状态 Badge */}
                    <div className={cn(
                        "inline-flex items-center gap-1.5 px-2 py-1 rounded-md w-fit border",
                        template.locked
                            ? "bg-amber-500/10 border-amber-500/20 text-amber-600 dark:text-amber-500"
                            : "bg-emerald-500/10 border-emerald-500/20 text-emerald-600 dark:text-emerald-500"
                    )}>
                        {template.locked ? <Lock className="h-3 w-3" /> : <Unlock className="h-3 w-3" />}
                        <span className="font-medium text-[10px] uppercase">{template.locked ? 'Locked' : 'Open'}</span>
                    </div>

                    {/* 尺寸 Badge */}
                    <div className="inline-flex items-center gap-1.5 px-2 py-1 rounded-md w-fit border bg-muted/50 border-border/50 text-muted-foreground ml-auto">
                        <Ruler className="h-3 w-3" />
                        <span className="font-mono text-[10px]">
                            {template.metadata.width}×{template.metadata.height}×{template.metadata.length}
                        </span>
                    </div>
                </div>

                {/* Footer Info */}
                <div className="mt-3 flex items-center justify-between border-t border-border/40 pt-2 text-[10px] text-muted-foreground/60">
                    <div className="flex items-center gap-1">
                        <span className="bg-primary/10 text-primary px-1.5 py-0.5 rounded text-[9px] font-bold">
                            V{template.latestVersion || '1.0'}
                        </span>
                    </div>
                    <div className="flex items-center gap-1">
                        <Clock className="h-2.5 w-2.5" />
                        <span>{formattedDate}</span>
                    </div>
                </div>
            </div>

            {/* 噪点纹理层 (Noise Overlay) - 可选 */}
            {/* 这会让渐变看起来更有质感，不会有色阶断层 */}
            <div className="absolute inset-0 opacity-[0.03] pointer-events-none z-0"
                style={{ backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E")` }}
            />
        </div>
    );
};