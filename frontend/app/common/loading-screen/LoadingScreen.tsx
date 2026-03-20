// src/components/loading-screen.tsx
import * as React from "react";
import { Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";

interface LoadingScreenProps extends React.HTMLAttributes<HTMLDivElement> {
  delay?: number; // 初始显示的延迟（毫秒），防止页面秒开时的闪烁
  isLoading?: boolean; // 加载状态
  message?: string; // 自定义加载消息
}

export function LoadingScreen({
  delay = 100,
  isLoading = true,
  message = "正在为您的青春与创意加载资源...",
  className,
  ...props
}: LoadingScreenProps) {
  // show 控制组件是否渲染在 DOM 中
  const [show, setShow] = React.useState(false);
  // isFadingOut 控制组件退出时的透明度过渡
  const [isFadingOut, setIsFadingOut] = React.useState(false);

  React.useEffect(() => {
    let mountTimer: NodeJS.Timeout;
    let unmountTimer: NodeJS.Timeout;

    if (isLoading) {
      // 延迟挂载，防止极短的加载导致屏幕闪烁
      mountTimer = setTimeout(() => {
        setShow(true);
        setIsFadingOut(false);
      }, delay);
    } else {
      // 触发淡出过渡
      setIsFadingOut(true);
      // 等待过渡动画完成 (500ms) 后，将组件从 DOM 移除
      unmountTimer = setTimeout(() => {
        setShow(false);
      }, 500);
    }

    return () => {
      clearTimeout(mountTimer);
      clearTimeout(unmountTimer);
    };
  }, [isLoading, delay]);

  // 如果不显示且不在淡出过程中，直接不渲染
  if (!show) return null;

  return (
    <div
      className={cn(
        // 基础布局：固定定位全屏，背景模糊
        "fixed inset-0 z-50 flex flex-col items-center justify-center gap-10 p-6",
        "bg-background/95 backdrop-blur-sm dark:bg-background/90",
        // 退出过渡动画：根据 isFadingOut 状态切换透明度
        "transition-opacity duration-500 ease-in-out",
        isFadingOut ? "opacity-0" : "opacity-100",
        className,
      )}
      {...props}
    >
      {/* Logo 容器 - 带有缩放和淡入效果 */}
      <div className="w-32 h-32 md:w-56 md:h-56 animate-in zoom-in-90 fade-in duration-700 ease-out">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 512 512"
          fill="none"
          className="w-full h-full"
        >
          <g transform="translate(0, 20)">
            {/* 紫色几何体 - 使用 animationDelay 实现交错入场，animationFillMode: 'both' 确保延迟期间保持透明 */}
            <path
              d="M 256 140 L 360 190 L 256 240 L 152 190 Z"
              className="fill-primary/30 animate-in fade-in duration-700"
              style={{ animationDelay: "100ms", animationFillMode: "both" }}
            />
            <path
              d="M 152 190 L 256 240 L 256 360 L 152 310 Z"
              className="fill-primary animate-in fade-in duration-700"
              style={{ animationDelay: "200ms", animationFillMode: "both" }}
            />
            <path
              d="M 256 240 L 360 190 L 360 310 L 256 360 Z"
              className="fill-primary/70 animate-in fade-in duration-700"
              style={{ animationDelay: "300ms", animationFillMode: "both" }}
            />
          </g>

          {/* 绿色叶子/苹果 - 淡入后应用 Tailwind 自带的 pulse 持续呼吸动画 */}
          <path
            d="M 256 165 Q 210 110 256 75 Q 302 110 256 165"
            className="fill-secondary animate-in fade-in zoom-in duration-700 animate-pulse"
            style={{ animationDelay: "500ms", animationFillMode: "both" }}
          />

          {/* 抽象边角 */}
          <path
            d="M 100 160 V 100 H 160"
            strokeWidth="6"
            strokeLinecap="round"
            strokeLinejoin="round"
            className="stroke-muted-foreground/40 dark:stroke-muted-foreground/60 animate-in fade-in duration-1000"
            style={{ animationDelay: "600ms", animationFillMode: "both" }}
          />
          <path
            d="M 352 412 V 352 H 412"
            strokeWidth="6"
            strokeLinecap="round"
            strokeLinejoin="round"
            transform="rotate(180 382 382)"
            className="stroke-muted-foreground/40 dark:stroke-muted-foreground/60 animate-in fade-in duration-1000"
            style={{ animationDelay: "600ms", animationFillMode: "both" }}
          />
        </svg>
      </div>

      {/* 网站名称和加载文本容器 - 带有从底部滑入和淡入的效果 */}
      <div
        className="flex flex-col items-center gap-6 text-center animate-in slide-in-from-bottom-6 fade-in duration-700 ease-out"
        style={{ animationDelay: "400ms", animationFillMode: "both" }}
      >
        <h1 className="text-4xl font-extrabold tracking-tighter text-foreground md:text-6xl">
          <span className="font-light text-muted-foreground">Idunn</span>
          <span>Templates</span>
        </h1>

        <div className="flex flex-col items-center gap-4">
          <div className="flex items-center gap-3">
            {/* shadcn 标准加载图标，自带 spin 动画 */}
            <Loader2 className="h-6 w-6 animate-spin text-primary" />

            {/* 提示文本，使用 pulse 让它看起来在加载中 */}
            <p className="text-xl text-muted-foreground/80 animate-pulse">
              {message}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
