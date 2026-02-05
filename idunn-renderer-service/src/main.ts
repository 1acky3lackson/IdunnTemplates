import { Buffer } from 'buffer';
// @ts-ignore
window.Buffer = Buffer;

import './style.css';
import { renderSchematic, SchematicHandles } from '@enginehub/schematicwebviewer';

interface CameraOptions {
    alpha?: number;
    beta?: number;
    radius?: number;
}

declare global {
    interface Window {
        renderSchematic: (url: string, width: number, height: number, options?: CameraOptions) => Promise<void>;
        // 新增函数：直接接收 Base64 字符串
        renderSchematicFromBase64: (base64Str: string, width: number, height: number, options?: CameraOptions) => Promise<void>;
    }
}

const canvas = document.getElementById("canvas") as HTMLCanvasElement | null;
const statusDiv = document.getElementById("status") as HTMLDivElement | null;
const controlsDiv = document.getElementById("controls") as HTMLDivElement | null;

if (!canvas) throw new Error("Canvas element not found!");

let currentHandle: SchematicHandles | null = null;
const urlParams = new URLSearchParams(window.location.search);
const isHeadless = urlParams.get('headless') === 'true';

if (isHeadless && controlsDiv) {
    controlsDiv.style.display = 'none';
}

// 核心渲染逻辑（内部调用）
async function executeRender(
    base64Data: string, 
    width: number, 
    height: number, 
    options: CameraOptions = {}
) {
    if (!canvas) return;

    if (currentHandle) {
        currentHandle.destroy();
        currentHandle = null;
    }

    if (!isHeadless) updateStatus("正在构建 3D 场景...");

    currentHandle = await renderSchematic(
        canvas,
        base64Data,
        {
            size: { width, height },
            getClientJarUrl: async () => '/mc-assets.zip', 
            renderBars: false,
            renderArrow: false,
            orbit: false,
            antialias: false,
            backgroundColor: 0xFFFFFF, // 需与 Server 端抠图颜色一致
            disableAutoRender: false,
        }
    );

    const engine = currentHandle.getEngine();
    const scene = engine.scenes[0];
    const camera = scene.activeCamera;

    if (camera) {
        const arcCam = camera as any;
        const defaultAlpha = Math.PI / 4; 
        const defaultBeta = Math.atan(Math.sqrt(4));
        
        arcCam.alpha = typeof options.alpha === 'number' ? options.alpha : defaultAlpha;
        arcCam.beta = typeof options.beta === 'number' ? options.beta : defaultBeta;

        const radiusFactor = options.radius ?? 0.65;
        arcCam.radius = radiusFactor * arcCam.radius;

        if (arcCam.rebuildInertia) arcCam.rebuildInertia();
    }

    currentHandle.render();
    finalizeRender();
}

// === 暴露给 Puppeteer 的新函数 ===
window.renderSchematicFromBase64 = async (base64Str, width, height, options = {}) => {
    try {
        setupCanvas(width, height);
        await executeRender(base64Str, width, height, options);
    } catch (e) {
        handleError(e);
    }
};

// === 原有的基于 URL 的函数 (保持兼容) ===
window.renderSchematic = async (url, width, height, options = {}) => {
    try {
        setupCanvas(width, height);
        if (!isHeadless) updateStatus("正在获取文件...");
        const response = await fetch(url);
        const blob = await response.blob();
        const base64 = await new Promise<string>((resolve) => {
            const reader = new FileReader();
            reader.onloadend = () => resolve((reader.result as string).split(',')[1]);
            reader.readAsDataURL(blob);
        });
        await executeRender(base64, width, height, options);
    } catch (e) {
        handleError(e);
    }
};

function setupCanvas(width: number, height: number) {
    if (canvas) {
        canvas.width = width;
        canvas.height = height;
        canvas.style.width = `${width}px`;
        canvas.style.height = `${height}px`;
    }
}

function finalizeRender() {
    setTimeout(() => {
        document.body.setAttribute('data-status', 'ready');
        if (!isHeadless) updateStatus("渲染完毕！", "success");
    }, 500);
}

function handleError(e: any) {
    console.error(e);
    const msg = e instanceof Error ? e.message : String(e);
    document.body.setAttribute('data-error', msg);
}

function updateStatus(text: string, className?: string) {
    if (statusDiv) {
        statusDiv.innerText = text;
        statusDiv.className = className || "";
    }
}