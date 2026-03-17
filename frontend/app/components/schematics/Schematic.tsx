"use client";

import React, { useEffect, useState } from "react";
import {
  Maximize2,
  X,
  Download,
  HelpCircle,
  RefreshCw,
  AlertCircle,
} from "lucide-react";

// Shadcn UI 组件
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { Skeleton } from "@/components/ui/skeleton";

// 假设该查看器是纯功能性的，不依赖 antd
import JKSchematicViewer from "./SchematicViewer";

interface SchematicProps {
  src: string | (() => string | Promise<string>);
  title: string;
  downloadable?: boolean;
  antialias?: boolean;
  renderBars?: boolean;
  renderArrow?: boolean;
  resourcePacks?: string[];
  backgroundColor?: number;
  orbit?: boolean;
  orbitSpeed?: number;
  inFullScreen?: boolean;
  onCloseFullScreen?: () => void;
  onOpenFullScreen?: () => void;
}

/**
 * 错误展示组件 - 完全移除 antd 样式
 */
const SchematicError = ({ onRetry }: { onRetry?: () => void }) => {
  return (
    <div className="flex flex-col items-center justify-center w-full aspect-video rounded-xl bg-destructive/5 border-2 border-dashed border-destructive/20 gap-4 p-6">
      <div className="p-3 bg-destructive/10 rounded-full text-destructive">
        <AlertCircle size={32} />
      </div>
      <div className="text-center">
        <h3 className="font-semibold text-lg text-foreground">
          Schematic 加载失败
        </h3>
        <p className="text-sm text-muted-foreground mt-1 max-w-62.5">
          无法读取模型文件，请检查文件链接或稍后重试。
        </p>
      </div>
      {onRetry && (
        <Button variant="outline" size="sm" onClick={onRetry} className="gap-2">
          <RefreshCw size={14} /> 尝试重试
        </Button>
      )}
    </div>
  );
};

const Schematic: React.FC<SchematicProps> = ({
  src,
  title,
  downloadable = true,
  antialias = true,
  renderBars = true,
  renderArrow = true,
  resourcePacks = [],
  backgroundColor,
  orbit: initialOrbit = true,
  orbitSpeed = 1,
  inFullScreen = false,
  onCloseFullScreen,
  onOpenFullScreen,
}) => {
  const [isOrbiting, setIsOrbiting] = useState(initialOrbit);
  const [resolvedUrl, setResolvedUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [isMobile, setIsMobile] = useState(false);

  // 替代 useResponsive 钩子
  useEffect(() => {
    const checkMobile = () => setIsMobile(window.innerWidth < 768);
    checkMobile();
    window.addEventListener("resize", checkMobile);
    return () => window.removeEventListener("resize", checkMobile);
  }, []);

  // 解析 URL 逻辑
  useEffect(() => {
    const resolveUrl = async () => {
      setLoading(true);
      try {
        if (typeof src === "function") {
          const result = await src();
          setResolvedUrl(result);
        } else {
          setResolvedUrl(src);
        }
      } catch (error) {
        console.error("Schematic URL resolution failed:", error);
      } finally {
        setLoading(false);
      }
    };
    resolveUrl();
  }, [src]);

  const handleDownload = () => {
    if (resolvedUrl) {
      const link = document.createElement("a");
      link.href = resolvedUrl;
      link.download = title || "schematic";
      link.click();
    }
  };

  return (
    <TooltipProvider delayDuration={200}>
      <Card className="overflow-hidden border-border/60 shadow-md transition-all">
        {/* Header - 标题栏 */}
        <CardHeader className="flex flex-row items-center justify-between space-y-0 px-4 py-3 bg-secondary/20 border-b">
          <div className="flex items-center gap-3 flex-1 min-w-0">
            <Tooltip>
              <TooltipTrigger asChild>
                <div className="cursor-help text-muted-foreground hover:text-primary transition-colors">
                  <HelpCircle size={18} />
                </div>
              </TooltipTrigger>
              <TooltipContent
                side="bottom"
                className="p-3 text-xs space-y-1 shadow-xl"
              >
                <p className="font-bold border-b pb-1 mb-1">操作指南</p>
                <p>• 左键/中键：旋转模型</p>
                <p>• 滚轮：缩放视距</p>
                <p>• 右键：平移视角</p>
              </TooltipContent>
            </Tooltip>

            <CardTitle className="text-sm md:text-base font-medium truncate flex-1 tracking-tight">
              {title}
            </CardTitle>

            <div className="flex items-center gap-2 px-3 py-1 bg-background/80 rounded-full border border-border/50">
              <span className="text-[10px] uppercase tracking-tighter font-bold text-muted-foreground hidden sm:inline">
                Auto Orbit
              </span>
              <Switch
                checked={isOrbiting}
                onCheckedChange={setIsOrbiting}
                className="scale-75"
              />
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-1.5 ml-4">
            {!inFullScreen && onOpenFullScreen && (
              <Button
                variant="ghost"
                size="icon"
                onClick={onOpenFullScreen}
                className="h-8 w-8"
              >
                <Maximize2 size={16} />
              </Button>
            )}
            {inFullScreen && onCloseFullScreen && (
              <Button
                variant="ghost"
                size="icon"
                onClick={onCloseFullScreen}
                className="h-8 w-8"
              >
                <X size={16} />
              </Button>
            )}
            {downloadable && (
              <Button
                variant="outline"
                size="sm"
                onClick={handleDownload}
                className="hidden sm:flex h-8 gap-2 bg-background"
              >
                <Download size={14} />
                <span>下载文件</span>
              </Button>
            )}
          </div>
        </CardHeader>

        {/* Content Area */}
        <CardContent className="p-0 bg-slate-50 dark:bg-zinc-950 relative overflow-hidden">
          <div
            className="w-full relative transition-all duration-300"
            style={{ height: isMobile ? "350px" : "550px" }}
          >
            {loading ? (
              <div className="absolute inset-0 flex flex-col items-center justify-center p-12 gap-4">
                <Skeleton className="w-full h-full rounded-lg" />
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="flex items-center gap-2 text-muted-foreground animate-pulse">
                    <RefreshCw size={18} className="animate-spin" />
                    <span className="text-sm">正在加载模型资源...</span>
                  </div>
                </div>
              </div>
            ) : resolvedUrl ? (
              <JKSchematicViewer
                src={resolvedUrl}
                errorElement={
                  <SchematicError onRetry={() => setResolvedUrl(null)} />
                }
                key={`schematic-v2-${isOrbiting}-${resolvedUrl}`}
                options={{
                  antialias,
                  renderBars,
                  renderArrow,
                  resourcePacks,
                  orbit: isOrbiting,
                  orbitSpeed,
                  backgroundColor,
                  size: {
                    width: 500,
                    height: isMobile ? 350 : 550,
                  },
                }}
              />
            ) : (
              <SchematicError />
            )}
          </div>
        </CardContent>
      </Card>
    </TooltipProvider>
  );
};

export default Schematic;
