import React, { useEffect, useRef, useMemo } from "react";
import { createNoise3D, type NoiseFunction3D } from "simplex-noise";
import alea from "alea";

// --- 1. 类型定义 ---

interface Range {
  min: number;
  max: number;
}

interface MeshGradientProps extends React.HTMLAttributes<HTMLDivElement> {
  id: string; // 唯一标识，作为随机种子
  count?: number; // 光球数量
  theme?: "light" | "dark" | "system";

  // 范围控制
  speedRange?: Range; // 运动速度范围 (推荐 0.001 - 0.01)
  radiusRange?: Range; // 半径范围 (%)
  opacityRange?: Range; // 透明度范围 (0-1)

  // HSL 颜色分量范围
  hueRange?: Range; // 0-360
  saturationRange?: Range; // 0-100
  lightnessRange?: Range; // 0-100

  blurAmount?: string; // 背景模糊程度，如 "60px"
}

// --- 2. 主组件 ---

const MeshGradientBackground: React.FC<MeshGradientProps> = ({
  id,
  count = 6,
  theme = "dark",
  speedRange = { min: 0.002, max: 0.005 },
  radiusRange = { min: 30, max: 60 },
  opacityRange = { min: 0.2, max: 0.5 },
  hueRange = { min: 0, max: 360 },
  saturationRange = { min: 50, max: 80 },
  lightnessRange = { min: 40, max: 60 },
  blurAmount = "0px",
  className,
  style,
  children,
  ...props
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const requestRef = useRef<number | null>(null);
  const timeRef = useRef<number>(0);

  // 初始化噪声函数：基于 id 生成稳定的种子
  const noise: NoiseFunction3D = useMemo(() => {
    const prng = alea(id); // 使用 alea 包装种子字符串
    return createNoise3D(prng);
  }, [id]);

  // 辅助函数：将噪声 (-1 ~ 1) 映射到指定范围
  const mapRange = (val: number, range: Range) => {
    const normalized = (val + 1) / 2; // 0 ~ 1
    return range.min + normalized * (range.max - range.min);
  };

  const animate = () => {
    if (!containerRef.current) return;

    timeRef.current += 1;
    const t = timeRef.current;
    const gradients: string[] = [];

    // 根据主题微调全局透明度
    const themeAlphaMultiplier = theme === "light" ? 0.6 : 1.0;

    for (let i = 0; i < count; i++) {
      /**
       * 使用噪声的不同“层” (Z轴位移) 来确保各个属性之间不完全同步
       * i * 0.1 等偏移量是为了让不同的光球在噪声场中处于不同位置
       */

      // 1. 速度感应 (每个球的独立时间流速)
      const individualSpeed = mapRange(noise(i, 0, t * 0.0005), speedRange);
      const step = t * individualSpeed;

      // 2. 位置 (X, Y)
      const posX = mapRange(noise(i + 10, 10, step), { min: -20, max: 120 });
      const posY = mapRange(noise(i + 20, 20, step), { min: -20, max: 120 });

      // 3. 颜色 (HSL)
      const h = mapRange(noise(i + 30, 30, step * 0.5), hueRange);
      const s = mapRange(noise(i + 40, 40, step * 0.5), saturationRange);
      const l = mapRange(noise(i + 50, 50, step * 0.5), lightnessRange);

      // 4. 透明度与半径
      const a =
        mapRange(noise(i + 60, 60, step), opacityRange) * themeAlphaMultiplier;
      const r = mapRange(noise(i + 70, 70, step), radiusRange);

      gradients.push(
        `radial-gradient(at ${posX.toFixed(1)}% ${posY.toFixed(1)}%, ` +
          `hsla(${h.toFixed(0)}, ${s.toFixed(0)}%, ${l.toFixed(0)}%, ${a.toFixed(2)}) 0%, ` +
          `transparent ${r.toFixed(0)}%)`,
      );
    }

    // 直接操作 DOM 样式，避免 React 重渲染开销
    containerRef.current.style.backgroundImage = gradients.join(", ");
    requestRef.current = requestAnimationFrame(animate);
  };

  useEffect(() => {
    requestRef.current = requestAnimationFrame(animate);
    return () => {
      if (requestRef.current !== null) {
        cancelAnimationFrame(requestRef.current);
      }
    };
    // 当核心配置改变时，重启动画循环
  }, [id, count, theme, speedRange, hueRange, saturationRange, lightnessRange]);

  return (
    <div
      ref={containerRef}
      className={className}
      style={{
        width: "100%",
        height: "100%",
        position: "relative",
        overflow: "hidden",
        willChange: "background-image",
        // 这里的 filter 决定了光球是“清晰的”还是“弥散的”
        filter: blurAmount !== "0px" ? `blur(${blurAmount})` : undefined,
        ...style,
      }}
      {...props}
    >
      {/* 确保子元素在渐变之上 */}
      <div style={{ position: "relative", zIndex: 1 }}>{children}</div>
    </div>
  );
};

export default MeshGradientBackground;
