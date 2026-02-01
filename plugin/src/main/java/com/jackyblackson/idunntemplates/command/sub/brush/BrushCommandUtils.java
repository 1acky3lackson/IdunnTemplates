package com.jackyblackson.idunntemplates.command.sub.brush;

import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrushCommandUtils {
    public static List<String> getBrushChanelTabCompleteForPlayer(SessionManager sessionManager, Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        if (matName != null) {
            var session = sessionManager.getSession(player.getUniqueId());
            BrushSession bs = session.getPreference().getBoundBrushes().get(matName);
            if (bs != null) {
                return new ArrayList<>(bs.getChannels().keySet());
            }
        }
        return List.of("<unknown>");
    }
}
