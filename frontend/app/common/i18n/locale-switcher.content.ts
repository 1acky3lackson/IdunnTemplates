import { Locales, t, type Dictionary } from "intlayer";

const localeSwitcherContent = {
  key: "locale-switcher",
  content: {
    // 用于 Button 的 aria-label，增强无障碍支持
    localeSwitcherLabel: t({
      [Locales.ENGLISH]: "Select language",
      [Locales.CHINESE]: "选择语言",
      [Locales.CHINESE_TRADITIONAL]: "選擇語言",
    }),
    // 如果你将来想在下拉菜单顶部加一个标题
    menuTitle: t({
      [Locales.ENGLISH]: "Language",
      [Locales.CHINESE]: "语言设置",
      [Locales.CHINESE_TRADITIONAL]: "語言設置",
    }),
  },
} satisfies Dictionary;

export default localeSwitcherContent;
