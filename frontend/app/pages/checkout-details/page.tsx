// 引入通用表格组件和 API 客户端
import CheckoutDetails from "~/common/checkout-details/CheckoutDetails";

// ---------- 辅助工具 ----------

// 状态标签颜色映射 (根据你实际的枚举值进行调整)
const statusColorMap: Record<string, string> = {
  CREATED: "bg-gray-500 hover:bg-gray-600",
  CONFIRMED: "bg-blue-500 hover:bg-blue-600",
  RELEASED: "bg-green-500 hover:bg-green-600",
  FINISHED: "bg-emerald-600 hover:bg-emerald-700",
  REFUNDED: "bg-red-500 hover:bg-red-600",
};

// 角色标签颜色映射
const roleColorMap: Record<string, string> = {
  CREATOR: "bg-purple-100 text-purple-800 border-purple-200",
  PLATFORM: "bg-blue-100 text-blue-800 border-blue-200",
  AGENCY: "bg-orange-100 text-orange-800 border-orange-200",
};

// 时间格式化工具
const formatTime = (ms?: number | null) => {
  if (!ms) return "-";
  return new Date(ms).toLocaleString();
};

// 金额格式化工具 (假设后端传来的直接是元或者具体数值)
const formatMoney = (amount?: number | null) => {
  if (amount === undefined || amount === null) return "-";
  return `¥${amount.toFixed(2)}`;
};

// ---------- 主页面组件 ----------

export default function CheckoutDetailsPage() {
  return <CheckoutDetails />;
}
