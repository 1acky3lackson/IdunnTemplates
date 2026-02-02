package com.jackyblackson.idunntemplates.command.sub.brush.preset;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushPreset;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.BrushPresetManager;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class BrushPresetSaveCommand extends BaseSubCommand {

    private final BrushPresetManager presetManager;
    private final SessionManager sessionManager;

    public BrushPresetSaveCommand(BrushPresetManager presetManager, SessionManager sessionManager) {
        this.presetManager = presetManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // args: save [<ns>:]<name> <desc...>
        // indices: 0     1           2...
        if (args.length < 3) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.save.usage"));
            return;
        }

        String query = args[1];
        String description = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        
        String namespace = presetManager.resolveNamespace(query, player);
        String name = presetManager.resolveName(query);

        // Check permission if not personal
        if (!namespace.equals("player." + player.getName())) {
            if (!player.hasPermission(PermissionNames.Brushes.Presets.saveToNamespace$N + namespace)) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.no_perm_save", namespace));
                return;
            }
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

        // Create Preset
        BrushPreset preset = new BrushPreset(name, player.getName(), description, brushSession.getChannels());
        presetManager.savePreset(namespace, preset);
        
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.save.success", name, namespace));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
