export const NETEASE_RAW_VIRTUAL_POINT_TYPE = "付费钻石";
export const NETEASE_DISPLAY_VIRTUAL_POINT_TYPE = "虚拟点数";

export function toPointTypeDisplay(value?: string | null) {
  if (!value) return value ?? "";
  return value.trim() === NETEASE_RAW_VIRTUAL_POINT_TYPE
    ? NETEASE_DISPLAY_VIRTUAL_POINT_TYPE
    : value;
}

export function fromPointTypeDisplay(value?: string | null) {
  if (!value) return value ?? "";
  return value.trim() === NETEASE_DISPLAY_VIRTUAL_POINT_TYPE
    ? NETEASE_RAW_VIRTUAL_POINT_TYPE
    : value;
}
