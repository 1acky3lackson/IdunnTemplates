# 阶段 3：核心逻辑 - 自动蒙版裁切 (Auto-Masking)

**目标**：确保子实例在视觉上不会超出父模板的边界。

## 1. 涉及文件

*   `src/main/java/com/jackyblackson/idunntemplates/manager/InstanceManager.java`
*   `src/main/java/com/jackyblackson/idunntemplates/core/util/TransformUtil.java` (假设存在，用于坐标变换)

## 2. 详细实现

### 2.1 计算逻辑

我们需要计算子实例世界 AABB 与父模板世界 AABB 的“溢出”部分，并将这个溢出量逆变换为子实例的局部坐标系下的 Mask 值。

**Instance 字段回顾**：
`maskXNeg`, `maskXPos`, `maskYNeg`, `maskYPos`, `maskZNeg`, `maskZPos`。
这些值代表从该方向向内“切掉”多少个方块。

### 2.2 算法实现步骤

在 `registerRecursiveRelationships` 中或之前调用：

```java
public void calculateAndApplyAutoMask(Instance instance, Template childTemplate, Template parentTemplate) {
    // 1. 获取世界坐标系下的边界
    BoundingBox childBox = CalculateInstanceWorldBox(instance, childTemplate);
    BoundingBox parentBox = GetTemplateWorldBox(parentTemplate);

    // 2. 计算世界坐标系下的裁剪量 (World Cut)
    // 如果 childMinX < parentMinX，说明左边超出了，需要裁剪 (parentMinX - childMinX)
    int worldCutMinX = Math.max(0, parentBox.getMinX() - childBox.getMinX());
    int worldCutMaxX = Math.max(0, childBox.getMaxX() - parentBox.getMaxX());
    
    int worldCutMinY = Math.max(0, parentBox.getMinY() - childBox.getMinY());
    int worldCutMaxY = Math.max(0, childBox.getMaxY() - parentBox.getMaxY());
    
    int worldCutMinZ = Math.max(0, parentBox.getMinZ() - childBox.getMinZ());
    int worldCutMaxZ = Math.max(0, childBox.getMaxZ() - parentBox.getMaxZ());

    // 3. 将世界裁剪量映射到局部 Mask
    // 这取决于 Instance 的 Rotation 和 Flip
    // 假设 TransformUtil 有方法可以将 World Vector 转换成 Local Vector (只考虑方向)
    
    // 简单示例逻辑 (需要完善旋转矩阵处理)：
    applyMaskBasedOnRotation(instance, instance.getRotation(), instance.getFlip(), 
        worldCutMinX, worldCutMaxX, 
        worldCutMinY, worldCutMaxY, 
        worldCutMinZ, worldCutMaxZ);
}

private void applyMaskBasedOnRotation(Instance instance, int rotation, boolean flip, 
                                      int wMinX, int wMaxX, int wMinY, int wMaxY, int wMinZ, int wMaxZ) {
    // Y 轴通常不变 (除非支持翻转 Y)
    instance.setMaskYNeg(wMinY);
    instance.setMaskYPos(wMaxY);

    // 处理 X/Z 平面旋转 (0, 90, 180, 270)
    // 这里的 rotation 是顺时针角度
    switch (rotation) {
        case 0:
            // World X+ -> Local X+
            // World Z+ -> Local Z+
            instance.setMaskXNeg(wMinX);
            instance.setMaskXPos(wMaxX);
            instance.setMaskZNeg(wMinZ);
            instance.setMaskZPos(wMaxZ);
            break;
        case 90:
            // World X+ -> Local Z+ (假设)
            // World Z+ -> Local X-
            // 需要详细推导变换矩阵
            instance.setMaskZNeg(wMinX); // World X- (wMinX) 对应 Local Z-
            instance.setMaskZPos(wMaxX);
            instance.setMaskXPos(wMinZ); // World Z- (wMinZ) 对应 Local X+ ? 需校验
            instance.setMaskXNeg(wMaxZ);
            break;
        // ... Case 180, 270
    }
    
    // 如果有 Flip，还需要交换左右
    if (flip) {
        // swap maskXNeg/Pos or maskZNeg/Pos depending on flip axis
    }
}
```

**关键点**：必须确保 `mask` 值被设置到了 `Instance` 对象上，并且在随后的 `placeInstance` 核心逻辑（WorldEdit 粘贴）中被正确使用。现有的 `EffectManager` 或粘贴逻辑应该已经支持读取 mask 字段进行裁剪，如果没有，需要去实现它（但这通常属于现有功能）。

## 3. 验证计划

1.  **场景设计**：
    *   Parent A: 10x10x10 的空心盒子。
    *   Child B: 20x2x2 的长条。
2.  **测试用例**：
    *   **Case 1 (无旋转)**：将 B 沿 X 轴放置，使得两端伸出 A。
        *   预期：A 内部可见 10 格长的 B，两端截断。Instance 的 maskXNeg/Pos 应该非零。
    *   **Case 2 (旋转)**：将 B 旋转 90 度（沿 Z 轴）放置。
        *   预期：视觉上被截断。Instance 的 maskZNeg/Pos (或对应轴) 应该非零。
3.  **视觉验证**：
    *   进入游戏观察实际放置结果。
    *   检查 `Instance` 数据的 `mask` 字段值是否合理。
