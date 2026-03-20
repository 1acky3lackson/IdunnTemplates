import { Locales, t, type Dictionary } from "intlayer";

const templateCollectionContent = {
  key: "template-collection",
  content: {
    title: t({
      [Locales.ENGLISH]: "Template Collections",
      [Locales.CHINESE]: "模板合集",
      [Locales.CHINESE_TRADITIONAL]: "模板合集",
    }),
    subtitle: t({
      [Locales.ENGLISH]: "Manage your organized groups of templates.",
      [Locales.CHINESE]: "管理您的模板合集。",
      [Locales.CHINESE_TRADITIONAL]: "管理您的模板合集。",
    }),
    columns: {
        name: t({
            [Locales.ENGLISH]: "Name",
            [Locales.CHINESE]: "名称",
            [Locales.CHINESE_TRADITIONAL]: "名稱",
        }),
        description: t({
            [Locales.ENGLISH]: "Description",
            [Locales.CHINESE]: "描述",
            [Locales.CHINESE_TRADITIONAL]: "描述",
        }),
        visibility: t({
            [Locales.ENGLISH]: "Visibility",
            [Locales.CHINESE]: "可见性",
            [Locales.CHINESE_TRADITIONAL]: "可見性",
        }),
        private: t({
            [Locales.ENGLISH]: "Private",
            [Locales.CHINESE]: "私有",
            [Locales.CHINESE_TRADITIONAL]: "私有",
        }),
        public: t({
            [Locales.ENGLISH]: "Public",
            [Locales.CHINESE]: "公开",
            [Locales.CHINESE_TRADITIONAL]: "公開",
        }),
        creator: t({
            [Locales.ENGLISH]: "Creator",
            [Locales.CHINESE]: "创建者",
            [Locales.CHINESE_TRADITIONAL]: "創建者",
        }),
        createdAt: t({
            [Locales.ENGLISH]: "Created At",
            [Locales.CHINESE]: "创建时间",
            [Locales.CHINESE_TRADITIONAL]: "創建時間",
        }),
        path: t({
            [Locales.ENGLISH]: "Path",
            [Locales.CHINESE]: "路径",
            [Locales.CHINESE_TRADITIONAL]: "路徑",
        }),
        uuid: t({
            [Locales.ENGLISH]: "UUID",
            [Locales.CHINESE]: "唯一标识码",
            [Locales.CHINESE_TRADITIONAL]: "唯一標識碼",
        }),
        bindChannel: t({
            [Locales.ENGLISH]: "Bind Channel",
            [Locales.CHINESE]: "绑定通道",
            [Locales.CHINESE_TRADITIONAL]: "綁定通道",
        }),
        bindChannelPlaceholder: t({
            [Locales.ENGLISH]: "custom",
            [Locales.CHINESE]: "定制",
            [Locales.CHINESE_TRADITIONAL]: "定制",
        }),
        bindRight: t({
            [Locales.ENGLISH]: "Right Click (right)",
            [Locales.CHINESE]: "右键 (right)",
            [Locales.CHINESE_TRADITIONAL]: "右鍵 (right)",
        }),
        bindLeft: t({
            [Locales.ENGLISH]: "Left Click (left)",
            [Locales.CHINESE]: "左键 (left)",
            [Locales.CHINESE_TRADITIONAL]: "左鍵 (left)",
        }),
        bindOther: t({
            [Locales.ENGLISH]: "Other...",
            [Locales.CHINESE]: "其他...",
            [Locales.CHINESE_TRADITIONAL]: "其他...",
        }),
    },
    actions: {
        random: t({
            [Locales.ENGLISH]: "Random",
            [Locales.CHINESE]: "随便看看",
            [Locales.CHINESE_TRADITIONAL]: "隨便看看",
        }),
        oneMore: t({
            [Locales.ENGLISH]: "One More",
            [Locales.CHINESE]: "再来一个",
            [Locales.CHINESE_TRADITIONAL]: "再來一個",
        }),
        add: t({
            [Locales.ENGLISH]: "Add",
            [Locales.CHINESE]: "添加",
            [Locales.CHINESE_TRADITIONAL]: "添加",
        }),
        cancel: t({
            [Locales.ENGLISH]: "Cancel",
            [Locales.CHINESE]: "取消",
            [Locales.CHINESE_TRADITIONAL]: "取消",
        }),
        create: t({
            [Locales.ENGLISH]: "Create",
            [Locales.CHINESE]: "创建",
            [Locales.CHINESE_TRADITIONAL]: "創建",
        }),
        save: t({
            [Locales.ENGLISH]: "Save",
            [Locales.CHINESE]: "保存",
            [Locales.CHINESE_TRADITIONAL]: "保存",
        }),
        remove: t({
            [Locales.ENGLISH]: "Remove",
            [Locales.CHINESE]: "移除",
            [Locales.CHINESE_TRADITIONAL]: "移除",
        }),
        addTemplate: t({
            [Locales.ENGLISH]: "Add Template",
            [Locales.CHINESE]: "添加模板",
            [Locales.CHINESE_TRADITIONAL]: "添加模板",
        }),
        copyCommand: t({
            [Locales.ENGLISH]: "Copy Command",
            [Locales.CHINESE]: "复制指令",
            [Locales.CHINESE_TRADITIONAL]: "複製指令",
        })
    },
    dialogs: {
        createCollectionTitle: t({
            [Locales.ENGLISH]: "Create Collection",
            [Locales.CHINESE]: "新建合集",
            [Locales.CHINESE_TRADITIONAL]: "新建合集",
        }),
        createCollectionDesc: t({
            [Locales.ENGLISH]: "Add a new template collection.",
            [Locales.CHINESE]: "添加一个新的模板合集。",
            [Locales.CHINESE_TRADITIONAL]: "添加一個新的模板合集。",
        }),
        editCollectionTitle: t({
            [Locales.ENGLISH]: "Edit Collection",
            [Locales.CHINESE]: "编辑合集",
            [Locales.CHINESE_TRADITIONAL]: "編輯合集",
        }),
        editCollectionDesc: t({
            [Locales.ENGLISH]: "Modify existing collection details.",
            [Locales.CHINESE]: "修改已有的合集信息。",
            [Locales.CHINESE_TRADITIONAL]: "修改已有的合集信息。",
        }),
        randomTemplateTitle: t({
            [Locales.ENGLISH]: "Random Selection",
            [Locales.CHINESE]: "随机选取",
            [Locales.CHINESE_TRADITIONAL]: "隨機選取",
        }),
        randomTemplateDesc: t({
            [Locales.ENGLISH]: "Discover a random template from this collection.",
            [Locales.CHINESE]: "从该合集中随机发现一个模板。",
            [Locales.CHINESE_TRADITIONAL]: "從該合集中隨機發現一個模板。",
        }),
        addTemplateTitle: t({
            [Locales.ENGLISH]: "Add Template",
            [Locales.CHINESE]: "添加模板",
            [Locales.CHINESE_TRADITIONAL]: "添加模板",
        }),
        addTemplateDesc: t({
            [Locales.ENGLISH]: "Enter the Template UUID to add to this collection.",
            [Locales.CHINESE]: "输入要添加到此合集的模板 UUID。",
            [Locales.CHINESE_TRADITIONAL]: "輸入要添加到此合集的模板 UUID。",
        }),
        uuidPlaceholder: t({
            [Locales.ENGLISH]: "e.g. 123e4567-e89b-12d3...",
            [Locales.CHINESE]: "例如 123e4567-e89b-12d3...",
            [Locales.CHINESE_TRADITIONAL]: "例如 123e4567-e89b-12d3...",
        }),
        onlyVisibleToYou: t({
            [Locales.ENGLISH]: "Only visible to you",
            [Locales.CHINESE]: "仅对您可见",
            [Locales.CHINESE_TRADITIONAL]: "僅對您可見",
        }),
        confirmDeleteCollection: t({
            [Locales.ENGLISH]: "Are you sure you want to delete this collection?",
            [Locales.CHINESE]: "您确定要删除此合集吗？",
            [Locales.CHINESE_TRADITIONAL]: "您確定要刪除此合集嗎？",
        }),
        confirmRemoveTemplate: t({
            [Locales.ENGLISH]: "Remove this template from the collection?",
            [Locales.CHINESE]: "从合集中移除此模板吗？",
            [Locales.CHINESE_TRADITIONAL]: "從合集中移除此模板嗎？",
        })
    },
    messages: {
        loading: t({
            [Locales.ENGLISH]: "Loading Collection...",
            [Locales.CHINESE]: "加载合集中...",
            [Locales.CHINESE_TRADITIONAL]: "加載合集中...",
        }),
        createSuccess: t({
            [Locales.ENGLISH]: "Collection created successfully",
            [Locales.CHINESE]: "合集创建成功",
            [Locales.CHINESE_TRADITIONAL]: "合集創建成功",
        }),
        createFailed: t({
            [Locales.ENGLISH]: "Failed to create collection",
            [Locales.CHINESE]: "创建合集失败",
            [Locales.CHINESE_TRADITIONAL]: "創建合集失敗",
        }),
        updateSuccess: t({
            [Locales.ENGLISH]: "Collection updated successfully",
            [Locales.CHINESE]: "合集更新成功",
            [Locales.CHINESE_TRADITIONAL]: "合集更新成功",
        }),
        updateFailed: t({
            [Locales.ENGLISH]: "Failed to update collection",
            [Locales.CHINESE]: "合集更新失败",
            [Locales.CHINESE_TRADITIONAL]: "合集更新失敗",
        }),
        deleteSuccess: t({
            [Locales.ENGLISH]: "Collection deleted successfully",
            [Locales.CHINESE]: "合集删除成功",
            [Locales.CHINESE_TRADITIONAL]: "合集刪除成功",
        }),
        deleteFailed: t({
            [Locales.ENGLISH]: "Failed to delete collection",
            [Locales.CHINESE]: "合集删除失败",
            [Locales.CHINESE_TRADITIONAL]: "合集刪除失敗",
        }),
        removeTemplateSuccess: t({
            [Locales.ENGLISH]: "Template removed from collection",
            [Locales.CHINESE]: "模板已从合集移除",
            [Locales.CHINESE_TRADITIONAL]: "模板已從合集移除",
        }),
        removeTemplateFailed: t({
            [Locales.ENGLISH]: "Failed to remove template",
            [Locales.CHINESE]: "移除模板失败",
            [Locales.CHINESE_TRADITIONAL]: "移除模板失敗",
        }),
        addTemplateSuccess: t({
            [Locales.ENGLISH]: "Template added successfully",
            [Locales.CHINESE]: "模板添加成功",
            [Locales.CHINESE_TRADITIONAL]: "模板添加成功",
        }),
        addTemplateFailed: t({
            [Locales.ENGLISH]: "Failed to add template",
            [Locales.CHINESE]: "模板添加失败",
            [Locales.CHINESE_TRADITIONAL]: "模板添加失敗",
        }),
        randomTemplateFailed: t({
            [Locales.ENGLISH]: "Failed to fetch random template",
            [Locales.CHINESE]: "获取随机模板失败",
            [Locales.CHINESE_TRADITIONAL]: "獲取隨機模板失敗",
        }),
        noTemplateFound: t({
            [Locales.ENGLISH]: "No valid template found in this collection.",
            [Locales.CHINESE]: "当前合集中暂无有效模板。",
            [Locales.CHINESE_TRADITIONAL]: "當前合集中暫無有效模板。",
        }),
        commandCopied: t({
            [Locales.ENGLISH]: "Command copied to clipboard",
            [Locales.CHINESE]: "指令已复制到剪贴板",
            [Locales.CHINESE_TRADITIONAL]: "指令已複製到剪貼簿",
        })
    }
  },
} satisfies Dictionary;

export default templateCollectionContent;
