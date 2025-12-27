package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.PlayerPreference;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetsUpdateCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final SetManager setManager;

    public SetsUpdateCommand(SessionManager sessionManager, SetManager setManager) {
        this.sessionManager = sessionManager;
        this.setManager = setManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn set update (<namespace>:)<name>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn set update (<namespace>:)<name>");
            return;
        }

        String rawName = args[1];
        
        // Parse namespace
        String namespace = null;
        String name = rawName;
        if (rawName.contains(":")) {
            String[] parts = rawName.split(":", 2);
            namespace = parts[0];
            name = parts[1];
        }
        
        // Logic:
        // 1. If explicit namespace:
        //    - If "player.<me>", update private.
        //    - Else, check permission (idunn.set.update.<namespace>) and update global.
        // 2. If no namespace:
        //    - Check private "player.<me>". If exists, update.
        //    - Else, check permission "idunn.set.update.global", update "global".

        PlayerPreference pref = sessionManager.getSession(player.getUniqueId()).getPreference();
        TemplateSet current = pref.getCurrentSet();
        
        // Prepare new content (clone current)
        TemplateSet newSet = new TemplateSet();
        newSet.setRotate(current.getRotate());
        newSet.setFlipX(current.getFlipX());
        newSet.setFlipZ(current.getFlipZ());
        for (var src : current.getSources()) {
            newSet.addSource(src.getPath(), src.getWeight());
        }

        boolean updated = false;
        
        if (namespace != null) {
            if (namespace.equalsIgnoreCase("player." + player.getName())) {
                // Explicit private
                if (!pref.getSavedSets().containsKey(name)) {
                    player.sendMessage(ChatColor.RED + "Private set not found: " + name);
                    return;
                }
                pref.getSavedSets().put(name, newSet);
                updated = true;
            } else if (namespace.startsWith("player.")) {
                 player.sendMessage(ChatColor.RED + "Cannot update other player's set.");
                 return;
            } else {
                // Global Namespace
                if (!player.hasPermission("idunn.set.update." + namespace)) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to update sets in namespace: " + namespace);
                    return;
                }
                TemplateSet existing = setManager.getSetExact(namespace, name);
                if (existing == null) {
                    player.sendMessage(ChatColor.RED + "Set not found in namespace " + namespace + ": " + name);
                    return;
                }
                setManager.saveGlobalSet(namespace, name, newSet);
                updated = true;
            }
        } else {
            // No namespace
            if (pref.getSavedSets().containsKey(name)) {
                pref.getSavedSets().put(name, newSet);
                updated = true;
                player.sendMessage(ChatColor.GREEN + "Updated private set: " + name);
            } else {
                // Try global default
                if (!player.hasPermission("idunn.set.update.global")) {
                     player.sendMessage(ChatColor.RED + "Set not found in private storage, and no permission to update global.");
                     return;
                }
                TemplateSet existing = setManager.getSetExact("global", name);
                if (existing != null) {
                    setManager.saveGlobalSet("global", name, newSet);
                    updated = true;
                    player.sendMessage(ChatColor.GREEN + "Updated global set: " + name);
                } else {
                    player.sendMessage(ChatColor.RED + "Set not found: " + name);
                    return;
                }
            }
        }
        
        if (updated) {
            sessionManager.saveSession(player.getUniqueId());
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
