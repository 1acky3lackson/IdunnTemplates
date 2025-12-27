package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SetsAddSubsetCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final SetManager setManager;

    public SetsAddSubsetCommand(SessionManager sessionManager, SetManager setManager) {
        this.sessionManager = sessionManager;
        this.setManager = setManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn set add subset <set_name> [weight]
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn set add subset <set_reference> [weight]");
            return;
        }
        
        String subset = args[1];
        double weight = 1.0;
        if (args.length > 2) {
            try {
                weight = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid weight.");
                return;
            }
        }
        
        // Validate if set exists (warn if not, but allow? Recursive might be created later? 
        // Better to warn but allow, or strict check?
        // Let's do a soft check and warn.)
        
        TemplateSet target = setManager.getSet(subset, player.getName());
        if (target == null) {
            player.sendMessage(ChatColor.YELLOW + "Warning: Subset '" + subset + "' not found currently. It might be invalid.");
        }
        
        // Add with "set:" prefix to ensure it's treated as a set reference
        String sourcePath = "set:" + subset;
        
        TemplateSet set = sessionManager.getSession(player.getUniqueId()).getPreference().getCurrentSet();
        set.addSource(sourcePath, weight);
        sessionManager.saveSession(player.getUniqueId());
        sessionManager.regenerateNextPlacement(player.getUniqueId());
        
        player.sendMessage(ChatColor.GREEN + "Added subset source: " + subset + " (Weight: " + weight + ")");
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            List<String> options = new ArrayList<>();
            // Private
            options.addAll(sessionManager.getSession(player.getUniqueId()).getPreference().getSavedSets().keySet());
            // Global Namespaces
            for (String ns : setManager.getLoadedNamespaces()) {
                for (String name : setManager.getGlobalNamespace(ns).keySet()) {
                    options.add(ns + ":" + name);
                }
            }
            return filter(options, args[1]);
        }
        return Collections.emptyList();
    }
}
