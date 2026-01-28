# 递归模板功能测试计划 (Recursive Templates Test Plan)

本文档旨在验证递归模板（嵌套模板）功能的完整性，覆盖数据存储、交互逻辑、几何计算及级联更新引擎。

## 0. 测试前准备 (Prerequisites)

为了顺利进行测试，请先准备以下基础模板：

1.  **模板 P (Parent - "The House")**:
    *   形状：一个 10x10x10 的空心立方体（例如用玻璃建造）。
    *   定义好 Master Region。
    *   保存为 `users/tester/parent_house`。
2.  **模板 C (Child - "The Table")**:
    *   形状：一个 3x2x2 的简单结构（例如栅栏+压力板）。
    *   保存为 `users/tester/child_table`。
3.  **模板 G (Grandchild - "The Flower")**:
    *   形状：一个 1x1x1 的花盆。
    *   保存为 `users/tester/grandchild_flower`。
4.  **工具准备**:
    *   确保拥有 OP 权限或 `idunn.admin` 权限。
    *   打开服务器控制台/日志窗口以观察调试信息。

---

## 1. 基础放置与交互 (Basic Placement & Interaction)

**目标**：验证放置拦截逻辑、CLI 提示及关联建立。

### Case 1.1: 拦截嵌套放置
*   **操作**：
    1.  站在 **P (Parent)** 的 Master Region 内部。
    2.  输入指令：`/idunn place users/tester/child_table`。
*   **预期结果**：
    *   **失败**：屏幕显示 "Nested Placement Detected" 警告。
    *   **提示**：显示 "You are placing [child_table] inside [parent_house]"。
    *   **按钮**：显示 `[Confirm in parent_house]` 按钮。

### Case 1.2: 确认放置与关联
*   **操作**：
    1.  点击 Case 1.1 中生成的 `[Confirm]` 按钮。
*   **预期结果**：
    *   **成功**：提示 "Instance placed"。
    *   **数据检查**：
        *   检查 `instances/` 下生成的 JSON 文件，`embeddedInTemplateId` 字段应为 P 的 UUID。
        *   检查 `templates/users/tester/parent_house.json`，`childTemplateInstances` 应包含 C 的 ID。
        *   检查 `templates/users/tester/child_table.json`，`parentTemplateInstances` 应包含 P 的 ID。

### Case 1.3: 命令行参数绕过
*   **操作**：
    1.  获取 P 的 UUID（从文件或 info 指令）。
    2.  输入指令：`/idunn place users/tester/child_table -confirm -parent:<UUID_OF_P>`。
*   **预期结果**：
    *   **成功**：直接放置成功，无警告弹窗。

---

## 2. 多重叠边界测试 (Ambiguity & Overlap)

**目标**：验证当一个位置同时属于多个父模板时的处理逻辑。

### Case 2.1: 双重叠区域选择
*   **前置**：
    *   在世界中放置两个重叠的父模板实例 P1 和 P2（创造一个重叠区域）。
    *   或者直接定义两个重叠的 Template Master Region。
*   **操作**：
    1.  站在 P1 和 P2 的重叠区域内。
    2.  尝试放置 C：`/idunn place users/tester/child_table`。
*   **预期结果**：
    *   **警告**：显示 "inside the master region of 2 templates"。
    *   **列表**：列出 P1 和 P2 的名字。
    *   **交互**：出现两个按钮 `[Confirm in P1]` 和 `[Confirm in P2]`。
    *   **行为**：点击 P1 按钮后，实例只与 P1 绑定，不与 P2 绑定。

---

## 3. 自动蒙版裁切 (Auto-Masking)

**目标**：验证子实例超出父模板边界时是否被正确裁剪（视觉 + 数据）。

### Case 3.1: 简单边界溢出
*   **操作**：
    1.  在 P 的边界处放置 C，使其一半在 P 内，一半在 P 外。
    2.  确认放置。
*   **预期结果**：
    *   **视觉**：在游戏中看到的 C 只有一半，超出 P 边界的部分消失。
    *   **数据**：检查 C 实例的 JSON，`maskXNeg` / `maskXPos` 等字段应有非零值。

### Case 3.2: 旋转后的裁切 (Rotation)
*   **操作**：
    1.  输入指令：`/idunn place users/tester/child_table 90` (旋转 90 度)。
    2.  放置在 P 的边界处（例如 P 的 Z 轴边界）。
*   **预期结果**：
    *   **视觉**：正确裁切。
    *   **逻辑验证**：由于旋转了 90 度，世界坐标的 Z 轴裁切量应该映射到实例局部坐标的 X 轴 Mask 上。

### Case 3.3: 完全溢出 (无效放置)
*   **操作**：
    1.  尝试放置 C，使其 AABB 虽然与 P 相交，但在 Mask 计算后有效体积为 0（例如仅擦边，或者计算逻辑认为完全在外部）。
    2.  *(注：根据当前代码逻辑，只要 AABB 相交就会触发，WorldEdit 可能会处理为空 Paste)*
*   **预期结果**：
    *   不应报错，或者生成一个不可见的实例。

---

## 4. 级联更新引擎 (Cascading Updates)

**目标**：验证“子变父变”的自动化流程及性能调度。

### Case 4.1: 单层更新 (Child -> Parent)
*   **前置**：已在 P 内部放置了 C，并建立了关联。
*   **操作**：
    1.  修改 C 的原始模板（在 C 的 Master Region 加一块石头）。
    2.  提交 C：`/idunn commit users/tester/child_table`。
*   **预期结果**：
    1.  C 的实例更新（出现石头）。
    2.  **等待 1-2 秒**（调度器延迟）。
    3.  控制台显示 "Executing cascading auto-commit for Parent Template: [parent_house]"。
    4.  P 生成新版本（System Commit）。
    5.  如果 P 有被放置在世界其他地方的实例，那些实例也会自动更新，显示出内部 C 的变化。

### Case 4.2: 多层递归 (Grandchild -> Child -> Parent)
*   **前置**：G 嵌入在 C 中，C 嵌入在 P 中 (P -> C -> G)。
*   **操作**：
    1.  修改 G（花盆里加朵花）。
    2.  提交 G。
*   **预期结果**：
    *   链式反应：G 更新 -> (延迟) -> C 自动 Commit -> (延迟) -> P 自动 Commit。
    *   最终检查 P 的所有实例，都应该能看到那朵花。

### Case 4.3: 冷却与防抖 (Cooldown)
*   **操作**：
    1.  在 1 秒内连续提交 3 次 C 的修改。
*   **预期结果**：
    *   控制台可能显示 "Skipping cascading update ... due to cooldown"。
    *   P 不会疯狂产生 3 个新版本，而是合并更新或丢弃中间的触发。

---

## 5. 异常与边界 (Edge Cases)

### Case 5.1: 循环依赖 (Cycle Detection)
*   **前置**：
    *   创建模板 A 和 B。
    *   在 A 中放置 B，在 B 中放置 A（需要在放置时强制指定 parent 或手动修改数据制造此场景）。
*   **操作**：
    1.  更新 A。
*   **预期结果**：
    *   A 更新 -> 触发 B 更新 -> B 尝试触发 A 更新。
    *   **安全拦截**：`CascadingUpdateManager` 或 `pendingUpdates` 逻辑应检测到 A 已经在处理队列或冷却中，停止无限循环。
    *   **日志**：不应出现 StackOverflowError。

### Case 5.2: 父模板被删除
*   **操作**：
    1.  建立 P -> C 关系。
    2.  删除 P 的模板文件（模拟文件丢失）。
    3.  更新 C。
*   **预期结果**：
    *   更新 C 时，尝试查找 P 失败。
    *   系统应优雅处理（Catch Exception / Null Check），C 更新成功，控制台提示无法找到父模板，不崩服。

### Case 5.3: 世界未加载
*   **操作**：
    1.  P 的 Master Region 所在的世界被卸载。
    2.  更新 C。
*   **预期结果**：
    *   `TemplateManager.commitTemplateSystem` 在尝试抓取 P 的区域时，检测到 `world == null`。
    *   输出警告日志 "Cannot auto-commit ... World not loaded"。
    *   流程终止，不崩服。

---

## 6. 测试总结报告 (Test Report)

测试完成后，请记录以下信息：

*   [ ] 所有 Phase 1-5 的核心功能是否按预期工作？
*   [ ] 自动蒙版的旋转映射方向是否正确（X/Z轴是否搞反）？
*   [ ] 级联更新的延迟感是否可接受（默认 0.25s 轮询）？
*   [ ] 是否存在明显的性能卡顿（特别是 Case 4.2 多层更新时）？
