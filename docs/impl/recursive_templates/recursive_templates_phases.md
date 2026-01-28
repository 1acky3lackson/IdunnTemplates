# 递归模板 (Recursive Templates) 实现阶段分解

基于 `recursive_templates_impl.md` 的设计，我们将实施过程分为以下五个阶段。每个阶段应确保代码可编译且单元测试/基本功能验证通过。

## 阶段 1：数据模型升级 (Data Model Upgrade)

**目标**：建立支持递归关系的存储结构，确保数据能够正确序列化和反序列化。

**范围**：
*   `src/main/java/.../core/domain/Instance.java`
*   `src/main/java/.../core/domain/TemplateMetadata.java`
*   相关的存储/序列化逻辑 (如 Gson TypeAdapters，如果需要)。

**实现步骤**：
1.  **修改 `TemplateMetadata`**：
    *   增加 `Map<UUID, List<Instance>> childTemplateInstances`。
    *   增加 `Map<UUID, List<Instance>> parentTemplateInstances`。
    *   确保这些 Map 被正确初始化（避免 null）。
2.  **修改 `Instance`**：
    *   增加 `UUID embeddedInTemplateId` 字段。
3.  **验证持久化**：
    *   编写单元测试或临时运行逻辑，创建一个填充了这些字段的 TemplateMetadata 对象，保存到磁盘，然后读取，验证数据完整性（特别是 Map 结构是否能被 Gson 正确处理，Instance 对象是否完整）。

## 阶段 2：核心逻辑 - 空间查询与关联管理 (Spatial Query & Relationship Management)

**目标**：能够识别放置行为是否发生在某个模板内部，并建立双向关联。

**范围**：
*   `src/main/java/.../manager/TemplateManager.java`
*   `src/main/java/.../manager/InstanceManager.java`

**实现步骤**：
1.  **实现空间查询**：
    *   在 `TemplateManager` 中添加 `getIntersectingTemplates(String worldId, BoundingBox box)` 方法。
    *   遍历所有模板的 Master Region，返回与给定 Box 相交的模板列表。
2.  **放置检测挂钩**：
    *   在 `InstanceManager.placeInstance` (或其准备阶段) 调用上述方法。
    *   如果发现相交，暂时记录日志或打印调试信息。
3.  **关联数据维护**：
    *   编写逻辑将新实例添加到 Parent 的 `childTemplateInstances`。
    *   将新实例添加到 Child 的 `parentTemplateInstances`。
    *   设置 Instance 的 `embeddedInTemplateId`。
    *   **注意**：此阶段先不实现“拒绝/确认”交互，而是默认允许并建立关联，以便测试数据流。

## 阶段 3：核心逻辑 - 自动蒙版裁切 (Auto-Masking)

**目标**：确保子实例在视觉上不会超出父模板的边界。

**范围**：
*   `src/main/java/.../manager/InstanceManager.java`
*   `src/main/java/.../core/util/TransformUtil.java` (或类似工具类)

**实现步骤**：
1.  **计算相交区域**：
    *   计算 Child Instance AABB 与 Parent Template AABB 的交集。
    *   如果完全不相交（但在阶段2被检测到，可能是因为容差），则取消放置。
2.  **坐标转换与 Mask 计算**：
    *   计算世界坐标系下的裁切量 (如 `cutMinX = validMinX - originalMinX`)。
    *   **逆变换**：将世界坐标系的裁切量转换回 Instance 的局部坐标系（考虑 Rotation 和 Flip）。
    *   *难点*：需要正确处理旋转后的坐标轴映射（例如旋转90度后，世界系的 X 轴裁切可能对应局部的 Z 轴 Mask）。
3.  **应用 Mask**：
    *   将计算出的 Mask 值 (maskXNeg, maskXPos, etc.) 设置给即将放置的 Instance 对象。
4.  **验证**：
    *   在游戏中尝试将一个大物体放入一个小模板区域，验证超出部分是否被正确截断。

## 阶段 4：级联更新引擎 (Cascading Update Engine)

**目标**：实现“子变父变”的自动化链式反应。

**范围**：
*   `src/main/java/.../manager/TemplateUpdater.java`
*   `src/main/java/.../manager/TemplateManager.java`

**实现步骤**：
1.  **更新后触发**：
    *   在 `TemplateUpdater` 完成某模板（子模板 S）的实例更新后。
    *   读取 S 的 `parentTemplateInstances` Map。
2.  **触发父模板提交**：
    *   遍历所有 Parent Template UUID。
    *   对每个 Parent P，调用 `TemplateManager.commitTemplate(P, ...)`。
    *   使用特殊的 Commit Message (如 "Auto-commit: Dependencies updated")。
3.  **防环处理 (Cycle Detection)**：
    *   修改更新/提交方法的签名，接受一个 `Set<UUID> processingChain` 上下文。
    *   如果 P 已经在 Chain 中，则跳过，防止死循环 (A->B->A)。
4.  **递归验证**：
    *   建立测试场景：A 包含 B，B 包含 C。
    *   修改 C，观察 B 是否自动 Commit，随后 A 是否自动 Commit。

## 阶段 5：用户交互与安全 (User Interaction & UX)

**目标**：提供清晰的 CLI 交互，防止意外的嵌套放置，并完善用户体验。

**范围**：
*   `src/main/java/.../command/sub/PlaceCommand.java`
*   `src/main/java/.../manager/InstanceManager.java`

**实现步骤**：
1.  **交互流程改造**：
    *   在 `PlaceCommand` 中处理 `InstanceManager` 返回的“检测到父模板”状态（可能是异常或特殊返回值）。
    *   如果没有 `-confirm` 标志：
        *   发送 JSON 消息，列出父模板名称。
        *   提供 `[View]` (TP) 和 `[Confirm]` (Run command with -confirm) 按钮。
        *   停止放置。
2.  **指令参数支持**：
    *   更新 `PlaceCommand` 参数解析，支持 `-confirm` 和 `-parent:<uuid>`（如果存在重叠的多个父模板，可能需要指定具体哪一个，或者默认全部包含）。
3.  **最终集成测试**：
    *   跑通全流程：放置 -> 提示确认 -> 确认放置(自动裁剪+关联) -> 修改子模板 -> 父模板自动更新。
