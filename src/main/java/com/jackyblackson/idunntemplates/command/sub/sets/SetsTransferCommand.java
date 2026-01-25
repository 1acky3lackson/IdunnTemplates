package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.PlayerPreference;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import com.jackyblackson.idunntemplates.permission.PermissionNames;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetsTransferCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final SetManager setManager;

    public SetsTransferCommand(SessionManager sessionManager, SetManager setManager) {
        this.sessionManager = sessionManager;
        this.setManager = setManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn set transferToGlobal <private_set_name> <namespace>:name
        if (args.length < 3) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.transfer.usage"));
            return;
        }

        String privateName = args[1];
        String targetRaw = args[2];
        
        PlayerPreference pref = sessionManager.getSession(player.getUniqueId()).getPreference();
        TemplateSet privateSet = pref.getSavedSets().get(privateName);
        
        if (privateSet == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.transfer.private_not_found", privateName));
            return;
        }
        
        String namespace = "global";
        String name = targetRaw;
        
        if (targetRaw.contains(":")) {
            String[] parts = targetRaw.split(":", 2);
            namespace = parts[0];
            name = parts[1];
        } else {
             player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.transfer.assume_global"));
        }
        
        if (namespace.startsWith("player.")) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.transfer.cannot_transfer_player"));
            return;
        }
        
        if (!player.hasPermission(PermissionNames.Sets.saveToNamespace$N + namespace)) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.transfer.no_permission_create_ns", namespace));
            return;
        }
        
        // Check if exists
        if (setManager.getSetExact(namespace, name) != null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.transfer.set_already_exists", namespace, name));
            return;
        }
        
        // Clone
        TemplateSet newSet = new TemplateSet();
        newSet.setRotate(privateSet.getRotate());
        newSet.setFlipX(privateSet.getFlipX());
        newSet.setFlipZ(privateSet.getFlipZ());
        for (var src : privateSet.getSources()) {
            newSet.addSource(src.getPath(), src.getWeight());
        }
        
        setManager.saveGlobalSet(namespace, name, newSet);
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.transfer.success", privateName, namespace, name));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
             return filter(new java.util.ArrayList<>(sessionManager.getSession(player.getUniqueId()).getPreference().getSavedSets().keySet()), args[1]);
        }
        return Collections.emptyList();
    }
}
