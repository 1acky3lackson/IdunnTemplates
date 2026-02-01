package com.jackyblackson.idunntemplates.permission;

/**
 * 记录所有的权限节点.
 *
 * <p>
 * 命名规则：
 * <ol>
 * <li>涉及 Path 或类似递归检查的权限，在其名称后面添加 $R，其内容后面不含 "."</li>
 * <li>涉及后面拼接 namespace 的权限，其名称后面添加 $N，其内容后面包含 "."</li>
 * </ol>
 * </p>
 */
public class PermissionNames {
    public static class Resizes {
        public static final String reload = "idunn.resize.reload";
        public static final String resizeOthers = "idunn.resize.others";
        public static final String extendedResize = "idunn.resize.extended";
        public static final String bypassCooldown = "idunn.resize.bypass.cooldown";
    }
    public static class Templates {
        public static final String place = "idunn.template.place";
        public static final String usePath$R = "idunn.template.use";
        public static final String commitToAll = "idunn.template.commit_all";
        public static final String commitToPath = "idunn.template.commit.";
        public static final String commitToPath$R = "idunn.template.commit";

        public static final String createPersonal = "idunn.template.save.personal";
        public static final String createInPath = "idunn.template.save";
        public static final String createInPath$R = "idunn.template.save";

    }
    public static class Sets {
        public static final String saveToNamespace$N = "idunn.set.save.";
        public static final String updateInNamespace$N = "idunn.set.update.";
        public static final String updateGlobal = "idunn.set.update.global";
    }
    public static class Brushes {
        public static class Presets {
            public static final String loadNamespace$N = "idunn.brush.preset.load.";
            public static final String saveToNamespace$N = "idunn.brush.preset.save.";
        }
    }
}
