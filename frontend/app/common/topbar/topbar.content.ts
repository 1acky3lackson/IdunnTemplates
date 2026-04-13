import { Locales, t, type Dictionary } from "intlayer";

const topbarContent = {
  key: "topbar",
  content: {
    // 网站标题
    siteTitle: t({
      [Locales.ENGLISH]: "IdunnTemplates™",
      [Locales.CHINESE]: "建筑模板管理系统",
      [Locales.CHINESE_TRADITIONAL]: "Idunn™ 模板",
    }),
    login: t({
      [Locales.ENGLISH]: "LOGIN",
      [Locales.CHINESE]: "登录",
      [Locales.CHINESE_TRADITIONAL]: "登入",
    }),
    logout: t({
      [Locales.ENGLISH]: "LOGOUT",
      [Locales.CHINESE]: "登出",
      [Locales.CHINESE_TRADITIONAL]: "登出",
    }),
    meBtn: t({
      [Locales.ENGLISH]: "My Status",
      [Locales.CHINESE]: "我的",
      [Locales.CHINESE_TRADITIONAL]: "我的",
    }),
  },
} satisfies Dictionary;

export default topbarContent;
