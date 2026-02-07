import { Locales, t, type Dictionary } from "intlayer";

const creatorPickerContent = {
    key: "creatorpicker",
    content: {
        creatorPicker: {
            meLabel: t({
                [Locales.ENGLISH]: "Me",
                [Locales.CHINESE]: "我",
                [Locales.CHINESE_TRADITIONAL]: "我",
            }),
        }
    },
} satisfies Dictionary;

export default creatorPickerContent;
