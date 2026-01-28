# 阶段 2：核心逻辑 - 空间查询与关联管理 (Spatial Query & Relationship Management)

**目标**：能够识别放置行为是否发生在某个模板内部，并建立双向关联。

## 1. 涉及文件

*   `src/main/java/com/jackyblackson/idunntemplates/manager/TemplateManager.java`
*   `src/main/java/com/jackyblackson/idunntemplates/manager/InstanceManager.java`

## 2. 详细实现

### 2.1 空间查询 (`TemplateManager`)

我们需要快速找到某个 AABB 落在哪些模板的 Master Region 内。

```java
// TemplateManager.java

/**
 * 查找与给定世界坐标区域相交的所有模板。
 * 
 * @param worldId 世界名称
 * @param minX 区域最小X
 * @param minY 区域最小Y
 * @param minZ 区域最小Z
 * @param maxX 区域最大X
 * @param maxY 区域最大Y
 * @param maxZ 区域最大Z
 * @return 相交的模板列表
 */
public List<Template> getIntersectingTemplates(String worldId, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    List<Template> intersecting = new ArrayList<>();
    
    for (Template template : getAllTemplates()) { // 假设有 getAllTemplates 方法
        TemplateMetadata meta = template.getMetadata();
        
        // 1. 快速过滤：世界不同则跳过
        if (!meta.getWorld().equals(worldId)) {
            continue;
        }
        
        // 2. AABB 相交检测
        // 模板的区域
        int tMinX = meta.getX();
        int tMinY = meta.getY();
        int tMinZ = meta.getZ();
        int tMaxX = tMinX + meta.getWidth() - 1;
        int tMaxY = tMinY + meta.getHeight() - 1;
        int tMaxZ = tMinZ + meta.getLength() - 1;

        // 检查是否重叠 (AABB Check)
        // 只有当所有轴都重叠时才算相交
        boolean overlapX = (minX <= tMaxX) && (maxX >= tMinX);
        boolean overlapY = (minY <= tMaxY) && (maxY >= tMinY);
        boolean overlapZ = (minZ <= tMaxZ) && (maxZ >= tMinZ);

        if (overlapX && overlapY && overlapZ) {
            intersecting.add(template);
        }
    }
    return intersecting;
}
```

### 2.2 放置时的检测与关联 (`InstanceManager`)

在 `placeInstance` 方法中（或者在实际保存/放置方块之前），我们需要介入。

**逻辑流：**

1.  **计算待放置实例的 AABB**：
    *   根据 `location` (origin) 和 `template.getSize()` 以及 `rotation` 计算出该实例在世界中的占地范围。
2.  **查询父模板**：
    *   调用 `templateManager.getIntersectingTemplates(...)`。
3.  **处理关联 (Binding)**：
    *   如果找到父模板 `parentTemplate`：
        *   **更新 Instance**：`instance.setEmbeddedInTemplateId(parentTemplate.getId())`。
        *   **更新 Parent Metadata**：
            *   获取 `parentTemplate.getMetadata().getChildTemplateInstances()`。
            *   获取或创建 Key 为 `instance.getTemplateId()` (子模板ID) 的 List。
            *   将 `instance` 加入 List。
        *   **更新 Child Metadata (Self)**：
            *   获取 `childTemplate.getMetadata().getParentTemplateInstances()`。
            *   获取或创建 Key 为 `parentTemplate.getId()` 的 List。
            *   将 `instance` 加入 List。
        *   **保存**：调用 `templateManager.saveTemplateMetadata(...)` 保存两个模板的元数据。

**代码片段示意 (`InstanceManager.java`)**：

```java
public void registerRecursiveRelationships(Instance instance, Template childTemplate) {
    // 1. 计算 AABB (简化代码，需调用 Utility)
    BoundingBox instanceBox = CalculateInstanceWorldBox(instance, childTemplate);
    
    // 2. 查找父模板
    List<Template> parents = templateManager.getIntersectingTemplates(
        instance.getWorld(), 
        instanceBox.getMinX(), instanceBox.getMinY(), instanceBox.getMinZ(),
        instanceBox.getMaxX(), instanceBox.getMaxY(), instanceBox.getMaxZ()
    );

    // 假设目前只处理第一个找到的父模板 (未来可能处理多个嵌套，暂且取第一个)
    if (!parents.isEmpty()) {
        Template parentTemplate = parents.get(0);
        
        // 3. 建立关系
        instance.setEmbeddedInTemplateId(parentTemplate.getId());
        
        // 更新父模板记录
        Map<UUID, List<Instance>> childMap = parentTemplate.getMetadata().getChildTemplateInstances();
        childMap.computeIfAbsent(childTemplate.getId(), k -> new ArrayList<>()).add(instance);
        
        // 更新子模板记录
        Map<UUID, List<Instance>> parentMap = childTemplate.getMetadata().getParentTemplateInstances();
        parentMap.computeIfAbsent(parentTemplate.getId(), k -> new ArrayList<>()).add(instance);
        
        // 4. 保存更改
        templateManager.saveTemplateMetadata(parentTemplate);
        templateManager.saveTemplateMetadata(childTemplate);
        
        // 日志
        getLogger().info("Recursive link created: Child " + childTemplate.getName() + 
                         " embedded in Parent " + parentTemplate.getName());
    }
}
```

## 3. 验证计划

1.  **创建测试环境**：
    *   创建模板 A (Parent)，定义其 Master Region。
    *   创建模板 B (Child)。
2.  **执行放置**：
    *   使用指令或代码将 B 的实例放置在 A 的区域内。
3.  **断言检查**：
    *   检查 `instance` 对象的 `embeddedInTemplateId` 是否为 A 的 ID。
    *   读取 A 的 Metadata，检查 `childTemplateInstances` 是否包含 B 的 ID 和该实例。
    *   读取 B 的 Metadata，检查 `parentTemplateInstances` 是否包含 A 的 ID 和该实例。
4.  **边界测试**：
    *   将 B 放置在 A 的区域外 -> 应该无关联。
    *   将 B 放置在 A 的边缘（部分重叠）-> 应该关联（且在 Phase 3 会被裁切）。
