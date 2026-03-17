import React from "react";
import { User, Check, Users } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox"; // 确保你安装了 shadcn checkbox
import { useAuth } from "../auth/auth-provider";
import { Badge } from "@/components/ui/badge"; // 建议使用 shadcn 的 Badge，如果没有可替换为 span
import { useIntlayer } from "react-intlayer";

interface CreatorInfo {
  uuid?: string;
  name?: string;
}

interface CreatorPickerProps {
  creators: CreatorInfo[];
  value?: string | string[]; // 单选为 string (uuid), 多选为 string[]
  onValueChange?: (value: any) => void;
  multiple?: boolean;
  className?: string;
  title?: string; // 可选的列表标题
}

export const CreatorPicker = ({
  creators,
  value,
  onValueChange,
  multiple = false,
  className,
  title,
}: CreatorPickerProps) => {
  const { isAuthenticated, user } = useAuth();
  const { creatorPicker } = useIntlayer("creatorpicker");

  // 处理显示文本的逻辑
  const getDisplayName = (creator: CreatorInfo) => {
    if (creator.name) return creator.name;
    if (creator.uuid) return `ID: ${creator.uuid.substring(0, 8)}`;
    return "Unknown User";
  };

  const handleSelect = (uuid: string) => {
    if (!onValueChange) return;

    if (multiple) {
      const currentValues = Array.isArray(value) ? value : [];
      if (currentValues.includes(uuid)) {
        onValueChange(currentValues.filter((v) => v !== uuid));
      } else {
        onValueChange([...currentValues, uuid]);
      }
    } else {
      // 单选模式：点击已选中的则取消选择，或者保持选中
      onValueChange(value === uuid ? undefined : uuid);
    }
  };

  const isSelected = (uuid: string) => {
    if (multiple && Array.isArray(value)) {
      return value.includes(uuid);
    }
    return value === uuid;
  };

  return (
    <div className={cn("space-y-1", className)}>
      {/* 列表头部 (可选) */}
      {title && (
        <div className="px-2 py-1.5 text-xs font-semibold text-muted-foreground flex items-center">
          <Users className="mr-2 h-3.5 w-3.5" />
          {title}
        </div>
      )}

      <div className="flex flex-col gap-1">
        {creators.length === 0 ? (
          <div className="px-4 py-8 text-center text-sm text-muted-foreground italic">
            No creators found
          </div>
        ) : (
          creators.map((creator) => {
            // debugger;
            const uuid = creator.uuid || "";
            const selected = isSelected(uuid);
            const isCurrentUser =
              isAuthenticated &&
              user &&
              user?.username &&
              creator.name?.toLowerCase() ===
                (user.username as string).toLowerCase();

            return (
              <Button
                key={uuid}
                variant={selected && !multiple ? "secondary" : "ghost"}
                size="sm"
                className={cn(
                  "w-full justify-start h-8 px-2 font-normal transition-all relative",
                  selected &&
                    !multiple &&
                    "bg-accent text-accent-foreground font-medium",
                  isCurrentUser &&
                    "bg-primary/5 hover:bg-primary/10 border-primary/10",
                  "group",
                )}
                onClick={() => handleSelect(uuid)}
              >
                {/* 多选模式下的 Checkbox */}
                {multiple ? (
                  <div className="mr-2 shrink-0">
                    <Checkbox
                      checked={selected}
                      onCheckedChange={() => handleSelect(uuid)}
                      className="h-4 w-4"
                    />
                  </div>
                ) : (
                  // 单选模式下的图标占位
                  <div className="w-4 mr-2 flex justify-center shrink-0">
                    <User
                      className={cn(
                        "h-4 w-4 transition-colors",
                        selected
                          ? "text-primary"
                          : "text-muted-foreground/60 group-hover:text-primary/70",
                        isCurrentUser && "text-primary",
                      )}
                    />
                  </div>
                )}

                <span
                  className={cn(
                    "truncate flex-1 text-left flex items-center gap-2",
                    isCurrentUser && "font-medium",
                  )}
                >
                  {getDisplayName(creator)}
                  {isCurrentUser && (
                    <span className="text-[10px] bg-primary/10 text-primary px-1.5 py-0.5 rounded-full font-bold uppercase tracking-wider">
                      {creatorPicker.meLabel}
                    </span>
                  )}
                </span>

                {/* 单选模式下的勾选标记 */}
                {selected && !multiple && (
                  <Check className="ml-auto h-3 w-3 opacity-60" />
                )}
              </Button>
            );
          })
        )}
      </div>
    </div>
  );
};
