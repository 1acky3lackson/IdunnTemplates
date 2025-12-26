# Idunn Template - Implementation Phases

本文档将 `Idunn Template` 项目拆分为五个较为独立的开发阶段。每个阶段都建立在前一个阶段的基础上，旨在逐步构建一个稳定、可扩展的动态模板系统。

## Phase 1: 核心基础与模板管理 (Core & Template Management)

**目标**: 建立项目骨架，实现核心数据结构，允许用户将 WorldEdit 选区保存为带有版本控制的模板。

### 实现目标
1.  **项目脚手架**: 配置 Gradle/Maven，引入 WorldEdit/FastAsyncWorldEdit 依赖。
2.  **文件系统抽象**: 实现模板文件的存储路径解析逻辑（`<base>/users/...` vs `<base>/dir/...`）。
3.  **模板数据模型**:
    *   定义 `Template` 类与 `TemplateVersion` 类。
    *   实现 `metadata.json` 的序列化/反序列化 (Gson/Jackson)。
4.  **指令系统 (部分)**:
    *   `/idunn save <name>`: 将当前 WorldEdit 选区保存为模板。
    *   需处理 `.schem` 文件的物理存储。
    *   自动生成初始版本号 (base64 timestamp)。
5.  **权限系统基础**: 实现 `idunn.template.save.*` 的权限检查逻辑。

### 具体实现方法
*   创建一个 `TemplateManager` 单例，负责管理所有已加载的模板元数据。
*   使用 WorldEdit API (`ClipboardFormat`, `BlockArrayClipboard`) 进行 Schematic 的读写。
*   **关键类**:
    *   `TemplateStorage`: 处理磁盘 I/O，路径映射。
    *   `TemplateMetadata`: POJO，对应 `metadata.json`。
*   **测试点**: 成功保存一个选区，并能在磁盘上看到正确的目录结构和 JSON/Schem 文件。

---

## Phase 2: 实例存储与静态放置 (Instance Storage & Static Placement)

**目标**: 实现实例 (Instance) 的数据结构与空间索引存储，允许玩家将模板“实例化”到世界中（暂不包含自动更新逻辑）。

### 实现目标
1.  **实例数据模型**: 定义 `Instance` 类，包含基准坐标、旋转/翻转状态、版本历史等。
2.  **空间分区存储 (Spatial Partitioning)**:
    *   实现 8x8 区块的分区算法 (`PartitionKey` 计算)。
    *   实现 `InstanceManager`，负责根据坐标加载/卸载对应的分区文件。
    *   **异步 I/O**: 确保 JSON 的读写在后台线程进行，主线程仅操作内存缓存。
3.  **放置指令**:
    *   `/idunn place <template_path>`: 在玩家当前位置放置模板实例。
    *   支持基本的旋转/翻转参数。
    *   放置后立即在内存和磁盘中记录实例数据。
4.  **区块加载监听**: 监听 `ChunkLoadEvent`，预加载实例数据。

### 具体实现方法
*   **存储格式**: `instances/<world>/r.<x>.<z>.json`。
*   **放置逻辑**:
    *   读取模板的最新 `.schem`。
    *   应用旋转/翻转变换 (使用 WorldEdit 的 `AffineTransform`)。
    *   使用 `EditSession` 将方块粘贴到世界。
    *   创建 `Instance` 对象并注册到 `InstanceManager`。
*   **关键类**:
    *   `InstanceRepository`: 负责分区文件的读写。
    *   `ChunkListener`: 触发数据加载。
*   **测试点**: 放置模板后重启服务器，实例数据依然存在；查看 JSON 文件确认坐标正确。

---

## Phase 3: 核心更新算法 (The Update Core)

**目标**: 实现“保守更新”算法，这是本插件最核心的逻辑，确保模板更新不破坏玩家的修改。

### 实现目标
1.  **更新检测**: 当实例加载或手动触发时，检测当前实例版本是否落后于模板最新版本。
2.  **三方比对算法 (Diff Algorithm)**:
    *   输入: `OldSchem`, `NewSchem`, `WorldRegion`。
    *   逻辑: 实现文档中的 `bo` (旧), `bn` (新), `br` (实) 比对逻辑。
    *   支持 `Structural_Void` 和 `Air` 的特殊处理。
    *   支持忽略容器 NBT 的模糊比对。
3.  **版本提交指令**:
    *   `/idunn commit <message>`: 允许玩家在原位置提交新版本。
    *   提交后触发该模板下所有活跃实例的更新。

### 具体实现方法
*   **算法优化**:
    *   比对过程可能涉及大量方块，需分批处理或限制单次操作的计算量（如果主线程卡顿）。
    *   利用 WorldEdit 的 `Extent` 接口来抽象读取操作。
*   **变换处理**:
    *   在比对前，需计算出 `OldSchem` 和 `NewSchem` 在当前世界坐标系下的准确映射（考虑实例已有的旋转/翻转）。
*   **关键类**:
    *   `UpdateCalculator`: 纯逻辑类，输入三个 BlockAccess，输出变更列表。
    *   `TemplateUpdater`: 编排更新流程，执行方块变更。
*   **测试点**:
    *   场景 1: 玩家修改了实例的一面墙，更新模板（墙没变），玩家修改保留。
    *   场景 2: 模板增加了一把椅子，更新后实例中出现椅子。

---

## Phase 4: 笔刷系统与便捷工具 (Brush & Tools)

**目标**: 提高构建效率，提供类似于 WorldEdit 笔刷的工具，支持随机化放置。

### 实现目标
1.  **笔刷绑定**:
    *   `/idunn brush <template_dir>`: 将目录绑定到手中物品。
    *   支持参数: `-r` (随机旋转), `-f` (随机翻转)。
2.  **随机选择逻辑**:
    *   解析目录及其子目录下所有可用模板。
    *   根据权重（如果有）或均匀随机选择一个模板进行放置。
3.  **交互事件**:
    *   监听 `PlayerInteractEvent`，触发笔刷放置逻辑。

### 具体实现方法
*   利用 metadata 存储玩家手中的物品绑定的笔刷设置（或存储在 NBT 中）。
*   复用 Phase 2 的放置逻辑，但在调用前动态计算旋转/翻转参数。
*   **关键类**:
    *   `BrushManager`: 管理玩家的笔刷会话。
    *   `TemplateSelector`: 负责递归查找和随机选择模板。

---

## Phase 5: 完整性与集成 (Integrity & Polish)

**目标**: 处理边缘情况，集成 WorldEdit Undo，完善删除逻辑，优化性能。

### 实现目标
1.  **WorldEdit Undo 集成**:
    *   尝试监听 WorldEdit 事件（如 `EditSessionEvent`）。
    *   或者实现 `/idunn undo`: 专门撤销上一次 Idunn 操作，并同步删除实例数据。
2.  **软删除系统**:
    *   实现实例和模板的软删除标记逻辑。
    *   实现指令 `/idunn delete instance` 和 `/idunn delete template`。
3.  **性能优化**:
    *   对 `metadata.json` 和实例 JSON 增加缓存层。
    *   确保大批量更新时服务器 TPS 稳定（任务分片）。
4.  **权限完善**:
    *   全面测试所有细粒度权限 (`idunn.instance.delete.player.<name>` 等)。

### 具体实现方法
*   **Undo 策略**: 记录每次放置操作生成的 `InstanceID` 列表。当执行 undo 时，查找对应的 ID 并标记为删除。
*   **任务调度**: 使用 Bukkit 的 `Scheduler` 将大型更新任务拆分为多个 tick 执行。
*   **最终验收**: 进行全流程测试（创建 -> 放置 -> 修改实例 -> 更新模板 -> 自动更新 -> 撤销）。
