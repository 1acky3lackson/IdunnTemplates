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
      [Locales.ENGLISH]:
        "Modern template management platform for Minecraft Servers.",
      [Locales.CHINESE]: "专为 Minecraft 服务器打造的现代化模板管理平台。",
      [Locales.CHINESE_TRADITIONAL]:
        "專為 Minecraft 伺服器打造的現代化模板管理平台。",
    }),
    menus: {
      templates: t({
        [Locales.ENGLISH]: "Templates",
        [Locales.CHINESE]: "模板",
        [Locales.CHINESE_TRADITIONAL]: "模板",
      }),
      tags: t({
        [Locales.ENGLISH]: "Tags",
        [Locales.CHINESE]: "标签",
        [Locales.CHINESE_TRADITIONAL]: "標籤",
      }),
      sets: t({
        [Locales.ENGLISH]: "Sets",
        [Locales.CHINESE]: "合集",
        [Locales.CHINESE_TRADITIONAL]: "合集",
      }),
      brushes: t({
        [Locales.ENGLISH]: "Brushes",
        [Locales.CHINESE]: "笔刷",
        [Locales.CHINESE_TRADITIONAL]: "筆刷",
      }),
      utilities: t({
        [Locales.ENGLISH]: "Utilities",
        [Locales.CHINESE]: "工具",
        [Locales.CHINESE_TRADITIONAL]: "工具",
      }),
      commercial: t({
        [Locales.ENGLISH]: "Commercial",
        [Locales.CHINESE]: "商业化",
        [Locales.CHINESE_TRADITIONAL]: "商业化",
      }),
      templateManagement: t({
        [Locales.ENGLISH]: "Template Management",
        [Locales.CHINESE]: "模板与合集管理",
        [Locales.CHINESE_TRADITIONAL]: "模板與合集管理",
      }),
      projectCollab: t({
        [Locales.ENGLISH]: "Project Collaboration",
        [Locales.CHINESE]: "项目与商品管理",
        [Locales.CHINESE_TRADITIONAL]: "項目與商品管理",
      }),
      revenueSharing: t({
        [Locales.ENGLISH]: "Revenue Sharing",
        [Locales.CHINESE]: "收益分成管理",
        [Locales.CHINESE_TRADITIONAL]: "收益分成管理",
      }),
      financial: t({
        [Locales.ENGLISH]: "Virtual Points",
        [Locales.CHINESE]: "虚拟点数管理",
        [Locales.CHINESE_TRADITIONAL]: "虛擬點數管理",
      }),
      netease: t({
        [Locales.ENGLISH]: "Business",
        [Locales.CHINESE]: "业务",
        [Locales.CHINESE_TRADITIONAL]: "業務",
      }),
      system: t({
        [Locales.ENGLISH]: "System",
        [Locales.CHINESE]: "系统管理",
        [Locales.CHINESE_TRADITIONAL]: "系統管理",
      }),
    },
    items: {
      searchTemplates: t({
        [Locales.ENGLISH]: "Search Templates",
        [Locales.CHINESE]: "搜索模板",
        [Locales.CHINESE_TRADITIONAL]: "搜尋模板",
      }),
      searchTemplatesDesc: t({
        [Locales.ENGLISH]:
          "Browse, search and filter templates we have available.",
        [Locales.CHINESE]: "浏览、搜索并筛选我们提供的所有模板。",
        [Locales.CHINESE_TRADITIONAL]: "瀏覽、搜尋並篩選我們提供的所有模板。",
      }),
      viewFolders: t({
        [Locales.ENGLISH]: "View Folders",
        [Locales.CHINESE]: "查看目录",
        [Locales.CHINESE_TRADITIONAL]: "查看目錄",
      }),
      viewFoldersDesc: t({
        [Locales.ENGLISH]: "Lookup templates using the folder system.",
        [Locales.CHINESE]: "使用文件夹系统快速查找模板。",
        [Locales.CHINESE_TRADITIONAL]: "使用資料夾系統快速查找模板。",
      }),
      browseTags: t({
        [Locales.ENGLISH]: "Browse Tags",
        [Locales.CHINESE]: "浏览标签",
        [Locales.CHINESE_TRADITIONAL]: "瀏覽標籤",
      }),
      browseTagsDesc: t({
        [Locales.ENGLISH]: "Use tags to categorize and find templates easily.",
        [Locales.CHINESE]: "使用标签对模板进行分类，轻松找到所需内容。",
        [Locales.CHINESE_TRADITIONAL]:
          "使用標籤對模板進行分類，輕鬆找到所需內容。",
      }),
      // Sets 部分
      browseSets: t({
        [Locales.ENGLISH]: "Browse Sets",
        [Locales.CHINESE]: "浏览合集",
        [Locales.CHINESE_TRADITIONAL]: "瀏覽合集",
      }),
      browseSetsDesc: t({
        [Locales.ENGLISH]: "Browse all available template sets.",
        [Locales.CHINESE]: "浏览所有可用的模板合集。",
        [Locales.CHINESE_TRADITIONAL]: "瀏覽所有可用的模板合集。",
      }),
      mySets: t({
        [Locales.ENGLISH]: "My Template Sets",
        [Locales.CHINESE]: "我的合集",
        [Locales.CHINESE_TRADITIONAL]: "我的合集",
      }),
      mySetsDesc: t({
        [Locales.ENGLISH]: "View and manage your personal sets.",
        [Locales.CHINESE]: "查看并管理您的个人模板合集。",
        [Locales.CHINESE_TRADITIONAL]: "查看並管理您的個人模板合集。",
      }),
      createSet: t({
        [Locales.ENGLISH]: "Create New Set",
        [Locales.CHINESE]: "创建合集",
        [Locales.CHINESE_TRADITIONAL]: "創建合集",
      }),
      createSetDesc: t({
        [Locales.ENGLISH]: "Create a new template set.",
        [Locales.CHINESE]: "创建一个新的模板合集。",
        [Locales.CHINESE_TRADITIONAL]: "創建一個新的模板合集。",
      }),
      // Brushes 部分
      browseBrushes: t({
        [Locales.ENGLISH]: "Browse Brushes",
        [Locales.CHINESE]: "浏览笔刷",
        [Locales.CHINESE_TRADITIONAL]: "瀏覽刷子",
      }),
      browseBrushesDesc: t({
        [Locales.ENGLISH]: "Browse all available brushes.",
        [Locales.CHINESE]: "浏览所有可用的刷子。",
        [Locales.CHINESE_TRADITIONAL]: "瀏覽所有可用的刷子。",
      }),
      myBrushes: t({
        [Locales.ENGLISH]: "My Brushes",
        [Locales.CHINESE]: "我的笔刷",
        [Locales.CHINESE_TRADITIONAL]: "我的刷子",
      }),
      myBrushesDesc: t({
        [Locales.ENGLISH]: "View and manage your personal brushes.",
        [Locales.CHINESE]: "查看并管理您的个人笔刷。",
        [Locales.CHINESE_TRADITIONAL]: "查看並管理您的個人筆刷。",
      }),
      createBrush: t({
        [Locales.ENGLISH]: "Create New Brush",
        [Locales.CHINESE]: "创建笔刷",
        [Locales.CHINESE_TRADITIONAL]: "創建刷子",
      }),
      createBrushDesc: t({
        [Locales.ENGLISH]: "Create a new brush.",
        [Locales.CHINESE]: "创建一个新的笔刷工具。",
        [Locales.CHINESE_TRADITIONAL]: "創建一個新的筆刷工具。",
      }),
      // Utilities 部分
      resize: t({
        [Locales.ENGLISH]: "Resize Player",
        [Locales.CHINESE]: "缩放玩家大小",
        [Locales.CHINESE_TRADITIONAL]: "調整大小工具",
      }),
      resizeDesc: t({
        [Locales.ENGLISH]: "Resize player models easily.",
        [Locales.CHINESE]: "轻松调整玩家模型的大小。",
        [Locales.CHINESE_TRADITIONAL]: "輕鬆調整玩家模型的大小。",
      }),
      commercial: {
        balances: {
          title: t({
            [Locales.ENGLISH]: "User Virtual Points",
            [Locales.CHINESE]: "用户虚拟点数",
            [Locales.CHINESE_TRADITIONAL]: "用戶虛擬點數",
          }),
          desc: t({
            [Locales.ENGLISH]: "View user virtual points and related data",
            [Locales.CHINESE]: "查看用户的虚拟点数及相关数据",
            [Locales.CHINESE_TRADITIONAL]: "查看用戶的虛擬點數及相關數據",
          }),
        },
        checkout: {
          title: t({
            [Locales.ENGLISH]: "Checkout Records",
            [Locales.CHINESE]: "收益分成记录",
            [Locales.CHINESE_TRADITIONAL]: "收益分成記錄",
          }),
          desc: t({
            [Locales.ENGLISH]:
              "View records of order settlements to user earnings",
            [Locales.CHINESE]: "查看订单分成到用户收益的记录",
            [Locales.CHINESE_TRADITIONAL]: "查看訂單分成到用戶收益的記錄",
          }),
        },
        transactions: {
          title: t({
            [Locales.ENGLISH]: "Virtual Point Change Records",
            [Locales.CHINESE]: "虚拟点数变动",
            [Locales.CHINESE_TRADITIONAL]: "虛擬點數變動記錄",
          }),
          desc: t({
            [Locales.ENGLISH]: "View the list of user virtual point change records",
            [Locales.CHINESE]: "查看用户虚拟点数变动记录列表",
            [Locales.CHINESE_TRADITIONAL]: "查看用戶虛擬點數變動記錄列表",
          }),
        },
        globalParams: {
          title: t({
            [Locales.ENGLISH]: "Global Settlement Parameters",
            [Locales.CHINESE]: "全局结算参数",
            [Locales.CHINESE_TRADITIONAL]: "全局結算參數",
          }),
          desc: t({
            [Locales.ENGLISH]: "View or modify global settlement parameters",
            [Locales.CHINESE]: "查看或修改全局的结算参数",
            [Locales.CHINESE_TRADITIONAL]: "查看或修改全局的結算參數",
          }),
        },
        projects: {
          title: t({
            [Locales.ENGLISH]: "Construction Projects",
            [Locales.CHINESE]: "建造项目",
            [Locales.CHINESE_TRADITIONAL]: "建造項目",
          }),
          desc: t({
            [Locales.ENGLISH]:
              "View the list of projects for commercialization",
            [Locales.CHINESE]: "查看用于商业化的项目列表",
            [Locales.CHINESE_TRADITIONAL]: "查看用於商業化的項目列表",
          }),
        },
        neteaseProducts: {
          title: t({
            [Locales.ENGLISH]: "Products",
            [Locales.CHINESE]: "商品管理",
            [Locales.CHINESE_TRADITIONAL]: "商品管理",
          }),
          desc: t({
            [Locales.ENGLISH]:
              "View all listed products",
            [Locales.CHINESE]: "管理系统内用户录入的商品",
            [Locales.CHINESE_TRADITIONAL]: "管理系統內使用者錄入的商品",
          }),
        },
        neteaseOrders: {
          title: t({
            [Locales.ENGLISH]: "Sales Orders",
            [Locales.CHINESE]: "订单管理",
            [Locales.CHINESE_TRADITIONAL]: "訂單管理",
          }),
          desc: t({
            [Locales.ENGLISH]:
              "View all product orders",
            [Locales.CHINESE]: "查看商城中商品的全部订单",
            [Locales.CHINESE_TRADITIONAL]: "查看商城中商品的全部訂單",
          }),
        },
        neteaseWithdraw: {
          title: t({
            [Locales.ENGLISH]: "Processing Records",
            [Locales.CHINESE]: "处理记录",
            [Locales.CHINESE_TRADITIONAL]: "處理記錄",
          }),
          desc: t({
            [Locales.ENGLISH]: "View processing records and usage details",
            [Locales.CHINESE]: "查看处理记录及其使用情况",
            [Locales.CHINESE_TRADITIONAL]: "查看處理記錄及其使用情況",
          }),
        },
        systemWithdraw: {
          title: t({
            [Locales.ENGLISH]: "Processing Console",
            [Locales.CHINESE]: "处理流程管理",
            [Locales.CHINESE_TRADITIONAL]: "處理流程管理",
          }),
          desc: t({
            [Locales.ENGLISH]: "Manage and review user processing requests",
            [Locales.CHINESE]: "管理和审批用户的处理请求",
            [Locales.CHINESE_TRADITIONAL]: "管理和審批用戶的處理請求",
          }),
        },
        servers: {
          title: t({
            [Locales.ENGLISH]: "Server Management",
            [Locales.CHINESE]: "服务器管理",
            [Locales.CHINESE_TRADITIONAL]: "伺服器管理",
          }),
          desc: t({
            [Locales.ENGLISH]: "Manage trusted servers and Tokens",
            [Locales.CHINESE]: "管理受信任的服务器以及 Token",
            [Locales.CHINESE_TRADITIONAL]: "管理受信任的伺服器以及 Token",
          }),
        },
      },
    },
  },
} satisfies Dictionary;

export default navigationContent;
