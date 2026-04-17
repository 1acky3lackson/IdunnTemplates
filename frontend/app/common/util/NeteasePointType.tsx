import { CircleQuestionMark } from "lucide-react";
import type React from "react";
import { toPointTypeDisplay } from "./point-type-display";

const NeteasePointType: React.FC<{ point: string | null | undefined }> = ({
  point,
}) => {
  const displayPoint = toPointTypeDisplay(point);

  switch (point) {
    case "diamond":
    case "diamonds":
    case "钻石":
      return (
        <div className="my-auto">
          <img src="/resources/diamond.png" className="h-[1em] w-auto" />
        </div>
      );
    default:
      return (
        <div className="my-auto inline-flex items-center gap-1">
          <CircleQuestionMark />
          <span>{displayPoint || "-"}</span>
        </div>
      );
  }
};

export default NeteasePointType;
