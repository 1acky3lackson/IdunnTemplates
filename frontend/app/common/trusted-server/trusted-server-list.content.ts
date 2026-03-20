import { t, type Dictionary } from "intlayer";

const trustedServerListContent = {
  key: "trusted-server-list",
  content: {
    columns: {
      name: t({ "zh-CN": "名称", en: "Name" }),
      remarks: t({ "zh-CN": "备注", en: "Remarks" }),
      token: t({ "zh-CN": "Token (点击复制)", en: "Token (Click to copy)" }),
      createdAt: t({ "zh-CN": "创建时间", en: "Created At" }),
      createdBy: t({ "zh-CN": "创建人", en: "Created By" }),
    },
    actions: {
      create: t({ "zh-CN": "新建凭证", en: "Create Token" }),
      cancel: t({ "zh-CN": "取消", en: "Cancel" }),
      submit: t({ "zh-CN": "提交", en: "Submit" }),
      confirmDelete: t({ "zh-CN": "确认删除", en: "Confirm Delete" }),
    },
    createDialog: {
      title: t({ "zh-CN": "新建服务端凭证", en: "Create Server Token" }),
      description: t({ "zh-CN": "凭证生成后将永久有效。你可以随时在这里删除以吊销权限。", en: "The token will be permanently valid after generation. You can delete it here at any time to revoke access." }),
      nameLabel: t({ "zh-CN": "服务端名称", en: "Server Name" }),
      namePlaceholder: t({ "zh-CN": "例如：一区生存服", en: "e.g. Server-01" }),
      remarksLabel: t({ "zh-CN": "备注说明", en: "Remarks" }),
      remarksPlaceholder: t({ "zh-CN": "可选", en: "Optional" }),
    },
    deleteConfirm: {
      title: t({ "zh-CN": "危险操作", en: "Danger Action" }),
      description: t({ "zh-CN": "确定要删除服务端 {name} 吗？与之对应的客户端将立即失去调用接口的权限。", en: "Are you sure you want to delete server {name}? The corresponding client will immediately lose access to the endpoints." }),
    },
    messages: {
      errorFetch: t({ "zh-CN": "获取列表失败", en: "Failed to fetch list" }),
      createSuccess: t({ "zh-CN": "创建成功", en: "Created successfully" }),
      createError: t({ "zh-CN": "创建失败", en: "Failed to create" }),
      deleteSuccess: t({ "zh-CN": "删除成功", en: "Deleted successfully" }),
      deleteError: t({ "zh-CN": "删除失败", en: "Failed to delete" }),
      copied: t({ "zh-CN": "已复制到剪贴板", en: "Copied to clipboard" }),
    }
  },
} satisfies Dictionary;

export default trustedServerListContent;
