import React, { useState } from "react"
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip"
import { cn } from "@/lib/utils"

interface CopyableTextProps {
  value: string;
  children: React.ReactNode;
  className?: string;
}

export function Copyable({ value, children, className }: CopyableTextProps) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(value);
      setCopied(true);
      // 2秒后重置提示文字
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error("复制失败:", err);
    }
  };

  return (
    <TooltipProvider delayDuration={300}>
      <Tooltip>
        <TooltipTrigger asChild>
          <span
            onClick={handleCopy}
            className={cn(
              "cursor-pointer hover:text-primary transition-colors border-b border-dashed border-muted-foreground/50",
              className
            )}
          >
            {children}
          </span>
        </TooltipTrigger>
        <TooltipContent className="flex flex-col gap-1 px-3 py-2">
          <p className="text-xs font-mono bg-muted p-1 rounded break-all max-w-50 text-primary-foreground">
            {value}
          </p>
          <p className="text-[10px] text-muted-foreground font-medium italic">
            {copied ? "✅ 已复制到剪贴板" : "🖱️ 点击可复制"}
          </p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}