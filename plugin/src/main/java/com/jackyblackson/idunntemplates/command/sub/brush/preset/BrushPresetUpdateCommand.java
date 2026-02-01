package com.jackyblackson.idunntemplates.command.sub.brush.preset;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushPreset;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.BrushPresetManager;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.permission.PermissionNames;

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
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.update.usage"));
            return;
        }

        String query = args[1];
        String namespace = presetManager.resolveNamespace(query, player);
        String name = presetManager.resolveName(query);

        // Check permission if not personal
        if (!namespace.equals("player." + player.getName())) {
            if (!player.hasPermission(PermissionNames.Brushes.Presets.saveToNamespace$N + namespace)) { // Reusing save permission for update
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.no_perm_update", namespace));
                return;
            }
        }
        
        // Check existence
        BrushPreset existing = presetManager.getPreset(namespace, name);
        if (existing == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.update.not_exist", name, namespace));
            return;
        }

        // Get current brush session
        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        if (matName == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.common.no_item"));
            return;
        }

        var session = sessionManager.getSession(player.getUniqueId());
        BrushSession brushSession = session.getPreference().getBoundBrushes().get(matName);

        if (brushSession == null || brushSession.getChannels().isEmpty()) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.save.no_brushes"));
            return;
        }

        // Update Preset
        existing.setChannels(brushSession.getChannels());
        existing.setCreator(player.getName()); // Update creator? Maybe last modifier.
        presetManager.savePreset(namespace, existing);
        
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.update.success", name, namespace));
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
