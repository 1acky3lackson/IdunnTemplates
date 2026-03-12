// routes/ProjectsRoute.tsx
import React from 'react';
import axios from 'axios';
import { ProjectManagerPage, projectSchema, type ProjectApi } from '~/common/project/ProjectManager';
import { buildSearchString, buildSortString } from '~/common/util/search-test-utils';
import { IDUNN_API } from '~/api';
import { requireAllFields, requireAllFieldsStrict } from '~/common/util/require-all-fields';
import { deepNullToUndefined } from '~/common/util/null-to-undefined';

const API_BASE = '/api/v1';

// 实现 ProjectApi 接口
const api: ProjectApi = {
  /**
   * 获取项目分页列表
   * @param page 页码（从0开始）
   * @param criteria 搜索条件对象（由 Waterfall 自动生成）
   */
  fetchProjects: async (page, criteria) => {
    // 将 criteria 转换为后端所需的 search 字符串
    const search = buildSearchString(criteria, projectSchema);
    // 如果有排序条件，转换为排序字符串（如 "id:desc,name:asc"）
    const sort = criteria.sort ? buildSortString(criteria.sort) : undefined;

    const response = await IDUNN_API.apiV1CommercialProjectsGet(
        buildSearchString(criteria, projectSchema),
        page,
        20,
        buildSortString(criteria.sort)
    )
    return response.data;
  },

  /**
   * 创建新项目
   */
  createProject: async (data) => {
    const response = await IDUNN_API.apiV1CommercialProjectsPost(requireAllFields(
        data
    ))
    return response.data;
  },

  /**
   * 更新项目
   */
  updateProject: async (id, data) => {
    const response = await IDUNN_API.apiV1CommercialProjectsIdPut(
        `${id}`,
        data as any
    )
    return response.data;
  },
};

/**
 * 项目管理路由页面
 * 直接渲染 ProjectManagerPage，并传入 api 实现
 */
export default function ProjectsRoute() {
  return (
    <div className="p-6">
      <ProjectManagerPage api={api} />
    </div>
  );
}