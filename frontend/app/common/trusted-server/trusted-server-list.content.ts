import { Locales, t, type Dictionary } from "intlayer";

const trustedServerListContent = {
  key: "trusted-server-list",
  content: {
    columns: {
      name: t({ [Locales.CHINESE]: "名称", [Locales.CHINESE_TRADITIONAL]: "名稱", [Locales.ENGLISH]: "Name" }),
      remarks: t({ [Locales.CHINESE]: "备注", [Locales.CHINESE_TRADITIONAL]: "備註", [Locales.ENGLISH]: "Remarks" }),
      token: t({ [Locales.CHINESE]: "Token (点击复制)", [Locales.CHINESE_TRADITIONAL]: "Token（點擊複製）", [Locales.ENGLISH]: "Token (Click to copy)" }),
      createdAt: t({ [Locales.CHINESE]: "创建时间", [Locales.CHINESE_TRADITIONAL]: "建立時間", [Locales.ENGLISH]: "Created At" }),
      createdBy: t({ [Locales.CHINESE]: "创建人", [Locales.CHINESE_TRADITIONAL]: "建立者", [Locales.ENGLISH]: "Created By" }),
    },
    actions: {
      create: t({ [Locales.CHINESE]: "新建凭证", [Locales.CHINESE_TRADITIONAL]: "新增憑證", [Locales.ENGLISH]: "Create Token" }),
      cancel: t({ [Locales.CHINESE]: "取消", [Locales.CHINESE_TRADITIONAL]: "取消", [Locales.ENGLISH]: "Cancel" }),
      submit: t({ [Locales.CHINESE]: "提交", [Locales.CHINESE_TRADITIONAL]: "提交", [Locales.ENGLISH]: "Submit" }),
      confirmDelete: t({ [Locales.CHINESE]: "确认删除", [Locales.CHINESE_TRADITIONAL]: "確認刪除", [Locales.ENGLISH]: "Confirm Delete" }),
    },
    createDialog: {
      title: t({ [Locales.CHINESE]: "新建服务端凭证", [Locales.CHINESE_TRADITIONAL]: "新增服務端憑證", [Locales.ENGLISH]: "Create Server Token" }),
      description: t({ [Locales.CHINESE]: "凭证生成后将永久有效。你可以随时在这里删除以吊销权限。", [Locales.CHINESE_TRADITIONAL]: "憑證生成後將永久有效。你可以隨時在這裡刪除以吊銷權限。", [Locales.ENGLISH]: "The token will be permanently valid after generation. You can delete it here at any time to revoke access." }),
      nameLabel: t({ [Locales.CHINESE]: "服务端名称", [Locales.CHINESE_TRADITIONAL]: "服務端名稱", [Locales.ENGLISH]: "Server Name" }),
      namePlaceholder: t({ [Locales.CHINESE]: "例如：一区生存服", [Locales.CHINESE_TRADITIONAL]: "例如：一區生存服", [Locales.ENGLISH]: "e.g. Server-01" }),
      remarksLabel: t({ [Locales.CHINESE]: "备注说明", [Locales.CHINESE_TRADITIONAL]: "備註說明", [Locales.ENGLISH]: "Remarks" }),
      remarksPlaceholder: t({ [Locales.CHINESE]: "可选", [Locales.CHINESE_TRADITIONAL]: "可選", [Locales.ENGLISH]: "Optional" }),
    },
    deleteConfirm: {
      title: t({ [Locales.CHINESE]: "危险操作", [Locales.CHINESE_TRADITIONAL]: "危險操作", [Locales.ENGLISH]: "Danger Action" }),
      description: t({ [Locales.CHINESE]: "确定要删除服务端 {name} 吗？与之对应的客户端将立即失去调用接口的权限。", [Locales.CHINESE_TRADITIONAL]: "確定要刪除服務端 {name} 嗎？與之對應的客戶端將立即失去呼叫介面的權限。", [Locales.ENGLISH]: "Are you sure you want to delete server {name}? The corresponding client will immediately lose access to the endpoints." }),
    },
    messages: {
      errorFetch: t({ [Locales.CHINESE]: "获取列表失败", [Locales.CHINESE_TRADITIONAL]: "取得列表失敗", [Locales.ENGLISH]: "Failed to fetch list" }),
      createSuccess: t({ [Locales.CHINESE]: "创建成功", [Locales.CHINESE_TRADITIONAL]: "建立成功", [Locales.ENGLISH]: "Created successfully" }),
      createError: t({ [Locales.CHINESE]: "创建失败", [Locales.CHINESE_TRADITIONAL]: "建立失敗", [Locales.ENGLISH]: "Failed to create" }),
      deleteSuccess: t({ [Locales.CHINESE]: "删除成功", [Locales.CHINESE_TRADITIONAL]: "刪除成功", [Locales.ENGLISH]: "Deleted successfully" }),
      deleteError: t({ [Locales.CHINESE]: "删除失败", [Locales.CHINESE_TRADITIONAL]: "刪除失敗", [Locales.ENGLISH]: "Failed to delete" }),
      copied: t({ [Locales.CHINESE]: "已复制到剪贴板", [Locales.CHINESE_TRADITIONAL]: "已複製到剪貼簿", [Locales.ENGLISH]: "Copied to clipboard" }),
    }
  },
} satisfies Dictionary;

export default trustedServerListContent;
