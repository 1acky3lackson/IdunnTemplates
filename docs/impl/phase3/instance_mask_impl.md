# 实例蒙版 (Instance Mask) 实现说明

## 1. 概述

实例蒙版功能允许用户在放置或更新实例时，通过定义六个方向（X-, X+, Y-, Y+, Z-, Z+）的缩进值（Mask），来裁剪模板的有效区域。被蒙版遮挡的区域在放置和更新计算中将被忽略（视为 Structural Void / 跳过处理）。

核心目标：
*   **数据存储**：在 `Instance` 对象中保存蒙版数据。
*   **放置逻辑**：在 `InstanceManager` 放置时，仅放置未被遮挡的区域。
*   **更新逻辑**：在 `TemplateUpdater` / `DiffCalculator` 计算差异时，忽略被遮挡的区域。
*   **交互接口**：提供修改实例蒙版属性的指令。

## 2. 数据模型变更

### 2.1 修改 `Instance.java`

在 `src/main/java/com/jackyblackson/idunntemplates/core/domain/Instance.java` 中增加以下字段：

```java
    // Mask / Indentation (Default 0)
    // Positive values indicate how much to shrink from that face.
    private int maskXNeg; // X- (Left)
    private int maskXPos; // X+ (Right)
    private int maskYNeg; // Y- (Bottom)
    private int maskYPos; // Y+ (Top)
    private int maskZNeg; // Z- (Back)
    private int maskZPos; // Z+ (Front)
```

并提供相应的 Getters 和 Setters。

### 2.2 持久化

由于 `FileInstanceRepository` 使用 Gson 进行序列化，只需修改 `Instance` 类，数据将自动保存和读取，无需修改 Repository 代码。

## 3. 核心逻辑实现

### 3.1 放置逻辑 (`InstanceManager.java`)

在 `placeInstanceAndReturn` 方法中，利用 WorldEdit 的 `EditSession` Mask 功能。

1.  **获取模板原始尺寸**：从 Clipboard 的 Region 获取。
2.  **计算有效局部区域**：
    *   `minX = originalMinX + maskXNeg`
    *   `maxX = originalMaxX - maskXPos`
    *   (同理 Y 和 Z)
3.  **构建世界空间掩码 (RegionMask)**：
    *   由于实例存在旋转 (Rotation) 和 翻转 (Flip)，简单的局部裁剪需要转换到世界坐标。
    *   **算法**：
        1.  定义局部有效包围盒 (Cuboid)。
        2.  应用实例的 Transform (Rotate/Flip) 变换该包围盒的 8 个顶点。
        3.  应用实例的放置原点 (Origin) 偏移。
        4.  计算变换后顶点的最小/最大世界坐标，构建一个新的世界空间 AABB (Axis-Aligned Bounding Box)。注意：由于 Minecraft 仅支持 90 度旋转，变换后的形状仍然是 AABB。
    *   **应用**：创建 `CuboidRegion` 并通过 `BlockMask` 或直接设置 `EditSession` 的 Mask。

**代码思路**：

```java
// ... 在 createPaste 之前 ...

// 1. Calculate Local Effective Bounds
BlockVector3 clipMin = clipboard.getRegion().getMinimumPoint();
BlockVector3 clipMax = clipboard.getRegion().getMaximumPoint();

int validMinX = clipMin.getX() + instance.getMaskXNeg();
int validMaxX = clipMax.getX() - instance.getMaskXPos();
// ... (Y, Z)

// 2. Create Valid Region in World Space
// 我们可以通过创建一个只包含有效区域的 RegionMask 来实现
// 或者更简单：在 PasteBuilder 中使用 mask ? WorldEdit 的 Paste 操作通常支持 Mask，但那是 "Target Mask" (覆盖限制)。
// 我们需要的是 "Source Mask" (源裁剪)。

// WorldEdit 的 API 中，ClipboardHolder.createPaste(...) 生成的 Operation 并不直接支持 Source Mask。
// 替代方案：在 Paste 之前，使用 RegionMask 设置 EditSession 的 mask。
// 这实际上是 Target Mask，但如果我们将 Target Mask 设置为 "变换后的有效区域"，效果是一样的（只允许在该区域内修改方块）。

Region validWorldRegion = calculateWorldRegion(instance, validMinX, validMaxX, ...);
Mask existingMask = editSession.getMask();
Mask boundMask = new RegionMask(validWorldRegion);

// 组合 Mask (Intersection)
if (existingMask != null) {
    editSession.setMask(new IntersectionMask(existingMask, boundMask));
} else {
    editSession.setMask(boundMask);
}

// ... 执行 Paste ...
```

### 3.2 更新逻辑 (`DiffCalculator.java` / `TemplateUpdater.java`)

在 `DiffCalculator` 中，我们需要忽略掉蒙版区域外的方块。

修改 `getTransformedBlocks` 方法或 `calculateDiff` 方法，传入 Mask 参数。

**推荐方案**：修改 `getTransformedBlocks` 签名，接受 `Instance` 对象或者具体的 mask 值。

```java
// In DiffCalculator.java

public Map<BlockVector3, BlockState> getTransformedBlocks(Clipboard clipboard, AffineTransform transform, Instance instance) {
    // ...
    Region region = clipboard.getRegion();
    BlockVector3 minPos = region.getMinimumPoint();
    BlockVector3 maxPos = region.getMaximumPoint(); // Used for checking bounds

    for (BlockVector3 position : region) {
        // Check Masks (Local Space)
        // position is absolute in clipboard
        
        int relX = position.getX() - minPos.getX(); // 0 to Width
        int relY = position.getY() - minPos.getY();
        int relZ = position.getZ() - minPos.getZ();
        
        int width = maxPos.getX() - minPos.getX() + 1;
        // ...
        
        // Check Indents
        if (relX < instance.getMaskXNeg()) continue;
        if (relX >= width - instance.getMaskXPos()) continue;
        // ... (Y, Z)

        // Standard processing...
        BlockState block = clipboard.getBlock(position);
        // ... transform ...
    }
    // ...
}
```

在 `TemplateUpdater.updateSingleInstance` 中调用 `calculateDiff` 时，需要确保将 Mask 信息传递进去。

### 3.3 边界情况处理

*   **Mask 过大**：如果 `maskXNeg + maskXPos >= width`，则有效区域为空。逻辑应处理此情况，不进行任何放置或更新。
*   **更新现有实例**：当用户修改 Mask 后，通常希望立即刷新实例。需要提供 `update` 指令或在设置属性后自动触发 `TemplateUpdater`。
    *   *注意*：如果 Mask 变大（有效区域变小），原有的方块会被忽略（DiffCalculator 视其为 Structural Void），因此**不会**被自动删除。这符合 "忽略 (Ignore)" 的语义。如果用户希望删除多余部分，可能需要额外的 "Trim" 逻辑，但当前设计遵循 "忽略" 原则。

## 4. 指令接口

新增或修改指令以支持设置 Mask。

建议扩展 `Instance` 指令组：

*   `/idunn instance mask <id> <face> <amount>`
    *   `face`: `x+`, `x-`, `y+`, `y-`, `z+`, `z-`, `all`, `clear`
    *   `amount`: 整数
*   或者整合进 `/idunn instance prop` (如果存在通用属性设置指令)。目前 `SetsPropCommand` 存在，但 `InstanceProp` 不存在。建议新建 `InstanceMaskCommand`。

### 示例用法
```bash
/idunn instance mask <uuid> x- 2   # 左侧缩进2格
/idunn instance mask <uuid> y+ 5   # 顶部缩进5格
```

## 5. 实现步骤

1.  **Domain**: 修改 `Instance.java`。
2.  **Manager**: 更新 `InstanceManager` 的 `placeInstance` 逻辑，增加 `calculateWorldRegion` 辅助方法。
3.  **Calc**: 更新 `DiffCalculator` 支持 Mask 过滤。
4.  **Updater**: `TemplateUpdater` 适配新的 `DiffCalculator` 签名。
5.  **Command**: 实现 `InstanceMaskCommand` 并注册到 `IdunnCommand`。

