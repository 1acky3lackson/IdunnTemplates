// src/components/logo-icon.tsx
import type { SVGProps } from "react";

/**
 * IdunnTemplates Logo 组件
 * 忠实于原始 SVG 结构和 OKLCH 颜色，
 * 并应用了 Tailwind CSS 过渡和暗模式优化。
 */
export function LogoIcon({ className, ...props }: SVGProps<SVGSVGElement>) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 512 512"
      fill="none"
      className={className}
      {...props}
    >
      <g transform="translate(0, 20)">
        {/* 紫色几何体 - 亮部 */}
        <path
          d="M 256 140 L 360 190 L 256 240 L 152 190 Z"
          className="fill-primary/30 transition-colors duration-300"
        />
        {/* 紫色几何体 - 暗部 */}
        <path
          d="M 152 190 L 256 240 L 256 360 L 152 310 Z"
          className="fill-primary transition-colors duration-300"
        />
        {/* 紫色几何体 - 中间部 */}
        <path
          d="M 256 240 L 360 190 L 360 310 L 256 360 Z"
          className="fill-primary/70 transition-colors duration-300"
        />
      </g>

      {/* 绿色叶子/苹果 */}
      <path
        d="M 256 165 Q 210 110 256 75 Q 302 110 256 165"
        className="fill-secondary transition-colors duration-300"
      />

      {/* 抽象边角 */}
      <path
        d="M 100 160 V 100 H 160"
        strokeWidth="6"
        strokeLinecap="round"
        strokeLinejoin="round"
        // 暗模式下稍微加深边角描边的对比度
        className="stroke-muted-foreground/40 dark:stroke-muted-foreground/60 transition-colors duration-300"
      />
      <path
        d="M 352 412 V 352 H 412"
        strokeWidth="6"
        strokeLinecap="round"
        strokeLinejoin="round"
        transform="rotate(180 382 382)"
        // 暗模式下稍微加深边角描边的对比度
        className="stroke-muted-foreground/40 dark:stroke-muted-foreground/60 transition-colors duration-300"
      />
    </svg>
  );
}
