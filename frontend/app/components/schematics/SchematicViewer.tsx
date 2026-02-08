'use client';

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { renderSchematic, type SchematicHandles } from "@enginehub/schematicwebviewer";
import { fetchFileToBase64 } from './fetchBase64';
import { Loader2, AlertCircle } from "lucide-react";

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
  backgroundColor?: number | 'transparent';
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
  getClientJarUrl: async () => '/minecraft/schem-display/mc-assets-latest.zip',
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
  const fetchResult = useRef<string>('');

  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);
  const [isMobile, setIsMobile] = useState(false);

  // 1. 响应式监听 (取代 useResponsive)
  useEffect(() => {
    const mql = window.matchMedia('(max-width: 768px)');
    const handler = (e: MediaQueryListEvent | MediaQueryList) => setIsMobile(e.matches);
    handler(mql);
    mql.addEventListener('change', handler);
    return () => mql.removeEventListener('change', handler);
  }, []);

  // 2. 渲染核心逻辑
  const drawSchematic = useCallback(async (base64Data: string) => {
    if (!canvasRef.current || !base64Data) return;

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

      // 合并配置
      const mergedOptions: SchematicViewRenderOptions = {
        ...DEFAULT_OPTIONS,
        size: isMobile ? { width: 300, height: 350 } : { width: 500, height: 500 },
        ...options,
      };

      rendererRef.current = await renderSchematic(
        canvasRef.current,
        base64Data,
        mergedOptions
      );

      setIsLoading(false);
      onLoad?.();
    } catch (error) {
      console.error('Schematic 渲染失败:', error);
      setIsError(true);
      setIsLoading(false);
      onError?.(error as Error);
    }
  }, [options, isMobile, onLoad, onError]);

  // 3. 数据抓取与生命周期管理
  useEffect(() => {
    let isMounted = true;

    const init = async () => {
      setIsLoading(true);
      setIsError(false);

      try {
        const { base64 } = await fetchFileToBase64(src);
        if (!isMounted) return;
        
        fetchResult.current = base64;
        await drawSchematic(base64);
      } catch (error) {
        if (!isMounted) return;
        console.error('Schematic 加载失败:', error);
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
  }, [src]);

  // 4. 监听配置项变化（如 orbit 开关）进行局部热更新
  useEffect(() => {
    if (fetchResult.current && !isLoading) {
      drawSchematic(fetchResult.current);
    }
  }, [
    options?.orbit,
    options?.orbitSpeed,
    options?.renderBars,
    options?.renderArrow,
    options?.backgroundColor,
    isMobile
  ]);

  return (
    <div
      ref={containerRef}
      className="relative w-full h-full flex items-center justify-center min-h-[inherit] overflow-hidden rounded-xl bg-background"
    >
      {/* 错误状态 UI */}
      {isError && (
        <div className="absolute inset-0 z-10 flex items-center justify-center bg-background/80 backdrop-blur-sm">
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
        <div className="absolute inset-0 z-20 flex flex-col items-center justify-center bg-background gap-3">
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
          isLoading ? 'opacity-0 scale-95' : 'opacity-100 scale-100'
        } ${isError ? 'hidden' : 'block'}`}
        style={{
          width: '100%',
          height: '100%',
          display: isError ? 'none' : 'block',
        }}
      />
    </div>
  );
}