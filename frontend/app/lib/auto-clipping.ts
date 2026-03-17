/**
 * 图像处理：自动裁剪并缩放
 * 逻辑：从 WebGL Canvas 提取图像，在 2D Canvas 中进行像素分析、裁剪并重新缩放
 */
export async function processAutoClipping(
  sourceCanvas: HTMLCanvasElement,
  backgroundColor: number | "transparent",
  targetWidth: number,
  targetHeight: number,
  padding: number,
): Promise<string> {
  // 1. 将 WebGL Canvas 内容转为 Image 对象，以便在 2D 环境中使用
  const img = new Image();
  img.src = sourceCanvas.toDataURL("image/png");
  await new Promise((resolve) => (img.onload = resolve));

  // 2. 创建一个临时的 2D Canvas 进行像素分析
  const processingCanvas = document.createElement("canvas");
  processingCanvas.width = sourceCanvas.width;
  processingCanvas.height = sourceCanvas.height;
  const ctx = processingCanvas.getContext("2d", { willReadFrequently: true });
  if (!ctx) return img.src;

  ctx.drawImage(img, 0, 0);
  const w = processingCanvas.width;
  const h = processingCanvas.height;
  const imgData = ctx.getImageData(0, 0, w, h);
  const data = imgData.data;

  // 3. 背景颜色解析
  let bgR = 255,
    bgG = 255,
    bgB = 255,
    isTransparent = false;
  if (backgroundColor === "transparent") {
    isTransparent = true;
  } else if (typeof backgroundColor === "number") {
    bgR = (backgroundColor >> 16) & 0xff;
    bgG = (backgroundColor >> 8) & 0xff;
    bgB = backgroundColor & 0xff;
  }

  // 4. 扫描边界
  let minX = w,
    minY = h,
    maxX = 0,
    maxY = 0;
  let foundAny = false;

  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const i = (y * w + x) * 4;
      const a = data[i + 3];
      let isContent = false;

      if (isTransparent) {
        if (a > 10) isContent = true;
      } else {
        const r = data[i],
          g = data[i + 1],
          b = data[i + 2];
        const tolerance = 10;
        if (
          Math.abs(r - bgR) > tolerance ||
          Math.abs(g - bgG) > tolerance ||
          Math.abs(b - bgB) > tolerance
        ) {
          isContent = true;
        }
      }

      if (isContent) {
        if (x < minX) minX = x;
        if (x > maxX) maxX = x;
        if (y < minY) minY = y;
        if (y > maxY) maxY = y;
        foundAny = true;
      }
    }
  }

  if (!foundAny) return img.src;

  // 5. 计算裁剪范围 (带微量 Padding)
  minX = Math.max(0, minX - padding);
  minY = Math.max(0, minY - padding);
  maxX = Math.min(w, maxX + padding);
  maxY = Math.min(h, maxY + padding);
  const cropW = maxX - minX;
  const cropH = maxY - minY;

  // 6. 创建最终输出 Canvas (用户要求的尺寸)
  const outCanvas = document.createElement("canvas");
  outCanvas.width = targetWidth;
  outCanvas.height = targetHeight;
  const outCtx = outCanvas.getContext("2d");
  if (!outCtx) return img.src;

  // 填充背景色
  if (!isTransparent) {
    outCtx.fillStyle = `rgb(${bgR}, ${bgG}, ${bgB})`;
    outCtx.fillRect(0, 0, targetWidth, targetHeight);
  }

  // 7. 计算缩放比例 (Contain 模式)
  const scale = Math.min(targetWidth / cropW, targetHeight / cropH);
  const finalW = cropW * scale;
  const finalH = cropH * scale;
  const offX = (targetWidth - finalW) / 2;
  const offY = (targetHeight - finalH) / 2;

  // 8. 绘制结果
  // 使用图像平滑处理
  outCtx.imageSmoothingEnabled = true;
  outCtx.imageSmoothingQuality = "high";
  outCtx.drawImage(
    processingCanvas,
    minX,
    minY,
    cropW,
    cropH,
    offX,
    offY,
    finalW,
    finalH,
  );

  return outCanvas.toDataURL("image/png");
}

export default processAutoClipping;
