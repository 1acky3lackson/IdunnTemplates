package com.jackyblackson.idunntemplates.command.sub.brush;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BrushListCommand extends BaseSubCommand {

    private final SessionManager sessionManager;

    public BrushListCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn brush list [material_to_retrieve]
        
        // If argument provided, it's a retrieval request
        if (args.length >= 2) {
             String matName = args[1].toUpperCase();
             handleRetrieve(player, matName);
             return;
        }

        // Otherwise list
        var session = sessionManager.getSession(player.getUniqueId());
        var boundBrushes = session.getPreference().getBoundBrushes();
        
        if (boundBrushes.isEmpty()) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.list.empty"));
            return;
        }
        
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.list.header"));
        for (Map.Entry<String, BrushSession> entry : boundBrushes.entrySet()) {
            String matName = entry.getKey();
            BrushSession brushSession = entry.getValue();

            if (brushSession.getChannels().isEmpty()) {
                continue;
            }
            
            TextComponent msg = new TextComponent(ChatColor.GREEN + "- " + matName);
            String channels = String.join(", ", brushSession.getChannels().keySet());
            msg.addExtra(new TextComponent(ChatColor.GRAY + " [" + channels + "]"));
            
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn brush list " + matName));
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.list.tooltip", matName)).create()));
            
            player.spigot().sendMessage(msg);
        }
    }

    private void handleRetrieve(Player player, String matName) {
        // Validate material
        Material mat;
        try {
            mat = Material.valueOf(matName);
        } catch (IllegalArgumentException e) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.list.invalid_mat", matName));
            return;
        }

        var session = sessionManager.getSession(player.getUniqueId());
        if (!session.getPreference().getBoundBrushes().containsKey(matName)) {
             player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.list.not_bound", matName));
             return;
        }
        
        ItemStack item = new ItemStack(mat);
        player.getInventory().setItemInMainHand(item);
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.list.equipped", matName));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        // args[0] = list
        // args[1] = possible materials
        if (args.length == 2) {
            var session = sessionManager.getSession(player.getUniqueId());
            return filter(new ArrayList<>(session.getPreference().getBoundBrushes().keySet()), args[1]);
        }
        return Collections.emptyList();
    }
}
