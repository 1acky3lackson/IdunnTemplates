package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetsListCommand extends BaseSubCommand {

    private final SessionManager sessionManager;

    public SetsListCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // List PRESETS, not contents of current set (which is visible in BossBar)
        
        var presets = sessionManager.getSession(player.getUniqueId()).getPreference().getSavedSets();
        if (presets.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No saved presets.");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Saved Sets ===");
        for (String name : presets.keySet()) {
            TextComponent msg = new TextComponent("- " + name);
            msg.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn set load " + name));
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to load").create()));
            player.spigot().sendMessage(msg);
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
