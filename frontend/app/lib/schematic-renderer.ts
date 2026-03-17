import { Buffer } from "buffer";
import {
  renderSchematic,
  type SchematicHandles,
} from "@enginehub/schematicwebviewer";

// 1. 确保 Buffer 在浏览器环境可用
if (!("Buffer" in window)) {
  // @ts-ignore
  window.Buffer = Buffer;
}

export interface AutoClippingProperties {
  backgroundColor: number | "transparent";
  targetWidth: number;
  targetHeight: number;
  padding: number;
}

export interface RenderSchemOptions {
  width?: number;
  height?: number;
  alpha?: number; // 摄像机水平旋转角
  beta?: number; // 摄像机垂直旋转角
  radius?: number; // 摄像机缩放系数
  assetsUrl?: string; // mc-assets.zip 的地址
  backgroundColor?: number | "transparent"; // 背景颜色
  offsetY?: number; // 摄像机垂直偏移
  autoFraming?: boolean; // 3D 场景自动聚焦
  autoClipping?: boolean; // [新增] 是否进行后处理：自动裁剪空白并最大化
  autoClippingConfig?: AutoClippingProperties; // [新增] 裁剪配置
}

/**
 * 前端异步渲染 Schematic 文件并返回 PNG Base64
 */
export async function renderSchemFile(
  input: string | (() => Promise<ArrayBuffer | string | any>),
  options: RenderSchemOptions = {},
): Promise<string> {
  const {
    width = 800,
    height = 600,
    assetsUrl = "/mc-assets.zip",
    backgroundColor = 0xffffff,
    autoFraming = true,
    autoClipping = false, // 默认为 false
    autoClippingConfig = {
      backgroundColor: backgroundColor,
      targetWidth: width,
      targetHeight: height,
      padding: 10,
    },
  } = options;

  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;

  // --- 2. 处理输入 (URL 转 Base64) ---
  let base64Data = "";
  try {
    if (typeof input === "function") {
      const result = await input();
      const data = result && result.data ? result.data : result;

      if (data instanceof ArrayBuffer) {
        base64Data = Buffer.from(data).toString("base64");
      } else if (typeof data === "string") {
        base64Data = data.replace(/^data:.*;base64,/, "");
      } else {
        throw new Error("Function must return ArrayBuffer or Base64 string");
      }
    } else {
      const response = await fetch(input);
      if (!response.ok)
        throw new Error(`Failed to fetch schematic: ${response.statusText}`);
      const arrayBuffer = await response.arrayBuffer();
      base64Data = Buffer.from(arrayBuffer).toString("base64");
    }
  } catch (e) {
    throw new Error(`Error loading schematic source: ${e}`);
  }

  let schematicHandle: SchematicHandles | null = null;

  try {
    // --- 3. 执行渲染 ---
    schematicHandle = await renderSchematic(canvas, base64Data, {
      size: { width, height },
      getClientJarUrl: async () => assetsUrl,
      renderBars: false,
      renderArrow: false,
      orbit: false,
      antialias: true,
      // 如果启用了 autoClipping 且背景不是透明，建议渲染时先用透明背景，
      // 这样裁剪算法更容易识别边界，裁剪后再填充背景色。
      // 这里为了保持逻辑兼容，维持用户原设定。
      backgroundColor:
        backgroundColor === "transparent"
          ? 0x000000
          : (backgroundColor as number),
      disableAutoRender: false,
    });

    // --- 4. 调整摄像机 ---
    const engine = schematicHandle.getEngine();
    const scene = engine.scenes[0];

    if (backgroundColor === "transparent") {
      scene.clearColor.set(0, 0, 0, 0);
    }
    scene.autoClear = true;

    const camera = scene.activeCamera as any;
    if (camera) {
      const arcCam = camera;
      const defaultAlpha = Math.PI / 4;
      const defaultBeta = Math.atan(Math.sqrt(4));

      if (options.radius !== undefined && options.radius > 0) {
        const radiusFactor = options.radius ?? 0.65;
        arcCam.radius = radiusFactor * arcCam.radius;
      }

      arcCam.useFramingBehavior = true;
      const framingBehavior = arcCam.getBehaviorByName("Framing");
      if (framingBehavior && autoFraming) {
        const worldExtends = scene.getWorldExtends();
        framingBehavior.zoomOnBoundingInfo(worldExtends.min, worldExtends.max);
      }

      arcCam.alpha =
        typeof options.alpha === "number" ? options.alpha : defaultAlpha;
      arcCam.beta =
        typeof options.beta === "number" ? options.beta : defaultBeta;

      if (options.radius !== undefined && options.radius > 0) {
        // 再次应用 radius 覆盖 framing 的结果（如果需要）
        const radiusFactor = options.radius ?? 0.65;
        arcCam.radius = radiusFactor * arcCam.radius;
      }

      const offsetV = options.offsetY ?? 0;
      if (arcCam.target && offsetV !== 0) {
        arcCam.target.y += offsetV;
      }

      if (arcCam.rebuildInertia) arcCam.rebuildInertia();
      arcCam.getViewMatrix(true);
    }

    if (camera.rebuildInertia) camera.rebuildInertia();

    // --- 5. 强制渲染一帧 ---
    await new Promise<void>((resolve) => {
      scene.executeWhenReady(() => {
        schematicHandle?.render();
        resolve();
      });
    });

    // --- 6. [优化后的] Auto Clipping 后处理 ---
    if (autoClipping) {
      const { processAutoClipping } = await import("./auto-clipping");
      // 注意：因为 WebGL Canvas 上下文冲突，该函数现在返回处理后的 DataURL
      const resultDataUrl = await processAutoClipping(
        canvas,
        autoClippingConfig.backgroundColor,
        autoClippingConfig.targetWidth,
        autoClippingConfig.targetHeight,
        autoClippingConfig.padding,
      );
      return resultDataUrl;
    }

    // 如果不开启 autoClipping，则直接导出原始 WebGL Canvas 内容
    return canvas.toDataURL("image/png");
  } catch (error) {
    console.error("Render failed:", error);
    throw error;
  } finally {
    // --- 7. 清理资源 ---
    if (schematicHandle) {
      schematicHandle.destroy();
    }
    // canvas 会被垃圾回收
  }
}
