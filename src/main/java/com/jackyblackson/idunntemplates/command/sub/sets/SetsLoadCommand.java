package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.core.domain.PlayerPreference;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetsLoadCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final SetManager setManager;

    public SetsLoadCommand(SessionManager sessionManager, SetManager setManager) {
        this.sessionManager = sessionManager;
        this.setManager = setManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn set load <name>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn set load <name>");
            return;
        }
        
        String rawName = args[1];
        PlayerPreference pref = sessionManager.getSession(player.getUniqueId()).getPreference();
        
        // Use SetManager logic: try explicit, then player.<me>, then global
        TemplateSet saved = setManager.getSet(rawName, player.getName());
        
        if (saved == null) {
            player.sendMessage(ChatColor.RED + "Preset not found: " + rawName);
            return;
        }
        
        // Copy to current
        TemplateSet current = new TemplateSet();
        current.setRotate(saved.getRotate());
        current.setFlipX(saved.getFlipX());
        current.setFlipZ(saved.getFlipZ());
        for (var src : saved.getSources()) {
            current.addSource(src.getPath(), src.getWeight());
        }
        
        pref.setCurrentSet(current);
        sessionManager.saveSession(player.getUniqueId());
        sessionManager.regenerateNextPlacement(player.getUniqueId());
        
        player.sendMessage(ChatColor.GREEN + "Loaded preset: " + rawName);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            List<String> list = new ArrayList<>(sessionManager.getSession(player.getUniqueId()).getPreference().getSavedSets().keySet());
            // Add global sets
            for (String ns : setManager.getLoadedNamespaces()) {
                for (String name : setManager.getGlobalNamespace(ns).keySet()) {
                    list.add(ns + ":" + name);
                }
            }
            return filter(list, args[1]);
        }
        return Collections.emptyList();
    }
}
