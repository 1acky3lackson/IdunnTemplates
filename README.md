<p align="center">
  <img src="design/logo.svg" width="160" alt="IdunnTemplates Logo" />
</p>

# IdunnTemplates

IdunnTemplates 是一个基于 Minecraft Spigot/Paper 的高级建筑模板与实例管理插件。它旨在解决大型建筑项目中重复结构的快速复用、版本管理以及非破坏性编辑问题。通过引入“模板（Template）”和“实例（Instance）”的概念，让建筑师能够像使用类和对象一样管理建筑结构。

## 核心解决的问题

1.  **重复结构的快速铺设**：通过将建筑保存为模板，可以快速在世界各地通过“实例”的方式放置，无需每次手动复制粘贴。
2.  **源头更新，全局同步**：所有实例都关联到同一个模板。一旦修改并提交了模板的母版（Master），所有关联的实例都会自动更新，极大地降低了修改成本。
3.  **非破坏性编辑**：实例的放置和更新通过 WorldEdit API 进行，支持 Undo 操作。
4.  **多样化放置**：支持模板组（Template Set），允许定义多个模板及其权重，随机放置以丰富建筑多样性。
5.  **灵活的远程构建**：引入**模板笔刷系统**，通过**多通道（Channels）**绑定（如左/右键及自定义按键），让单个工具能承载多种构建逻辑；通过**笔刷预设（Presets）**，一键切换复杂的笔刷配置。
6.  **可视化辅助**：提供粒子特效和 BossBar/ActionBar 提示，直观显示模板范围、当前选中的模板组信息以及放置预览。
7.  **权限与安全**：基于权限系统的操作控制，以及基于 WorldEdit Mask 的放置保护（如仅限空方块放置）。

## 主要功能

*   **模板管理**：创建、保存、更新模板。模板拥有版本控制，始终保持最新。
*   **实例系统**：基于模板放置实例。实例记录了位置、旋转、翻转状态。
*   **模板组（Set）**：支持将多个模板组合，设置权重，实现随机放置。支持命名空间（全局/个人）和递归引用。
*   **模板笔刷（Brush）**：
    *   **多通道绑定**：将不同的 Set/Path 绑定到物品的 Left/Right 或自定义通道。
    *   **笔刷预设**：保存和加载包含多个通道配置的复杂笔刷设置，支持跨玩家共享。
    *   **可视化预览**：BossBar 实时显示笔刷状态及“下一次放置”的预览信息。
*   **智能交互**：
    *   **魔杖（Wand）**：绑定物品作为魔杖，右键快速放置实例。
    *   **粒子特效**：显示模板母版范围、实例范围、放置预览。
    *   **Action Bar / Boss Bar**：实时显示当前操作状态和目标信息。
*   **智能指令**：
    *   **Smart Commit**：自动识别脚下的模板区域进行提交。
    *   **Smart TP**：智能传送至最近的模板或实例。

## 常见操作流程

### 1. 创建模板 (Template)
1. 在世界中建造好你的建筑原型。
2. 使用 WorldEdit 选择该区域（`//pos1`, `//pos2`）。
3. 站在选区内的某一点，这一点将会成为未来粘贴的中心位置（类似 WorldEdit 的复制粘贴的坐标逻辑）
4. 执行 `/idunn template create <path>` 将选区保存为模板。例如：`/idunn template create houses/small_house`。
5. 这将在你选区的最小点位置创建一个“母版锚点”。

### 2. 放置实例 (Instance)
1.  将当前模板设置为刚才创建的模板（如果不使用 Set）：
    *   使用 `/idunn template place <path>` 放置一次。
2.  使用模板组（Set）进行批量/随机放置：
    *   添加源：`/idunn set add path houses/small_house`。
    *   绑定魔杖：手里拿着金锄头（或其他物品），输入 `/idunn pref wand bind`。
    *   右键地面即可放置实例。

### 3. 修改与更新
1.  回到模板的母版位置（可以通过 `/idunn template tp <path>` 传送）。
2.  直接修改建筑方块。
3.  站在母版范围内，输入 `/idunn commit <message>`（例如：`/idunn commit "Fix roof"`）。
4.  插件会自动重新根据母版范围截图，并更新所有已放置的该模板的实例。

### 4. 使用模板组 (Template Set)
1.  **添加内容**：
    *   添加单个模板：`/idunn set add path <path> [weight]`
    *   添加其他组（递归）：`/idunn set add subset <namespace:name> [weight]`
2.  **保存与加载**：
    *   保存个人预设：`/idunn set save <name>`
    *   保存全局预设：`/idunn set save global:<name>` (需要权限)
    *   加载预设：`/idunn set load <name>`
3.  **属性调整**：
    *   设置随机旋转：`/idunn set prop rotate random`
    *   设置随机翻转：`/idunn set prop flipx random`

### 5. 笔刷系统 (Brush System)
将模板或模板组绑定到物品上，通过远程射线放置。
1.  **绑定笔刷**：
    *   `/idunn brush bind right path <path> [-r] [-x] [-z]`：绑定右键，使用指定路径，可选随机旋转/翻转。
    *   `/idunn brush bind left set <name>`：绑定左键，使用指定模板组。
2.  **笔刷预设**：
    *   保存当前物品所有笔刷配置：`/idunn brush preset save <name> "My Brush"`
    *   加载预设：`/idunn brush preset load all <name>`
    *   加载特定通道：`/idunn brush preset load channel right <name> left` (将预设的 right 通道加载到当前物品的 left 通道)
3.  **高级操作**：
    *   自定义通道：`/idunn brush bind custom1 ...`，通过 `/idunn brush trigger custom1` 触发。
    *   修改属性：`/idunn brush modify right rotate 90`。
    *   添加来源：`/idunn brush source add path right <new_path>`。

### 6. 偏好设置
*   限制仅在空方块放置：`/idunn pref placeOnEmptyOnly true`
*   管理空方块列表：`/idunn pref emptyBlocks list/add/remove`
*   开关 Action Bar 提示：`/idunn pref actionBar true`

## 指令手册

### 核心指令

| 指令格式 | 描述 |
| :--- | :--- |
| `/idunn reload` | 重载插件配置。 |
| `/idunn tp` | 智能传送：如果附近有模板母版或实例，传送过去。 |
| `/idunn commit <message>` | **智能提交**：提交当前脚下模板母版的修改。 |
| `/idunn commit <path> <message>`| **指定提交**：指定路径提交模板修改。 |

### 模板管理 (`/idunn template ...`)

| 指令格式                                              | 描述 |
|:--------------------------------------------------| :--- |
| `list [path]`                                     | 列出所有模板或指定路径下的模板。 |
| `create <name / relative path> [base path]`       | 将当前 WorldEdit 选区保存为新模板。 |
| `tp <path>`                                       | 传送到指定模板的母版锚点位置。 |
| `place <path> [rotation] [flipX] [flipY] [flipZ]` | 在脚下放置指定模板的一个实例。 |

### 实例管理 (`/idunn instance ...`)

| 指令格式 | 描述 |
| :--- | :--- |
| `list [radius]` | 列出附近的实例（默认半径100）。 |
| `tp <instanceId>` | 传送到指定 ID 的实例位置。 |
| `undo <instanceId>` | 撤销（删除）指定实例并恢复方块。 |

### 模板组/预设 (`/idunn set ...`)

| 指令格式 | 描述 |
| :--- | :--- |
| `list` | 列出所有保存的预设（个人 + 全局）。 |
| `view` | 查看当前正在使用的 Set 的详细信息（源、权重、属性）。 |
| `clear` | 清空当前 Set 的所有内容。 |
| `add path <path> [weight]` | 向当前 Set 添加一个模板路径源。 |
| `add subset <ns:name> [weight]`| 向当前 Set 添加一个引用 Set 源（支持递归）。 |
| `remove <path>` | 从当前 Set 移除指定源。 |
| `prop <key> <value>` | 修改 Set 属性（rotate, flipx, flipz）。 |
| `save <name>` | 保存当前 Set 为个人预设。 |
| `save <ns:name>` | 保存当前 Set 到指定命名空间（如 `global:town`）。 |
| `load <name>` | 加载预设（优先个人，后全局）。 |
| `update <name>` | 更新已保存的预设（覆盖保存）。 |
| `transferToGlobal <name> <ns:new>`| 将个人预设转移到全局/其他命名空间。 |
| `place` | 从当前 Set 随机抽取一个模板并在脚下放置。 |

### 笔刷管理 (`/idunn brush ...`)

| 指令格式 | 描述 |
| :--- | :--- |
| `bind <channel> path/set <val> [flags]` | 绑定笔刷到手持物品的指定通道 (right/left/custom)。flags: `-r`(random rot), `-x`/`-z`(flip)。 |
| `unbind <channel>` | 解绑指定通道。 |
| `trigger <channel>` | 手动触发指定通道的笔刷放置逻辑。 |
| `modify <channel> <prop> <val>` | 修改笔刷属性 (rotate, flipx, flipz, noair, emptyonly)。 |
| `source add <path/set> <val> [w]` | 向笔刷添加新的内容源。 |
| `source list <channel>` | 列出笔刷的所有内容源。 |
| `source remove <channel> <index>` | 移除笔刷内容源。 |
| `preset save [<ns>:]<name>` | 保存当前物品笔刷配置为预设。 |
| `preset update [<ns>:]<name>` | 更新笔刷预设。 |
| `preset load all [<ns>:]<name>` | 加载预设的所有通道到当前物品。 |
| `preset load channel ...` | 加载预设的特定通道。 |

### 个人偏好 (`/idunn pref ...`)

| 指令格式 | 描述 |
| :--- | :--- |
| `wand [bind]` | 查看当前魔杖物品，或绑定手中物品为魔杖。 |
| `placeOnEmptyOnly <true/false>` | 开关：是否仅允许在“空方块”列表中放置实例。 |
| `emptyBlocks list` | 列出被视为“空方块”的列表。 |
| `emptyBlocks add <Material>` | 添加方块到空方块列表。 |
| `emptyBlocks remove <Material>` | 从空方块列表移除。 |
| `particles <type> <true/false>` | 开关各类粒子特效（template/instance/wand）。 |
| `bossbar <type> <true/false>` | 开关各类 BossBar 提示（template/instance/set）。 |
| `actionBar <true/false>` | 开关 Action Bar 实时状态显示。 |

---
**Permissions**:
*   `idunn.admin`: 所有权限。
*   `idunn.template.create`: 创建模板。
*   `idunn.template.modify.all`: 修改所有模板。
*   `idunn.template.modify.<path>`: 修改特定路径模板。
*   `idunn.set.create.global`: 在 global 命名空间创建 Set。
*   `idunn.set.update.global`: 更新 global 命名空间 Set。
*   `idunn.brush.preset.save.<namespace>`: 保存/更新笔刷预设到指定命名空间。
*   `idunn.brush.preset.load.<namespace>`: 从指定命名空间加载笔刷预设。
