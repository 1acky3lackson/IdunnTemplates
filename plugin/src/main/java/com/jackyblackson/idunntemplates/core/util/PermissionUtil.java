package com.jackyblackson.idunntemplates.core.util;

import org.bukkit.entity.Player;

public class PermissionUtil {
    public static boolean hasRecursivePermission(Player player, String base, String path) {
        // 玩家对个人的路径有绝对的控制权
        if ((path + "/").startsWith("users/" + player.getName() + "/")) {
            return true;
        }
        String[] parts = path.split("/");
        StringBuilder current = new StringBuilder(base);
        if (player.hasPermission(current.toString())) return true;

        for (String part : parts) {
            current.append(".").append(part);
            if (player.hasPermission(current.toString())) return true;
        }
        return false;
    }
}
