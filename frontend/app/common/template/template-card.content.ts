import { Locales, t, type Dictionary } from "intlayer";

const templateCardContent = {
  key: "template-card",
  content: {
    templateCard: {
      notAllowedToChangeAngle: t({
        [Locales.CHINESE]: "渲染中不允许切换角度，请等待渲染完毕……",
        [Locales.CHINESE_TRADITIONAL]: "渲染中不允许切換角度，請等候渲染完畢……",
        [Locales.ENGLISH]:
          "Please wait for the rendering to complete before switching angles.",
      }),
      noVersionMessages: t({
        [Locales.CHINESE]: "无提交信息...",
        [Locales.CHINESE_TRADITIONAL]: "无提交信息...",
        [Locales.ENGLISH]: "No commit message...",
      }),
      initMessage: t({
        [Locales.CHINESE]: "创建此模板",
        [Locales.CHINESE_TRADITIONAL]: "创建此模板",
        [Locales.ENGLISH]: "Create the template",
      }),
      cascadingUpdateMessage: t({
        [Locales.CHINESE]: "自动级联更新",
        [Locales.CHINESE_TRADITIONAL]: "自动级联更新",
        [Locales.ENGLISH]: "Auto cascading update",
      }),
      unknownUser: t({
        [Locales.CHINESE]: "未知用户",
        [Locales.CHINESE_TRADITIONAL]: "未知用户",
        [Locales.ENGLISH]: "Unknown User",
      }),
      autoUpdatingSystem: t({
        [Locales.CHINESE]: "自动更新系统",
        [Locales.CHINESE_TRADITIONAL]: "自动更新系统",
        [Locales.ENGLISH]: "Auto Updating System",
      }),
      manage: t({
        [Locales.CHINESE]: "管理模板",
        [Locales.CHINESE_TRADITIONAL]: "管理範本",
        [Locales.ENGLISH]: "Manage Templates",
      }),
      move: {
        label: t({
          [Locales.CHINESE]: "移动位置",
          [Locales.CHINESE_TRADITIONAL]: "移動位置",
          [Locales.ENGLISH]: "Move Location",
        }),
        msg: {
          success: t({
            [Locales.CHINESE]: "模板移动成功",
            [Locales.CHINESE_TRADITIONAL]: "範本移動成功",
            [Locales.ENGLISH]: "Template moved successfully",
          }),
          failed: t({
            [Locales.CHINESE]: "移动模板失败",
            [Locales.CHINESE_TRADITIONAL]: "移動範本失敗",
            [Locales.ENGLISH]: "Failed to move template",
          }),
          error: t({
            [Locales.CHINESE]: "移动模板时出错",
            [Locales.CHINESE_TRADITIONAL]: "移動範本時發生錯誤",
            [Locales.ENGLISH]: "Error moving template",
          }),
        },
      },
      transfer: {
        label: t({
          [Locales.CHINESE]: "转移所有权",
          [Locales.CHINESE_TRADITIONAL]: "轉移所有權",
          [Locales.ENGLISH]: "Transfer Ownership",
        }),
        msg: {
          success: t({
            [Locales.CHINESE]: "模板转移成功",
            [Locales.CHINESE_TRADITIONAL]: "範本轉移成功",
            [Locales.ENGLISH]: "Template transferred successfully",
          }),
          failed: t({
            [Locales.CHINESE]: "转移模板失败",
            [Locales.CHINESE_TRADITIONAL]: "轉移範本失敗",
            [Locales.ENGLISH]: "Failed to transfer template",
          }),
          error: t({
            [Locales.CHINESE]: "转移模板时出错",
            [Locales.CHINESE_TRADITIONAL]: "轉移範本時發生錯誤",
            [Locales.ENGLISH]: "Error transferring template",
          }),
        },
      },
    },
  },
} satisfies Dictionary;

export default templateCardContent;
