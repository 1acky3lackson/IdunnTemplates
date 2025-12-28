package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.core.domain.PlayerSession;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSettings;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.core.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.util.concurrent.ThreadLocalRandom;

public class BrushManager implements Listener {

    private final SessionManager sessionManager;
    private final TemplateManager templateManager;
    private final InstanceManager instanceManager;
    private final SetManager setManager;

    private static final int MAX_DISTANCE = 100;

    public BrushManager(SessionManager sessionManager, TemplateManager templateManager, InstanceManager instanceManager, SetManager setManager) {
        this.sessionManager = sessionManager;
        this.templateManager = templateManager;
        this.instanceManager = instanceManager;
        this.setManager = setManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        
        if (matName == null) return;
        
        String channel = null;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            channel = "right";
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            channel = "left";
        }
        
        if (channel == null) return;

        // Prevent block breaking on left click if brush bound (or checked in triggerBrush)
        // We need to check existence before triggering to decide on cancelling event?
        // For now, let's try to trigger. If it returns true (executed or found), we cancel left click.
        // Actually, logic says: "If brush bound... cancel".
        
        boolean executed = triggerBrush(player, channel);
        if (channel.equals("left") && executed) {
            event.setCancelled(true);
        }
    }

    public boolean triggerBrush(Player player, String channel) {
        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        if (matName == null) return false;

        var session = sessionManager.getSession(player.getUniqueId());
        if (session == null) return false;

        BrushSession brushSession = session.getPreference().getBoundBrushes().get(matName);
        BrushSettings settings = null;
        
        if (brushSession != null) {
            settings = brushSession.getSettings(channel);
        }

        // Default Wand Logic
        if (settings == null && item.getType() == Material.BLAZE_ROD && "right".equalsIgnoreCase(channel)) {
            // Create temporary settings using global set
            TemplateSet globalSet = session.getPreference().getCurrentSet();
            if (globalSet != null && !globalSet.getSources().isEmpty()) {
                settings = new BrushSettings();
                settings.setContent(globalSet); // Share reference or clone? Reference is fine for reading.
                // Inherit default settings
            }
        }

        if (settings == null) return false;

        // RayTrace
        RayTraceResult trace = player.rayTraceBlocks(MAX_DISTANCE);
        if (trace == null || trace.getHitBlock() == null) return false;

        // Execute placement
        executeBrush(player, settings, trace.getHitBlock().getLocation());
        return true;
    }
    
    public void executeBrush(Player player, BrushSettings settings, org.bukkit.Location targetLoc) {
        if (settings.getNextPlacement() == null) {
            updateNextPlacement(settings, player);
        }
        var next = settings.getNextPlacement();
        if (next == null || next.getTemplate() == null) return;

        try {
            var instance = instanceManager.placeInstanceAndReturn(player, next.getTemplate(), targetLoc, next.getRotation(), next.isFlipX(), false, next.isFlipZ());
            MessageUtil.sendMessageAfterPlace(instance, player);
            // Update for next time
            updateNextPlacement(settings, player);
        } catch (Exception e) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Brush error: " + e.getMessage());
        }
    }

    public void updateNextPlacement(BrushSettings settings, Player player) {
        Template t = settings.getContent().pickRandom(templateManager, name -> setManager.getSet(name, player.getName()), false);
        if (t == null) {
            settings.setNextPlacement(null);
            return;
        }
        int rot = resolveRotation(settings.getRotation());
        boolean fx = resolveFlip(settings.getFlipX());
        boolean fz = resolveFlip(settings.getFlipZ());
        settings.setNextPlacement(new PlayerSession.NextPlacement(t, rot, fx, fz));
    }
    
    private int resolveRotation(BrushSettings.RotationMode mode) {
        return switch (mode) {
            case FIXED_0 -> 0;
            case FIXED_90 -> 90;
            case FIXED_180 -> 180;
            case FIXED_270 -> 270;
            case RANDOM -> {
                int[] rots = {0, 90, 180, 270};
                yield rots[ThreadLocalRandom.current().nextInt(rots.length)];
            }
            default -> 0;
        };
    }
    
    private boolean resolveFlip(BrushSettings.FlipMode mode) {
        return switch (mode) {
            case TRUE -> true;
            case FALSE -> false;
            case RANDOM -> ThreadLocalRandom.current().nextBoolean();
            default -> false;
        };
    }
}
