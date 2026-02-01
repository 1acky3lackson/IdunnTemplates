package com.jackyblackson.idunntemplates.command.sub.brush;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrushUnbindCommand extends BaseSubCommand {

    private final SessionManager sessionManager;

    public BrushUnbindCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn brush unbind <channel>
        if (args.length < 2) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.unbind.usage"));
            return;
        }
        
        String channel = args[1].toLowerCase();
        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        
        if (matName == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.common.no_item"));
            return;
        }
        
        var session = sessionManager.getSession(player.getUniqueId());
        var pref = session.getPreference();
        BrushSession brushSession = pref.getBoundBrushes().get(matName);
        
        if (brushSession != null && brushSession.getSettings(channel) != null) {
            brushSession.removeSettings(channel);
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.unbind.success", channel, matName));
        } else {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.unbind.not_bound", channel, matName));
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
                    return new ArrayList<>(bs.getChannels().keySet());
                }
            }
        }
        return Collections.emptyList();
    }
}
