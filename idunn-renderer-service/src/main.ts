// === 1. Polyfill Node.js Globals (必须在最前面) ===
import { Buffer } from 'buffer';
// @ts-ignore
window.Buffer = Buffer;

import './style.css';
import { renderSchematic, SchematicHandles } from '@enginehub/schematicwebviewer';
import { TextureLoader } from 'three';

// === Type Definitions ===
interface CameraOptions {
    alpha?: number;  // Horizontal rotation (radians)
    beta?: number;   // Vertical rotation (radians)
    radius?: number; // Zoom level (distance from target)
}

// === 类型定义 ===
declare global {
    interface Window {
        // Updated signature to accept options
        renderSchematic: (
            url: string, 
            width: number, 
            height: number, 
            options?: CameraOptions
        ) => Promise<void>;
    }
}

// === DOM 元素获取 ===
const canvas = document.getElementById("canvas") as HTMLCanvasElement | null;
const statusDiv = document.getElementById("status") as HTMLDivElement | null;
const controlsDiv = document.getElementById("controls") as HTMLDivElement | null;

if (!canvas) throw new Error("Canvas element not found!");

// 保存当前的渲染句柄，用于销毁旧场景
let currentHandle: SchematicHandles | null = null;

// === 1. UI 隐藏逻辑 (Headless Check) ===
const urlParams = new URLSearchParams(window.location.search);
const isHeadless = urlParams.get('headless') === 'true';

if (isHeadless && controlsDiv) {
    controlsDiv.style.display = 'none';
}

// === 2. 工具函数：Fetch 转 Base64 ===
// 对应 React代码中的 fetchFileToBase64
async function fetchFileToBase64(url: string): Promise<string> {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Failed to fetch schematic: ${response.statusText}`);
    }
    const blob = await response.blob();
    
    return new Promise<string>((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => {
            // result 格式为 "data:application/octet-stream;base64,AAAA..."
            // 我们只需要逗号后面的部分
            const result = reader.result as string;
            const base64 = result.split(',')[1];
            resolve(base64);
        };
        reader.onerror = reject;
        reader.readAsDataURL(blob);
    });
}

// === 3. 核心渲染逻辑 ===
async function startRendererWithUrl(
    url: string, 
    width: number, 
    height: number, 
    options: CameraOptions = {} // Accept options with default empty object
) {
    if (!canvas) return;

    // 清理旧场景 (React useEffect cleanup 逻辑)
    if (currentHandle) {
        currentHandle.destroy();
        currentHandle = null;
    }

    if (!isHeadless) updateStatus("正在下载并转换 Base64...");

    // 1. 获取 Base64
    const base64Data = await fetchFileToBase64(url);

    if (!isHeadless) updateStatus("正在构建 3D 场景...");

    // 2. 调用 @enginehub/schematicwebviewer
    // 对应 React代码中的 drawSchematic -> renderSchematic
    currentHandle = await renderSchematic(
        canvas,
        base64Data,
        {
            // 对应 React代码中的 options
            size: { width, height },
            // 关键：指向本地 public 目录下的 zip，加速加载
            getClientJarUrl: async () => '/mc-assets.zip', 
            
            renderBars: false,
            renderArrow: false,
            orbit: false, // 截图模式不需要自动旋转
            orbitSpeed: 0.02,
            antialias: true,
            backgroundColor: 0x012345, // 透明背景
            debug: false,
            disableAutoRender: false,
        }
    );

    // === 使用 Engine 调整相机 ===
    const engine = currentHandle.getEngine();

    const scene = engine.scenes[0];
    
    // 1. 获取场景 (通常只有一个场景，取第一个即可)
    const camera = scene.activeCamera;

    if (camera) {
        const arcCam = camera as any;

        // --- Defaults ---
        // Horizontal: 45 degrees (Math.PI / 4)
        // Vertical: Isometric (~35.264 degrees)
        const defaultAlpha = Math.PI / 4; 
        const defaultBeta = Math.atan(Math.sqrt(4));
        
        // --- Apply Options or Defaults ---
        // Use passed options if valid numbers, otherwise use defaults
        arcCam.alpha = typeof options.alpha === 'number' ? options.alpha : defaultAlpha;
        arcCam.beta = typeof options.beta === 'number' ? options.beta : defaultBeta;

        const radius = options.radius ?? 0.65;

        // Radius handling:
        // The library calculates an automatic radius. If a specific radius is provided via options, use it.
        // Otherwise, keep the library's calculated radius (or multiply it by a factor if you want a default zoom).
        // if (typeof options.radius === 'number') {
        arcCam.radius = radius * arcCam.radius;
        // } 
        // Example: If you wanted a default zoom factor relative to auto-calculated:
        // else { arcCam.radius = arcCam.radius * 0.8; }

        if (arcCam.rebuildInertia) {
            arcCam.rebuildInertia();
        }
    }

    // 3. 手动触发一次渲染
    // 因为开启了 disableAutoRender，我们需要手动告诉场景绘制这一帧
    currentHandle.render();

    // 4. 渲染完成处理
    // 因为是手动渲染，不需要 delay 等待，直接完成
    finalizeRender();
}

// === 4. 全局暴露给 Puppeteer ===
window.renderSchematic = async (
    url: string, 
    width: number, 
    height: number, 
    options: CameraOptions = {}
) => {
    try {
        if (canvas) {
            canvas.width = width;
            canvas.height = height;
            canvas.style.width = `${width}px`;
            canvas.style.height = `${height}px`;
        }

        await startRendererWithUrl(url, width, height, options);

    } catch (e) {
        console.error(e);
        const msg = e instanceof Error ? e.message : String(e);
        document.body.setAttribute('data-error', msg);
    }
};

// === 5. 辅助逻辑 ===
function finalizeRender() {
    console.log("Render finished");
    
    // 给一点时间让 Three.js 彻底完成首帧渲染
    setTimeout(() => {
        document.body.setAttribute('data-status', 'ready');
        if (!isHeadless) updateStatus("渲染完毕！", "success");
    }, 500);
}

function updateStatus(text: string, className?: string) {
    if (statusDiv) {
        statusDiv.innerText = text;
        statusDiv.className = className || "";
    }
}

// === 6. 本地开发手动调试 ===
const renderBtn = document.getElementById('renderBtn');
if (renderBtn) {
    renderBtn.addEventListener('click', () => {
        const urlInput = document.getElementById('urlInput') as HTMLInputElement | null;
        if (urlInput && urlInput.value) {
            // 模拟 Puppeteer 调用
            window.renderSchematic(urlInput.value, window.innerWidth, window.innerHeight);
        }
    });
}