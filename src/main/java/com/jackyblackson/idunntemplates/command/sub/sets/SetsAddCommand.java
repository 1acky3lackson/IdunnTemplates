package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SetsAddCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final TemplateManager templateManager;

    public SetsAddCommand(SessionManager sessionManager, TemplateManager templateManager) {
        this.sessionManager = sessionManager;
        this.templateManager = templateManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn set add <path> [weight]
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn set add <path> [weight]");
            return;
        }
        
        String path = args[1];
        double weight = 1.0;
        if (args.length > 2) {
            try {
                weight = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid weight.");
                return;
            }
        }
        
        TemplateSet set = sessionManager.getSession(player.getUniqueId()).getPreference().getCurrentSet();
        set.addSource(path, weight);
        sessionManager.saveSession(player.getUniqueId());
        
        player.sendMessage(ChatColor.GREEN + "Added source: " + path + " (Weight: " + weight + ")");
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            Set<String> paths = new HashSet<>();
            templateManager.getTemplates().forEach(t -> {
                String p = t.getPath();
                if (p.startsWith("_")) p = p.substring(1);
                paths.add(p);
                
                // Add directory parts
                String[] parts = p.split("/");
                StringBuilder current = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    current.append(parts[i]).append("/");
                    paths.add(current.toString());
                }
            });
            return filter(new ArrayList<>(paths), args[1]);
        }
        return Collections.emptyList();
    }
}
