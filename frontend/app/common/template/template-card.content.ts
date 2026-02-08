import { Locales, t, type Dictionary } from "intlayer";

const templateCardContent = {
    key: "template-card",
    content: {
        templateCard: {
            notAllowedToChangeAngle: t({
                [Locales.CHINESE]: "渲染中不允许切换角度，请等待渲染完毕……",
                [Locales.CHINESE_TRADITIONAL]: "渲染中不允许切換角度，請等候渲染完畢……",
                [Locales.ENGLISH]: "Please wait for the rendering to complete before switching angles."
            }),
            noVersionMessages: t({
                [Locales.CHINESE]: "无提交信息...",
                [Locales.CHINESE_TRADITIONAL]: "无提交信息...",
                [Locales.ENGLISH]: "No commit message..."
            }),
            initMessage: t({
                [Locales.CHINESE]: "创建此模板",
                [Locales.CHINESE_TRADITIONAL]: "创建此模板",
                [Locales.ENGLISH]: "Create the template"
            }),
            cascadingUpdateMessage: t({
                [Locales.CHINESE]: "自动级联更新",
                [Locales.CHINESE_TRADITIONAL]: "自动级联更新",
                [Locales.ENGLISH]: "Auto cascading update"
            }),
            unknownUser: t({
                [Locales.CHINESE]: "未知用户",
                [Locales.CHINESE_TRADITIONAL]: "未知用户",
                [Locales.ENGLISH]: "Unknown User"
            }),
            autoUpdatingSystem: t({
                [Locales.CHINESE]: "自动更新系统",
                [Locales.CHINESE_TRADITIONAL]: "自动更新系统",
                [Locales.ENGLISH]: "Auto Updating System"
            })
        }
    },
} satisfies Dictionary;

export default templateCardContent;
