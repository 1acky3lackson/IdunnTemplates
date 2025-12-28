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
            player.sendMessage(ChatColor.RED + "Usage: /idunn brush preset save [<namespace>:]<name> <description>");
            return;
        }

        String query = args[1];
        String description = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        
        String namespace = presetManager.resolveNamespace(query, player);
        String name = presetManager.resolveName(query);

        // Check permission if not personal
        if (!namespace.equals("player." + player.getName())) {
            if (!player.hasPermission("idunn.brush.preset.save." + namespace)) {
                player.sendMessage(ChatColor.RED + "You do not have permission to save to namespace '" + namespace + "'.");
                return;
            }
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

        // Create Preset
        BrushPreset preset = new BrushPreset(name, player.getName(), description, brushSession.getChannels());
        presetManager.savePreset(namespace, preset);
        
        player.sendMessage(ChatColor.GREEN + "Saved brush preset '" + name + "' to namespace '" + namespace + "'.");
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
