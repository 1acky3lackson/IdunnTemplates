package com.jackyblackson.idunntemplates.core.permission;

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
        public static final String reload =                 "idunn.resize.reload";
        public static final String resizeOthers =           "idunn.resize.others";
        public static final String extendedResize =         "idunn.resize.extended";
        public static final String bypassCooldown =         "idunn.resize.bypass.cooldown";
    }
    public static class Templates {
        public static final String place =                  "idunn.template.place";
        public static final String usePath$R =              "idunn.template.use";
        public static final String commitToAll =            "idunn.template.commit_all";
        public static final String commitToPath =           "idunn.template.commit.";
        public static final String commitToPath$R =         "idunn.template.commit";

        public static final String createPersonal =         "idunn.template.save.personal";
        public static final String createInPath =           "idunn.template.save";
        public static final String createInPath$R =         "idunn.template.save";

        // New permission for modify/move/transfer
        public static final String modifyPath$R =           "idunn.template.modify";
    }
    public static class Sets {
        public static final String saveToNamespace$N =      "idunn.set.save.";
        public static final String updateInNamespace$N =    "idunn.set.update.";
        public static final String updateGlobal =           "idunn.set.update.global";
    }
    public static class Brushes {
        public static class Presets {
            public static final String loadNamespace$N =    "idunn.brush.preset.load.";
            public static final String saveToNamespace$N =  "idunn.brush.preset.save.";
        }
    }

    public static class Commercial {

        public static class Admin {
            public static final String admin =                     "taixue.commercial.admin";
            public static final String triggerCheckoutOrder =      "taixue.commercial.admin.trigger.order";
            public static final String triggerCheckoutDetail =     "taixue.commercial.admin.trigger.checkout-detail";
            public static final String triggerReleaseBalance =     "taixue.commercial.admin.trigger.release";
        }

        public static class Project {

            public static final String listAll =            "taixue.commercial.project.list.all";
            public static final String create =             "taixue.commercial.project.create";
            public static final String modify =             "taixue.commercial.project.modify";
            public static final String delete =             "taixue.commercial.project.delete";

            public static class Contribution {
                public static final String list =           "taixue.commercial.project.contribution.list";
                public static final String group =          "taixue.commercial.project.contribution.group";
                public static final String modify =         "taixue.commercial.project.contribution.modify";
                public static final String add =            "taixue.commercial.project.contribution.add";
                public static final String delete =         "taixue.commercial.project.contribution.delete";
                public static final String recalculate =    "taixue.commercial.project.contribution.recalculate";
            }
        }

        public static class Product {
            public static final String listAll =            "taixue.commercial.product.list.all";
            public static final String bindProject =        "taixue.commercial.product.bind";
            public static final String modify =             "taixue.commercial.product.modify";
            public static final String changeStatus =       "taixue.commercial.product.change-status";
        }

        public static class Order {
            public static final String listAll =            "taixue.commercial.order.list.all";
        }

        public static class CheckoutParam {
            public static final String list =               "taixue.commercial.checkout-param.list";
            public static final String update =               "taixue.commercial.checkout-param.update";
        }

        public static class Balance {
            public static final String list =               "taixue.commercial.balance.list";
            public static final String checkoutAll =        "taixue.commercial.balance.checkout.all";
            public static final String transactionAll =     "taixue.commercial.balance.transaction.all";
        }

        public static class Withdraw {
            public static final String list =               "taixue.commercial.withdraw.list";
            public static final String create =             "taixue.commercial.withdraw.create";
            public static final String allocation =         "taixue.commercial.withdraw.allocation";
        }

        public static class SystemWithdraw {
            public static final String listAll =            "taixue.commercial.system-withdraw.list.all";
            public static final String manage =             "taixue.commercial.system-withdraw.manage";
        }
    }
}
