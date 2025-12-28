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
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn brush unbind <channel>");
            return;
        }
        
        String channel = args[0].toLowerCase();
        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        
        if (matName == null) {
            player.sendMessage(ChatColor.RED + "You must hold an item.");
            return;
        }
        
        var session = sessionManager.getSession(player.getUniqueId());
        var pref = session.getPreference();
        BrushSession brushSession = pref.getBoundBrushes().get(matName);
        
        if (brushSession != null && brushSession.getSettings(channel) != null) {
            brushSession.removeSettings(channel);
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Unbound channel '" + channel + "' from " + matName);
        } else {
            player.sendMessage(ChatColor.YELLOW + "No brush bound to channel '" + channel + "' on " + matName);
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 1) {
            ItemStack item = player.getInventory().getItemInMainHand();
            String matName = ItemUtil.getBrushKey(item);
            if (matName != null) {
                var session = sessionManager.getSession(player.getUniqueId());
                BrushSession bs = session.getPreference().getBoundBrushes().get(matName);
                if (bs != null) {
                    return filter(new ArrayList<>(bs.getChannels().keySet()), args[0]);
                }
            }
        }
        return Collections.emptyList();
    }
}
