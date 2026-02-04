import { Locales, t, type Dictionary } from "intlayer";

const axiosContent = {
    key: "axios",
    content: {
        error: {
            401: t({
                [Locales.ENGLISH]: "Unauthorized, please log in again.",
                [Locales.CHINESE]: "未授权，请重新登录。",
                [Locales.CHINESE_TRADITIONAL]: "未授權，請重新登入。",
            }),
            403: t({
                [Locales.ENGLISH]: "Access denied.",
                [Locales.CHINESE]: "拒绝访问。",
                [Locales.CHINESE_TRADITIONAL]: "拒絕訪問。",
            }),
            500: t({
                [Locales.ENGLISH]: "Internal server error.",
                [Locales.CHINESE]: "服务器内部错误。",
                [Locales.CHINESE_TRADITIONAL]: "伺服器內部錯誤。",
            }),
            connectionError: t({
                [Locales.ENGLISH]: "Connection error: ",
                [Locales.CHINESE]: "连接错误: ",
                [Locales.CHINESE_TRADITIONAL]: "連接錯誤: ",
            }),
            networkError: t({
                [Locales.ENGLISH]: "Network error, please check your connection.",
                [Locales.CHINESE]: "网络异常，请检查您的网络连接。",
                [Locales.CHINESE_TRADITIONAL]: "網絡異常，請檢查您的網絡連接。",
            }),
        }
    },
} satisfies Dictionary;

export default axiosContent;
