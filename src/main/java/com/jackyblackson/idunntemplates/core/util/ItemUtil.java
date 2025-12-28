package com.jackyblackson.idunntemplates.core.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    /**
     * Gets the key for looking up brush settings for this item.
     * Uses Material name.
     */
    public static String getBrushKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        return item.getType().name();
    }
}
