# 模板笔刷

模板笔刷允许玩家通过绑定一个 set 到一个物品上，从而实现远距离地放置模板。一个物品可以绑定左键/右键两个笔刷。
当玩家用物品左键/右键时，如果绑定了笔刷，则由玩家头部位置随着视角进行射线追踪。
如果在 idunn 配置中配置的最大追踪距离内追踪到了非空方块（空方快列表参见玩家的 preference 中的配置），
那么在这个方块的位置放置模板。

## 笔刷属性

每个笔刷都有以下属性，每个笔刷间独立
* 旋转（rotate）：0/90/180/270/random，默认0，或玩家 preference 设置中的值
  * 在笔刷绑定指令中使用 -r 的 flag 表示 rotate = random
* x轴翻转（flipx）：true/false/random，默认false，或玩家 preference 设置中的值
  * 在笔刷绑定指令中使用 -x 的 flag 表示 flipx = random
* z轴翻转（flipz）：true/false/random，默认false，或玩家 preference 设置中的值
  * 在笔刷绑定指令中使用 -z 的 flag 表示 flipz = random
  * 在笔刷绑定指令中使用 -xz 的 flag 表示 flipx = random 且 flipz = random
* 排除空气（noair）：true/false，默认true，或玩家 preference 设置中的值
* 只作用于非空方块（emptyOnly）：true/false，默认true，或玩家 preference 设置中的值

指令中的 flag 遵循 unix 风格，可以连写也可以分卸，比如 `-rxz` 和 `-r -xz` 以及 `-r -x -z` 是等价的。

如果在指令中没有提供某些笔刷属性的指定 flag，那么绑定笔刷时其值与当前玩家  preference 中的设置一致。

## 默认笔刷

玩家绑定的 wand 天然作为一个默认的笔刷。如果玩家当前有在 set 模式，那么wand的右键绑定这个 set。
笔刷的各项设置和玩家当前set放置的设置一样。

## 绑定笔刷

玩家可以通过 `/idunn brush bind right/left path <path> [-r] [-x] [-z]` 绑定临时的笔刷到当前拿着的物品上，笔刷的内容set的源仅包含这个目录。

玩家可以通过 `/idunn brush bind right/left set [<namespace>:]<name> [-r] [-x] [-z]` 绑定临时的笔刷到当前拿着的物品上，笔刷的内容set即为这个set。

玩家通过 `/idunn brush unbind right/left` 来解绑当前手上物品的左/右键绑定的物品。

## 自定义笔刷通道

玩家通过 `/idunn brush bind <chanel name> path/set <value> [-r] [-x] [-z]` 来将对应的path或者set绑定到当前手持物品的 名为 name 的这个chanel。

玩家绑定在非 left/right 的 chanel 上的笔刷无法通过左右键触发，但玩家可以通过 `/idunn brush trigger <name>` 来触发这个笔刷的放置逻辑。
这样玩家可以通过客户端等的按键绑定 mod 来实现更丰富的笔刷绑定。

玩家通过 `/idunn brush unbind <chanel name>` 来解绑。

## 修改笔刷参数

玩家通过 `/idunn brush modify <chanel name> <prop name> <value>` 指令来修改当前手上物品对应 chanel 绑定笔刷对应 prop 属性的值。

## 添加笔刷内容来源

笔刷的内容来源是一个 set，而这个 set 可以有多个内容来源。

玩家可以通过 `/idunn brush source add set/path left/right/<chanel> <value>` 指令来添加新的内容来源

玩家可以通过 `/idunn brush source list left/right/<chanel>` 来打印一个内容来源的列表。每一项前面都有一个红色的叉子，当玩家点击，则删除这个内容来源条目。

## 笔刷显示

当玩家手上物品绑定有笔刷时，玩家的 set 的 bossbar 不再显示，在 bossbar 中每个笔刷展示一个 bossbar，显示：

``` text
<CHANEL_NAME>(total <source_count>) | next: <NEXT_TEMPLATE_PATH> | <BRUSH_SETTINGS>
```

其中 BRUSH_SETTINGS 中，笔刷的每一个属性都用一个短的字符串表示，其中

* 旋转（R）
  * 如果数值不是random，那么显示为 `R-<value>`，比如 `R-90` 或 `R-rand`
* 翻转（F）
  * 显示为 `F-XZ`，其中X和Z字母表示X和Z轴翻转是否启用，启用则显示为绿色，否则为红色，随机则显示为橙色。
* 排除空气（noair）
  * 如果设置为 true，那么显示 `noair`，否则不显示
* 只作用于非空方块（emptyOnly）
  * 如果设置为 true，那么显示 `emptyOnly`，否则不显示

这些字符串按照上面列表的顺序排列，并且每个之间间隔一个空格来分隔。

## 笔刷预设

玩家可以将当前手持物品绑定的所有 chanel 及其具体设置保存为一个笔刷预设。笔刷的预设有命名空间、名称、描述信息、保存笔刷的玩家名、各个 chanel 的绑定信息等属性。

### 保存预设

玩家可以用 `/idunn brush preset save [<namespace>:]<name> <description>` 来储存当前手上物品绑定的所有通道的笔刷内容到这个预设。

如果玩家没有提供 namespace，那么默认储存到个人的 namespace (`player.<name>`) 中，这个预设对其他玩家不可见。

如果玩家提供的 namespace 不是其个人命名空间，则需要玩家有保存笔刷预设到那个命名空间的权限。

### 更新预设

玩家可以用 `/idunn brush preset update [<namespace>:]<name>` 来将当前手上物品绑定的所有通道的笔刷内容更新到这个预设。

如果玩家没有提供 namespace，那么默认储存到个人的 namespace (`player.<playername>`) 中，这个预设对其他玩家不可见。

如果玩家提供的 namespace 不是其个人命名空间，则需要玩家有更新笔刷预设到那个命名空间的权限。

### 加载预设

玩家可以将一个指定预设的全部通道加载到当前手持物品中，使用 `/idunn brush preset load all [<namespace>:]<name>`。
注意：

* 如果没有提供 namespace，则加载玩家个人的 namespace (`player.<playername>`) 中的对应 name 的项
* 如果加载的不是个人的命名空间中的预设，则需要玩家具有那个命名空间预设加载的权限
* 如果当前物品已经有重名的通道绑定，那么加载失败
  * 提醒用户重名的通道。消息中每一个重名通道名后有一个红色的叉子，玩家点击即可快速删除通道绑定（通过上面提到的 `/idunn brush unbind <chanel name>` 指令）

玩家可以通过 `/idunn brush preset load chanel <preset chanel name> [<namespace>:]<name> [<target chanel name>]` 
来将某个笔刷预设的某个通道 `<preset chanel name>` 绑定到 当前手持物品的笔刷。注意：

* 如果不提供 `<target chanel name>`，那么绑定到同名的 chanel 中
* 如果没有提供 namespace，则加载玩家个人的 namespace (`player.<playername>`) 中的对应 name 的项
* 如果加载的不是个人的命名空间中的预设，则需要玩家具有那个命名空间预设加载的权限
* 如果当前物品已经有重名的通道绑定，那么加载失败
  * 提醒用户重名的通道。消息中每一个重名通道名后有一个红色的叉子，玩家点击即可快速删除通道绑定（通过上面提到的 `/idunn brush unbind <chanel name>` 指令）