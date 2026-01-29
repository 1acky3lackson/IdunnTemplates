# 嵌套模板 V2 实现计划

基于 V2 的设计理念，通过引入“暂存区”和“锁定”机制，将流式构建转变为事务式构建，以支持更好的协作与 Undo/Redo 体验。

## 阶段 1：数据模型扩展 (Model Extension)

**目标**：在数据层支持“锁”状态和“暂存区”数据的持久化。

### 1.1 TemplateMetadata 扩展
*   **文件**: `src/main/java/.../core/domain/TemplateMetadata.java`
*   **变更**:
    *   增加字段 `boolean locked` (默认 false)。
    *   增加方法 `isLocked()`, `setLocked(boolean)`.

### 1.2 暂存区数据结构 (Staging Area)
*   **文件**: 新建 `src/main/java/.../core/domain/StagedChanges.java`
*   **内容**:
    *   记录待提交的子实例操作：`List<Instance> addedInstances`。
    *   记录待移除的子实例 ID：`List<String> removedInstanceIds` (如果支持移除)。
    *   关联的模板 ID。
*   **持久化**:
    *   在 `TemplateStorage` 中增加保存和读取 `StagedChanges` 的方法（如保存为 `templates/path/to/template.staged.json`）。

## 阶段 2：锁定与暂存逻辑 (Locking & Staging Logic)

**目标**：拦截直接写入，转而写入暂存区，并处理锁状态。

### 2.1 修改放置逻辑
*   **文件**: `src/main/java/.../manager/InstanceManager.java`
*   **变更**:
    *   在 `placeInstanceAndReturn` 中，当检测到父模板 (`confirmedParentId` 不为空) 时：
        *   检查父模板是否已锁定 (`parent.getMetadata().isLocked()`)。
        *   如果不为 Locked，**自动上锁** (`setLocked(true)`) 并发送提示。
        *   **不**直接调用 `registerRecursiveRelationships` (即不写入 metadata 的 `childTemplateInstances`)。
        *   而是将该 Instance 添加到父模板对应的 `StagedChanges` 中。
        *   保存 `StagedChanges` 到磁盘。
        *   保存 Instance 到 `InstanceRepository` (因为实例在世界中是真实存在的)。
        *   **不**触发级联更新 (`scheduleUpdate`)。

### 2.2 历史记录适配
*   **文件**: `src/main/java/.../core/history/IdunnHistoryWrapper.java`
*   **变更**:
    *   新增历史类型 `STAGED_PLACE`。
    *   记录内容包含：实例数据、父模板 ID。
*   **文件**: `src/main/java/.../manager/HistoryManager.java`
*   **变更**:
    *   实现 `undo`: 删除 Instance，从 `StagedChanges` 中移除该 Instance，如果 StagedChanges 空了，是否解锁？(V2设计未明确，建议保持锁定直到手动提交或解锁)。
    *   实现 `redo`: 恢复 Instance，重新加入 `StagedChanges`。

## 阶段 3：提交逻辑 (Commit Logic)

**目标**：将暂存区内容合并到正式数据，并触发更新。

### 3.1 扩展 CommitCommand
*   **文件**: `src/main/java/.../command/sub/CommitCommand.java`
*   **变更**:
    *   在执行 commit 前，检查是否存在 `StagedChanges`。
    *   如果有：
        1.  **合并数据**: 将 `StagedChanges.addedInstances` 写入 `TemplateMetadata.childTemplateInstances`。
        2.  **清理暂存**: 删除 `StagedChanges` 文件。
        3.  **强制更新**: 检查所有子实例（包括新加入的和旧有的）是否为最新版本，如果不是，在 WorldEdit 复制 Master Region 之前先在世界中更新它们（或者在复制后的 Clipboard 中替换？V2 提议是“强制更新”，通常指更新世界中的方块）。
            *   *注意*：这需要调用 `TemplateUpdater`。
        4.  **解锁**: 设置 `TemplateMetadata.locked = false`。
        5.  **常规流程**: 执行原有的 Save Schematic -> New Version -> Save Metadata。
        6.  **触发级联**: 此时才调用 `CascadingUpdateManager.scheduleUpdate`。

## 阶段 4：用户交互与提示 (UX & Notifications)

**目标**：让用户感知到锁定状态，防止遗忘提交。

### 4.1 BossBar / ActionBar 提示
*   **文件**: `src/main/java/.../listener/PlayerMoveListener.java` (或新建)
*   **变更**:
    *   监听玩家移动。
    *   判断玩家是否处于某个 Locked 模板的 Master Region 内。
    *   如果是，显示 BossBar/ActionBar: "Editing [TemplateName] (Locked) - Don't forget to Commit!"。

### 4.2 放置时的提示
*   **文件**: `src/main/java/.../command/sub/PlaceCommand.java` (或通过 InstanceManager 返回信息)
*   **变更**:
    *   当触发自动上锁时，发送 Title 或醒目 Chat 消息：“模板已锁定。您的更改已暂存，请完成后执行 /idunn commit。”

## 阶段 5：清理与一致性 (Cleanup & Consistency)

**目标**：防止僵尸锁和数据不一致。

### 5.1 服务器启动检查
*   **文件**: `IdunnTemplates.onEnable`
*   **变更**:
    *   扫描所有模板。
    *   如果发现 `locked = true` 但没有对应的 `StagedChanges` 文件 -> 自动解锁。
    *   (可选) 如果发现有 StagedChanges 但很久没动了 -> 打印警告日志。

### 5.2 强制解锁指令
*   **文件**: 新建 `src/main/java/.../command/sub/UnlockCommand.java`
*   **功能**:
    *   强制丢弃暂存区内容（或合并？）并解锁模板。
    *   用于处理异常情况。

## 实施顺序

1.  **Phase 1**: 数据结构 (`TemplateMetadata`, `StagedChanges`).
2.  **Phase 2**: 改造 `InstanceManager` 实现锁定和暂存。
3.  **Phase 3**: 改造 `CommitCommand` 实现合并与解锁。
4.  **Phase 4**: 添加 UI 提示。
5.  **Phase 5**: 补充清理逻辑和 Undo/Redo 适配。
