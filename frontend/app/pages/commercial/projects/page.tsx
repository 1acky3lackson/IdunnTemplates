// routes/ProjectsRoute.tsx
import React from "react";
import {
  ProjectManagerPage,
  projectSchema,
  type ProjectApi,
} from "~/common/project/ProjectManager";
import {
  buildSearchString,
  buildSortString,
} from "~/common/util/search-test-utils";
import { IDUNN_API } from "~/api";
import { deepNullToUndefined } from "~/common/util/null-to-undefined";
import apiClient from "@/lib/axios";

const API_BASE = "/api/v1";

function normalizeProjectPayload(data: Record<string, any>) {
  return {
    name: data.name,
    displayName: data.displayName,
    description: data.description?.trim() ? data.description : null,
    pathName: data.pathName,
    kind: data.kind,
    modelKind: data.modelKind?.trim() ? data.modelKind : null,
    worldId: data.worldId ?? null,
    minX: data.minX ?? null,
    minY: data.minY ?? null,
    minZ: data.minZ ?? null,
    maxX: data.maxX ?? null,
    maxY: data.maxY ?? null,
    maxZ: data.maxZ ?? null,
    tpX: data.tpX ?? null,
    tpY: data.tpY ?? null,
    tpZ: data.tpZ ?? null,
    tpYaw: data.tpYaw ?? null,
    tpPitch: data.tpPitch ?? null,
    parentProjectId: data.parentProjectId ?? null,
  };
}

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
      buildSortString(criteria.sort),
    );
    return response.data;
  },

  /**
   * 创建新项目
   */
  createProject: async (data) => {
    const response = await apiClient.post(
      "/api/v1/commercial/projects",
      normalizeProjectPayload(data),
    );
    return response.data;
  },

  /**
   * 更新项目
   */
  updateProject: async (id, data) => {
    const response = await apiClient.put(
      `/api/v1/commercial/projects/${id}`,
      normalizeProjectPayload(data as Record<string, any>),
    );
    return response.data;
  },
};

/**
 * 项目管理路由页面
 * 直接渲染 ProjectManagerPage，并传入 api 实现
 */
export default function ProjectsRoute() {
  document.title = "建造项目";
  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">建造项目</h1>
      <ProjectManagerPage api={api} />
    </div>
  );
}
