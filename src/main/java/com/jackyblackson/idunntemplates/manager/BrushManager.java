package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSettings;
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
        
        var session = sessionManager.getSession(player.getUniqueId());
        if (session == null) return;
        
        BrushSession brushSession = session.getPreference().getBoundBrushes().get(matName);
        if (brushSession == null) return;
        
        String channel = null;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            channel = "right";
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            channel = "left";
        }
        
        if (channel == null) return;
        
        BrushSettings settings = brushSession.getSettings(channel);
        if (settings == null) return;
        
        // Prevent block breaking on left click if brush bound
        if (channel.equals("left")) {
            event.setCancelled(true);
        }
        
        // RayTrace
        RayTraceResult trace = player.rayTraceBlocks(MAX_DISTANCE);
        if (trace == null || trace.getHitBlock() == null) return;
        
        // Execute placement
        executeBrush(player, settings, trace.getHitBlock().getLocation());
    }
    
    public void executeBrush(Player player, BrushSettings settings, org.bukkit.Location targetLoc) {
        Template t = settings.getContent().pickRandom(templateManager, name -> setManager.getSet(name, player.getName()), false);
        if (t == null) return;
        
        int rot = resolveRotation(settings.getRotation());
        boolean fx = resolveFlip(settings.getFlipX());
        boolean fy = false;
        boolean fz = resolveFlip(settings.getFlipZ());
        
        // Use emptyOnly and noAir preferences?
        // InstanceManager has placeOnEmptyOnly support based on PlayerPreference.
        // BrushSettings has its own emptyOnly.
        // We might need to override player preference context or pass it to InstanceManager.
        // Current InstanceManager reads PlayerPreference directly.
        // To support brush-specific overrides, InstanceManager needs refactoring or we temporary mod preference? No.
        // Ideally InstanceManager should accept placement flags.
        // For Phase 4.2, we stick to basic placement.
        // We can check emptyOnly here manually before calling place?
        // RayTrace hit a block, so it's not air. 
        // If emptyOnly is true, we should check if the hit block is "empty" (replaceable)?
        // Wait, brush usually places "on top" or "at" the block.
        // If "at", it replaces.
        // If "on top", we need adjacent.
        // Usually brushes replace.
        // Let's assume replace at targetLoc.
        
        try {
            var instance = instanceManager.placeInstanceAndReturn(player, t, targetLoc, rot, fx, fy, fz);
            MessageUtil.sendMessageAfterPlace(instance, player);
        } catch (Exception e) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Brush error: " + e.getMessage());
        }
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
