import { Locales, t, type Dictionary } from "intlayer";

const smartTemplatePreviewContent = {
  key: "smart-template-preview",
  content: {
    renderering: {
      messageMain: t({
        [Locales.ENGLISH]: "Generating 3D Preview...",
        [Locales.CHINESE]: "正在生成 3D 预览...",
        [Locales.CHINESE_TRADITIONAL]: "正在生成 3D 預覽...",
      }),
      messageSecondary: t({
        [Locales.ENGLISH]: "Client-side rendering",
        [Locales.CHINESE]: "客户端本地渲染",
        [Locales.CHINESE_TRADITIONAL]: "用戶端本地渲染",
      }),
    },
    error: {
      messageMain: t({
        [Locales.ENGLISH]: "No Preview...",
        [Locales.CHINESE]: "无预览内容...",
        [Locales.CHINESE_TRADITIONAL]: "無預覽內容...",
      }),
    },
    tooLarge: {
      messageMain: t({
        [Locales.ENGLISH]: "Structure Too Large",
        [Locales.CHINESE]: "建筑体积过大",
        [Locales.CHINESE_TRADITIONAL]: "建築體積過大",
      }),
      messageSecondaryPart1: t({
        [Locales.ENGLISH]: "Rendering for ",
        [Locales.CHINESE]: "已禁用 ",
        [Locales.CHINESE_TRADITIONAL]: "已禁用 ",
      }),
      messageSecondaryPart2: t({
        [Locales.ENGLISH]: " is disabled to prevent browser lag.",
        [Locales.CHINESE]: " 的预览，以防止浏览器卡顿。",
        [Locales.CHINESE_TRADITIONAL]: " 的預覽，以防止瀏覽器卡頓。",
      }),
    },
  },
} satisfies Dictionary;

export default smartTemplatePreviewContent;
