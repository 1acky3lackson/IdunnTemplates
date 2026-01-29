package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSettings;
import com.jackyblackson.idunntemplates.core.effect.ParticleUtil;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.permission.PermissionNames;

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
    private final BrushManager brushManager;
    
    private final Map<UUID, BossBar> activeBossBars = new ConcurrentHashMap<>();
    private final Map<UUID, List<BossBar>> activeSetBars = new ConcurrentHashMap<>();

    private static final double VIEW_DISTANCE = 48.0;
    private static final double GRID_SPACING = 10.0;
    
    private static final List<org.bukkit.Color> COLOR_PALETTE = Arrays.asList(
            org.bukkit.Color.WHITE, org.bukkit.Color.SILVER, org.bukkit.Color.GRAY, org.bukkit.Color.ORANGE,
            org.bukkit.Color.RED, org.bukkit.Color.MAROON, org.bukkit.Color.YELLOW, org.bukkit.Color.OLIVE,
            org.bukkit.Color.LIME, org.bukkit.Color.GREEN, org.bukkit.Color.AQUA, org.bukkit.Color.TEAL,
            org.bukkit.Color.BLUE, org.bukkit.Color.NAVY, org.bukkit.Color.FUCHSIA, org.bukkit.Color.PURPLE
    );
    
    // ... constructor ...
    public EffectManager(TemplateManager templateManager, InstanceRepository instanceRepository, SessionManager sessionManager, SetManager setManager, BrushManager brushManager) {
        this.templateManager = templateManager;
        this.instanceRepository = instanceRepository;
        this.sessionManager = sessionManager;
        this.setManager = setManager;
        this.brushManager = brushManager;
    }

    private Particle.DustOptions getParticleColor(Instance inst) {
        int hash = Objects.hash(inst.getTemplateId(), inst.getWorldId(), inst.getX(), inst.getY(), inst.getZ());
        org.bukkit.Color color = COLOR_PALETTE.get(Math.abs(hash) % COLOR_PALETTE.size());
        return new Particle.DustOptions(color, 1.0f);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handlePlayer(player);
            if (!handlePlayerBrush(player)) {
                handlePlayerSet(player);
            }
        }
    }

    private boolean handlePlayerBrush(Player player) {
        var session = sessionManager.getSession(player.getUniqueId());
        if (session == null) return false;
        
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        if (matName == null) return false;
        
        BrushSession brushSession = session.getPreference().getBoundBrushes().get(matName);
        if (brushSession == null || brushSession.getChannels().isEmpty()) return false;
        
        List<BossBar> bars = activeSetBars.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        
        Map<String, BrushSettings> channels = brushSession.getChannels();
        int needed = channels.size();
        
        // Adjust size
        while (bars.size() < needed) {
            BossBar b = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
            b.addPlayer(player);
            bars.add(b);
        }
        while (bars.size() > needed) {
            BossBar b = bars.remove(bars.size() - 1);
            b.removeAll();
        }
        
        int i = 0;
        List<String> sortedKeys = new ArrayList<>(channels.keySet());
        sortedKeys.sort((a, b) -> {
            int scoreA = a.equals("right") ? 1 : (a.equals("left") ? 2 : 3);
            int scoreB = b.equals("right") ? 1 : (b.equals("left") ? 2 : 3);
            if (scoreA != scoreB) return scoreA - scoreB;
            return a.compareTo(b);
        });
        
        for (String ch : sortedKeys) {
            BrushSettings settings = channels.get(ch);
            
            if (settings.getNextPlacement() == null) {
                brushManager.updateNextPlacement(settings, player);
            }
            
            String title = formatBrushBar(ch, settings, player);
            bars.get(i).setTitle(title);
            bars.get(i).setColor(BarColor.YELLOW);
            i++;
        }
        
        return true;
    }
    
    private String formatBrushBar(String channel, BrushSettings settings, Player player) {
        int count = settings.getContent().getSources().size();
        
        String nextPath = "None";
        if (settings.getNextPlacement() != null && settings.getNextPlacement().getTemplate() != null) {
             nextPath = settings.getNextPlacement().getTemplate().getPath();
        }
        
        String randRot = settings.getRotation() == BrushSettings.RotationMode.RANDOM 
                ? com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.brush.rand_rot")
                : (settings.getRotation().name().startsWith("FIXED_") 
                        ? settings.getRotation().name().substring(6) 
                        : settings.getRotation().name().toLowerCase());

        String randFlip = com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.brush.rand_flip");
        
        ChatColor colX;
        if (settings.getFlipX() == BrushSettings.FlipMode.TRUE) colX = ChatColor.GREEN;
        else if (settings.getFlipX() == BrushSettings.FlipMode.FALSE) colX = ChatColor.RED;
        else colX = ChatColor.GOLD;
        String flipXStr = colX + (settings.getFlipX() == BrushSettings.FlipMode.RANDOM ? randFlip : "X");
        
        ChatColor colZ;
        if (settings.getFlipZ() == BrushSettings.FlipMode.TRUE) colZ = ChatColor.GREEN;
        else if (settings.getFlipZ() == BrushSettings.FlipMode.FALSE) colZ = ChatColor.RED;
        else colZ = ChatColor.GOLD;
        String flipZStr = colZ + (settings.getFlipZ() == BrushSettings.FlipMode.RANDOM ? randFlip : "Z");
        
        String noAir = settings.isNoAir() ? com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.brush.no_air") : "";
        String emptyOnly = settings.isEmptyOnly() ? com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.brush.empty_only") : "";
        
        return com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.brush.format", 
                channel.toUpperCase(), String.valueOf(count), nextPath, randRot, flipXStr, flipZStr, noAir, emptyOnly);
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
            BossBar b = bars.removeLast();
            b.removeAll();
        }
        
        // Update Content
        // Header
        // Resolve total templates including recursive sets
        int totalTemplates = set.resolveTemplates(templateManager, name -> setManager.getSet(name, player.getName()), false).size();
        String header = com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.set.header",
                String.valueOf(totalTemplates), set.getRotate(), set.getFlipX(), set.getFlipZ());
        bars.getFirst().setTitle(header);
        
        // Sources
        for (int i = 0; i < sources.size(); i++) {
            com.jackyblackson.idunntemplates.core.set.TemplateSetSource src = sources.get(i);
            
            // Temporary set to resolve single source
            com.jackyblackson.idunntemplates.core.set.TemplateSet tmp = new com.jackyblackson.idunntemplates.core.set.TemplateSet();
            tmp.addSource(src.getPath(), src.getWeight());
            int count = tmp.resolveTemplates(templateManager, name -> setManager.getSet(name, player.getName()), false).size();
            
            String line = com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.set.source", 
                    String.format("%.1f", src.getWeight()), String.valueOf(count), src.getPath());
            bars.get(i+1).setTitle(line);
        }
    }

    private void handlePlayer(Player player) {
        // State tracking for BossBar
        if (!player.hasPermission(PermissionNames.Templates.place)) return;     // 玩家无权使用 idunn
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

        if (holdingWand && pref.isParticleWand()) {
            ParticleUtil.spawnMagicParticles(player.getLocation().add(0, 1, 0));
        }

        Location pLoc = player.getLocation();

        // 2. Template Origins (Master)
        for (Template t : templateManager.getTemplates()) {
            TemplateMetadata meta = t.getMetadata();
            if (!meta.getWorldId().equals(Objects.requireNonNull(pLoc.getWorld()).getUID())) continue;
            
            Location min = new Location(pLoc.getWorld(), meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
            if (min.distance(pLoc) > VIEW_DISTANCE * 2) continue; // optimization
            
            Location max = min.clone().add(meta.getWidth(), meta.getHeight(), meta.getLength());
            
            boolean canCommit = hasCommitPermission(player, t);
            Particle particle = canCommit ? Particle.HAPPY_VILLAGER : Particle.ANGRY_VILLAGER;

            boolean locked = t.isLocked();
            if (locked) {
                particle = Particle.GLOW_SQUID_INK;
            }
            // Draw Grid Box
            if (min.distance(pLoc) < VIEW_DISTANCE) {
                if (pref != null && pref.isParticleTemplateBoundaries()) {
                    ParticleUtil.drawSurfaceGridAABB(min, max, GRID_SPACING, particle);
                }
            }
            
            // Check Inside for BossBar
            if (isInAABB(pLoc, min, max)) {
                if (pref != null && pref.isBossBarTemplate()) {
                    bossBarTitle = com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.bossbar.template_master", (canCommit ? ChatColor.GREEN : ChatColor.RED).toString(), t.getPath()) + (locked ? " LOCKED" : "");
                    bossBarColor = canCommit ? BarColor.GREEN : BarColor.RED;
                }
            }
        }

        // 3. Instances
        List<Instance> instances = instanceRepository.getAllLoadedInstances();
        for (Instance inst : instances) {
            if (!inst.getWorldId().equals(Objects.requireNonNull(pLoc.getWorld()).getUID())) continue;
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
                    if (pref != null && (pref.isParticleInstanceBoundaries() || holdingWand)) {
                        Particle.DustOptions dustOptions = getParticleColor(inst);
                        ParticleUtil.drawSurfaceGridAABB(min, max, GRID_SPACING, Particle.DUST, dustOptions);
                        // Draw Line to Center
                        ParticleUtil.drawLine(player.getLocation().add(0, 1, 0), center, Particle.FLAME, 1.0, 0, 0, 0, 1, null);
                        
                        // Draw Masked Box
                        if (inst.getMaskXNeg() > 0 || inst.getMaskXPos() > 0 || inst.getMaskYNeg() > 0 || inst.getMaskYPos() > 0 || inst.getMaskZNeg() > 0 || inst.getMaskZPos() > 0) {
                            Location[] masked = calculateMaskedBounds(inst, min, max);
                            if (masked != null) {
                                ParticleUtil.drawSurfaceGridAABB(masked[0], masked[1], GRID_SPACING, Particle.FLAME, null);
                            }
                        }
                    }
                }
            }
            
            if (isInside) {
                if (bossBarTitle == null) {
                    if (pref != null && pref.isBossBarInstance()) {
                        bossBarTitle = com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.bossbar.instance", t.getPath(), inst.getId().substring(0,8));
                        bossBarColor = BarColor.BLUE;
                    }
                }
            }
        }
        
        // Update BossBar
        updateBossBar(player, bossBarTitle, bossBarColor);
        
        // 4. Action Bar
        sendActionBar(player, pref, session);
    }

    private Location[] calculateMaskedBounds(Instance inst, Location worldMin, Location worldMax) {
        // 1. Get Local Masks
        int mxn = inst.getMaskXNeg();
        int mxp = inst.getMaskXPos();
        int mzn = inst.getMaskZNeg();
        int mzp = inst.getMaskZPos();
        int myn = inst.getMaskYNeg();
        int myp = inst.getMaskYPos();

        // 2. Apply Flips (Swap opposing masks)
        if (inst.isFlipX()) {
            int tmp = mxn; mxn = mxp; mxp = tmp;
        }
        if (inst.isFlipZ()) {
            int tmp = mzn; mzn = mzp; mzp = tmp;
        }
        if (inst.isFlipY()) {
            int tmp = myn; myn = myp; myp = tmp;
        }

        // 3. Apply Rotation (0, 90, 180, 270 CCW)
        // Map masks to World Faces:
        // w_mxn (West/MinX), w_mxp (East/MaxX)
        // w_mzn (North/MinZ), w_mzp (South/MaxZ)
        
        int rot = (inst.getRotationY() % 360 + 360) % 360;
        
        int w_mxn = mxn;
        int w_mxp = mxp;
        int w_mzn = mzn;
        int w_mzp = mzp;

        switch (rot) {
            case 90:
                // X+ -> Z+, X- -> Z-
                // Z+ -> X-, Z- -> X+
                w_mzp = mxp; // World Z+ < Old X+
                w_mzn = mxn; // World Z- < Old X-
                w_mxn = mzp; // World X- < Old Z+
                w_mxp = mzn; // World X+ < Old Z-
                break;
            case 180:
                // X -> -X, Z -> -Z
                w_mxn = mxp;
                w_mxp = mxn;
                w_mzn = mzp;
                w_mzp = mzn;
                break;
            case 270:
                // X -> -Z, Z -> X
                w_mzn = mxp; // World Z- < Old X+
                w_mzp = mxn; // World Z+ < Old X-
                w_mxp = mzp; // World X+ < Old Z+
                w_mxn = mzn; // World X- < Old Z-
                break;
        }

        // 4. Calculate World Coords
        double minX = worldMin.getX() + w_mxn;
        double maxX = worldMax.getX() - w_mxp;
        double minY = worldMin.getY() + myn; // Y doesn't rotate around Y-axis
        double maxY = worldMax.getY() - myp;
        double minZ = worldMin.getZ() + w_mzn;
        double maxZ = worldMax.getZ() - w_mzp;

        // Check validity
        if (minX > maxX || minY > maxY || minZ > maxZ) return null;

        return new Location[]{
                new Location(worldMin.getWorld(), minX, minY, minZ),
                new Location(worldMin.getWorld(), maxX, maxY, maxZ)
        };
    }
    
    private void sendActionBar(Player player, com.jackyblackson.idunntemplates.core.domain.PlayerPreference pref, com.jackyblackson.idunntemplates.core.domain.PlayerSession session) {
        if (pref == null || !pref.isShowActionBar()) return;                    // 手动关闭
        if (!player.hasPermission(PermissionNames.Templates.place)) return;     // 玩家无权使用 idunn
        StringBuilder sb = new StringBuilder();
        sb.append(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.action_bar.prefix"));
        
        // EmptyOnly Status
        String status = pref.isPlaceOnEmptyOnly() 
                ? com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.action_bar.empty_only_on")
                : com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.action_bar.empty_only_off");
        sb.append(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.action_bar.empty_only", status));
        
        // Next Template Info
        if (session != null) {
            var next = session.getNextPlacement();
            if (next != null && next.getTemplate() != null) {
                String flipX = next.isFlipX() ? com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.action_bar.flip_x") : "";
                String flipZ = next.isFlipZ() ? com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.action_bar.flip_z") : "";
                
                sb.append(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.action_bar.next", 
                        next.getTemplate().getName(), String.valueOf(next.getRotation()), flipX, flipZ));
            }
        }
        
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent(sb.toString()));
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
        if (p.hasPermission(PermissionNames.Templates.commitToAll)) return true;
        // Check path permission
        String path = t.getPath().replace("/", ".");
        if (path.startsWith("_")) path = path.substring(1);
        return p.hasPermission(PermissionNames.Templates.commitToPath + path);
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
            if (!meta.getWorldId().equals(Objects.requireNonNull(to.getWorld()).getUID())) continue;

            // Check permission first? No, only check collision first to save perf.
            if (to.distanceSquared(new Location(to.getWorld(), meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ())) > 10000) continue; // Fast reject

            Location min = new Location(to.getWorld(), meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
            Location max = min.clone().add(meta.getWidth(), meta.getHeight(), meta.getLength());

            if (isInAABB(to, min, max)) {
             // Player is entering or inside.
                if (!hasCommitPermission(player, t)) {
                    // Deny
                    event.setCancelled(true);
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "effect.entry_denied", t.getName()));

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
