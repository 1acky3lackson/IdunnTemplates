package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetsClearCommand extends BaseSubCommand {

    private final SessionManager sessionManager;

    public SetsClearCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        TemplateSet set = sessionManager.getSession(player.getUniqueId()).getPreference().getCurrentSet();
        set.clear();
        sessionManager.saveSession(player.getUniqueId());
        sessionManager.regenerateNextPlacement(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Current set cleared.");
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
