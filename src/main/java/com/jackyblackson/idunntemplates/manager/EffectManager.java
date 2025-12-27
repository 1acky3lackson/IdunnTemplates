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
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EffectManager extends BukkitRunnable implements Listener {

    private final TemplateManager templateManager;
    private final InstanceRepository instanceRepository;
    private final SessionManager sessionManager;
    private final SetManager setManager;
    
    private final Map<UUID, BossBar> activeBossBars = new ConcurrentHashMap<>();
    private final Map<UUID, List<BossBar>> activeSetBars = new ConcurrentHashMap<>();

    private static final double VIEW_DISTANCE = 48.0;
    private static final double GRID_SPACING = 10.0;
    
    // ... constructor ...
    public EffectManager(TemplateManager templateManager, InstanceRepository instanceRepository, SessionManager sessionManager, SetManager setManager) {
        this.templateManager = templateManager;
        this.instanceRepository = instanceRepository;
        this.sessionManager = sessionManager;
        this.setManager = setManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handlePlayer(player);
            handlePlayerSet(player);
        }
    }
    
    private void handlePlayerSet(Player player) {
        var session = sessionManager.getSession(player.getUniqueId());
        if (session == null) return;
        
        com.jackyblackson.idunntemplates.core.domain.PlayerPreference pref = session.getPreference();
        List<BossBar> bars = activeSetBars.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());

        // Check preference
        if (!pref.isBossBarSet()) {
            if (!bars.isEmpty()) {
                for (BossBar b : bars) {
                    b.removeAll();
                }
                bars.clear();
            }
            return;
        }

        com.jackyblackson.idunntemplates.core.set.TemplateSet set = pref.getCurrentSet();
        List<com.jackyblackson.idunntemplates.core.set.TemplateSetSource> sources = set.getSources();
        
        if (sources.isEmpty()) {
            // Clear all
            for (BossBar b : bars) {
                b.removeAll();
            }
            bars.clear();
            return;
        }
        
        // We need 1 header + N source bars
        int needed = 1 + sources.size();
        
        // Adjust size
        while (bars.size() < needed) {
            BossBar b = Bukkit.createBossBar("", BarColor.PURPLE, BarStyle.SOLID);
            b.addPlayer(player);
            bars.add(b);
        }
        while (bars.size() > needed) {
            BossBar b = bars.remove(bars.size() - 1);
            b.removeAll();
        }
        
        // Update Content
        // Header
        // Resolve total templates including recursive sets
        int totalTemplates = set.resolveTemplates(templateManager, name -> setManager.getSet(name, player.getName()), false).size();
        String header = String.format("Sets: %d templates, rotate: %s, flipx: %s, flipz: %s",
                totalTemplates, set.getRotate(), set.getFlipX(), set.getFlipZ());
        bars.get(0).setTitle(header);
        
        // Sources
        for (int i = 0; i < sources.size(); i++) {
            com.jackyblackson.idunntemplates.core.set.TemplateSetSource src = sources.get(i);
            
            // Temporary set to resolve single source
            com.jackyblackson.idunntemplates.core.set.TemplateSet tmp = new com.jackyblackson.idunntemplates.core.set.TemplateSet();
            tmp.addSource(src.getPath(), src.getWeight());
            int count = tmp.resolveTemplates(templateManager, name -> setManager.getSet(name, player.getName()), false).size();
            
            String line = String.format("[%.1f] (%d templates) %s", src.getWeight(), count, src.getPath());
            bars.get(i+1).setTitle(line);
        }
    }

    private void handlePlayer(Player player) {
        // State tracking for BossBar
        String bossBarTitle = null;
        BarColor bossBarColor = null;
        
        var session = sessionManager.getSession(player.getUniqueId());
        com.jackyblackson.idunntemplates.core.domain.PlayerPreference pref = (session != null) ? session.getPreference() : null;

        // 1. Check Wand
        boolean holdingWand = false;
        if (pref != null) {
            String wandMat = pref.getWandMaterialName();
            try {
                Material mat = Material.valueOf(wandMat);
                if (player.getInventory().getItemInMainHand().getType() == mat) {
                    holdingWand = true;
                }
            } catch (IllegalArgumentException ignored) {}
        }

        if (holdingWand && pref != null && pref.isParticleWand()) {
            ParticleUtil.spawnMagicParticles(player.getLocation().add(0, 1, 0));
        }

        Location pLoc = player.getLocation();

        // 2. Template Origins (Master)
        for (Template t : templateManager.getTemplates()) {
            TemplateMetadata meta = t.getMetadata();
            if (!meta.getWorldId().equals(pLoc.getWorld().getUID())) continue;
            
            Location min = new Location(pLoc.getWorld(), meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
            if (min.distance(pLoc) > VIEW_DISTANCE * 2) continue; // optimization
            
            Location max = min.clone().add(meta.getWidth(), meta.getHeight(), meta.getLength());
            
            boolean canCommit = hasCommitPermission(player, t);
            Particle particle = canCommit ? Particle.HAPPY_VILLAGER : Particle.ANGRY_VILLAGER;
            
            // Draw Grid Box
            if (min.distance(pLoc) < VIEW_DISTANCE) {
                if (pref != null && pref.isParticleTemplateBoundaries()) {
                    ParticleUtil.drawSurfaceGridAABB(min, max, GRID_SPACING, particle);
                }
            }
            
            // Check Inside for BossBar
            if (isInAABB(pLoc, min, max)) {
                if (pref != null && pref.isBossBarTemplate()) {
                    bossBarTitle = (canCommit ? ChatColor.GREEN : ChatColor.RED) + "Template Master: " + t.getPath();
                    bossBarColor = canCommit ? BarColor.GREEN : BarColor.RED;
                }
            }
        }

        // 3. Instances
        List<Instance> instances = instanceRepository.getAllLoadedInstances();
        for (Instance inst : instances) {
            if (!inst.getWorldId().equals(pLoc.getWorld().getUID())) continue;
            if (Math.abs(inst.getX() - pLoc.getX()) > VIEW_DISTANCE * 2 || Math.abs(inst.getZ() - pLoc.getZ()) > VIEW_DISTANCE * 2) continue;

            Template t = templateManager.getTemplate(inst.getTemplateId());
            if (t == null) continue;

            Location[] corners = calculateCorners(inst, t);
            Location center = calculateCenter(corners);
            
            // Since we know corners align to axes (0/90/180/270 rot), we can extract min/max for AABB check
            double minX = corners[0].getX(), minY = corners[0].getY(), minZ = corners[0].getZ();
            double maxX = corners[6].getX(), maxY = corners[6].getY(), maxZ = corners[6].getZ();
            Location min = new Location(pLoc.getWorld(), minX, minY, minZ);
            Location max = new Location(pLoc.getWorld(), maxX, maxY, maxZ);

            boolean isInside = isInAABB(pLoc, min, max);
            
            if (holdingWand || isInside) {
                // Draw Box
                if (min.distance(pLoc) < VIEW_DISTANCE) {
                    if (pref != null && pref.isParticleInstanceBoundaries()) {
                        ParticleUtil.drawSurfaceGridAABB(min, max, GRID_SPACING, Particle.END_ROD);
                        // Draw Line to Center
                        ParticleUtil.drawLine(player.getLocation().add(0, 1, 0), center, Particle.FLAME, 1.0, 0, 0, 0, 1);
                    }
                }
            }
            
            if (isInside) {
                if (bossBarTitle == null) {
                    if (pref != null && pref.isBossBarInstance()) {
                        bossBarTitle = ChatColor.BLUE + "Instance: " + t.getPath() + " (" + inst.getId().substring(0,8) + ")";
                        bossBarColor = BarColor.BLUE;
                    }
                }
            }
        }
        
        // Update BossBar
        updateBossBar(player, bossBarTitle, bossBarColor);
    }
    
    private void updateBossBar(Player player, String title, BarColor color) {
        BossBar bar = activeBossBars.get(player.getUniqueId());
        
        if (title == null) {
            if (bar != null) {
                bar.removeAll();
                activeBossBars.remove(player.getUniqueId());
            }
            return;
        }
        
        if (bar == null) {
            bar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
            bar.addPlayer(player);
            activeBossBars.put(player.getUniqueId(), bar);
        } else {
            bar.setTitle(title);
            bar.setColor(color);
            if (!bar.getPlayers().contains(player)) {
                bar.addPlayer(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BossBar bar = activeBossBars.remove(event.getPlayer().getUniqueId());
        if (bar != null) bar.removeAll();
        
        List<BossBar> setBars = activeSetBars.remove(event.getPlayer().getUniqueId());
        if (setBars != null) {
            for (BossBar b : setBars) b.removeAll();
        }
    }
    
    // Simple AABB draw (Deprecated in favor of Grid)
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
