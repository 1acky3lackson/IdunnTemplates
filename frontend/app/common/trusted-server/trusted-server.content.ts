import { t, type Dictionary } from "intlayer";
import { Locales } from "intlayer";

export const trustedServerContent = {
  key: "trusted-server",
  content: {
    title: t({
      [Locales.ENGLISH]: "Trusted Servers",
      [Locales.CHINESE]: "信任服务器",
      [Locales.CHINESE_TRADITIONAL]: "信任伺服器",
    }),
    subtitle: t({
      [Locales.ENGLISH]: "Manage your trusted server list. Generate authentication tokens to allow external nodes to integrate securely.",
      [Locales.CHINESE]: "配置外部调用您的服务器节点权限并获取 Tokens。",
      [Locales.CHINESE_TRADITIONAL]: "配置外部調用您的伺服器節點權限並獲取 Tokens。",
    }),
    columns: {
        name: t({
            [Locales.ENGLISH]: "Name",
            [Locales.CHINESE]: "名称",
            [Locales.CHINESE_TRADITIONAL]: "名稱",
        }),
        description: t({
            [Locales.ENGLISH]: "Description",
            [Locales.CHINESE]: "描述",
            [Locales.CHINESE_TRADITIONAL]: "描述",
        }),
        token: t({
            [Locales.ENGLISH]: "Token",
            [Locales.CHINESE]: "Token",
            [Locales.CHINESE_TRADITIONAL]: "Token",
        }),
        creator: t({
            [Locales.ENGLISH]: "Creator",
            [Locales.CHINESE]: "创建者",
            [Locales.CHINESE_TRADITIONAL]: "創建者",
        }),
        createdAt: t({
            [Locales.ENGLISH]: "Created At",
            [Locales.CHINESE]: "创建时间",
            [Locales.CHINESE_TRADITIONAL]: "創建時間",
        }),
    },
    actions: {
        copyFullToken: t({
            [Locales.ENGLISH]: "Copy full token",
            [Locales.CHINESE]: "复制完整 Token",
            [Locales.CHINESE_TRADITIONAL]: "複製完整 Token",
        }),
        add: t({
            [Locales.ENGLISH]: "Add",
            [Locales.CHINESE]: "添加",
            [Locales.CHINESE_TRADITIONAL]: "添加",
        }),
        cancel: t({
            [Locales.ENGLISH]: "Cancel",
            [Locales.CHINESE]: "取消",
            [Locales.CHINESE_TRADITIONAL]: "取消",
        })
    },
    dialogs: {
        addServerTitle: t({
            [Locales.ENGLISH]: "Add Server",
            [Locales.CHINESE]: "添加服务器节点",
            [Locales.CHINESE_TRADITIONAL]: "添加伺服器節點",
        }),
        addServerDesc: t({
            [Locales.ENGLISH]: "Register a new trusted server to obtain an access token.",
            [Locales.CHINESE]: "注册一个新的信任服务器以获取访问口令。",
            [Locales.CHINESE_TRADITIONAL]: "註冊一個新的信任伺服器以獲取訪問口令。",
        }),
        confirmDelete: t({
            [Locales.ENGLISH]: "Are you sure you want to delete server",
            [Locales.CHINESE]: "确定要移除服务器",
            [Locales.CHINESE_TRADITIONAL]: "確定要移除伺服器",
        }),
    },
    messages: {
        tokenCopied: t({
            [Locales.ENGLISH]: "Token copied to clipboard",
            [Locales.CHINESE]: "Token 已复制到剪贴板",
            [Locales.CHINESE_TRADITIONAL]: "Token 已複製到剪貼簿",
        }),
        deleteSuccess: t({
            [Locales.ENGLISH]: "Server deleted successfully",
            [Locales.CHINESE]: "删除成功",
            [Locales.CHINESE_TRADITIONAL]: "刪除成功",
        }),
        deleteFailed: t({
            [Locales.ENGLISH]: "Failed to delete server",
            [Locales.CHINESE]: "删除失败",
            [Locales.CHINESE_TRADITIONAL]: "刪除失敗",
        }),
        addSuccess: t({
            [Locales.ENGLISH]: "Server added successfully",
            [Locales.CHINESE]: "添加成功",
            [Locales.CHINESE_TRADITIONAL]: "添加成功",
        }),
        addFailed: t({
            [Locales.ENGLISH]: "Failed to add server",
            [Locales.CHINESE]: "添加失败",
            [Locales.CHINESE_TRADITIONAL]: "添加失敗",
        }),
        loading: t({
            [Locales.ENGLISH]: "Loading...",
            [Locales.CHINESE]: "加载中...",
            [Locales.CHINESE_TRADITIONAL]: "加載中...",
        })
    }
  },
} satisfies Dictionary;

export default trustedServerContent;
