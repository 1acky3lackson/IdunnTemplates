// src/api/index.ts
import apiClient from "@/lib/axios"; // 你之前写的自定义 axios 实例
import { DefaultApi } from "./generated/api"; // 指向生成的接口文件
import { Configuration } from "./generated/configuration";
import { env } from "process";

export const getBackendBaseUrl = (): string => {
    const envBackendUrl = import.meta.env.VITE_API_URL;
    if(envBackendUrl === "VITE_PROXY") {
        return "";
    }
    if (envBackendUrl) {
        return envBackendUrl;
    }
    return envBackendUrl || "http://localhost:8080";
};

/**
 * 基础配置
 * 你可以在这里处理特殊的 basePath，
 * 或者直接留空，因为我们在 apiClient 中已经定义了 baseURL
 */
const apiConfig = new Configuration({
    basePath: getBackendBaseUrl(), 
});

export const getThumbnailUrlForTemplate = (templateId: string, angle: 0 | 1 | 2 | 3): string => {
    return `${getBackendBaseUrl()}/api/v1/templates/${templateId}/thumbnail?angle=${angle}`;
}

// 添加一个响应拦截器或使用内置的缓存谓词
// apiClient.interceptors.request.use((config) => {
//     // 示例：如果路径包含 /api/v1/templates，缓存 5 分钟 (300,000ms)
//     if (config.url?.includes('/api/v1/templates')) {
//         config.cache = {
//             ttl: 1000 * 60 * 5, // 5分钟
//             interpretHeader: false, // 忽略后端的 Cache-Control，由前端强制控制
//         };
//     }
    
//     // 示例：如果路径包含 /api/v1/categories，缓存 1 小时
//     if (config.url?.includes('/api/v1/categories')) {
//         config.cache = {
//             ttl: 1000 * 60 * 60,
//         };
//     }

//     return config;
// });

/**
 * 导出单例接口实例
 * 这里的注入顺序：apiConfig, basePath, axiosInstance
 */
export const IDUNN_API = new DefaultApi(apiConfig, undefined, apiClient);