package com.jackyblackson.idunntemplates.listener;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.UUID;

public class PlayerMoveListener implements Listener {
    
    private final TemplateManager templateManager;

    public PlayerMoveListener(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null) return;
        Location from = event.getFrom();
        
        // Performance optimization: only check when block changes
        if (to.getBlockX() == from.getBlockX() && 
            to.getBlockY() == from.getBlockY() && 
            to.getBlockZ() == from.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        UUID worldId = to.getWorld().getUID();
        
        // Check if player is inside any LOCKED template's master region
        // We use the spatial query method from TemplateManager
        // AABB check is fast enough for a single player
        
        List<Template> intersecting = templateManager.getIntersectingTemplates(
                worldId,
                to.getBlockX(), to.getBlockY(), to.getBlockZ(),
                to.getBlockX(), to.getBlockY(), to.getBlockZ()
        );
        
        for (Template t : intersecting) {
            if (t.getMetadata().isLocked()) {
                // Show Action Bar
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        new TextComponent(ChatColor.GOLD + "🚧 Editing " + ChatColor.WHITE + t.getName() + 
                                ChatColor.GOLD + " (Locked) - Don't forget to " + ChatColor.YELLOW + "/idunn commit" + ChatColor.GOLD + "! 🚧"));
                return; // Only show one notification
            }
        }
    }
}
