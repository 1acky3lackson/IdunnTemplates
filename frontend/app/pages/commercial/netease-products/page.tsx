// routes/NeteaseProductsRoute.tsx
import React from 'react';
import {
  NeteaseProductManagerPage,
  type ProductApi,
  type NeteaseProduct,
  type ProductPageResponse,
  type NeteaseProductStatus,
  productSchema,
} from '~/common/netease-product/NeteaseProductManager';
import { type ProjectApi, projectSchema } from '~/common/project/ProjectManager';
import { buildSearchString, buildSortString } from '~/common/util/search-test-utils';
import { deepNullToUndefined } from '~/common/util/null-to-undefined';
import { IDUNN_API } from '~/api';

/**
 * 产品管理路由页面
 */
export default function NeteaseProductsRoute() {
  // 实现 ProductApi
  const productApi: ProductApi = {
    fetchProducts: async (page, criteria) => {
      const search = buildSearchString(criteria, productSchema);
      const sort = criteria.sort ? buildSortString(criteria.sort) : undefined;
      const response = await IDUNN_API.apiV1CommercialNeteaseProductsGet(
        search,
        page,
        20,
        sort
      );
      // 将返回的数据转换为组件期望的类型
      const rawPage = deepNullToUndefined(response.data);
      return {
        ...rawPage,
        content: rawPage.content.map((item: any) => convertToNeteaseProduct(item)),
      } as ProductPageResponse;
    },

    updateProduct: async (id, data) => {
      const response = await IDUNN_API.apiV1CommercialNeteaseProductsIdPut(
        id,
        data
      );
      const raw = deepNullToUndefined(response.data);
      return convertToNeteaseProduct(raw);
    },
  };

  // 辅助函数：将 API 返回的原始数据转换为组件所需的 NeteaseProduct 类型
  const convertToNeteaseProduct = (raw: any): NeteaseProduct => ({
    id: raw.id,
    itemId: raw.itemId,
    itemName: raw.itemName,
    internalStatus: raw.internalStatus as NeteaseProductStatus, // 强制转换枚举
    project: raw.project,
    price: raw.price,
    priceType: raw.priceType,
    createTimeMs: raw.createTimeMs,
    updateTimeMs: raw.updateTimeMs,
    // 如果还有其他字段，请按需添加
  });

  // 实现 ProjectApi（仅 fetchProjects 被实际使用）
  const projectApi: ProjectApi = {
    fetchProjects: async (page, criteria) => {
      const search = buildSearchString(criteria, projectSchema);
      const sort = criteria.sort ? buildSortString(criteria.sort) : "";
      const response = await IDUNN_API.apiV1CommercialProjectsGet(
        search,
        page,
        20,
        sort
      );
      return response.data;
    },
    createProject: async () => {
      throw new Error('createProject is not supported in this context');
    },
    updateProject: async () => {
      throw new Error('updateProject is not supported in this context');
    },
  };

  return (
    <div className="p-6">
      <NeteaseProductManagerPage
        productApi={productApi}
        projectApi={projectApi}
      />
    </div>
  );
}