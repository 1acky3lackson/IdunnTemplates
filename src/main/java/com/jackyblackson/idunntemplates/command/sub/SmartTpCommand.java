package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SmartTpCommand extends BaseSubCommand {

    private final TemplateManager templateManager;
    private final InstanceRepository instanceRepository;

    public SmartTpCommand(TemplateManager templateManager, InstanceRepository instanceRepository) {
        this.templateManager = templateManager;
        this.instanceRepository = instanceRepository;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn tp [no args]
        
        Location pLoc = player.getLocation();
        
        // 1. Check if inside a Master Template
        for (Template t : templateManager.getTemplates()) {
            TemplateMetadata meta = t.getMetadata();
            if (!meta.getWorldId().equals(pLoc.getWorld().getUID())) continue;
            
            Location min = new Location(pLoc.getWorld(), meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
            Location max = min.clone().add(meta.getWidth(), meta.getHeight(), meta.getLength());
            
            if (isInAABB(pLoc, min, max)) {
                handleInMaster(player, t);
                return;
            }
        }
        
        // 2. Check if inside an Instance
        List<Instance> instances = instanceRepository.getAllLoadedInstances();
        for (Instance inst : instances) {
             if (!inst.getWorldId().equals(pLoc.getWorld().getUID())) continue;
             // Rough check
             if (Math.abs(inst.getX() - pLoc.getX()) > 200) continue;
             
             Template t = templateManager.getTemplate(inst.getTemplateId());
             if (t == null) continue;
             
             Location[] corners = calculateCorners(inst, t);
             // Get AABB from corners
             double minX = corners[0].getX(), minY = corners[0].getY(), minZ = corners[0].getZ();
             double maxX = corners[6].getX(), maxY = corners[6].getY(), maxZ = corners[6].getZ();
             
             if (isInAABB(pLoc, new Location(pLoc.getWorld(), minX, minY, minZ), new Location(pLoc.getWorld(), maxX, maxY, maxZ))) {
                 handleInInstance(player, inst, t);
                 return;
             }
        }
        
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "tp.smart.none"));
    }

    private void handleInMaster(Player player, Template template) {
        List<Instance> instances = instanceRepository.getAllLoadedInstances().stream()
                .filter(i -> i.getTemplateId().equals(template.getId()))
                .collect(Collectors.toList());
        
        if (instances.isEmpty()) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "tp.smart.no_instances"));
            return;
        }
        
        if (instances.size() == 1) {
            Instance target = instances.get(0);
            tpToInstance(player, target);
            return;
        }
        
        // Multiple instances
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "tp.smart.select_header", String.valueOf(instances.size())));
        for (Instance inst : instances) {
            String idShort = inst.getId().substring(0, 8);
            TextComponent msg = new TextComponent("- Instance " + idShort + " @ " + inst.getX() + "," + inst.getY() + "," + inst.getZ());
            msg.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn instance tp " + inst.getId()));
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "tp.smart.click_tp")).create()));
            player.spigot().sendMessage(msg);
        }
    }
    
    private void handleInInstance(Player player, Instance instance, Template template) {
        // TP to master
        TemplateMetadata meta = template.getMetadata();
        org.bukkit.World world = Bukkit.getWorld(meta.getWorldId());
        if (world == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "tp.smart.master_world_not_loaded"));
            return;
        }
        
        Location target = new Location(world, meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
        player.teleport(target);
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "tp.smart.tp_master", template.getName()));
    }
    
    private void tpToInstance(Player player, Instance target) {
        org.bukkit.World w = Bukkit.getWorld(target.getWorldId());
        if (w == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "tp.world_not_loaded"));
            return;
        }
        player.teleport(new Location(w, target.getX(), target.getY(), target.getZ()));
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "tp.smart.tp_instance", target.getId().substring(0, 8)));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
    
    private boolean isInAABB(Location loc, Location min, Location max) {
        return loc.getX() >= min.getX() && loc.getX() <= max.getX() &&
               loc.getY() >= min.getY() && loc.getY() <= max.getY() &&
               loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
    }

    // Reuse logic from EffectManager? Or duplicate for simplicity to avoid circular deps.
    // Duplicating small logic is fine for now.
    private Location[] calculateCorners(Instance inst, Template t) {
        TemplateMetadata meta = t.getMetadata();
        int width = meta.getWidth();
        int height = meta.getHeight();
        int length = meta.getLength();
        
        int rot = Math.abs(inst.getRotationY()) % 360;
        int currentWidth = (rot == 90 || rot == 270) ? length : width;
        int currentLength = (rot == 90 || rot == 270) ? width : length;
        
        double minX = inst.getX();
        double minY = inst.getY();
        double minZ = inst.getZ();
        
        double maxX = minX + currentWidth;
        double maxY = minY + height;
        double maxZ = minZ + currentLength;
        
        Location[] worldCorners = new Location[8];
        org.bukkit.World world = Bukkit.getWorld(inst.getWorldId());
        
        // Just need min and max really for AABB check, but sticking to array structure
        worldCorners[0] = new Location(world, minX, minY, minZ);
        // ... indices ...
        worldCorners[6] = new Location(world, maxX, maxY, maxZ);
        
        return worldCorners;
    }
}
