package com.jackyblackson.idunntemplates.core.store;

import org.bukkit.entity.Player;

public class PermissionUtil {
    public static boolean hasRecursivePermission(Player player, String base, String path) {
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
