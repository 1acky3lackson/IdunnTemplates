import { Buffer } from 'buffer';
import { renderSchematic, type SchematicHandles } from '@enginehub/schematicwebviewer';

// 1. 确保 Buffer 在浏览器环境可用 (这是 schematicwebviewer 的依赖)
if (!('Buffer' in window)) {
    // @ts-ignore
    window.Buffer = Buffer;
}

export interface RenderSchemOptions {
    width?: number;
    height?: number;
    alpha?: number; // 摄像机水平旋转角
    beta?: number;  // 摄像机垂直旋转角
    radius?: number; // 摄像机缩放系数
    assetsUrl?: string; // mc-assets.zip 的地址，默认为 /mc-assets.zip
    backgroundColor?: number | 'transparent'; // 背景颜色
}

/**
 * 前端异步渲染 Schematic 文件并返回 PNG Base64
 * @param input - Schematic 文件的 URL 或者 Base64 字符串
 * @param options - 渲染配置项
 * @returns Promise<string> - 图片的 Base64 字符串 (data:image/png;base64,...)
 */
export async function renderSchemFile(
    input: string,
    options: RenderSchemOptions = {}
): Promise<string> {
    const {
        width = 800,
        height = 600,
        assetsUrl = '/mc-assets.zip', // 确保你的前端静态资源里有这个文件
        backgroundColor = 0xFFFFFF, // 如果库支持，设为 0 或 null 可能获得透明背景
    } = options;
    // debugger;
    // --- 1. 创建不可见的 Canvas ---
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    // 注意：不仅是不添加到 DOM，我们甚至不需要设置 display: none，
    // 因为只要不 append，它就是内存中的对象。

    // --- 2. 处理输入 (URL 转 Base64) ---
    // schematicwebviewer 的 renderSchematic 第二个参数实际上只接受 base64 字符串
    let base64Data = '';

    // 简单的判断是否为 Base64 (这里假设 URL 不包含换行符，且 Base64 较长)
    // 也可以通过正则判断，或者由调用方明确
    // const isBase64 = (str: string) => !str.includes('/') || str.length > 2000 || !str.startsWith('http');

    // if (isBase64(input)) {
    //     // 如果输入包含了 data URI 前缀，去掉它
    //     base64Data = input.includes(',') ? input.split(',')[1] : input;
    // } else {
        // 如果是 URL，先 fetch 下来
        try {
            const response = await fetch(input);
            if (!response.ok) throw new Error(`Failed to fetch schematic: ${response.statusText}`);
            const arrayBuffer = await response.arrayBuffer();
            base64Data = Buffer.from(arrayBuffer).toString('base64');
        } catch (e) {
            throw new Error(`Error loading schematic from URL: ${e}`);
        }
    // }

    let schematicHandle: SchematicHandles | null = null;

    try {
        // --- 3. 执行渲染 ---
        schematicHandle = await renderSchematic(
            canvas,
            base64Data,
            {
                size: { width, height },
                getClientJarUrl: async () => assetsUrl,
                renderBars: false,
                renderArrow: false,
                orbit: false,
                antialias: true, // 前端生成图建议开启抗锯齿，除非为了像素风
                // 尝试处理背景: 如果库支持 transparent，这里可以改。
                // 保持和你 Server 端一致的白色背景：
                backgroundColor: backgroundColor === 'transparent' ? 0x000000 : (backgroundColor as number),
                disableAutoRender: false, // 我们手动控制一帧
            }
        );

        // --- 4. 调整摄像机 (复用你原有的数学逻辑) ---
        const engine = schematicHandle.getEngine();
        const scene = engine.scenes[0];

        // 尝试设置透明背景 (如果 engine 支持)
        if (backgroundColor === 'transparent') {
            scene.clearColor.set(0, 0, 0, 0); // RGBA: 全透明
        }

        const camera = scene.activeCamera;

        if (camera) {
            const arcCam = camera as any;
            const defaultAlpha = Math.PI / 4;
            const defaultBeta = Math.atan(Math.sqrt(4));

            arcCam.alpha = typeof options.alpha === 'number' ? options.alpha : defaultAlpha;
            arcCam.beta = typeof options.beta === 'number' ? options.beta : defaultBeta;

            const radiusFactor = options.radius ?? 0.65;
            // 原始代码逻辑: arcCam.radius = radiusFactor * arcCam.radius;
            // 注意：renderSchematic 初始化后 radius 可能还未完全计算好边界，
            // 但通常此时已有默认值。
            arcCam.radius = radiusFactor * arcCam.radius;

            if (arcCam.rebuildInertia) arcCam.rebuildInertia();
        }

        // --- 5. 强制渲染一帧并导出 ---
        // 确保资源（纹理）加载完毕。虽然 renderSchematic await 了，
        // 但有时候纹理是异步解码的。BabylonJS 通常会处理好，
        // 但为了保险，可以等待几毫秒或者使用 scene.executeWhenReady

        await new Promise<void>((resolve) => {
            // 等待场景准备就绪（纹理加载等）
            scene.executeWhenReady(() => {
                // 强制渲染当前帧
                schematicHandle?.render();
                resolve();
            });
        });

        // 导出图片
        // image/png 默认支持透明通道（如果 scene.clearColor 是透明的）
        const dataUrl = canvas.toDataURL('image/png');

        return dataUrl;

    } catch (error) {
        console.error("Render failed:", error);
        throw error;
    } finally {
        // --- 6. 清理资源 ---
        // WebGL 上下文是有限的，务必销毁
        if (schematicHandle) {
            schematicHandle.destroy();
        }
        // 帮助 GC
        canvas.width = 1;
        canvas.height = 1;
    }
}