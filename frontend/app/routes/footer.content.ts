import { Locales, t, type Dictionary } from "intlayer";

const footerContent = {
    key: "footer",
    content: {
        footer: {
            copyright: t({
                [Locales.ENGLISH]: 'Copyright',
                [Locales.CHINESE]: '版权所有',
                [Locales.CHINESE_TRADITIONAL]: '版權所有'
            }),
            arr: t({
                [Locales.ENGLISH]: '. All rights reserved.',
                [Locales.CHINESE]: '。保留所有权利。',
                [Locales.CHINESE_TRADITIONAL]: '。保留所有權利。'
            }),
            creator: {
                // Made with + <emoji: heart> + by + Jacky Blackson + @ + Taixue
                madeWith: t({
                    [Locales.ENGLISH]: 'Made with',
                    [Locales.CHINESE]: '用心制作',
                    [Locales.CHINESE_TRADITIONAL]: '用心製作'
                }),
                by: t({
                    [Locales.ENGLISH]: 'by',
                    [Locales.CHINESE]: '作者',
                    [Locales.CHINESE_TRADITIONAL]: '作者'
                }),
                taixue: t({
                    [Locales.ENGLISH]: 'Taixue',
                    [Locales.CHINESE]: '太学Taixue',
                    [Locales.CHINESE_TRADITIONAL]: '太學Taixue'
                }),
            }
        }
    },
} satisfies Dictionary;

export default footerContent;