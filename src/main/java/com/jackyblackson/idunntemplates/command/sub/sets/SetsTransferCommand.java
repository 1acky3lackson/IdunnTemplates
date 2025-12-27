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
            player.sendMessage(ChatColor.RED + "Usage: /idunn set transferToGlobal <private_set_name> <namespace>:name");
            return;
        }

        String privateName = args[1];
        String targetRaw = args[2];
        
        PlayerPreference pref = sessionManager.getSession(player.getUniqueId()).getPreference();
        TemplateSet privateSet = pref.getSavedSets().get(privateName);
        
        if (privateSet == null) {
            player.sendMessage(ChatColor.RED + "Private set not found: " + privateName);
            return;
        }
        
        String namespace = "global";
        String name = targetRaw;
        
        if (targetRaw.contains(":")) {
            String[] parts = targetRaw.split(":", 2);
            namespace = parts[0];
            name = parts[1];
        } else {
             player.sendMessage(ChatColor.YELLOW + "No namespace provided, assuming 'global'.");
        }
        
        if (namespace.startsWith("player.")) {
            player.sendMessage(ChatColor.RED + "Cannot transfer to a player namespace.");
            return;
        }
        
        if (!player.hasPermission("idunn.set.create." + namespace)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to create sets in namespace: " + namespace);
            return;
        }
        
        // Check if exists
        if (setManager.getSetExact(namespace, name) != null) {
            player.sendMessage(ChatColor.RED + "Set already exists in namespace " + namespace + ": " + name + ". Use 'update' to overwrite.");
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
        player.sendMessage(ChatColor.GREEN + "Transferred " + privateName + " to " + namespace + ":" + name);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
             return filter(new java.util.ArrayList<>(sessionManager.getSession(player.getUniqueId()).getPreference().getSavedSets().keySet()), args[1]);
        }
        return Collections.emptyList();
    }
}
