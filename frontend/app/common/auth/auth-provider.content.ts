import { Locales, t, type Dictionary } from "intlayer";

const authProviderContent = {
    key: "auth-provider",
    content: {
        // 登录成功
        loginSuccessTitle: t({
            [Locales.ENGLISH]: "Login Successful",
            [Locales.CHINESE]: "登录成功",
            [Locales.CHINESE_TRADITIONAL]: "登入成功",
        }),
        loginSuccessDesc: t({
            [Locales.ENGLISH]: "Welcome back",
            [Locales.CHINESE]: "欢迎回来",
            [Locales.CHINESE_TRADITIONAL]: "歡迎回來",
        }),

        // 登录失败
        loginErrorTitle: t({
            [Locales.ENGLISH]: "Login Failed",
            [Locales.CHINESE]: "登录失败",
            [Locales.CHINESE_TRADITIONAL]: "登入失敗",
        }),
        loginErrorDefault: t({
            [Locales.ENGLISH]: "Invalid username or password",
            [Locales.CHINESE]: "用户名或密码错误",
            [Locales.CHINESE_TRADITIONAL]: "使用者名稱或密碼錯誤",
        }),
        tokenMissing: t({
            [Locales.ENGLISH]: "Token not received from server",
            [Locales.CHINESE]: "未获取到 Token",
            [Locales.CHINESE_TRADITIONAL]: "未從伺服器接收到 Token",
        }),

        // 会话过期
        sessionExpiredTitle: t({
            [Locales.ENGLISH]: "Session Expired",
            [Locales.CHINESE]: "会话已过期",
            [Locales.CHINESE_TRADITIONAL]: "工作階段已過期",
        }),
        sessionExpiredDesc: t({
            [Locales.ENGLISH]: "Your session is about to expire, please log in again.",
            [Locales.CHINESE]: "您的登录凭证即将失效，请重新登录。",
            [Locales.CHINESE_TRADITIONAL]: "您的登入憑證即將失效，請重新登入。",
        }),
        reLoginAction: t({
            [Locales.ENGLISH]: "Log In",
            [Locales.CHINESE]: "去登录",
            [Locales.CHINESE_TRADITIONAL]: "去登入",
        }),
    },
} satisfies Dictionary;

export default authProviderContent;
