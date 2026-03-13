import type { SearchParam } from "../util/search-test-utils";

/**
 * 基于 Java NeteaseOrderController 解析逻辑生成的 Schema。
 * - search: { fuzzy: true } 对应 Java 中的 Operator.LIKE (field~:value)
 * - search: { fuzzy: false } 对应 Java 中的 Operator.EQ (field:value)
 * - sort: true 对应 Spring Data Pageable 支持的排序字段
 */
export const neteaseOrderSchema = {
  // 基础 ID 字段
  id: { sort: true },

  // 状态类字段（后端 convertValue 支持枚举/布尔转换，通常为精确匹配）
  internalStatus: { search: { fuzzy: false }, sort: true },
  refundStatus: { search: { fuzzy: false }, sort: true },
  officialChannel: { search: { fuzzy: false }, sort: true },

  // ID/UID 字符串字段（通常需要支持模糊搜索）
  appOrderId: { search: { fuzzy: true }, sort: true },
  appUid: { search: { fuzzy: true }, sort: true },

  // 数字类字段（后端 convertValue 会解析为 Long/Integer，需精确匹配）
  appOrderIdInt: { search: { fuzzy: false }, sort: true },
  appUidInt: { search: { fuzzy: false }, sort: true },
  price: { search: { fuzzy: false }, sort: true },
  discount: { search: { fuzzy: false }, sort: true },
  point: { search: { fuzzy: false }, sort: true },

  // 文本类字段（支持模糊搜索）
  productName: { search: { fuzzy: true }, sort: true },

  // 时间戳类（后端 convertValue 目前逻辑按 Long 处理，通常用于排序或精确查）
  shipTime: { sort: true },
  shipTimeMs: { sort: true },
  refundInTimeMs: { sort: true },

  // 嵌套对象字段（对应 resolvePath 方法处理 "product.id"）
  'product.id': { search: { fuzzy: false }, sort: true },
} as const;

/**
 * 自动推导出的查询参数类型
 */
export type OrderQueryParams = SearchParam<typeof neteaseOrderSchema>;