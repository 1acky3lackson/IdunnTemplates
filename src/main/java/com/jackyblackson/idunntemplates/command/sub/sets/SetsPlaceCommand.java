package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.InstanceManager;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetsPlaceCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final TemplateManager templateManager;
    private final InstanceManager instanceManager;
    private final SetManager setManager;

    public SetsPlaceCommand(SessionManager sessionManager, TemplateManager templateManager, InstanceManager instanceManager, SetManager setManager) {
        this.sessionManager = sessionManager;
        this.templateManager = templateManager;
        this.instanceManager = instanceManager;
        this.setManager = setManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        var session = sessionManager.getSession(player.getUniqueId());
        if (session == null) return;
        
        TemplateSet set = session.getPreference().getCurrentSet();
        if (set.getSources().isEmpty()) {
            player.sendMessage(ChatColor.RED + "Current set is empty.");
            return;
        }
        
        // Use pre-calculated placement
        com.jackyblackson.idunntemplates.core.domain.PlayerSession.NextPlacement next = session.getNextPlacement();
        if (next == null) {
            // Try generating now if missing
            sessionManager.regenerateNextPlacement(player.getUniqueId());
            next = session.getNextPlacement();
        }
        
        if (next == null || next.getTemplate() == null) {
            player.sendMessage(ChatColor.RED + "Could not resolve any templates from current set.");
            return;
        }
        
        Template t = next.getTemplate();
        int rot = next.getRotation();
        boolean fx = next.isFlipX();
        boolean fy = false;
        boolean fz = next.isFlipZ();

        player.sendMessage(ChatColor.YELLOW + "Placing from set: " + t.getName());
        
        try {
            // We need to capture the created instance ID for Undo.
            com.jackyblackson.idunntemplates.core.domain.Instance inst = 
                instanceManager.placeInstanceAndReturn(player, t, player.getLocation(), rot, fx, fy, fz);
                
            // Regenerate next
            sessionManager.regenerateNextPlacement(player.getUniqueId());
            
            // Generate clickable message
            net.md_5.bungee.api.chat.TextComponent msg = new net.md_5.bungee.api.chat.TextComponent("Placed " + t.getName() + " (" + inst.getId().substring(0,8) + ") ");
            msg.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            
            net.md_5.bungee.api.chat.TextComponent tp = new net.md_5.bungee.api.chat.TextComponent("[TP]");
            tp.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            tp.setBold(true);
            tp.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/idunn template tp " + t.getPath()));
            tp.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.ComponentBuilder("Teleport to Master").create()));
            
            net.md_5.bungee.api.chat.TextComponent undo = new net.md_5.bungee.api.chat.TextComponent(" [UNDO]");
            undo.setColor(net.md_5.bungee.api.ChatColor.RED);
            undo.setBold(true);
            // Undo command: we need a command to delete instance AND revert blocks.
            // I will implement /idunn instance undo <id>
            undo.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/idunn instance undo " + inst.getId()));
            undo.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.ComponentBuilder("Delete instance and undo blocks").create()));
            
            msg.addExtra(tp);
            msg.addExtra(undo);
            
            player.spigot().sendMessage(msg);

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error placing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
