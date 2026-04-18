"use client";

import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Buffer } from "buffer";
import {
  renderSchematic,
  type SchematicHandles,
} from "@enginehub/schematicwebviewer";
import { fetchFileToBase64 } from "./fetchBase64";
import { Loader2, AlertCircle } from "lucide-react";

if (typeof window !== "undefined" && !("Buffer" in window)) {
  // @ts-ignore
  window.Buffer = Buffer;
}

/**
 * 渲染配置选项
 */
export interface SchematicViewRenderOptions {
  size?: { width: number; height: number };
  getClientJarUrl?: () => Promise<string>;
  resourcePacks?: string[];
  renderBars?: boolean;
  renderArrow?: boolean;
  orbit?: boolean;
  orbitSpeed?: number;
  antialias?: boolean;
  backgroundColor?: number | "transparent";
  debug?: boolean;
  disableAutoRender?: boolean;
}

type SchematicViewerProps = {
  src: string;
  options?: SchematicViewRenderOptions;
  onError?: (error: Error) => void;
  onLoad?: () => void;
  loadingElement?: React.ReactNode;
  errorElement?: React.ReactNode;
};

const DEFAULT_OPTIONS: SchematicViewRenderOptions = {
  getClientJarUrl: async () => "/mc-assets.zip",
  renderBars: false,
  renderArrow: false,
  orbit: true,
  orbitSpeed: 0.02,
  antialias: true,
  backgroundColor: 0x00000000,
  debug: false,
  disableAutoRender: false,
};

export default function JKSchematicViewer({
  src,
  options,
  onError,
  onLoad,
  loadingElement,
  errorElement,
}: SchematicViewerProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const rendererRef = useRef<SchematicHandles | null>(null);
  const base64Ref = useRef<string>("");

  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);
  const [containerSize, setContainerSize] = useState({ width: 0, height: 0 });

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const observer = new ResizeObserver(([entry]) => {
      const width = Math.floor(entry.contentRect.width);
      const height = Math.floor(entry.contentRect.height);
      setContainerSize((prev) =>
        prev.width === width && prev.height === height
          ? prev
          : { width, height },
      );
    });

    observer.observe(container);
    return () => observer.disconnect();
  }, []);

  const mergedOptions = useMemo<SchematicViewRenderOptions>(() => {
    const width = options?.size?.width || containerSize.width || 500;
    const height = options?.size?.height || containerSize.height || 500;

    return {
      ...DEFAULT_OPTIONS,
      ...options,
      size: { width, height },
    };
  }, [options, containerSize]);

  // 2. 渲染核心逻辑
  const drawSchematic = useCallback(
    async (base64Data: string) => {
      if (!canvasRef.current || !base64Data) return;
      if (!mergedOptions.size || mergedOptions.size.width < 32 || mergedOptions.size.height < 32) {
        return;
      }

      try {
        // 如果已有实例，先销毁
        if (rendererRef.current) {
          rendererRef.current.destroy();
          rendererRef.current = null;
        }

        // 处理容器尺寸
        const container = containerRef.current;
        if (container) {
          const { width, height } = container.getBoundingClientRect();
          canvasRef.current.width = width;
          canvasRef.current.height = height;
        }

        rendererRef.current = await renderSchematic(
          canvasRef.current,
          base64Data,
          mergedOptions,
        );

        setIsLoading(false);
        onLoad?.();
      } catch (error) {
        console.error("Schematic 渲染失败:", error);
        setIsError(true);
        setIsLoading(false);
        onError?.(error as Error);
      }
    },
    [mergedOptions, onLoad, onError],
  );

  // 3. 数据抓取与生命周期管理
  useEffect(() => {
    let isMounted = true;

    const init = async () => {
      setIsLoading(true);
      setIsError(false);

      try {
        const { base64 } = await fetchFileToBase64(src);
        if (!isMounted) return;

        base64Ref.current = base64;
        await drawSchematic(base64);
      } catch (error) {
        if (!isMounted) return;
        console.error("Schematic 加载失败:", error);
        setIsError(true);
        setIsLoading(false);
        onError?.(error as Error);
      }
    };

    init();

    return () => {
      isMounted = false;
      if (rendererRef.current) {
        rendererRef.current.destroy();
        rendererRef.current = null;
      }
    };
  }, [src, onError]);

  // 4. 在容器尺寸或渲染选项变化时重绘
  useEffect(() => {
    if (base64Ref.current && !isLoading && !isError) {
      drawSchematic(base64Ref.current);
    }
  }, [
    drawSchematic,
    isLoading,
    isError,
    mergedOptions.size?.width,
    mergedOptions.size?.height,
    options?.orbit,
    options?.orbitSpeed,
    options?.renderBars,
    options?.renderArrow,
    options?.backgroundColor,
    options?.antialias,
    options?.debug,
    options?.disableAutoRender,
  ]);

  return (
    <div
      ref={containerRef}
      className="relative w-full h-full flex items-center justify-center min-h-[inherit] overflow-hidden rounded-xl"
    >
      {/* 错误状态 UI */}
      {isError && (
        <div className="absolute inset-0 z-10 flex items-center justify-center bg-background/65 backdrop-blur-sm">
          {errorElement || (
            <div className="flex flex-col items-center gap-2 text-destructive">
              <AlertCircle className="w-8 h-8" />
              <span className="text-sm font-medium">模型显示出错</span>
            </div>
          )}
        </div>
      )}

      {/* 加载状态 UI */}
      {isLoading && (
        <div className="absolute inset-0 z-20 flex flex-col items-center justify-center bg-background/50 backdrop-blur-[1px] gap-3">
          {loadingElement || (
            <>
              <Loader2 className="w-8 h-8 animate-spin text-primary/60" />
              <p className="text-xs text-muted-foreground animate-pulse font-medium">
                正在构建 3D 世界...
              </p>
            </>
          )}
        </div>
      )}

      {/* 3D 画布 */}
      <canvas
        ref={canvasRef}
        className={`transition-all duration-700 ease-in-out cursor-grab active:cursor-grabbing ${
          isLoading ? "opacity-0 scale-95" : "opacity-100 scale-100"
        } ${isError ? "hidden" : "block"}`}
        style={{
          width: "100%",
          height: "100%",
          display: isError ? "none" : "block",
        }}
      />
    </div>
  );
}
