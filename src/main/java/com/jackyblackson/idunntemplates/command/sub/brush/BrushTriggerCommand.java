package com.jackyblackson.idunntemplates.command.sub.brush;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.BrushManager;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BrushTriggerCommand extends BaseSubCommand {

    private final BrushManager brushManager;
    private final SessionManager sessionManager;

    public BrushTriggerCommand(BrushManager brushManager, SessionManager sessionManager) {
        this.brushManager = brushManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn brush trigger <channel>
        if (args.length < 2) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.trigger.usage"));
            return;
        }

        String channel = args[1];
        boolean success = brushManager.triggerBrush(player, channel);
        
        if (!success) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.trigger.failed", channel));
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            ItemStack item = player.getInventory().getItemInMainHand();
            String matName = ItemUtil.getBrushKey(item);
            if (matName != null) {
                var session = sessionManager.getSession(player.getUniqueId());
                BrushSession bs = session.getPreference().getBoundBrushes().get(matName);
                if (bs != null) {
                    return filter(new ArrayList<>(bs.getChannels().keySet()), args[1]);
                }
            }
            return filter(Arrays.asList("left", "right"), args[1]);
        }
        return Collections.emptyList();
    }
}
