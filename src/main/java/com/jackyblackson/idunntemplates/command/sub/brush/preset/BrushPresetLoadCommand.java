package com.jackyblackson.idunntemplates.command.sub.brush.preset;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushPreset;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSettings;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.BrushPresetManager;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrushPresetLoadCommand extends BaseSubCommand {

    private final BrushPresetManager presetManager;
    private final SessionManager sessionManager;

    public BrushPresetLoadCommand(BrushPresetManager presetManager, SessionManager sessionManager) {
        this.presetManager = presetManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // args[0] is 'load' (from parent group dispatch)
        // usage: load all <query>
        // usage: load channel <preset_chan> <query> [target_chan]
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn brush preset load <all|channel> ...");
            return;
        }

        String sub = args[1].toLowerCase();
        
        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        if (matName == null) {
            player.sendMessage(ChatColor.RED + "You must hold an item.");
            return;
        }
        
        var session = sessionManager.getSession(player.getUniqueId());
        BrushSession brushSession = session.getPreference().getBoundBrushes().computeIfAbsent(matName, k -> new BrushSession());

        if (sub.equals("all")) {
            handleLoadAll(player, args, brushSession);
        } else if (sub.equals("channel")) {
            handleLoadChannel(player, args, brushSession);
        } else {
            player.sendMessage(ChatColor.RED + "Unknown sub-command: " + sub);
        }
        
        sessionManager.saveSession(player.getUniqueId());
    }

    private void handleLoadAll(Player player, String[] args, BrushSession brushSession) {
        // load all <query>
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: ... load all [<namespace>:]<name>");
            return;
        }
        
        String query = args[2];
        BrushPreset preset = resolveAndCheck(player, query);
        if (preset == null) return;
        
        // Check conflicts
        List<String> conflicts = new ArrayList<>();
        for (String chan : preset.getChannels().keySet()) {
            if (brushSession.getChannels().containsKey(chan)) {
                conflicts.add(chan);
            }
        }
        
        if (!conflicts.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Cannot load preset. Conflicting channels found:");
            for (String conflict : conflicts) {
                TextComponent msg = new TextComponent(ChatColor.RED + "- " + conflict + " ");
                TextComponent x = new TextComponent(ChatColor.DARK_RED + "[X]");
                x.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unbind " + conflict).create()));
                x.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn brush unbind " + conflict));
                msg.addExtra(x);
                player.spigot().sendMessage(msg);
            }
            player.sendMessage(ChatColor.RED + "Please unbind these channels first.");
            return;
        }
        
        // Apply
        for (Map.Entry<String, BrushSettings> entry : preset.getChannels().entrySet()) {
            brushSession.getChannels().put(entry.getKey(), entry.getValue().clone());
        }
        player.sendMessage(ChatColor.GREEN + "Loaded preset '" + preset.getName() + "' (Channels: " + String.join(", ", preset.getChannels().keySet()) + ")");
    }

    private void handleLoadChannel(Player player, String[] args, BrushSession brushSession) {
        // load channel <preset_chan> <query> [target_chan]
        // indices: 0     1        2             3        4
        if (args.length < 4) {
             player.sendMessage(ChatColor.RED + "Usage: ... load channel <preset_channel> [<namespace>:]<name> [target_channel]");
             return;
        }
        
        String presetChannel = args[2];
        String query = args[3];
        String targetChannel = (args.length > 4) ? args[4] : presetChannel;
        
        BrushPreset preset = resolveAndCheck(player, query);
        if (preset == null) return;
        
        BrushSettings settings = preset.getChannels().get(presetChannel);
        if (settings == null) {
            player.sendMessage(ChatColor.RED + "Channel '" + presetChannel + "' not found in preset '" + preset.getName() + "'.");
            return;
        }
        
        if (brushSession.getChannels().containsKey(targetChannel)) {
            player.sendMessage(ChatColor.RED + "Channel '" + targetChannel + "' is already bound.");
            TextComponent msg = new TextComponent(ChatColor.RED + "Click to unbind: ");
            TextComponent x = new TextComponent(ChatColor.DARK_RED + "[X]");
            x.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn brush unbind " + targetChannel));
            msg.addExtra(x);
            player.spigot().sendMessage(msg);
            return;
        }
        
        brushSession.getChannels().put(targetChannel, settings.clone());
        
        player.sendMessage(ChatColor.GREEN + "Loaded channel '" + presetChannel + "' from preset to '" + targetChannel + "'.");
    }

    private BrushPreset resolveAndCheck(Player player, String query) {
        String namespace = presetManager.resolveNamespace(query, player);
        String name = presetManager.resolveName(query);
        
        if (!namespace.equals("player." + player.getName())) {
             if (!player.hasPermission("idunn.brush.preset.load." + namespace)) {
                 player.sendMessage(ChatColor.RED + "You do not have permission to load from namespace '" + namespace + "'.");
                 return null;
             }
        }
        
        BrushPreset preset = presetManager.getPreset(namespace, name);
        if (preset == null) {
            player.sendMessage(ChatColor.RED + "Preset not found: " + namespace + ":" + name);
            return null;
        }
        return preset;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return filter(java.util.Arrays.asList("all", "channel"), args[1]);
        }
        // ... more tab completion logic
        return Collections.emptyList();
    }
}
