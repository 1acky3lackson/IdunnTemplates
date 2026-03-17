import { Locales, t, type Dictionary } from "intlayer";

const themeContent = {
  key: "theme",
  content: {
    toggle: {
      darkMode: t({
        [Locales.ENGLISH]: "Dark",
        [Locales.CHINESE]: "暗色",
        [Locales.CHINESE_TRADITIONAL]: "暗色",
      }),
      lightMode: t({
        [Locales.ENGLISH]: "Light",
        [Locales.CHINESE]: "亮色",
        [Locales.CHINESE_TRADITIONAL]: "亮色",
      }),
      systemMode: t({
        [Locales.ENGLISH]: "System",
        [Locales.CHINESE]: "跟随系统",
        [Locales.CHINESE_TRADITIONAL]: "跟隨系統",
      }),
    },
  },
} satisfies Dictionary;

export default themeContent;
