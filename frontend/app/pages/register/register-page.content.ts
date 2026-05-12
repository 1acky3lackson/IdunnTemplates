import { t, type Dictionary, Locales } from "intlayer";

const registerContent = {
  key: "register-page",
  content: {
    // 页面标题与描述
    title: t({
      [Locales.ENGLISH]: "IdunnTemplates - Setup Password",
      [Locales.CHINESE]: "用户注册 - 设置密码",
      [Locales.CHINESE_TRADITIONAL]: "Idunn 模板 - 設定密碼",
    }),
    description: t({
      [Locales.ENGLISH]: "Please set a password for your account to register.",
      [Locales.CHINESE]: "请为您的账号设置一个密码以完成注册。",
      [Locales.CHINESE_TRADITIONAL]: "請為您的帳號設定一個密碼以完成註冊。",
    }),

    // 表单标签
    passwordLabel: t({
      [Locales.ENGLISH]: "Password",
      [Locales.CHINESE]: "密码",
      [Locales.CHINESE_TRADITIONAL]: "密碼",
    }),
    confirmPasswordLabel: t({
      [Locales.ENGLISH]: "Confirm Password",
      [Locales.CHINESE]: "确认密码",
      [Locales.CHINESE_TRADITIONAL]: "確認密碼",
    }),

    // 占位符
    passwordPlaceholder: t({
      [Locales.ENGLISH]: "Enter your password",
      [Locales.CHINESE]: "请输入密码",
      [Locales.CHINESE_TRADITIONAL]: "請輸入密碼",
    }),
    confirmPasswordPlaceholder: t({
      [Locales.ENGLISH]: "Confirm your password",
      [Locales.CHINESE]: "请确认密码",
      [Locales.CHINESE_TRADITIONAL]: "請確認密碼",
    }),

    // 验证错误信息
    passwordErrorMsg: t({
      [Locales.ENGLISH]: "Password must be at least 4 characters",
      [Locales.CHINESE]: "密码至少需要4个字符",
      [Locales.CHINESE_TRADITIONAL]: "密碼至少需要4個字元",
    }),
    confirmPasswordErrorMsg: t({
      [Locales.ENGLISH]: "Passwords do not match",
      [Locales.CHINESE]: "两次输入的密码不一致",
      [Locales.CHINESE_TRADITIONAL]: "兩次輸入的密碼不一致",
    }),

    // 按钮状态
    registerBtn: t({
      [Locales.ENGLISH]: "Register",
      [Locales.CHINESE]: "注册",
      [Locales.CHINESE_TRADITIONAL]: "註冊",
    }),
    registerBtnLoading: t({
      [Locales.ENGLISH]: "Registering...",
      [Locales.CHINESE]: "注册中...",
      [Locales.CHINESE_TRADITIONAL]: "註冊中...",
    }),

    // 提示信息
    registerSuccessTitle: t({
      [Locales.ENGLISH]: "Registration Successful",
      [Locales.CHINESE]: "注册成功",
      [Locales.CHINESE_TRADITIONAL]: "註冊成功",
    }),
    registerSuccessDesc: t({
      [Locales.ENGLISH]: "You can now log in with your new password.",
      [Locales.CHINESE]: "您现在可以使用新密码登录了。",
      [Locales.CHINESE_TRADITIONAL]: "您現在可以使用新密碼登入了。",
    }),
    registerErrorTitle: t({
      [Locales.ENGLISH]: "Registration Failed",
      [Locales.CHINESE]: "注册失败",
      [Locales.CHINESE_TRADITIONAL]: "註冊失敗",
    }),
    invalidTokenMsg: t({
      [Locales.ENGLISH]: "Invalid or missing registration token.",
      [Locales.CHINESE]: "无效或缺失的注册令牌。",
      [Locales.CHINESE_TRADITIONAL]: "無效或缺失的註冊令牌。",
    }),
  },
} satisfies Dictionary;

export default registerContent;
