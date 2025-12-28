# Template Brushes - Implementation Details

本文档详细描述了 `Idunn Template` 项目中 **Phase 4: 笔刷系统 (Brush System)** 的具体实现步骤。该系统允许玩家将模板集合绑定到物品上，通过射线追踪进行远程放置。

我们将实现过程分为五个子阶段，按照依赖关系逐步构建。

---

## Phase 4.1: 笔刷数据模型与基础架构 (Data Model & Infrastructure)

**目标**: 建立笔刷的数据结构，实现笔刷配置与玩家/物品的关联，并集成到现有的 `PlayerPreference` 中。

### 功能点与细节

1.  **定义 `BrushSettings` 类**:
    *   这是一个 POJO (Plain Old Java Object)，用于描述一个笔刷实例的所有属性。
    *   **包含字段**:
        *   `TemplateSet content`: 笔刷的内容源（复用现有的 `TemplateSet` 逻辑，包含 sources, weights）。
        *   `RotationMode rotation`: 枚举 (FIXED_0, FIXED_90..., RANDOM, INHERIT)。
        *   `FlipMode flipX/Z`: 枚举 (TRUE, FALSE, RANDOM, INHERIT)。
        *   `boolean noAir`: 是否排除空气 (默认 true)。
        *   `boolean emptyOnly`: 是否只在非空方块放置 (默认 true)。
    *   **方法**: 提供解析指令参数 (flags) 并更新属性的方法。

2.  **定义 `BrushSession` 类**:
    *   用于管理玩家当前手持物品的笔刷状态。
    *   **包含字段**:
        *   `Map<String, BrushSettings> channels`: 键为通道名 (如 "left", "right", "custom_1")，值为对应的笔刷设置。

3.  **扩展 `PlayerPreference`**:
    *   在 `PlayerPreference` 中添加字段 `Map<String, BrushSession> boundBrushes`。
    *   **键**: 物品的材质名称（Material Name，如 "IRON_SWORD"）。
    *   **值**: 该材质对应的笔刷会话数据。
    *   **持久化**: 确保这些数据能随 `PlayerPreference` 被 Gson 正确序列化/反序列化。

4.  **物品标识机制 (Item Identification)**:
    *   **变更**: 不再使用 Item UUID。
    *   **逻辑**: 直接读取玩家手持物品的 `Material.name()` 作为查找键。
    *   这意味着如果玩家绑定了 `IRON_SWORD`，所有 `IRON_SWORD` 类型的物品都会继承该笔刷配置。

### 实现要求
*   **依赖**: `TemplateSet`, `PlayerPreference`。
*   **位置**: `core/domain/brush/`。
*   **测试**: 创建一个 `BrushSettings`，并将其绑定到 "DIAMOND_HOE"，验证配置保存。

---

## Phase 4.2: 基础绑定与放置逻辑 (Binding & Placement)

**目标**: 实现 `/idunn brush bind` 指令（仅处理 set/path 绑定），并实现基于交互事件的射线追踪放置。

### 功能点与细节

1.  **指令解析器 (Flag Parser)**:
    *   实现一个工具类，用于解析 unix 风格的 flags (`-r`, `-x`, `-xz` 等)。
    *   输入: `String[] args`。
    *   输出: `BrushProperties` (临时对象) 或直接修改 `BrushSettings`。

2.  **绑定指令 (`/idunn brush bind`)**:
    *   **子命令**: `right`, `left` (暂时只支持这两个标准通道)。
    *   **参数**: `path <path>` 或 `set <name>`。
    *   **逻辑**:
        1.  获取玩家手持物品。如果是 `AIR`，则提示错误。
        2.  获取物品的 `Material.name()`。
        3.  在 `PlayerPreference` 中查找或创建该 Material 对应的 `BrushSession`。
        4.  在 `BrushSession` 中创建或更新对应通道 ("left"/"right") 的 `BrushSettings`。
        5.  解析 flags 并应用到设置中。
        6.  如果是 `set`，解析并克隆该 Set；如果是 `path`，创建一个包含该 path 的新 Set。

3.  **解绑指令 (`/idunn brush unbind`)**:
    *   获取手持物品 Material，移除对应通道的配置。

4.  **交互监听与放置 (Interaction & RayTrace)**:
    *   监听 `PlayerInteractEvent` (Right Click / Left Click)。
    *   获取手持物品的 Material。
    *   检查 `PlayerPreference` 中该 Material 是否有绑定的笔刷。
    *   **射线追踪**:
        *   使用 `player.rayTraceBlocks(maxDistance)`。
        *   获取目标方块 (`HitBlock`)。
    *   **放置逻辑**:
        *   从 `BrushSettings` 的 `content` (TemplateSet) 中随机选取模板。
        *   计算最终的旋转/翻转 (结合笔刷设置和随机逻辑)。
        *   调用 `InstanceManager.placeInstanceAndReturn` 在目标方块位置放置。
        *   **注意**: 放置位置应为目标方块的中心或上方，取决于具体需求（通常笔刷是点哪放哪）。

### 实现要求
*   **依赖**: `CommandGroup`, `SetManager`, `InstanceManager`。
*   **性能**: 射线追踪应限制最大距离（如 100 格）。
*   **防误触**: 左键绑定笔刷时，应取消方块破坏事件 (`event.setCancelled(true)`)。

---

## Phase 4.3: 高级属性与自定义通道 (Advanced Props & Channels)

**目标**: 完善笔刷的自定义能力，支持自定义通道、属性修改和内容源管理。

### 功能点与细节

1.  **扩展绑定指令**:
    *   支持自定义通道名: `/idunn brush bind <channel_name> ...`。
    *   验证通道名格式（字母数字）。

2.  **触发指令 (`/idunn brush trigger`)**:
    *   参数: `<channel_name>`。
    *   逻辑: 模拟一次放置操作，使用指定通道的配置，进行射线追踪并放置。
    *   用途: 供按键绑定 Mod 调用。

3.  **属性修改指令 (`/idunn brush modify`)**:
    *   格式: `<channel> <prop> <value>`。
    *   支持属性: `rotate`, `flipx`, `flipz`, `noair`, `emptyonly`。
    *   逻辑: 直接更新内存中的 `BrushSettings` 并保存 Session。

4.  **源管理指令 (`/idunn brush source`)**:
    *   `add`: 向笔刷内部的 `TemplateSet` 添加新的 path/set 来源。
    *   `list`: 打印当前笔刷的所有来源（带删除按钮）。
    *   这需要复用 `SetsAddCommand` 等逻辑，但作用对象是 `BrushSettings` 中的 Set。

5.  **默认笔刷 (Wand 集成)**:
    *   在 `PlayerInteractEvent` 中，如果物品是魔杖且未绑定显式的 "right" 通道：
    *   动态构建一个临时的 `BrushSettings`，其内容和属性直接引用玩家当前的全局 `TemplateSet`。
    *   执行放置逻辑。

### 实现要求
*   **重构**: 将 `TemplateSet` 的操作逻辑抽象出来，使其既能用于全局 Set，也能用于笔刷内的 Set。

---

## Phase 4.4: 笔刷预设系统 (Brush Presets)

**目标**: 允许玩家保存和加载复杂的笔刷配置（包含所有通道）。

### 功能点与细节

1.  **数据模型 `BrushPreset`**:
    *   包含: `String name`, `String creator`, `Map<String, BrushSettings> channels`。
    *   存储: 类似于 `TemplateSet`，存储在 `presets/brushes/` 目录下（支持命名空间）。

2.  **预设管理器 `BrushPresetManager`**:
    *   负责加载/保存 JSON 文件。
    *   处理命名空间逻辑 (`global`, `player.<name>`)。

3.  **指令实现**:
    *   `preset save`: 将当前物品的所有通道配置打包保存。
    *   `preset update`: 更新已存在的预设。
    *   `preset load all`: 覆盖当前物品的所有通道。
    *   `preset load channel`: 仅加载预设中的特定通道到当前物品的指定通道。

### 实现要求
*   **权限**: 检查 `idunn.brush.preset.save.<namespace>`。
*   **复用**: 尽可能复用 `SetManager` 的命名空间解析逻辑。

---

## Phase 4.5: 可视化与 UI (Visuals & Polish)

**目标**: 通过 BossBar 提供清晰的笔刷状态反馈。

### 功能点与细节

1.  **笔刷 BossBar 渲染逻辑**:
    *   在 `EffectManager` 中添加笔刷状态检测。
    *   **优先级**: 如果玩家手持绑定了笔刷的物品，**隐藏** 默认的 Set BossBar，**显示** 笔刷 BossBar。

2.  **多通道显示**:
    *   每个活跃通道显示一行 BossBar。
    *   如果是自定义通道（非 Left/Right），可能只在最近触发过或切换到该模式时显示（避免刷屏）。或者只显示 Left/Right 两个主通道。

3.  **格式化字符串**:
    *   实现文档中描述的格式: `<CHANNEL> | next: <TEMPLATE> | <SETTINGS>`。
    *   **Settings 格式化**:
        *   `R-<val>`: 旋转。
        *   `F-XZ`: 翻转 (根据状态变色: 绿=开启, 红=关闭, 橙=随机)。
        *   `noair`, `emptyOnly`: 仅在 true 时显示。

4.  **下一放置预览 (Next Preview)**:
    *   为笔刷也维护一个 `NextPlacement` 状态（类似于 Set）。
    *   当笔刷配置改变或放置后，重新计算下一个随机模板和旋转参数，并在 BossBar 中显示。

### 实现要求
*   **性能**: BossBar 更新频率不宜过高，但需响应及时。
*   **颜色**: 使用 `ChatColor` 丰富显示效果。
