package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.core.domain.PlayerPreference;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetsSaveCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final SetManager setManager;

    public SetsSaveCommand(SessionManager sessionManager, SetManager setManager) {
        this.sessionManager = sessionManager;
        this.setManager = setManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn set save <namespace:name>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn set save <name> or <namespace:name>");
            return;
        }
        
        String rawName = args[1];
        String namespace = null;
        String name = rawName;
        
        if (rawName.contains(":")) {
             String[] parts = rawName.split(":", 2);
             namespace = parts[0];
             name = parts[1];
        }
        
        PlayerPreference pref = sessionManager.getSession(player.getUniqueId()).getPreference();
        TemplateSet current = pref.getCurrentSet();
        TemplateSet saved = new TemplateSet();
        saved.setRotate(current.getRotate());
        saved.setFlipX(current.getFlipX());
        saved.setFlipZ(current.getFlipZ());
        for (var src : current.getSources()) {
            saved.addSource(src.getPath(), src.getWeight());
        }
        
        // Logic:
        // If namespace is "player.<me>" or null -> save to private
        // If namespace provided (and not private) -> save to global (check perm)
        
        if (namespace == null || namespace.equalsIgnoreCase("player." + player.getName())) {
             if (pref.getSavedSets().containsKey(name)) {
                 player.sendMessage(ChatColor.RED + "Set already exists: " + name + ". Use 'update' to overwrite.");
                 return;
             }
             pref.getSavedSets().put(name, saved);
             sessionManager.saveSession(player.getUniqueId());
             player.sendMessage(ChatColor.GREEN + "Saved current set to private: " + name);
        } else {
            // Global/Other namespace
            if (namespace.startsWith("player.")) {
                player.sendMessage(ChatColor.RED + "Cannot save to another player's namespace.");
                return;
            }
            if (!player.hasPermission("idunn.set.create." + namespace)) {
                 player.sendMessage(ChatColor.RED + "No permission to create set in namespace: " + namespace);
                 return;
            }
            if (setManager.getSetExact(namespace, name) != null) {
                player.sendMessage(ChatColor.RED + "Set already exists in " + namespace + ": " + name + ". Use 'update' command.");
                return;
            }
            setManager.saveGlobalSet(namespace, name, saved);
            player.sendMessage(ChatColor.GREEN + "Saved current set to " + namespace + ":" + name);
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
