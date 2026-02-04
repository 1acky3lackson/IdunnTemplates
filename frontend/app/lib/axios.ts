import axios, { type AxiosInstance, AxiosError } from "axios";
import { toast } from "sonner"
// 如果你需要根据语言环境通知后端，可以从 intlayer 或本地存储获取 locale
import { getLocale, getIntlayer } from "intlayer"; 
import axoiosContent from "./axios.content";

const apiClient: AxiosInstance = axios.create({
    timeout: 10000, // 10秒超时
    headers: {
        "Content-Type": "application/json",
    },
    // 核心改动：允许跨域携带 Cookie，并允许浏览器自动处理 Set-Cookie
    withCredentials: true,
});

// 请求拦截器
apiClient.interceptors.request.use(
    (config) => {
        // 1. 从 localStorage 获取 Token 并添加到 Header
        const token = localStorage.getItem("auth_token");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        // cookie 中 添加 auth_token = token
        const cookieToken = document.cookie += `; auth_token=${token}; path=/;`;

        // 2. 这里的辅助逻辑：告诉后端当前前端的语言环境
        // 假设你把语言存在了 cookie 或 html lang 标签中
        const currentLocale = document.documentElement.lang || "en";
        config.headers["Accept-Language"] = currentLocale;

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
apiClient.interceptors.response.use(
    (response) => {
        // 如果后端返回的是 { data: ..., status: 200 }，直接返回 data 部分
        return response;
    },
    (error: AxiosError) => {
        const content = getIntlayer("axios", undefined, []);
        // 统一处理错误
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    // Token 过期，可以在这里跳转到登录页或清空状态
                    // debugger;
                    localStorage.removeItem("auth_token");
                    window.location.href = "/login";
                    toast.error("未授权，请重新登录");
                    console.error("未授权，请重新登录");
                    break;
                case 403:
                    toast.error("拒绝访问");
                    console.error("拒绝访问");
                    break;
                case 500:
                    toast.error("服务器内部错误");
                    console.error("服务器内部错误");
                    break;
                default:
                    toast.error("连接错误: " + error.response.status);
                    console.error(`连接错误: ${error.response.status}`);
            }
        } else {
            console.error("网络异常，请检查您的网络连接");
        }
        return Promise.reject(error);
    }
);

export default apiClient;