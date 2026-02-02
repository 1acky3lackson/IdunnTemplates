import { t, type Dictionary, Locales } from "intlayer";

const loginContent = {
    key: "login-page",
    content: {
        // 页面标题与描述
        title: t({
            [Locales.ENGLISH]: "Idunn Templates",
            [Locales.CHINESE]: "Idunn Templates",
            [Locales.CHINESE_TRADITIONAL]: "Idunn Templates",
        }),
        description: t({
            [Locales.ENGLISH]: "Please enter your username and password to continue",
            [Locales.CHINESE]: "请输入您的账号密码以继续",
            [Locales.CHINESE_TRADITIONAL]: "請輸入您的帳號密碼以繼續",
        }),

        // 表单标签
        usernameLabel: t({
            [Locales.ENGLISH]: "Username",
            [Locales.CHINESE]: "用户名",
            [Locales.CHINESE_TRADITIONAL]: "使用者名稱",
        }),
        passwordLabel: t({
            [Locales.ENGLISH]: "Password",
            [Locales.CHINESE]: "密码",
            [Locales.CHINESE_TRADITIONAL]: "密碼",
        }),

        // 占位符
        usernamePlaceholder: t({
            [Locales.ENGLISH]: "Enter your username",
            [Locales.CHINESE]: "请输入用户名",
            [Locales.CHINESE_TRADITIONAL]: "請輸入使用者名稱",
        }),
        passwordPlaceholder: t({
            [Locales.ENGLISH]: "••••••••",
            [Locales.CHINESE]: "••••••••",
            [Locales.CHINESE_TRADITIONAL]: "••••••••",
        }),

        // 验证错误信息
        usernameErrorMsg: t({
            [Locales.ENGLISH]: "Username must be at least 2 characters",
            [Locales.CHINESE]: "用户名至少需要2个字符",
            [Locales.CHINESE_TRADITIONAL]: "使用者名稱至少需要2個字元",
        }),
        passwordErrorMsg: t({
            [Locales.ENGLISH]: "Password must be at least 4 characters",
            [Locales.CHINESE]: "密码至少需要4个字符",
            [Locales.CHINESE_TRADITIONAL]: "密碼至少需要4個字元",
        }),

        // 按钮状态
        loginBtn: t({
            [Locales.ENGLISH]: "Log In",
            [Locales.CHINESE]: "立即登录",
            [Locales.CHINESE_TRADITIONAL]: "立即登入",
        }),
        loginBtnLoading: t({
            [Locales.ENGLISH]: "Logging in...",
            [Locales.CHINESE]: "登录中...",
            [Locales.CHINESE_TRADITIONAL]: "登入中...",
        }),

        // 底部文字
        footerText: t({
            [Locales.ENGLISH]: "Protected internal system",
            [Locales.CHINESE]: "受保护的内部系统",
            [Locales.CHINESE_TRADITIONAL]: "受保護的內部系統",
        }),
    },
} satisfies Dictionary;

export default loginContent;