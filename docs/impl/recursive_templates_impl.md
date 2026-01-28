# 递归/嵌套模板 (Recursive Templates) 设计与实现文档

## 1. 概述

递归模板功能允许在模板的母版区域（Master Region）中放置其他模板的实例（子模板）。当子模板更新时，能够自动触发父模板的更新和提交，从而实现构建组件的复用和自动化维护。

**核心机制**：
1.  **嵌入检测**：放置实例时，检测是否位于某模板的母版范围内。
2.  **关联存储**：记录实例与父模板的归属关系。
3.  **自动蒙版**：确保子实例不超出父模板边界。
4.  **链式更新**：子实例更新 -> 触发父模板重绘 -> 父模板自动 Commit -> 父模板实例更新。

## 2. 数据模型变更

为了避免在级联更新时通过 ID 查找实例（开销较大），我们需要在模板元数据中直接内联保存相关的完整实例信息。

### 2.1 TemplateMetadata.java

增加两个 Map 分别记录“子模板实例”和“作为子模板存在的实例”。

```java
public class TemplateMetadata {
    // ... Existing fields ...

    /**
     * 记录位于此模板 Master Region 内的其他模板（子模板）的完整实例信息。
     * 用于：当子模板更新时，快速找到位于当前模板内的实例并触发重绘。
     * 
     * Key: Child Template UUID (子模板ID)
     * Value: List of Instance (该子模板在当前模板内的所有实例列表)
     */
    private Map<UUID, List<Instance>> childTemplateInstances = new HashMap<>();

    /**
     * 记录此模板作为子模板，放置在其他父模板中的完整实例信息。
     * 用于：当当前模板更新时，快速定位所有父模板并触发父模板的更新。
     * 
     * Key: Parent Template UUID (父模板ID)
     * Value: List of Instance (当前模板在父模板内的所有实例列表)
     */
    private Map<UUID, List<Instance>> parentTemplateInstances = new HashMap<>();

    // Getter & Setter
}
```

### 2.2 Instance.java

增加字段以记录该实例“寄生”在哪个模板中（方便反向校验）。

```java
public class Instance {
    // ... Existing fields ...
    
    /**
     * ID of the Template that contains this instance in its Master Region.
     * Null if this instance is placed in the wild.
     */
    private UUID embeddedInTemplateId;

    // Getter & Setter
}
```

## 3. 核心逻辑与实现位置

### 3.1 放置拦截与确认 (`InstanceManager.java`)

在 `placeInstanceAndReturn` 方法执行实际放置**之前**，插入检测逻辑。

**实现位置**：`InstanceManager.placeInstanceAndReturn` (或新建 `prePlaceCheck` 方法)

**逻辑流程**：
1.  计算待放置实例的世界坐标 AABB（根据 Location, Template Size, Rotation, Flip）。
2.  遍历所有 `Template`（通过 `TemplateManager`），检查其 Master Region 是否与实例 AABB 相交。
    *   *优化*：利用 `TemplateMetadata` 中的 `WorldId` 和 AABB 快速筛选。
3.  如果存在相交的父模板（可能多个）：
    *   **中断放置**：抛出特定异常或返回特殊状态，告知 Command 层需要用户确认。
    *   **构建提示信息**：列出所有相交的父模板名称、ID。
    *   **计算蒙版**：根据父模板的边界，计算子实例需要的 Mask 值，使其被裁剪在父模板内（见 3.2）。

**交互层 (`PlaceCommand.java`)**：
*   捕获“需要确认”的状态。
*   向玩家发送可点击的 JSON 文本：
    *   列出父模板。
    *   [TP] 按钮：传送玩家去查看父模板位置。
    *   [Confirm] 按钮：重新执行 `/idunn place ... -confirm -parent:<uuid>` 指令。

### 3.2 自动蒙版计算 (`InstanceManager.java`)

当玩家确认放置（或指令带上了 `-confirm` 及父模板上下文）时，计算并应用 Mask。

**算法**：
*   **输入**：子实例 World AABB (`cMin`, `cMax`)，父模板 World AABB (`pMin`, `pMax`)。
*   **逻辑**：
    *   `validMin = max(cMin, pMin)`
    *   `validMax = min(cMax, pMax)`
    *   如果 `validMin > validMax` (任意轴)，则完全在外部，无需放置。
    *   **Mask 计算**：
        *   将 World Space 的裁剪量转换为 Instance Local Space 的 Mask 值。
        *   这需要逆向应用实例的 Rotation/Flip 变换。
        *   *简化方案*：复用 `EffectManager` 中实现的 `calculateMaskedBounds` 的逆逻辑，或者利用 `InstanceMask` 的定义（从由面缩进）。
        *   例如：`WorldWestIndent = validMin.x - cMin.x`。如果未旋转，这就是 `maskXNeg`。如果旋转了 90 度，可能是 `maskZPos`。
*   **应用**：设置 `Instance` 的 mask 字段。

### 3.3 关联存储 (`InstanceManager.java`)

放置成功后：
1.  设置 `Instance.embeddedInTemplateId = parentTemplate.getId()`。
2.  获取父模板 Metadata，将 `instance.getId()` 加入 `embeddedInstanceIds`。
3.  保存 Instance 和 TemplateMetadata（通过 `TemplateManager.saveTemplateMetadata`）。

### 3.4 链式更新 (`TemplateUpdater.java`)

这是递归模板的核心“递归”部分。

**实现位置**：`TemplateUpdater.updateSingleInstance`

**逻辑流程**：
1.  实例更新完成（Blocks updated in world）。
2.  检查 `instance.getEmbeddedInTemplateId()`。
3.  如果不为空：
    *   获取父模板对象 `parentTemplate`。
    *   **防环检测**：
        *   需要传递一个上下文对象 `Set<UUID> updatingTemplates` 给 `updateInstances` 方法。
        *   如果 `parentTemplate.getId()` 已在集合中，跳过（防止死循环）。
    *   **触发自动提交**：
        *   调用 `TemplateManager.commitTemplate(parentTemplate, ...)`。
        *   Commit Message: "Auto-commit: Child template [Name] updated."
    *   **递归传播**：
        *   `commitTemplate` 内部会生成新版本，并再次调用 `TemplateUpdater.updateInstances` 更新父模板的实例。
        *   从而实现了 A -> B -> C 的更新链。

## 4. 详细方法签名建议

### TemplateManager
```java
// 获取与指定区域相交的模板
public List<Template> getIntersectingTemplates(String worldId, int x, int y, int z, int width, int height, int length);
```

### InstanceManager
```java
// 增加重载或参数用于确认放置
public Instance placeInstance(..., boolean force, UUID confirmedParentId);
```

### TemplateUpdater
```java
// 增加上下文参数用于防环
public void updateInstances(Template template, TemplateVersion newVersion, List<Instance> instances, Set<UUID> updateChainContext);
```

## 5. 潜在问题与处理

1.  **性能**：
    *   如果嵌套层级很深，一次更新可能触发大量 WorldEdit 操作。
    *   **建议**：使用 `BukkitRunnable` 延迟调度父模板的 Commit，合并短时间内的多次子模板更新（Debounce）。
    
2.  **数据一致性**：
    *   如果父模板被移动（Anchor 改变），嵌入关系是否失效？
    *   **处理**：我们不支持模板移动位置。

3.  **删除**：
    *   删除父模板时，子实例是否删除？
    *   我们不支持删除模板。

## 6. 总结

该设计通过“实例寄生”的方式建立了模板间的层级关系，利用现有的 `TemplateUpdater` 和 `Commit` 机制实现了自动化的自底向上更新。核心工作量在于放置时的几何计算（相交与蒙版）以及更新时的递归触发逻辑。
