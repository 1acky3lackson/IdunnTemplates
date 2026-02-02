import { Locales, t, type Dictionary } from "intlayer";

const navigationContent = {
    key: "navigation_bar",
    content: {
        logoTitle: t({
            [Locales.ENGLISH]: "IdunnTemplates",
            [Locales.CHINESE]: "IdunnTemplates",
            [Locales.CHINESE_TRADITIONAL]: "IdunnTemplates",
        }),
        logoDescription: t({
            [Locales.ENGLISH]: "Modern template management platform for Minecraft Servers.",
            [Locales.CHINESE]: "专为 Minecraft 服务器打造的现代化模板管理平台。",
            [Locales.CHINESE_TRADITIONAL]   : "專為 Minecraft 伺服器打造的現代化模板管理平台。",
        }),
        menus: {
            templates: t({ [Locales.ENGLISH]: "Templates", [Locales.CHINESE]: "模板", [Locales.CHINESE_TRADITIONAL]: "模板" }),
            tags: t({ [Locales.ENGLISH]: "Tags", [Locales.CHINESE]: "标签", [Locales.CHINESE_TRADITIONAL]: "標籤" }),
            sets: t({ [Locales.ENGLISH]: "Sets", [Locales.CHINESE]: "合集", [Locales.CHINESE_TRADITIONAL]: "合集" }),
            brushes: t({ [Locales.ENGLISH]: "Brushes", [Locales.CHINESE]: "刷子", [Locales.CHINESE_TRADITIONAL]: "刷子" }),
            utilities: t({ [Locales.ENGLISH]: "Utilities", [Locales.CHINESE]: "工具", [Locales.CHINESE_TRADITIONAL]: "工具" }),
        },
        items: {
            searchTemplates: t({ [Locales.ENGLISH]: "Search Templates", [Locales.CHINESE]: "搜索模板", [Locales.CHINESE_TRADITIONAL]: "搜尋模板" }),
            searchTemplatesDesc: t({
                [Locales.ENGLISH]: "Browse, search and filter templates we have available.",
                [Locales.CHINESE]: "浏览、搜索并筛选我们提供的所有模板。",
                [Locales.CHINESE_TRADITIONAL]: "瀏覽、搜尋並篩選我們提供的所有模板。"
            }),
            viewFolders: t({ [Locales.ENGLISH]: "View Folders", [Locales.CHINESE]: "查看目录", [Locales.CHINESE_TRADITIONAL]: "查看目錄" }),
            viewFoldersDesc: t({
                [Locales.ENGLISH]: "Lookup templates using the folder system.",
                [Locales.CHINESE]: "使用文件夹系统快速查找模板。",
                [Locales.CHINESE_TRADITIONAL]: "使用資料夾系統快速查找模板。"
            }),
            browseTags: t({ [Locales.ENGLISH]: "Browse Tags", [Locales.CHINESE]: "浏览标签", [Locales.CHINESE_TRADITIONAL]: "瀏覽標籤" }),
            browseTagsDesc: t({
                [Locales.ENGLISH]: "Use tags to categorize and find templates easily.",
                [Locales.CHINESE]: "使用标签对模板进行分类，轻松找到所需内容。",
                [Locales.CHINESE_TRADITIONAL]: "使用標籤對模板進行分類，輕鬆找到所需內容。"
            }),
            // Sets 部分
            browseSets: t({ [Locales.ENGLISH]: "Browse Sets", [Locales.CHINESE]: "浏览合集", [Locales.CHINESE_TRADITIONAL]: "瀏覽合集" }),
            browseSetsDesc: t({ [Locales.ENGLISH]: "Browse all available template sets.", [Locales.CHINESE]: "浏览所有可用的模板合集。", [Locales.CHINESE_TRADITIONAL]: "瀏覽所有可用的模板合集。" }),
            mySets: t({ [Locales.ENGLISH]: "My Template Sets", [Locales.CHINESE]: "我的合集", [Locales.CHINESE_TRADITIONAL]: "我的合集" }),
            mySetsDesc: t({ [Locales.ENGLISH]: "View and manage your personal sets.", [Locales.CHINESE]: "查看并管理您的个人模板合集。", [Locales.CHINESE_TRADITIONAL]: "查看並管理您的個人模板合集。" }),
            createSet: t({ [Locales.ENGLISH]: "Create New Set", [Locales.CHINESE]: "创建合集", [Locales.CHINESE_TRADITIONAL]: "創建合集" }),
            createSetDesc: t({ [Locales.ENGLISH]: "Create a new template set.", [Locales.CHINESE]: "创建一个新的模板合集。", [Locales.CHINESE_TRADITIONAL]: "創建一個新的模板合集。" }),
            // Brushes 部分
            browseBrushes: t({ [Locales.ENGLISH]: "Browse Brushes", [Locales.CHINESE]: "浏览刷子", [Locales.CHINESE_TRADITIONAL]: "瀏覽刷子" }),
            browseBrushesDesc: t({ [Locales.ENGLISH]: "Browse all available brushes.", [Locales.CHINESE]: "浏览所有可用的刷子。", [Locales.CHINESE_TRADITIONAL]: "瀏覽所有可用的刷子。" }),
            myBrushes: t({ [Locales.ENGLISH]: "My Brushes", [Locales.CHINESE]: "我的刷子", [Locales.CHINESE_TRADITIONAL]: "我的刷子" }),
            myBrushesDesc: t({ [Locales.ENGLISH]: "View and manage your personal brushes.", [Locales.CHINESE]: "查看并管理您的个人刷子。", [Locales.CHINESE_TRADITIONAL]: "查看並管理您的個人刷子。" }),
            createBrush: t({ [Locales.ENGLISH]: "Create New Brush", [Locales.CHINESE]: "创建刷子", [Locales.CHINESE_TRADITIONAL]: "創建刷子" }),
            createBrushDesc: t({ [Locales.ENGLISH]: "Create a new brush.", [Locales.CHINESE]: "创建一个新的刷子工具。", [Locales.CHINESE_TRADITIONAL]: "創建一個新的刷子工具。" }),
        }
    },
} satisfies Dictionary;

export default navigationContent;