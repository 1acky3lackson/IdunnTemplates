package com.jackyblackson.idunntemplates.command.sub.brush.preset;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushPreset;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.BrushPresetManager;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BrushPresetUpdateCommand extends BaseSubCommand {

    private final BrushPresetManager presetManager;
    private final SessionManager sessionManager;

    public BrushPresetUpdateCommand(BrushPresetManager presetManager, SessionManager sessionManager) {
        this.presetManager = presetManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // args: update [<ns>:]<name>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn brush preset update [<namespace>:]<name>");
            return;
        }

        String query = args[1];
        String namespace = presetManager.resolveNamespace(query, player);
        String name = presetManager.resolveName(query);

        // Check permission if not personal
        if (!namespace.equals("player." + player.getName())) {
            if (!player.hasPermission("idunn.brush.preset.save." + namespace)) { // Reusing save permission for update
                player.sendMessage(ChatColor.RED + "You do not have permission to update namespace '" + namespace + "'.");
                return;
            }
        }
        
        // Check existence
        BrushPreset existing = presetManager.getPreset(namespace, name);
        if (existing == null) {
            player.sendMessage(ChatColor.RED + "Preset '" + name + "' does not exist in namespace '" + namespace + "'. Use 'save' to create it.");
            return;
        }

        // Get current brush session
        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        if (matName == null) {
            player.sendMessage(ChatColor.RED + "You must hold an item.");
            return;
        }

        var session = sessionManager.getSession(player.getUniqueId());
        BrushSession brushSession = session.getPreference().getBoundBrushes().get(matName);

        if (brushSession == null || brushSession.getChannels().isEmpty()) {
            player.sendMessage(ChatColor.RED + "This item has no bound brushes.");
            return;
        }

        // Update Preset
        existing.setChannels(brushSession.getChannels());
        existing.setCreator(player.getName()); // Update creator? Maybe last modifier.
        presetManager.savePreset(namespace, existing);
        
        player.sendMessage(ChatColor.GREEN + "Updated brush preset '" + name + "' in namespace '" + namespace + "'.");
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            List<String> options = new ArrayList<>();
            // Suggest personal
            String personalNs = "player." + player.getName();
            Set<String> personal = presetManager.getPresetsInNamespace(personalNs).keySet();
            options.addAll(personal);
            
            // Suggest others if permission? Or just list all visible?
            // Usually tab complete lists what's available.
            for (String ns : presetManager.getNamespaces()) {
                if (!ns.equals(personalNs)) {
                    for (String name : presetManager.getPresetsInNamespace(ns).keySet()) {
                        options.add(ns + ":" + name);
                    }
                }
            }
            return filter(options, args[1]);
        }
        return Collections.emptyList();
    }
}
