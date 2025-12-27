package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetsRemoveCommand extends BaseSubCommand {

    private final SessionManager sessionManager;

    public SetsRemoveCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn set remove <path>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn set remove <path>");
            return;
        }
        
        String path = args[1];
        TemplateSet set = sessionManager.getSession(player.getUniqueId()).getPreference().getCurrentSet();
        
        if (set.removeSource(path)) {
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Removed source: " + path);
        } else {
            player.sendMessage(ChatColor.RED + "Source not found in current set.");
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
