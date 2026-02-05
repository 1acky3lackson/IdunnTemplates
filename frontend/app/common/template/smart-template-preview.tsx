import React, { useState, useEffect, useRef, useCallback } from 'react';
import { ImageIcon, Loader2, Box } from 'lucide-react';
import type { Template } from '~/api/generated/model';
import { getSchemLinkForTemplate } from '~/api/overrides/template-file-download-api';
import apiClient from '~/lib/axios';

interface SmartTemplatePreviewProps {
    template: Template;
    angle?: 0 | 1 | 2 | 3; // 用于获取缩略图的角度，默认 0 或 45
    className?: string;
    onDisplayFail?: () => void; // 最终失败回调
    
    // 假设这些辅助函数是从外部传入或导入的，这里作为 Props 为了演示
    getThumbnailUrl: (id: string, angle: 0 | 1 | 2 | 3) => string;
    // getSchemLink: (template: Template) => string;
}

type ViewState = 'thumbnail' | 'rendering' | 'rendered' | 'error';

export const SmartTemplatePreview: React.FC<SmartTemplatePreviewProps> = ({
    template,
    angle = 0,
    className = "",
    onDisplayFail,
    getThumbnailUrl,
    // getSchemLink
}) => {
    const [viewState, setViewState] = useState<ViewState>('thumbnail');
    const [renderSrc, setRenderSrc] = useState<string | null>(null);
    const renderAttemptedRef = useRef(false);
    const renderResultCache = useRef<Record<string, string>>({})

    // 当 template ID 变化时，重置状态
    useEffect(() => {
        setViewState('thumbnail');
        setRenderSrc(null);
        renderAttemptedRef.current = false;
    }, [template.id]);

    // 获取主色调（用于装饰 Loading 界面）
    const primaryColor = template.colorSchemes?.[0] 
        ? `#${template.colorSchemes[0]}` 
        : 'currentColor';

    const render = useCallback(async() => {
        if (renderResultCache.current[`${angle}`] !== undefined && renderResultCache.current[`${angle}`] !== null && renderResultCache.current[`${angle}`] !== "") {
            setRenderSrc(renderResultCache.current[`${angle}`] as string);
            setViewState('rendered');
            return renderResultCache.current[`${angle}`];
        }
        try {
            const schemUrl = getSchemLinkForTemplate(template);
            if (!schemUrl) throw new Error("No schem link available");
            
            // 动态导入，拆分模块
            const { renderSchemFile } = await import('~/lib/schematic-renderer');

            // 计算合适的渲染尺寸 (稍微大一点以保证清晰度)
            const renderWidth = 400; 
            const renderHeight = 400;

            // 调用之前的渲染函数
            const base64Image = await renderSchemFile(schemUrl, {
                width: renderWidth,
                height: renderHeight,
                alpha: Math.PI / 4 + Math.PI * 2 / 4 * angle, // 45度角
                beta: Math.atan(Math.sqrt(0.5)), // 标准等轴测视角
                radius: 0.6, // 稍微拉远一点防止切边
                backgroundColor: 'transparent' // 尝试透明背景
            });

            setRenderSrc(base64Image);
            renderResultCache.current[`${angle}`] = base64Image;

            setViewState('rendered');

            // 尝试上传缩略图 (利用 404 时设置的 cookie)
            apiClient.post('/api/v1/templates/thumbnail', base64Image, {
                headers: { 'Content-Type': 'text/plain' }
            }).catch(e => {
                // 默默失败，不影响用户体验
                console.warn("Auto-upload thumbnail failed (likely expected if no auth cookie):", e);
            });

        } catch (error) {
            console.error("Client-side rendering failed:", error);
            setViewState('error');
            if (onDisplayFail) onDisplayFail();
        }
    }, [template.id, angle])

    // 核心逻辑：触发前端渲染
    const handleTriggerRender = useCallback(async () => {
        // 防止重复触发
        if (renderAttemptedRef.current) return;
        renderAttemptedRef.current = true;

        setViewState('rendering');
    }, [template, onDisplayFail]);

    // 根据状态触发重渲染
    useEffect(() => {
        setViewState('thumbnail');
        renderResultCache.current = {};
    }, [template.id]);

    useEffect(() => {
        setViewState('thumbnail');
    }, [angle]);

    // === 子组件：渲染中状态 (美化版) ===
    const LoadingView = () => (
        <div className="flex h-full w-full flex-col items-center justify-center gap-3 bg-muted/20 animate-in fade-in duration-300">
            <div className="relative">
                {/* 旋转的加载圈 */}
                <div className="absolute inset-0 flex items-center justify-center">
                    <Loader2 className="h-8 w-8 animate-spin text-muted-foreground/40" />
                </div>
                {/* 中心的 3D 图标 */}
                <div className="relative flex h-8 w-8 items-center justify-center rounded-md bg-background/50 backdrop-blur-sm shadow-sm">
                    <Box className="h-4 w-4 text-muted-foreground" style={{ color: primaryColor }} />
                </div>
            </div>
            <div className="flex flex-col items-center gap-1">
                <span className="text-xs font-medium text-muted-foreground">
                    Generating 3D Preview...
                </span>
                <span className="text-[10px] text-muted-foreground/50">
                    Client-side rendering
                </span>
            </div>
        </div>
    );

    // === 子组件：错误状态 (你提供的设计) ===
    const ErrorView = () => (
        <div className="flex h-full w-full flex-col items-center justify-center gap-2 bg-muted/40 transition-colors group-hover:bg-muted/60">
            <div className="relative rounded-full bg-background/60 p-3 shadow-sm backdrop-blur-sm">
                <ImageIcon
                    className="h-6 w-6 text-muted-foreground/70"
                    strokeWidth={1.5}
                />
                <div
                    className="absolute bottom-2 right-2 h-1.5 w-1.5 rounded-full"
                    style={{ backgroundColor: primaryColor }}
                />
            </div>
            <span className="text-[10px] font-medium text-muted-foreground/60 tracking-wider">
                No Preview
            </span>
        </div>
    );

    // === 主渲染逻辑 ===
    return (
        <div className={`relative h-full w-full overflow-hidden rounded-md ${className}`}>
            
            {/* 1. 静态缩略图模式 */}
            {viewState === 'thumbnail' && (
                <img
                    src={getThumbnailUrl(template.id, angle)}
                    alt={template.name}
                    className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-105"
                    loading="lazy"
                    onError={() => {
                        // 图片加载失败，切换到渲染模式
                        handleTriggerRender();
                        render();
                    }}
                />
            )}

            {/* 2. 渲染中模式 */}
            {viewState === 'rendering' && <LoadingView />}

            {/* 3. 渲染成功模式 */}
            {viewState === 'rendered' && renderSrc && (
                <img
                    src={renderSrc}
                    alt={`${template.name} (Rendered)`}
                    className="h-full w-full object-contain p-2 animate-in fade-in zoom-in-95 duration-500"
                    // 如果生成的图片也坏了（极少见），回退到错误状态
                    onError={() => {
                        setViewState('error');
                        if (onDisplayFail) onDisplayFail();
                    }}
                />
            )}

            {/* 4. 失败模式 */}
            {viewState === 'error' && <ErrorView />}
        </div>
    );
};