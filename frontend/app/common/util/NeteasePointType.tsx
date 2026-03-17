import { CircleQuestionMark } from "lucide-react";
import type React from "react";

const NeteasePointType: React.FC<{ point: string | null | undefined }> = ({
  point,
}) => {
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
        <div className="my-auto">
          <CircleQuestionMark />
        </div>
      );
  }
};

export default NeteasePointType;
