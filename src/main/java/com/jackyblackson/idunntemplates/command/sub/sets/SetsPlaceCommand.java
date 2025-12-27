package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.manager.InstanceManager;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetsPlaceCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final TemplateManager templateManager;
    private final InstanceManager instanceManager;

    public SetsPlaceCommand(SessionManager sessionManager, TemplateManager templateManager, InstanceManager instanceManager) {
        this.sessionManager = sessionManager;
        this.templateManager = templateManager;
        this.instanceManager = instanceManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        TemplateSet set = sessionManager.getSession(player.getUniqueId()).getPreference().getCurrentSet();
        if (set.getSources().isEmpty()) {
            player.sendMessage(ChatColor.RED + "Current set is empty.");
            return;
        }
        
        Template t = set.pickRandom(templateManager);
        if (t == null) {
            player.sendMessage(ChatColor.RED + "Could not resolve any templates from current set.");
            return;
        }
        
        int rot = set.resolveRotation();
        boolean fx = set.resolveFlipX();
        boolean fy = false;
        boolean fz = set.resolveFlipZ(); // Usually only one flip needed? Spec said "flipx" and "flipy".
        // Minecraft structures usually have rotation (y) and mirror (left/right -> flip z or x).
        // My domain model has flipX/Y/Z.
        // Spec says: "x flip", "y flip".
        // Let's map setFlipY to domain flipZ (horizontal mirror) if that's what makes sense in MC, 
        // OR map to vertical flip if supported.
        // Usually vertical flip is rare.
        // Assuming spec "y flip" means flip along Y axis (mirroring X/Z)? No that's rotation.
        // Assuming spec means FlipX and FlipZ (Horizontal mirrors).
        // But the field in TemplateSet I made is flipY.
        // Let's map flipY to flipZ for now as it's common to have 2 horizontal mirrors.
        
        fz = fy; 
        fy = false; // Vertical flip usually off.

        player.sendMessage(ChatColor.YELLOW + "Placing from set: " + t.getName());
        
        try {
            // We need to capture the created instance ID for Undo.
            // InstanceManager.placeInstance returns void currently.
            // I need to update InstanceManager to return the Instance.
            com.jackyblackson.idunntemplates.core.domain.Instance inst = 
                instanceManager.placeInstanceAndReturn(player, t, player.getLocation(), rot, fx, fy, fz);
                
            // Message handled in InstanceManager? Or here?
            // The spec says: "place command... need to tell player selected template... and undo option".
            // Since InstanceManager handles logic, maybe I should move the messaging logic to a shared utility or let InstanceManager handle it?
            // But InstanceManager is generic.
            // Let's modify InstanceManager.placeInstance to return Instance, and handle messaging here.
            // Wait, existing PlaceCommand also needs this message.
            
            // I will update InstanceManager to return Instance, and then send the message here.
            
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
