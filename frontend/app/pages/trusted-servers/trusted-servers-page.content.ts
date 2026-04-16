import { Locales, t, type Dictionary } from "intlayer";

const trustedServersPageContent = {
  key: "trusted-servers-page",
  content: {
    title: t({
      [Locales.CHINESE]: "服务端凭证管理 (Server Tokens)",
      [Locales.CHINESE_TRADITIONAL]: "服務端憑證管理 (Server Tokens)",
      [Locales.ENGLISH]: "Server Tokens Management",
    }),
    description: t({
      [Locales.CHINESE]: "管理允许直接访问后端的信任服务端的永久 Token。通常用于其它微服务或者 Minecraft 服务端插件。将生成的 Token 填入对应服务端的配置中。",
      [Locales.CHINESE_TRADITIONAL]: "管理允許直接存取後端的信任服務端永久 Token。通常用於其他微服務或 Minecraft 服務端外掛。將生成的 Token 填入對應服務端的設定中。",
      [Locales.ENGLISH]: "Manage permanent tokens for trusted servers to directly access the backend. Usually used for other microservices or Minecraft server plugins. Paste the generated token into the configuration of the corresponding server.",
    }),
  },
} satisfies Dictionary;

export default trustedServersPageContent;
