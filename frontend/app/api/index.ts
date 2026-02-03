// src/api/index.ts
import apiClient from "@/lib/axios"; // 你之前写的自定义 axios 实例
import { DefaultApi } from "./generated/api"; // 指向生成的接口文件
import { Configuration } from "./generated/configuration";

/**
 * 基础配置
 * 你可以在这里处理特殊的 basePath，
 * 或者直接留空，因为我们在 apiClient 中已经定义了 baseURL
 */
const apiConfig = new Configuration({
    basePath: import.meta.env.VITE_API_URL || "http://localhost:8080", 
});

export const getBackendBaseUrl = (): string => {
    return import.meta.env.VITE_API_URL || "http://localhost:8080";
};

export const getThumbnailUrlForTemplate = (templateId: string, angle: 0 | 1 | 2 | 3): string => {
    return `${getBackendBaseUrl()}/api/v1/templates/${templateId}/thumbnail?angle=${angle}`;
}

/**
 * 导出单例接口实例
 * 这里的注入顺序：apiConfig, basePath, axiosInstance
 */
export const IDUNN_API = new DefaultApi(apiConfig, undefined, apiClient);