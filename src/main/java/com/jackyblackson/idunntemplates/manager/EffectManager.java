package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.effect.ParticleUtil;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class EffectManager extends BukkitRunnable implements Listener {

    private final TemplateManager templateManager;
    private final InstanceRepository instanceRepository;
    private final SessionManager sessionManager;

    private static final double VIEW_DISTANCE = 48.0;

    public EffectManager(TemplateManager templateManager, InstanceRepository instanceRepository, SessionManager sessionManager) {
        this.templateManager = templateManager;
        this.instanceRepository = instanceRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handlePlayer(player);
        }
    }

    private void handlePlayer(Player player) {
        // 1. Check Wand
        var session = sessionManager.getSession(player.getUniqueId());
        if (session == null) return;
        
        String wandMat = session.getPreference().getWandMaterialName();
        boolean holdingWand = false;
        try {
            Material mat = Material.valueOf(wandMat);
            if (player.getInventory().getItemInMainHand().getType() == mat) {
                holdingWand = true;
            }
        } catch (IllegalArgumentException ignored) {}

        if (holdingWand) {
            ParticleUtil.spawnMagicParticles(player.getLocation().add(0, 1, 0));
        }

        Location pLoc = player.getLocation();

        // 2. Template Origins (Master)
        // Check all templates? Maybe too many. 
        // Optimize: Check templates in same world and distance
        for (Template t : templateManager.getTemplates()) {
            TemplateMetadata meta = t.getMetadata();
            if (!meta.getWorldId().equals(pLoc.getWorld().getUID())) continue;
            
            Location min = new Location(pLoc.getWorld(), meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
            if (min.distance(pLoc) > VIEW_DISTANCE) continue;
            
            // Calculate max
            Location max = min.clone().add(meta.getWidth(), meta.getHeight(), meta.getLength());
            
            // Determine permission
            // Permission node: idunn.commit.<path> ?? Or prompt says "commit permission"
            // Usually "idunn.template.commit" or "idunn.template.edit" + path
            // Let's assume generic "idunn.template.commit.all" or specific
            boolean canCommit = hasCommitPermission(player, t);
            
            Particle particle = canCommit ? Particle.HAPPY_VILLAGER : Particle.ANGRY_VILLAGER;
            // Draw Box (AABB)
            drawAABB(min, max, particle);
        }

        // 3. Instances
        List<Instance> instances = instanceRepository.getAllLoadedInstances();
        for (Instance inst : instances) {
            if (!inst.getWorldId().equals(pLoc.getWorld().getUID())) continue;
            if (Math.abs(inst.getX() - pLoc.getX()) > VIEW_DISTANCE || Math.abs(inst.getZ() - pLoc.getZ()) > VIEW_DISTANCE) continue;

            Template t = templateManager.getTemplate(inst.getTemplateId());
            if (t == null) continue;

            Location[] corners = calculateCorners(inst, t);
            Location center = calculateCenter(corners);
            
            boolean isInside = isInside(pLoc, corners);
            
            if (holdingWand || isInside) {
                // Draw Box
                ParticleUtil.drawBox(corners, Particle.END_ROD);
                // Draw Line to Center
                ParticleUtil.drawLine(player.getLocation().add(0, 1, 0), center, Particle.FLAME, 1.0, 0, 0, 0, 1);
            }
        }
    }
    
    // Simple AABB draw
    private void drawAABB(Location min, Location max, Particle particle) {
        Location[] c = new Location[8];
        c[0] = min;
        c[1] = new Location(min.getWorld(), min.getX(), min.getY(), max.getZ());
        c[2] = new Location(min.getWorld(), min.getX(), max.getY(), min.getZ());
        c[3] = new Location(min.getWorld(), min.getX(), max.getY(), max.getZ());
        c[4] = new Location(min.getWorld(), max.getX(), min.getY(), min.getZ());
        c[5] = new Location(min.getWorld(), max.getX(), min.getY(), max.getZ());
        c[6] = new Location(min.getWorld(), max.getX(), max.getY(), min.getZ());
        c[7] = max;
        // Re-order for drawBox utility (which expects bottom 4 then top 4)
        // My utility expects: 
        // 0-3 bottom ring
        // 4-7 top ring
        // Let's adjust manually
        Location[] sorted = new Location[8];
        sorted[0] = new Location(min.getWorld(), min.getX(), min.getY(), min.getZ());
        sorted[1] = new Location(min.getWorld(), max.getX(), min.getY(), min.getZ());
        sorted[2] = new Location(min.getWorld(), max.getX(), min.getY(), max.getZ());
        sorted[3] = new Location(min.getWorld(), min.getX(), min.getY(), max.getZ());
        
        sorted[4] = new Location(min.getWorld(), min.getX(), max.getY(), min.getZ());
        sorted[5] = new Location(min.getWorld(), max.getX(), max.getY(), min.getZ());
        sorted[6] = new Location(min.getWorld(), max.getX(), max.getY(), max.getZ());
        sorted[7] = new Location(min.getWorld(), min.getX(), max.getY(), max.getZ());
        
        ParticleUtil.drawBox(sorted, particle);
    }

    private Location[] calculateCorners(Instance inst, Template t) {
        TemplateMetadata meta = t.getMetadata();
        int width = meta.getWidth();
        int height = meta.getHeight();
        int length = meta.getLength();
        
        // Adjust dimensions based on rotation (90 or 270 degrees swaps width and length)
        int rot = Math.abs(inst.getRotationY()) % 360;
        int currentWidth = (rot == 90 || rot == 270) ? length : width;
        int currentLength = (rot == 90 || rot == 270) ? width : length;
        
        // Instance X,Y,Z is defined as the Minimum Corner of the AABB
        double minX = inst.getX();
        double minY = inst.getY();
        double minZ = inst.getZ();
        
        double maxX = minX + currentWidth;
        double maxY = minY + height;
        double maxZ = minZ + currentLength;
        
        Location[] worldCorners = new Location[8];
        World world = Bukkit.getWorld(inst.getWorldId());
        
        // Bottom ring
        worldCorners[0] = new Location(world, minX, minY, minZ);
        worldCorners[1] = new Location(world, maxX, minY, minZ);
        worldCorners[2] = new Location(world, maxX, minY, maxZ);
        worldCorners[3] = new Location(world, minX, minY, maxZ);
        
        // Top ring
        worldCorners[4] = new Location(world, minX, maxY, minZ);
        worldCorners[5] = new Location(world, maxX, maxY, minZ);
        worldCorners[6] = new Location(world, maxX, maxY, maxZ);
        worldCorners[7] = new Location(world, minX, maxY, maxZ);
        
        return worldCorners;
    }
    
    private Vector rotateY(Vector v, int degrees) {
        // Standard rotation around Y axis
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double x = v.getX() * cos - v.getZ() * sin;
        double z = v.getX() * sin + v.getZ() * cos;
        return new Vector(x, v.getY(), z);
    }
    
    private Location calculateCenter(Location[] corners) {
        double x = 0, y = 0, z = 0;
        for (Location c : corners) {
            x += c.getX();
            y += c.getY();
            z += c.getZ();
        }
        return new Location(corners[0].getWorld(), x/8, y/8, z/8);
    }
    
    private boolean isInside(Location loc, Location[] corners) {
        // Point in Polygon/Polyhedron check. 
        // Since it's a convex cuboid, we can check if point is "between" all opposing face pairs.
        // Or simplified: transform point to local space (inverse transform) and check 0 <= p <= size.
        // Given we only have corners here and rotation might be arbitrary 90 deg steps.
        // Actually, with just 90 degree rotations, it's always an AABB? No, if rotated 45 it wouldn't be. 
        // But WorldEdit/Minecraft usually does 0, 90, 180, 270. So it IS an AABB aligned to axes?
        // Wait, if I rotate 90, the box is still axis-aligned.
        // Yes! Minecraft blocks are always axis aligned.
        // So we can just find min/max of the corners.
        
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        
        for (Location c : corners) {
            minX = Math.min(minX, c.getX());
            minY = Math.min(minY, c.getY());
            minZ = Math.min(minZ, c.getZ());
            maxX = Math.max(maxX, c.getX());
            maxY = Math.max(maxY, c.getY());
            maxZ = Math.max(maxZ, c.getZ());
        }
        
        return loc.getX() >= minX && loc.getX() <= maxX &&
               loc.getY() >= minY && loc.getY() <= maxY &&
               loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
    
    private boolean hasCommitPermission(Player p, Template t) {
        // Logic: Owner always can. 
        // Others need "idunn.template.modify.all" or specific?
        if (p.getUniqueId().equals(t.getMetadata().getCreatorId())) return true;
        if (p.hasPermission("idunn.template.modify.all")) return true;
        // Check path permission
        String path = t.getPath().replace("/", ".");
        if (path.startsWith("_")) path = path.substring(1);
        return p.hasPermission("idunn.template.modify." + path);
    }

    // --- Listener for Entry Denial ---
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null) return;
        Location from = event.getFrom();
        
        // Optimization: only check if block changed
        if (to.getBlockX() == from.getBlockX() && to.getBlockY() == from.getBlockY() && to.getBlockZ() == from.getBlockZ()) return;
        
        Player player = event.getPlayer();
        
        // Check Template Origins
        for (Template t : templateManager.getTemplates()) {
             TemplateMetadata meta = t.getMetadata();
             if (!meta.getWorldId().equals(to.getWorld().getUID())) continue;
             
             // Check permission first? No, only check collision first to save perf.
             if (to.distanceSquared(new Location(to.getWorld(), meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ())) > 10000) continue; // Fast reject

             Location min = new Location(to.getWorld(), meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
             Location max = min.clone().add(meta.getWidth(), meta.getHeight(), meta.getLength());
             
             if (isInAABB(to, min, max)) {
                 // Player is entering or inside.
                 if (!hasCommitPermission(player, t)) {
                     // Deny
                     event.setCancelled(true);
                     player.sendMessage(ChatColor.RED + "You do not have permission to enter the master template area: " + t.getName());
                     
                     // Push back slightly to prevent sticking
                     Vector direction = from.toVector().subtract(to.toVector()).normalize().multiply(0.5);
                     player.setVelocity(direction);
                     return;
                 }
             }
        }
    }
    
    private boolean isInAABB(Location loc, Location min, Location max) {
        return loc.getX() >= min.getX() && loc.getX() <= max.getX() &&
               loc.getY() >= min.getY() && loc.getY() <= max.getY() &&
               loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
    }
}
