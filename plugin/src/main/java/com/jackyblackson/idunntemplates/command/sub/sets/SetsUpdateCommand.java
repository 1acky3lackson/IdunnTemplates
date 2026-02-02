package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.PlayerPreference;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;

import org.bukkit.entity.Player;

import java.util.ArrayList;
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
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.update.usage"));
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

        if (namespace != null) {
            if (namespace.equalsIgnoreCase("player." + player.getName())) {
                // Explicit private
                if (!pref.getSavedSets().containsKey(name)) {
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.update.private_not_found", name));
                    return;
                }
                pref.getSavedSets().put(name, newSet);
            } else if (namespace.startsWith("player.")) {
                 player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.update.error_player"));
                 return;
            } else {
                // Global Namespace
                if (!player.hasPermission(PermissionNames.Sets.updateInNamespace$N + namespace)) {
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.update.no_perm", namespace));
                    return;
                }
                TemplateSet existing = setManager.getSetExact(namespace, name);
                if (existing == null) {
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.update.not_found_ns", namespace, name));
                    return;
                }
                setManager.saveGlobalSet(namespace, name, newSet);
            }
        } else {
            // No namespace
            if (pref.getSavedSets().containsKey(name)) {
                pref.getSavedSets().put(name, newSet);
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.update.success_private", name));
            } else {
                // Try global default
                if (!player.hasPermission(PermissionNames.Sets.updateGlobal)) {
                     player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.update.no_perm_global"));
                     return;
                }
                TemplateSet existing = setManager.getSetExact("global", name);
                if (existing != null) {
                    setManager.saveGlobalSet("global", name, newSet);
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.update.success_global", name));
                } else {
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.update.not_found", name));
                    return;
                }
            }
        }

        sessionManager.saveSession(player.getUniqueId());
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            List<String> list = new ArrayList<>(sessionManager.getSession(player.getUniqueId()).getPreference().getSavedSets().keySet());
            
            // Add global sets the player can update
            for (String ns : setManager.getLoadedNamespaces()) {
                if (player.hasPermission(PermissionNames.Sets.updateInNamespace$N + ns)) {
                    for (String name : setManager.getGlobalNamespace(ns).keySet()) {
                        list.add(ns + ":" + name);
                    }
                }
            }
            
            return filter(list, args[1]);
        }
        return Collections.emptyList();
    }
}
