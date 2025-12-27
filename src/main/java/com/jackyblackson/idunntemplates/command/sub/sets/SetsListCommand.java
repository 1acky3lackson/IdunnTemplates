package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SetsListCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final SetManager setManager;

    public SetsListCommand(SessionManager sessionManager, SetManager setManager) {
        this.sessionManager = sessionManager;
        this.setManager = setManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // List PRESETS
        
        var presets = sessionManager.getSession(player.getUniqueId()).getPreference().getSavedSets();
        Map<String, TemplateSet> globalSets = setManager.getGlobalNamespace("global");
        
        if (presets.isEmpty() && globalSets.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No saved presets found.");
            return;
        }
        
        if (!presets.isEmpty()) {
            player.sendMessage(ChatColor.GOLD + "=== Private Sets ===");
            for (String name : presets.keySet()) {
                TextComponent msg = new TextComponent("- " + name);
                msg.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn set load " + name));
                msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to load").create()));
                player.spigot().sendMessage(msg);
            }
        }
        
        for (String ns : setManager.getLoadedNamespaces()) {
            Map<String, TemplateSet> sets = setManager.getGlobalNamespace(ns);
            if (sets.isEmpty()) continue;
            
            player.sendMessage(ChatColor.GOLD + "=== Namespace: " + ns + " ===");
            for (String name : sets.keySet()) {
                TextComponent msg = new TextComponent("- " + ns + ":" + name);
                msg.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn set load " + ns + ":" + name));
                msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to load").create()));
                player.spigot().sendMessage(msg);
            }
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
