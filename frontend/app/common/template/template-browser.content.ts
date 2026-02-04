import { Locales, t, type Dictionary } from "intlayer";

const templateBrowserContent = {
    key: "template-browser",
    content: {
        filters: {
            sortBy: t({
                [Locales.ENGLISH]: "Sort By",
                [Locales.CHINESE]: "排序方式",
                [Locales.CHINESE_TRADITIONAL]: "排序方式",
            }),
            sortOrders: {
                newest: t({ [Locales.ENGLISH]: "Newest First", [Locales.CHINESE]: "最新优先", [Locales.CHINESE_TRADITIONAL]: "最新優先" }),
                oldest: t({ [Locales.ENGLISH]: "Oldest First", [Locales.CHINESE]: "最早优先", [Locales.CHINESE_TRADITIONAL]: "最早優先" }),
                newestLocked: t({ [Locales.ENGLISH]: "Newest Locked First", [Locales.CHINESE]: "最近锁定优先", [Locales.CHINESE_TRADITIONAL]: "最近鎖定優先" }),
                oldestLocked: t({ [Locales.ENGLISH]: "Oldest Locked First", [Locales.CHINESE]: "最早锁定优先", [Locales.CHINESE_TRADITIONAL]: "最早鎖定優先" }),
                tallest: t({ [Locales.ENGLISH]: "Tallest First", [Locales.CHINESE]: "最高优先", [Locales.CHINESE_TRADITIONAL]: "最高優先" }),
                widest: t({ [Locales.ENGLISH]: "Widest First", [Locales.CHINESE]: "最宽优先", [Locales.CHINESE_TRADITIONAL]: "最寬優先" }),
                longest: t({ [Locales.ENGLISH]: "Longest First", [Locales.CHINESE]: "最长优先", [Locales.CHINESE_TRADITIONAL]: "最長優先" }),
                nameAZ: t({ [Locales.ENGLISH]: "Name (A-Z)", [Locales.CHINESE]: "名称 (A-Z)", [Locales.CHINESE_TRADITIONAL]: "名稱 (A-Z)" }),
                nameZA: t({ [Locales.ENGLISH]: "Name (Z-A)", [Locales.CHINESE]: "名称 (Z-A)", [Locales.CHINESE_TRADITIONAL]: "名稱 (Z-A)" }),
            },
            lockedOnly: t({
                [Locales.ENGLISH]: "Show Locked Only",
                [Locales.CHINESE]: "仅显示已锁定",
                [Locales.CHINESE_TRADITIONAL]: "僅顯示已鎖定",
            }),
            categories: t({
                [Locales.ENGLISH]: "Filter Folders",
                [Locales.CHINESE]: "按文件夹筛选",
                [Locales.CHINESE_TRADITIONAL]: "按文件夾篩選",
            }),
            creators: t({
                [Locales.ENGLISH]: "Filter Creators",
                [Locales.CHINESE]: "按创建者筛选",
                [Locales.CHINESE_TRADITIONAL]: "按創作者篩選",
            }),
            minWidth: t({ [Locales.ENGLISH]: "Min Width (Z Axis)", [Locales.CHINESE]: "最小宽度（Z轴）", [Locales.CHINESE_TRADITIONAL]: "最小寬度（Z軸）" }),
            maxWidth: t({ [Locales.ENGLISH]: "Max Width (Z Axis)", [Locales.CHINESE]: "最大宽度（Z轴）", [Locales.CHINESE_TRADITIONAL]: "最大寬度（Z軸）" }),
            minLength: t({ [Locales.ENGLISH]: "Min Length (x Axis)", [Locales.CHINESE]: "最小长度（X轴）", [Locales.CHINESE_TRADITIONAL]: "最小長度（X軸）" }),
            maxLength: t({ [Locales.ENGLISH]: "Max Length (x Axis)", [Locales.CHINESE]: "最大长度（X轴）", [Locales.CHINESE_TRADITIONAL]: "最大長度（X軸）" }),  
            minHeight: t({ [Locales.ENGLISH]: "Min Height (Y Axis)", [Locales.CHINESE]: "最小高度（Y轴）", [Locales.CHINESE_TRADITIONAL]: "最小高度（Y軸）" }),
            maxHeight: t({ [Locales.ENGLISH]: "Max Height (Y Axis)", [Locales.CHINESE]: "最大高度（Y轴）", [Locales.CHINESE_TRADITIONAL]: "最大高度（Y軸）" }),
            blocksUnit: t({ [Locales.ENGLISH]: "blocks", [Locales.CHINESE]: "方块", [Locales.CHINESE_TRADITIONAL]: "方塊" }),
            notSpecified: t({
                [Locales.ENGLISH]: "Not Specified",
                [Locales.CHINESE]: "未指定",
                [Locales.CHINESE_TRADITIONAL]: "未指定",
            }),
            tabs: {
                folders: {
                    hoverTitle: t({
                        [Locales.ENGLISH]: "Folders",
                        [Locales.CHINESE]: "文件夹",
                        [Locales.CHINESE_TRADITIONAL]: "文件夾",
                    }),
                    hoverDesc: t({
                        [Locales.ENGLISH]: "Filter templates by folder structure.",
                        [Locales.CHINESE]: "通过文件夹结构筛选模板。",
                        [Locales.CHINESE_TRADITIONAL]: "通過文件夾結構筛选模板。",
                    }),
                    currentValue: t({
                        [Locales.ENGLISH]: "Current Folder is: ",
                        [Locales.CHINESE]: "当前文件夹：",
                        [Locales.CHINESE_TRADITIONAL]: "當前文件夾：",
                    }),
                    clearBtn: t({
                        [Locales.ENGLISH]: "Clear",
                        [Locales.CHINESE]: "清空",
                        [Locales.CHINESE_TRADITIONAL]: "清空",
                    }),
                },
                tags: {
                    hoverTitle: t({
                        [Locales.ENGLISH]: "Tags",
                        [Locales.CHINESE]: "标签",
                        [Locales.CHINESE_TRADITIONAL]: "標籤",
                    }),
                    hoverDesc: t({
                        [Locales.ENGLISH]: "Filter templates by tags.",
                        [Locales.CHINESE]: "通过标签筛选模板。",
                        [Locales.CHINESE_TRADITIONAL]: "通過標籤筛选模板。",
                    }),
                    currentValue: t({
                        [Locales.ENGLISH]: "Current Tags are: ",
                        [Locales.CHINESE]: "当前标签：",
                        [Locales.CHINESE_TRADITIONAL]: "當前標籤：",
                    }),
                    clearBtn: t({
                        [Locales.ENGLISH]: "Clear",
                        [Locales.CHINESE]: "清空",
                        [Locales.CHINESE_TRADITIONAL]: "清空",
                    }),
                },
                creators: {
                    hoverTitle: t({
                        [Locales.ENGLISH]: "Creators",
                        [Locales.CHINESE]: "创建者",
                        [Locales.CHINESE_TRADITIONAL]: "創作者",
                    }),
                    hoverDesc: t({
                        [Locales.ENGLISH]: "Filter templates by their creators.",
                        [Locales.CHINESE]: "通过创建者筛选模板。",
                        [Locales.CHINESE_TRADITIONAL]: "通過創作者筛选模板。",
                    }),
                    currentValue: t({
                        [Locales.ENGLISH]: "Current Creator are: ",
                        [Locales.CHINESE]: "当前筛选创建者：",
                        [Locales.CHINESE_TRADITIONAL]: "當前筛选創作者：",
                    }),
                    clearBtn: t({
                        [Locales.ENGLISH]: "Clear",
                        [Locales.CHINESE]: "清空",
                        [Locales.CHINESE_TRADITIONAL]: "清空",
                    }),
                },
                metrics: {
                    hoverTitle: t({
                        [Locales.ENGLISH]: "Metrics",
                        [Locales.CHINESE]: "尺寸",
                        [Locales.CHINESE_TRADITIONAL]: "尺寸",
                    }),
                    hoverDesc: t({
                        [Locales.ENGLISH]: "Filter templates by their dimensions.",
                        [Locales.CHINESE]: "通过尺寸筛选模板。",
                        [Locales.CHINESE_TRADITIONAL]: "通過尺寸筛选模板。",
                    }),
                    currentValue: t({
                        [Locales.ENGLISH]: "Current Metrics are: ",
                        [Locales.CHINESE]: "当前尺寸：",
                        [Locales.CHINESE_TRADITIONAL]: "當前尺寸：",
                    }),
                    unitMono: t({ [Locales.ENGLISH]: "block",
                        [Locales.CHINESE]: "方块",
                        [Locales.CHINESE_TRADITIONAL]: "方塊",
                    }),
                    unitPoly: t({ [Locales.ENGLISH]: "blocks",
                        [Locales.CHINESE]: "方块",
                        [Locales.CHINESE_TRADITIONAL]: "方塊",
                    }),
                    logicAnd: t({ [Locales.ENGLISH]: "and",
                        [Locales.CHINESE]: "且",
                        [Locales.CHINESE_TRADITIONAL]: "且",
                    }),
                    clearBtn: t({
                        [Locales.ENGLISH]: "Clear",
                        [Locales.CHINESE]: "清空",
                        [Locales.CHINESE_TRADITIONAL]: "清空",
                    }),
                    
                }
            }
        },
        view: {
            filterTitle: t({
                [Locales.ENGLISH]: "Filters",
                [Locales.CHINESE]: "筛选器",
                [Locales.CHINESE_TRADITIONAL]: "篩選器",
            }),
            loadingMore: t({
                [Locales.ENGLISH]: "Loading more...",
                [Locales.CHINESE]: "加载中...",
                [Locales.CHINESE_TRADITIONAL]: "加載中...",
            }),
            noTemplates: t({
                [Locales.ENGLISH]: "No templates found.",
                [Locales.CHINESE]: "未找到相关模版。",
                [Locales.CHINESE_TRADITIONAL]: "未找到相關模版。",
            }),
            clearFilters: t({
                [Locales.ENGLISH]: "Clear all filters",
                [Locales.CHINESE]: "清空所有筛选条件",
                [Locales.CHINESE_TRADITIONAL]: "清空所有篩選條件",
            }),
            endOfResults: t({
                [Locales.ENGLISH]: "End of results",
                [Locales.CHINESE]: "已加载全部结果",
                [Locales.CHINESE_TRADITIONAL]: "已加載全部結果",
            }),
            loadMoreBtn: t({
                [Locales.ENGLISH]: "Load More",
                [Locales.CHINESE]: "加载更多",
                [Locales.CHINESE_TRADITIONAL]: "加載更多",
            }),
        },
        searchBar: {
            filterBtn: t({ [Locales.ENGLISH]: "Filters", [Locales.CHINESE]: "过滤", [Locales.CHINESE_TRADITIONAL]: "過濾" }),
            advancedTitle: t({ [Locales.ENGLISH]: "Advanced Filters", [Locales.CHINESE]: "高级筛选", [Locales.CHINESE_TRADITIONAL]: "高級篩選" }),
            pathPrefixLabel: t({ [Locales.ENGLISH]: "Path Prefix", [Locales.CHINESE]: "路径前缀", [Locales.CHINESE_TRADITIONAL]: "路徑前綴" }),
            pathPrefixDesc: t({
                [Locales.ENGLISH]: "Matches paths starting with this prefix.",
                [Locales.CHINESE]: "匹配以此前缀开头的路径。",
                [Locales.CHINESE_TRADITIONAL]: "匹配以此前綴開頭的路徑。"
            }),
            nameMatchLabel: t({ [Locales.ENGLISH]: "Name Matches", [Locales.CHINESE]: "名称匹配", [Locales.CHINESE_TRADITIONAL]: "名稱匹配" }),
            nameMatchDesc: t({
                [Locales.ENGLISH]: "Fuzzy match on template name.",
                [Locales.CHINESE]: "模糊匹配模版名称。",
                [Locales.CHINESE_TRADITIONAL]: "模糊匹配模版名稱。"
            }),
            clearAdvanced: t({ [Locales.ENGLISH]: "Clear Advanced Filters", [Locales.CHINESE]: "清空高级筛选", [Locales.CHINESE_TRADITIONAL]: "清空高級篩選" }),
            mainPlaceholder: t({ [Locales.ENGLISH]: "Search paths fuzzy...", [Locales.CHINESE]: "模糊搜索路径...", [Locales.CHINESE_TRADITIONAL]: "模糊搜索路徑..." }),
            resultsCount: t({ [Locales.ENGLISH]: "results", [Locales.CHINESE]: "个结果", [Locales.CHINESE_TRADITIONAL]: "個結果" }),
        }
    },
} satisfies Dictionary;

export default templateBrowserContent;