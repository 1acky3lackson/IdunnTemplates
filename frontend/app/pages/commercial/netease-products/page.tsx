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
  

  return (
    <div className="p-6">
      <NeteaseProductManagerPage title="管理网易上架商品"/>
    </div>
  );
}