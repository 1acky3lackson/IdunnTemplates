import React, { useState, useEffect, useRef, useCallback } from 'react';
import { ImageIcon, Loader2, Box, Download, Ban, TriangleAlert } from 'lucide-react';
import type { Template } from '~/api/generated/model';
import { getSchemLinkForTemplate } from '~/api/overrides/template-file-download-api';
import apiClient from '~/lib/axios';
import { useIntlayer } from 'react-intlayer';

interface SmartTemplatePreviewProps {
    template: Template;
    angle?: 0 | 1 | 2 | 3; // 用于获取缩略图的角度，默认 0 或 45
    className?: string;
    onDisplayFail?: () => void; // 最终失败回调

    // 假设这些辅助函数是从外部传入或导入的，这里作为 Props 为了演示
    getThumbnailUrl: (id: string, angle: 0 | 1 | 2 | 3) => string;
    // getSchemLink: (template: Template) => string;
}

type ViewState = 'thumbnail' | 'rendering' | 'rendered' | 'error' | 'too_large';

const RENDER_REFUSE_THRESHOLD = 100 * 60 * 100;

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
    const [imageSrc, setImageSrc] = useState<string | null>(null);
    const renderAttemptedRef = useRef(false);
    const renderResultCache = useRef<Record<string, string>>({});
    const uploadTokenRef = useRef<string | null>(null);
    const { renderering, error, tooLarge } = useIntlayer('smart-template-preview');

    // 获取主色调（用于装饰 Loading 界面）
    const primaryColor = template.colorSchemes?.[0]
        ? `#${template.colorSchemes[0]}`
        : 'currentColor';

    const render = useCallback(async () => {
        if (renderResultCache.current[`${angle}`] !== undefined && renderResultCache.current[`${angle}`] !== null && renderResultCache.current[`${angle}`] !== "") {
            setRenderSrc(renderResultCache.current[`${angle}`] as string);
            setViewState('rendered');
            return renderResultCache.current[`${angle}`];
        }
        try {
            const templateSize = template.metadata.height * template.metadata.width * template.metadata.length;
            if (templateSize >= RENDER_REFUSE_THRESHOLD) {
                setViewState('too_large');
                return null;
            }

            const schemUrl = getSchemLinkForTemplate(template);
            if (!schemUrl) throw new Error("No schem link available");

            // 动态导入，拆分模块
            const { renderSchemFile } = await import('~/lib/schematic-renderer');

            // 计算合适的渲染尺寸 (稍微大一点以保证清晰度)
            const renderWidth = 600;
            const renderHeight = 400;

            // 调用之前的渲染函数
            const base64Image = await renderSchemFile(
                async () => {
                    const res = await apiClient.get(schemUrl, {
                        responseType: 'arraybuffer' // 确保 Axios 返回的是 ArrayBuffer
                    });
                    return res.data;
                },
                {
                    width: renderWidth,
                    height: renderHeight,
                    alpha: Math.PI / 4 + Math.PI * 2 / 4 * angle, // 45度角
                    beta: Math.atan(Math.sqrt(4)), // 标准等轴测视角
                    radius: 0.6, // 稍微拉远一点防止切边
                    backgroundColor: 'transparent' // 尝试透明背景
                }
            );

            setRenderSrc(base64Image);
            renderResultCache.current[`${angle}`] = base64Image;

            setViewState('rendered');

            // 尝试上传缩略图 (利用 token)
            const token = uploadTokenRef.current;
            if (token) {
                apiClient.post('/api/v1/templates/thumbnail', {
                    token: token,
                    image: base64Image
                }, {
                    headers: { 'Content-Type': 'application/json' }
                }).catch(e => {
                    // 默默失败，不影响用户体验
                    console.warn("Auto-upload thumbnail failed:", e);
                });
            }

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

    // 当 template ID 或 angle 变化时，重置并尝试加载图片
    useEffect(() => {
        setViewState('thumbnail');
        setRenderSrc(null);
        renderAttemptedRef.current = false;
        uploadTokenRef.current = null;
        setImageSrc(null);

        // 如果只是 angle 变化，不需要清空缓存，但这里简单起见，id 变化时清空缓存
        // 实际上之前的代码有重复 useEffect，这里合并逻辑
    }, [template.id, angle]);

    useEffect(() => {
        renderResultCache.current = {};
    }, [template.id]);

    useEffect(() => {
        let isMounted = true;
        const url = getThumbnailUrl(template.id, angle);

        apiClient.get(url, { responseType: 'blob' })
            .then(response => {
                if (!isMounted) return;
                const imageUrl = URL.createObjectURL(response.data);
                setImageSrc(imageUrl);
                setViewState('thumbnail');
            })
            .catch(async error => {
                if (!isMounted) return;
                if (error.response && error.response.status === 404) {
                    // 404, 解析 token 并触发渲染
                    try {
                        const text = await error.response.data.text();
                        const json = JSON.parse(text);
                        if (json.token) {
                            uploadTokenRef.current = json.token;
                        }
                    } catch (e) {
                        // ignore JSON parse error
                    }
                    handleTriggerRender();
                    render();
                } else {
                    setViewState('error');
                    if (onDisplayFail) onDisplayFail();
                }
            });

        return () => {
            isMounted = false;
            // imageSrc cleanup handled by state update but URL.revoke could be added if tracked
        };
    }, [template.id, angle, getThumbnailUrl, handleTriggerRender, render]);

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
                    {renderering.messageMain}
                </span>
                <span className="text-[10px] text-muted-foreground/50">
                    {renderering.messageSecondary}
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
                {error.messageMain}
            </span>
        </div>
    );

    /**
 * LimitExceededView - 纯展示组件
 * 用于 Schematic 文件过大或超出渲染能力时的占位
 */
    const LimitExceededView = ({
        primaryColor = "#f59e0b", // 默认警告橙
        fileName = "model.schematic"
    }) => {
        return (
            <div className="flex h-full w-full flex-col items-center justify-center gap-4 bg-muted/20 p-6 animate-in fade-in duration-500">
                <div className="relative">
                    {/* 背景装饰：静态的虚线圈，暗示“中断” */}
                    <div className="absolute -inset-3 rounded-full border border-dashed border-muted-foreground/30" />

                    {/* 图标叠层 */}
                    <div className="relative flex h-10 w-10 items-center justify-center rounded-lg bg-background shadow-md">
                        <Box className="h-5 w-5 text-muted-foreground/40" />
                        {/* 右下角叠加小警告图标 */}
                        <div className="absolute -bottom-1 -right-1 rounded-full bg-background p-0.5">
                            <TriangleAlert className="h-3.5 w-3.5" style={{ color: primaryColor }} />
                        </div>
                    </div>
                </div>

                <div className="flex flex-col items-center gap-1">
                    <span className="text-xs font-semibold text-foreground/80">
                        {tooLarge.messageMain}
                    </span>
                    <span className="max-w-40 text-center text-[10px] leading-relaxed text-muted-foreground/60">
                        {tooLarge.messageSecondaryPart1}<span className="italic text-nowrap p-0.5 px-1 rounded-md border bg-background/50">{template.path}</span>{tooLarge.messageSecondaryPart2}
                    </span>
                </div>
            </div>
        );
    };

    // === 主渲染逻辑 ===
    return (
        <div className={`relative h-full w-full overflow-hidden rounded-md ${className}`}>

            {/* 1. 静态缩略图模式 */}
            {viewState === 'thumbnail' && imageSrc && (
                <img
                    src={imageSrc}
                    alt={template.name}
                    className="h-full w-full object-contain transition-transform duration-700 group-hover:scale-105"
                    loading="lazy"
                // Error handling is now done in fetch
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

            {/* 5. 尺寸过大模式 */}
            {viewState === 'too_large' && <LimitExceededView />}
        </div>
    );
};