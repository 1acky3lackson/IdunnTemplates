import { IDUNN_API } from "..";
import type { Template } from "../generated";


// 1. 获取该方法的所有参数类型，得到一个元组: [string?, boolean?, number?, ...]
type ApiFuncType = typeof IDUNN_API.apiV1TemplatesGet;
type ApiArgs = Parameters<ApiFuncType>;

// 2. 定义对象类型，利用索引访问对应的参数类型
// 这样做的好处是：不需要手动写 string | undefined，完全引用源头
export interface TemplateSearchParams {
    pathPrefix?: ApiArgs[0]; // 对应 pathPrefix
    locked?: ApiArgs[1]; // 对应 locked
    minWidth?: ApiArgs[2]; // 对应 minWidth
    maxWidth?: ApiArgs[3]; // 对应 maxWidth
    worldId?: ApiArgs[4]; // 对应 worldId
    page?: ApiArgs[5]; // 对应 page
    size?: ApiArgs[6]; // 对应 size
    sort?: ApiArgs[7]; // 对应 sort
    minLength?: ApiArgs[8]; // 对应 minLength
    maxLength?: ApiArgs[9]; // 对应 maxLength
    minHeight?: ApiArgs[10]; // 对应 minHeight
    maxHeight?: ApiArgs[11]; // 对应 maxHeight
    pathLike?: ApiArgs[12]; // 对应 pathLike
    nameLike?: ApiArgs[13]; // 对应 nameLike
    // options 通常不需要透传给业务层，所以可以忽略
}

// 3. 封装一个 Wrapper 函数
export const searchTemplatesObjectParam = (params: TemplateSearchParams) => {
    return IDUNN_API.apiV1TemplatesGet(
        params.pathPrefix,
        params.locked,
        params.minWidth,
        params.maxWidth,
        params.worldId,
        params.page ?? 0,   // 可以在这里做默认值处理
        params.size ?? 10,  // 可以在这里做默认值处理
        params.sort ?? 'metadata.creationTime,desc', // 默认按创建时间降序
        params.minLength,
        params.maxLength,
        params.minHeight,
        params.maxHeight,
        params.pathLike,
        params.nameLike,
    );
};

// 1. 定义排序方向
export type SortDirection = 'asc' | 'desc';

// 2. 定义允许排序的基础字段 (根据文档 Section A)
type BaseSortKeys = 'path' | 'name';

// 3. 定义允许排序的 Metadata 字段 (根据文档 Section B & C)
// 这里直接提取 TemplateMetadata 的所有 Key。
// 如果 Metadata 中包含不支持排序的字段（如复杂对象），可以使用 Pick<TemplateMetadata, 'width' | 'height' ...> 手动筛选
type MetadataSortKeys = keyof Template['metadata'];

// 4. 【关键】组合出最终的联合类型
// 结果等同于: "path" | "name" | "metadata.width" | "metadata.creationTime" | ...
export type TemplateSortField =
    | BaseSortKeys
    | `metadata.${MetadataSortKeys}`;

// 5. 定义排序对象的结构 (方便前端状态管理)
export interface SortOption {
    field: TemplateSortField;
    direction: SortDirection;
}

/**
 * 构建单个排序字符串
 * @example sortBuilder('path', 'asc') // => "path,asc"
 * @example sortBuilder('metadata.width', 'desc') // => "metadata.width,desc"
 */
export function templateSearchSortBuilder(field: TemplateSortField, direction: SortDirection = 'asc'): string {
    return `${field},${direction}`;
}

/**
 * 批量构建排序数组 (用于直接传递给 API 接口)
 * @param sorts 排序对象数组
 */
export function templateSearchBatchBuildSortParams(sorts: SortOption[]): string[] {
    return sorts.map(s => templateSearchSortBuilder(s.field, s.direction));
}