package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.core.domain.PlayerPreference;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import com.jackyblackson.idunntemplates.permission.PermissionNames;

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
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.save.usage"));
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
                 player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.save.exists_private", name));
                 return;
             }
             pref.getSavedSets().put(name, saved);
             sessionManager.saveSession(player.getUniqueId());
             player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.save.success_private", name));
        } else {
            // Global/Other namespace
            if (namespace.startsWith("player.")) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.save.error_player_ns"));
                return;
            }
            if (!player.hasPermission(PermissionNames.Sets.saveToNamespace$N + namespace)) {
                 player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.save.no_perm", namespace));
                 return;
            }
            if (setManager.getSetExact(namespace, name) != null) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.save.exists_global", namespace, name));
                return;
            }
            setManager.saveGlobalSet(namespace, name, saved);
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.save.success_global", namespace, name));
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
