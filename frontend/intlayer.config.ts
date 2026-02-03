import { type IntlayerConfig, Locales } from 'intlayer';

const config: IntlayerConfig = {
  internationalization: {
    locales: [
      Locales.CHINESE, // 'zh-CN'
      Locales.CHINESE_TRADITIONAL, // 'zh-TW'
      Locales.ENGLISH,
    ]
  },

  editor: {
    /**
     * 必需
     * 应用程序的 URL。
     * 这是可视化编辑器的目标 URL。
     * 示例：'http://localhost:3000'
     */
    applicationURL: "http://localhost:5173",
    /**
     * 可选
     * 默认值为 `true`。如果为 `false`，编辑器将处于非活动状态且无法访问。
     * 可用于出于安全原因在特定环境（如生产环境）中禁用编辑器。
     */
    enabled: process.env.INTLAYER_ENABLED === 'true' || true,
    /**
     * 可选
     * 默认值为 `8000`。
     * 编辑器服务器的端口。
     */
    port: process.env.INTLAYER_EDITOR_PORT ? parseInt(process.env.INTLAYER_EDITOR_PORT) : 8000,
    /**
     * 可选
     * 默认值为 "http://localhost:8000"
     * 编辑器服务器的 URL。
     */
    editorURL: process.env.INTLAYER_EDITOR_URL,
  },
};

export default config;
