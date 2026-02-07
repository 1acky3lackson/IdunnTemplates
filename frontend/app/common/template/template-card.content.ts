import { Locales, t, type Dictionary } from "intlayer";

const templateCardContent = {
    key: "template-card",
    content: {
        templateCard: {
            notAllowedToChangeAngle: t({
                [Locales.CHINESE]: "渲染中不允许切换角度，请等待渲染完毕……",
                [Locales.CHINESE_TRADITIONAL]: "渲染中不允许切換角度，請等候渲染完畢……",
                [Locales.ENGLISH]: "Please wait for the rendering to complete before switching angles."
            })
        }
    },
} satisfies Dictionary;

export default templateCardContent;
