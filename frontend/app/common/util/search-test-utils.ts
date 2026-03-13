/**
 * 1. 基础配置定义
 */
export type FieldConfig = {
  search?: { fuzzy: boolean };
  sort?: boolean;
};

export type Schema = Record<string, FieldConfig>;

/**
 * 2. 核心修复：将搜索属性和特殊属性分开定义
 */

// 仅提取 search 相关的 key 及其对应的输入类型
export type SearchFields<T extends Schema> = {
  -readonly [K in keyof T as T[K] extends { search: { fuzzy: any } } ? K : never]?: 
    T[K] extends { search: { fuzzy: true } }
      ? string | { value: string; fuzzy?: boolean } 
      : string;
};

// 仅提取 sort 相关的 key
export type SortFields<T extends Schema> = Partial<Record<
  Extract<{ [K in keyof T]: T[K]['sort'] extends true ? K : never }[keyof T], string>, 
  'asc' | 'desc'
>>;

// 最终组合类型
export type SearchParam<T extends Schema> = SearchFields<T> & {
  sort?: SortFields<T>;
  rawSearch?: string;
};

/**
 * 3. 函数实现修复
 */
export function buildSearchString<T extends Schema>(
  params: SearchParam<T>,
  schema: T
): string {
  const conditions: string[] = [];
  
  // 使用 Record<string, any> 绕过严格的索引检查，因为我们内部有逻辑过滤
  const _params = params as Record<string, any>;

  for (const key in _params) {
    // 排除非搜索字段
    if (key === 'sort' || key === 'rawSearch') continue;
    
    const value = _params[key];
    if (value === undefined || value === null || value === '') continue;

    const fieldConfig = schema[key];
    // 验证 schema 中是否存在该搜索配置
    if (!fieldConfig || !fieldConfig.search) continue;

    let fieldValue: string;
    let isFuzzy = false;

    if (typeof value === 'string') {
      fieldValue = value;
      isFuzzy = false;
    } else if (typeof value === 'object' && value !== null && 'value' in value) {
      fieldValue = value.value;
      // 只有 schema 允许模糊查询时才应用 fuzzy 标志
      if (fieldConfig.search.fuzzy) {
        isFuzzy = !!value.fuzzy;
      }
    } else {
      continue;
    }

    if (!fieldValue) continue;
    const operator = isFuzzy ? '~' : '';
    conditions.push(`${key}${operator}:${fieldValue}`);
  }

  if (params.rawSearch?.trim()) {
    conditions.push(params.rawSearch.trim());
  }

  return conditions.join(',');
}

/**
 * 3. 新增：构建 Sort 字符串
 * 将 { id: 'desc', price: 'asc' } 转换为 "id:desc,price:asc"
 */
export function buildSortString<T extends Schema>(
  sort: SearchParam<T>['sort']
): string {
  if (!sort) return '';
  
  return Object.entries(sort)
    .filter(([_, dir]) => dir === 'asc' || dir === 'desc')
    .map(([field, dir]) => `${field},${dir}`) // 或者根据后端要求用 `${field},${dir}`
    .join(';');
}

// ========== 示例用法（无报错） ==========

const orderSchema = {
  internalStatus: { search: { fuzzy: false }, sort: true },
  'product.id': { search: { fuzzy: false }, sort: true },
  appOrderId: { search: { fuzzy: true }, sort: true },
  id: { sort: true }, // 仅排序
} as const;

type OrderQueryParams = SearchParam<typeof orderSchema>;

const query: OrderQueryParams = {
  internalStatus: 'ENTERED',
  appOrderId: { value: 'TEST', fuzzy: true },
  'product.id': '123',
  sort: {
    id: 'desc',
    internalStatus: 'asc'
    // price: 'asc' // 这里会报错，因为 orderSchema 里没写 price
  },
  rawSearch: 'extra:value'
};

const result = buildSearchString(query, orderSchema);
console.log(result);