# 阶段 5：用户交互与安全 (User Interaction & UX)

**目标**：提供清晰的 CLI 交互，防止意外的嵌套放置，并完善用户体验。

## 1. 涉及文件

*   `src/main/java/com/jackyblackson/idunntemplates/command/sub/PlaceCommand.java`
*   `src/main/java/com/jackyblackson/idunntemplates/manager/InstanceManager.java`

## 2. 详细实现

### 2.1 交互流程设计

我们不希望玩家在不知情的情况下创建复杂的嵌套关系。因此，当 `place` 操作检测到相交时，默认拦截。

**流程**：
1.  玩家输入 `/idunn place my_chair`。
2.  系统检测到目标位置在 `my_house` 的区域内。
3.  系统拦截，输出：
    > ⚠️ **Nested Placement Detected**
    > You are placing [my_chair] inside parent template: **[my_house]**.
    > This will create a recursive dependency.
    > 
    > [[Teleport to Parent]]  [[Confirm Placement]]
4.  玩家点击 `[Confirm Placement]`，实际上执行 `/idunn place my_chair -confirm -parent:uuid-of-house`。
5.  系统执行放置，应用蒙版，建立关联。

### 2.2 命令行参数解析 (`PlaceCommand`)

需要支持 flag 解析。

```java
// PlaceCommand.java

@Override
public void onExecute(CommandSender sender, String[] args) {
    // ... 解析常规参数 ...
    
    boolean confirm = false;
    UUID parentId = null;

    for (String arg : args) {
        if (arg.equalsIgnoreCase("-confirm")) {
            confirm = true;
        } else if (arg.startsWith("-parent:")) {
            try {
                parentId = UUID.fromString(arg.substring(8));
            } catch (IllegalArgumentException e) {
                // handle error
            }
        }
    }
    
    try {
        instanceManager.placeInstance(..., confirm, parentId);
    } catch (ParentTemplateConflictException e) {
        // 捕获异常，发送交互式 JSON 消息
        sendConfirmationMessage(sender, e.getIntersectingTemplates());
    }
}
```

### 2.3 异常与拦截 (`InstanceManager`)

```java
// InstanceManager.java

public void placeInstance(..., boolean confirm, UUID specificParentId) throws ParentTemplateConflictException {
    // 1. 检测相交
    List<Template> parents = templateManager.getIntersectingTemplates(...);
    
    if (!parents.isEmpty()) {
        // 如果没有确认，或者确认了但没有指定父模板（如果有歧义），则抛出
        if (!confirm) {
            throw new ParentTemplateConflictException(parents);
        }
        
        // 如果已确认，执行逻辑
        // (Phase 2 & 3 的逻辑在这里调用)
    }
    
    // 执行实际放置
}
```

### 2.4 JSON 消息构建

使用 Spigot/Bukkit 的 `TextComponent` 构建可点击消息。

```java
private void sendConfirmationMessage(CommandSender sender, List<Template> parents) {
    Template primary = parents.get(0);
    
    ComponentBuilder b = new ComponentBuilder("⚠️ Nested Placement Detected\n").color(ChatColor.YELLOW);
    b.append("Target is inside: ").append(primary.getName()).color(ChatColor.GOLD);
    
    // [Confirm] Button
    String cmd = "/idunn place ... -confirm -parent:" + primary.getId();
    b.append(" [Confirm] ").color(ChatColor.GREEN).bold(true)
     .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
     
    sender.spigot().sendMessage(b.create());
}
```

## 3. 验证计划

1.  **未确认测试**：
    *   尝试直接放置到父模板内。
    *   期望：操作被取消，收到提示消息。
2.  **交互测试**：
    *   点击提示消息中的 `[Confirm]`。
    *   期望：指令自动填充并执行，放置成功。
3.  **参数测试**：
    *   手动输入带 `-confirm` 的指令。
    *   期望：直接放置成功，无提示。

```