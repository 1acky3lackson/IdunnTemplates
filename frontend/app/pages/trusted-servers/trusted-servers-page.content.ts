import { t, type Dictionary } from "intlayer";

const trustedServersPageContent = {
  key: "trusted-servers-page",
  content: {
    title: t({
      "zh-CN": "服务端凭证管理 (Server Tokens)",
      en: "Server Tokens Management",
    }),
    description: t({
      "zh-CN": "管理允许直接访问后端的信任服务端的永久 Token。通常用于其它微服务或者 Minecraft 服务端插件。将生成的 Token 填入对应服务端的配置中。",
      en: "Manage permanent tokens for trusted servers to directly access the backend. Usually used for other microservices or Minecraft server plugins. Paste the generated token into the configuration of the corresponding server.",
    }),
  },
} satisfies Dictionary;

export default trustedServersPageContent;
