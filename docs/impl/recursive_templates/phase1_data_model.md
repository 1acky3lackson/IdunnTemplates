# 阶段 1：数据模型升级 (Data Model Upgrade)

**目标**：建立支持递归关系的存储结构，确保数据能够正确序列化和反序列化。

## 1. 涉及文件

*   `src/main/java/com/jackyblackson/idunntemplates/core/domain/Instance.java`
*   `src/main/java/com/jackyblackson/idunntemplates/core/domain/TemplateMetadata.java`

## 2. 详细实现

### 2.1 修改 `Instance.java`

我们需要增加一个字段来标记该实例是否“寄生”在另一个模板中。

```java
public class Instance {
    // ... 原有字段 ...

    /**
     * 如果此实例是放置在某个模板（父模板）的 Master Region 内，
     * 则此字段存储该父模板的 UUID。
     * 如果是放置在野外（Wild），则为 null。
     */
    private UUID embeddedInTemplateId;

    // Getter
    public UUID getEmbeddedInTemplateId() {
        return embeddedInTemplateId;
    }

    // Setter
    public void setEmbeddedInTemplateId(UUID embeddedInTemplateId) {
        this.embeddedInTemplateId = embeddedInTemplateId;
    }
}
```

### 2.2 修改 `TemplateMetadata.java`

我们需要两个 Map 来维护双向关系，并缓存实例信息以加速查询。

```java
import com.jackyblackson.idunntemplates.core.domain.Instance;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TemplateMetadata {
    // ... 原有字段 ...

    /**
     * 记录位于此模板 Master Region 内的其他模板（子模板）的完整实例信息。
     * 
     * Key: Child Template UUID (子模板的 ID)
     * Value: List of Instance (该子模板在当前模板内的所有实例列表)
     * 
     * 用途：当我们需要渲染或更新当前模板（作为父模板）时，
     * 可以直接读取此列表知道有哪些子模板实例在里面，
     * 而不需要去全局 InstanceRepository 搜索。
     */
    private Map<UUID, List<Instance>> childTemplateInstances = new HashMap<>();

    /**
     * 记录此模板作为子模板，放置在哪些父模板中，以及对应的实例信息。
     * 
     * Key: Parent Template UUID (父模板的 ID)
     * Value: List of Instance (当前模板在父模板内的所有实例列表)
     * 
     * 用途：当当前模板（作为子模板）发生变化时，
     * 遍历此 Map 的 Key (Parent UUIDs)，触发父模板的自动更新。
     * Value 中的 Instance 信息是冗余存储，用于快速校验或恢复。
     */
    private Map<UUID, List<Instance>> parentTemplateInstances = new HashMap<>();

    // Getters
    public Map<UUID, List<Instance>> getChildTemplateInstances() {
        if (childTemplateInstances == null) {
            childTemplateInstances = new HashMap<>();
        }
        return childTemplateInstances;
    }

    public Map<UUID, List<Instance>> getParentTemplateInstances() {
        if (parentTemplateInstances == null) {
            parentTemplateInstances = new HashMap<>();
        }
        return parentTemplateInstances;
    }

    // Setters
    public void setChildTemplateInstances(Map<UUID, List<Instance>> childTemplateInstances) {
        this.childTemplateInstances = childTemplateInstances;
    }

    public void setParentTemplateInstances(Map<UUID, List<Instance>> parentTemplateInstances) {
        this.parentTemplateInstances = parentTemplateInstances;
    }
}
```

## 3. 验证计划

由于主要涉及数据结构变更，验证重点在于序列化（Serialization）。项目可能使用 Gson 或 SnakeYAML 进行存储。

1.  **单元测试构造**：
    *   创建一个 `TemplateMetadata` 对象。
    *   创建几个 `Instance` 对象，设置 dummy 数据。
    *   填充 `childTemplateInstances` 和 `parentTemplateInstances`。
    *   填充 `Instance` 的 `embeddedInTemplateId`。
2.  **序列化测试**：
    *   将对象转换为 JSON/YAML 字符串。
    *   **检查点**：Map 结构是否保持 (`{"key-uuid": [instance1, instance2]}`)。
3.  **反序列化测试**：
    *   将字符串还原为对象。
    *   **检查点**：还原后的 Map 不为 null，且包含正确的数据，`Instance` 对象的字段完整。
4.  **兼容性测试**：
    *   读取一个**旧版本**生成的 `TemplateMetadata`（不含新字段）。
    *   **检查点**：新字段应默认为空 Map 或 null，不会导致 Crash。Getter 应处理 null 情况返回空 Map。
