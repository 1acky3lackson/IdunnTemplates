package com.jackyblackson.idunntemplates.command.sub.brush.preset;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushPreset;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSettings;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.BrushPresetManager;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;

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
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.usage"));
            return;
        }

        String sub = args[1].toLowerCase();
        
        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        if (matName == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.common.no_item"));
            return;
        }
        
        var session = sessionManager.getSession(player.getUniqueId());
        BrushSession brushSession = session.getPreference().getBoundBrushes().computeIfAbsent(matName, k -> new BrushSession());

        if (sub.equals("all")) {
            handleLoadAll(player, args, brushSession);
        } else if (sub.equals("channel")) {
            handleLoadChannel(player, args, brushSession);
        } else {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.unknown_sub", sub));
        }
        
        sessionManager.saveSession(player.getUniqueId());
    }

    private void handleLoadAll(Player player, String[] args, BrushSession brushSession) {
        // load all <query>
        if (args.length < 3) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.all.usage"));
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
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.all.conflict"));
            for (String conflict : conflicts) {
                TextComponent msg = new TextComponent(ChatColor.RED + "- " + conflict + " ");
                TextComponent x = new TextComponent(ChatColor.DARK_RED + "[X]");
                x.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.all.unbind_hover", conflict)).create()));
                x.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn brush unbind " + conflict));
                msg.addExtra(x);
                player.spigot().sendMessage(msg);
            }
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.all.unbind_hint"));
            return;
        }
        
        // Apply
        for (Map.Entry<String, BrushSettings> entry : preset.getChannels().entrySet()) {
            brushSession.getChannels().put(entry.getKey(), entry.getValue().clone());
        }
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.all.success", preset.getName(), String.join(", ", preset.getChannels().keySet())));
    }

    private void handleLoadChannel(Player player, String[] args, BrushSession brushSession) {
        // load channel <preset_chan> <query> [target_chan]
        // indices: 0     1        2             3        4
        if (args.length < 4) {
             player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.channel.usage"));
             return;
        }
        
        String presetChannel = args[2];
        String query = args[3];
        String targetChannel = (args.length > 4) ? args[4] : presetChannel;
        
        BrushPreset preset = resolveAndCheck(player, query);
        if (preset == null) return;
        
        BrushSettings settings = preset.getChannels().get(presetChannel);
        if (settings == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.channel.not_found", presetChannel, preset.getName()));
            return;
        }
        
        if (brushSession.getChannels().containsKey(targetChannel)) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.channel.already_bound", targetChannel));
            TextComponent msg = new TextComponent(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.channel.click_unbind"));
            msg.setColor(net.md_5.bungee.api.ChatColor.RED);
            TextComponent x = new TextComponent(ChatColor.DARK_RED + "[X]");
            x.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn brush unbind " + targetChannel));
            msg.addExtra(x);
            player.spigot().sendMessage(msg);
            return;
        }
        
        brushSession.getChannels().put(targetChannel, settings.clone());
        
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.load.channel.success", presetChannel, targetChannel));
    }

    private BrushPreset resolveAndCheck(Player player, String query) {
        String namespace = presetManager.resolveNamespace(query, player);
        String name = presetManager.resolveName(query);
        
        if (!namespace.equals("player." + player.getName())) {
             if (!player.hasPermission(PermissionNames.Brushes.Presets.loadNamespace$N + namespace)) {
                 player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.no_perm_load", namespace));
                 return null;
             }
        }
        
        BrushPreset preset = presetManager.getPreset(namespace, name);
        if (preset == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.preset.not_found", namespace + ":" + name));
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
