# 阶段 4：级联更新引擎 (Cascading Update Engine)

**目标**：实现“子变父变”的自动化链式反应。

## 1. 涉及文件

*   `src/main/java/com/jackyblackson/idunntemplates/manager/TemplateUpdater.java`
*   `src/main/java/com/jackyblackson/idunntemplates/manager/TemplateManager.java`

## 2. 详细实现

### 2.1 递归更新入口 (`TemplateUpdater`)

当一个模板（Child）的实例在世界中更新完毕后，需要触发所有包含它的父模板的更新。

```java
// TemplateUpdater.java

public void updateInstances(Template template, TemplateVersion version, /*...*/) {
    // ... 现有的更新逻辑 (Paste schematics to world) ...
    
    // 更新完成后，触发级联
    triggerParentUpdates(template, new HashSet<>());
}

/**
 * 递归触发父模板更新
 * @param childTemplate 刚发生变化的子模板
 * @param processingChain 防环上下文，记录正在处理更新链中的模板 ID
 */
private void triggerParentUpdates(Template childTemplate, Set<UUID> processingChain) {
    // 1. 防环检测
    if (processingChain.contains(childTemplate.getId())) {
        getLogger().warning("Detected recursive update cycle involving " + childTemplate.getName() + ". Stopping chain.");
        return;
    }
    processingChain.add(childTemplate.getId());

    // 2. 获取所有父模板
    Map<UUID, List<Instance>> parents = childTemplate.getMetadata().getParentTemplateInstances();
    if (parents.isEmpty()) {
        return;
    }

    getLogger().info("Cascading update: " + childTemplate.getName() + " triggers " + parents.size() + " parents.");

    // 3. 遍历触发
    for (UUID parentId : parents.keySet()) {
        Template parent = templateManager.getTemplate(parentId);
        if (parent == null) continue;

        // 核心操作：触发父模板的 Commit
        // Commit 会重新扫描父模板的区域（其中包含了刚刚更新过的子模板实例），
        // 生成新版本，并保存。
        // 注意：这里可能需要一个新的 Commit 类型或 flag，标记为 "Auto-Recursive"
        
        // 伪代码
        templateManager.commitTemplate(
            parent, 
            "Auto-commit: Updated dependency " + childTemplate.getName(),
            processingChain // 传递上下文，以便 commit 内部如果再次调用 updateInstances 能接上
        );
    }
    
    processingChain.remove(childTemplate.getId());
}
```

### 2.2 TemplateManager 的配合

`commitTemplate` 方法通常流程是：
`Save Region to Schematic` -> `Create Version` -> `Update Instances of this Template`.

我们需要确保 `commitTemplate` 接受 `processingChain` 参数，并传递给它调用的 `updateInstances`。

```java
// TemplateManager.java (伪代码)

public void commitTemplate(Template template, String message, Set<UUID> chainContext) {
    // 1. Save Schematic (这里会把世界里已经更新好的子模板方块存入父模板的 schematic)
    // ...
    
    // 2. Create Version
    TemplateVersion newVersion = ...;
    
    // 3. Update Instances (这一步是把父模板的新样子应用到父模板的所有实例上)
    // 这里实现了 A -> B -> C 的传播：B更新导致 A commit，A commit 后 A 的实例也要更新
    templateUpdater.updateInstances(template, newVersion, chainContext);
}
```

## 3. 验证计划

1.  **构造链式结构**：
    *   A 包含 B (Instance of B inside A)。
    *   B 包含 C (Instance of C inside B)。
2.  **触发更新**：
    *   修改 C (例如用 WorldEdit 改动 C 的 Master Region 里的方块)。
    *   手动执行 `/idunn commit C`。
3.  **观察日志与结果**：
    *   日志应显示：`Updating C` -> `Triggering Parent B` -> `Committing B` -> `Updating B instances` -> `Triggering Parent A` -> `Committing A` -> `Updating A instances`.
    *   最终检查世界中放置的 A 的实例，应该能看到 C 的变化被反映出来。
4.  **环路测试**：
    *   构造 A 包含 B，B 包含 A。
    *   更新 A。
    *   系统应在第二轮检测到环路并安全停止，不会 StackOverflow。
