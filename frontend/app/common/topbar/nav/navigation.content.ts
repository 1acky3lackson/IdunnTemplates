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
            [Locales.CHINESE_TRADITIONAL]: "專為 Minecraft 伺服器打造的現代化模板管理平台。",
        }),
        menus: {
            templates: t({ [Locales.ENGLISH]: "Templates", [Locales.CHINESE]: "模板", [Locales.CHINESE_TRADITIONAL]: "模板" }),
            tags: t({ [Locales.ENGLISH]: "Tags", [Locales.CHINESE]: "标签", [Locales.CHINESE_TRADITIONAL]: "標籤" }),
            sets: t({ [Locales.ENGLISH]: "Sets", [Locales.CHINESE]: "合集", [Locales.CHINESE_TRADITIONAL]: "合集" }),
            brushes: t({ [Locales.ENGLISH]: "Brushes", [Locales.CHINESE]: "笔刷", [Locales.CHINESE_TRADITIONAL]: "筆刷" }),
            utilities: t({ [Locales.ENGLISH]: "Utilities", [Locales.CHINESE]: "工具", [Locales.CHINESE_TRADITIONAL]: "工具" }),
            commercial: t({
                [Locales.ENGLISH]: "Commercial",
                [Locales.CHINESE]: "商业化",
                [Locales.CHINESE_TRADITIONAL]: "商业化"
            }),
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
            browseBrushes: t({ [Locales.ENGLISH]: "Browse Brushes", [Locales.CHINESE]: "浏览笔刷", [Locales.CHINESE_TRADITIONAL]: "瀏覽刷子" }),
            browseBrushesDesc: t({ [Locales.ENGLISH]: "Browse all available brushes.", [Locales.CHINESE]: "浏览所有可用的刷子。", [Locales.CHINESE_TRADITIONAL]: "瀏覽所有可用的刷子。" }),
            myBrushes: t({ [Locales.ENGLISH]: "My Brushes", [Locales.CHINESE]: "我的笔刷", [Locales.CHINESE_TRADITIONAL]: "我的刷子" }),
            myBrushesDesc: t({ [Locales.ENGLISH]: "View and manage your personal brushes.", [Locales.CHINESE]: "查看并管理您的个人笔刷。", [Locales.CHINESE_TRADITIONAL]: "查看並管理您的個人筆刷。" }),
            createBrush: t({ [Locales.ENGLISH]: "Create New Brush", [Locales.CHINESE]: "创建笔刷", [Locales.CHINESE_TRADITIONAL]: "創建刷子" }),
            createBrushDesc: t({ [Locales.ENGLISH]: "Create a new brush.", [Locales.CHINESE]: "创建一个新的笔刷工具。", [Locales.CHINESE_TRADITIONAL]: "創建一個新的筆刷工具。" }),
            // Utilities 部分
            resize: t({ [Locales.ENGLISH]: "Resize Player", [Locales.CHINESE]: "缩放玩家大小", [Locales.CHINESE_TRADITIONAL]: "調整大小工具" }),
            resizeDesc: t({ [Locales.ENGLISH]: "Resize player models easily.", [Locales.CHINESE]: "轻松调整玩家模型的大小。", [Locales.CHINESE_TRADITIONAL]: "輕鬆調整玩家模型的大小。" }),
            commercial: {
                balances: {
                    title: t({
                        [Locales.ENGLISH]: "User Balances",
                        [Locales.CHINESE]: "用户账户",
                        [Locales.CHINESE_TRADITIONAL]: "用戶賬戶"
                    }),
                    desc: t({
                        [Locales.ENGLISH]: "View user accounts and various balances",
                        [Locales.CHINESE]: "查看用户的账户、各类余额等",
                        [Locales.CHINESE_TRADITIONAL]: "查看用戶的賬戶、各類餘額等"
                    }),
                },
                checkout: {
                    title: t({
                        [Locales.ENGLISH]: "Checkout Records",
                        [Locales.CHINESE]: "结算记录",
                        [Locales.CHINESE_TRADITIONAL]: "結算記錄"
                    }),
                    desc: t({
                        [Locales.ENGLISH]: "View records of NetEase order settlements to user earnings",
                        [Locales.CHINESE]: "查看网易订单结算到用户收益的记录",
                        [Locales.CHINESE_TRADITIONAL]: "查看網易訂單結算到用戶收益的記錄"
                    }),
                },
                transactions: {
                    title: t({
                        [Locales.ENGLISH]: "Account Transactions",
                        [Locales.CHINESE]: "账户流水",
                        [Locales.CHINESE_TRADITIONAL]: "賬戶流水"
                    }),
                    desc: t({
                        [Locales.ENGLISH]: "View the list of user account transactions",
                        [Locales.CHINESE]: "查看用户账户的流水信息列表",
                        [Locales.CHINESE_TRADITIONAL]: "查看用戶賬戶的流水信息列表"
                    }),
                },
                globalParams: {
                    title: t({
                        [Locales.ENGLISH]: "Global Settlement Parameters",
                        [Locales.CHINESE]: "全局结算参数",
                        [Locales.CHINESE_TRADITIONAL]: "全局結算參數"
                    }),
                    desc: t({
                        [Locales.ENGLISH]: "View or modify global settlement parameters",
                        [Locales.CHINESE]: "查看或修改全局的结算参数",
                        [Locales.CHINESE_TRADITIONAL]: "查看或修改全局的結算參數"
                    }),
                },
                projects: {
                    title: t({
                        [Locales.ENGLISH]: "Taixue Projects",
                        [Locales.CHINESE]: "太学工程",
                        [Locales.CHINESE_TRADITIONAL]: "太學工程"
                    }),
                    desc: t({
                        [Locales.ENGLISH]: "View the list of projects for commercialization",
                        [Locales.CHINESE]: "查看用于商业化的工程列表",
                        [Locales.CHINESE_TRADITIONAL]: "查看用於商業化的工程列表"
                    }),
                },
                neteaseProducts: {
                    title: t({
                        [Locales.ENGLISH]: "Netease Products",
                        [Locales.CHINESE]: "网易上架商品",
                        [Locales.CHINESE_TRADITIONAL]: "網易上架商品"
                    }),
                    desc: t({
                        [Locales.ENGLISH]: "View all our products listed on the Netease Mall",
                        [Locales.CHINESE]: "查看我们上架到网易商城的所有商品",
                        [Locales.CHINESE_TRADITIONAL]: "查看我們上架到網易商城的所有商品"
                    }),
                },
                neteaseOrders: {
                    title: t({
                        [Locales.ENGLISH]: "Netease Sales Orders",
                        [Locales.CHINESE]: "网易销售订单",
                        [Locales.CHINESE_TRADITIONAL]: "網易銷售訂單"
                    }),
                    desc: t({
                        [Locales.ENGLISH]: "View all orders for products in the Netease Mall",
                        [Locales.CHINESE]: "查看网易商城中商品的全部订单",
                        [Locales.CHINESE_TRADITIONAL]: "查看網易商城中商品的全部訂單"
                    }),
                },
                neteaseWithdraw: {
                    title: t({
                        [Locales.ENGLISH]: "",
                        [Locales.CHINESE]: "网易提现记录",
                        [Locales.CHINESE_TRADITIONAL]: "網易銷售訂單"
                    }),
                    desc: t({
                        [Locales.ENGLISH]: "",
                        [Locales.CHINESE]: "查看网易提现记录及其使用情况",
                        [Locales.CHINESE_TRADITIONAL]: ""
                    }),
                },
            }
        }
    },
} satisfies Dictionary;

export default navigationContent;