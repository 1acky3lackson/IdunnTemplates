package com.jackyblackson.idunntemplates.command;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.manager.TemplateUpdater;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.jackyblackson.idunntemplates.manager.InstanceManager;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class IdunnCommand implements TabExecutor {

    private final TemplateManager templateManager;
    private final InstanceManager instanceManager;
    private final TemplateUpdater templateUpdater;
    private final InstanceRepository instanceRepository;

    public IdunnCommand(TemplateManager templateManager, InstanceManager instanceManager, TemplateUpdater templateUpdater, InstanceRepository instanceRepository) {
        this.templateManager = templateManager;
        this.instanceManager = instanceManager;
        this.templateUpdater = templateUpdater;
        this.instanceRepository = instanceRepository;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "save":
                // /idunn save <name> [path]
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /idunn save <name> [path]");
                    return true;
                }
                String saveName = args[1];
                String savePath = args.length > 2 ? args[2] : null;
                handleSave(player, saveName, savePath);
                return true;

            case "place":
                // /idunn place <path> [rot] [flipX] [flipY] [flipZ]
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /idunn place <templatePath> [rotation] [flipX] [flipY] [flipZ]");
                    return true;
                }
                String placePath = args[1];
                int rot = args.length > 2 ? parseInt(args[2], 0) : 0;
                boolean flipX = args.length > 3 && Boolean.parseBoolean(args[3]);
                boolean flipY = args.length > 4 && Boolean.parseBoolean(args[4]);
                boolean flipZ = args.length > 5 && Boolean.parseBoolean(args[5]);
                handlePlace(player, placePath, rot, flipX, flipY, flipZ);
                return true;

            case "commit":
                // /idunn commit <templatePath> <message>
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /idunn commit <templatePath> <message>");
                    return true;
                }
                String commitPath = args[1];
                StringBuilder msg = new StringBuilder();
                for (int i = 2; i < args.length; i++) msg.append(args[i]).append(" ");
                handleCommit(player, commitPath, msg.toString().trim());
                return true;

            case "list":
                // /idunn list [page]
                int page = args.length > 1 ? parseInt(args[1], 1) : 1;
                handleList(player, page);
                return true;

            case "reload":
                templateManager.reloadTemplates();
                player.sendMessage(ChatColor.GREEN + "Templates reloaded from disk.");
                return true;

            case "instances":
                // /idunn instances <templatePath> [page]
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /idunn instances <templatePath> [page]");
                    return true;
                }
                String instPath = args[1];
                int instPage = args.length > 2 ? parseInt(args[2], 1) : 1;
                handleListInstances(player, instPath, instPage);
                return true;

            case "tp":
                // /idunn tp <instanceId>
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /idunn tp <instanceId>");
                    return true;
                }
                String instanceId = args[1];
                handleTpInstance(player, instanceId);
                return true;

            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> cmds = new ArrayList<>();
            cmds.add("save");
            cmds.add("place");
            cmds.add("commit");
            cmds.add("list");
            cmds.add("reload");
            cmds.add("instances");
            cmds.add("tp");
            return filter(cmds, args[0]);
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("place") || sub.equals("commit") || sub.equals("instances")) {
            if (args.length == 2) {
                // Return template paths
                return filter(getTemplatePaths(), args[1]);
            }
        }

        if (sub.equals("place")) {
            if (args.length == 3) {
                List<String> rots = new ArrayList<>();
                rots.add("0"); rots.add("90"); rots.add("180"); rots.add("270");
                return filter(rots, args[2]);
            }
            if (args.length >= 4 && args.length <= 6) {
                List<String> bools = new ArrayList<>();
                bools.add("true"); bools.add("false");
                return filter(bools, args[args.length - 1]);
            }
        }
        
        if (sub.equals("tp")) {
            if (args.length == 2) {
                 // Return list of instance IDs (maybe limited to recent or nearby?)
                 // Returning all might be too many, but let's try.
                 List<String> ids = instanceRepository.getAllLoadedInstances().stream()
                         .map(Instance::getId)
                         .collect(Collectors.toList());
                 return filter(ids, args[1]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> getTemplatePaths() {
        return templateManager.getTemplates().stream()
                .map(t -> {
                    String p = t.getPath().replace("/_", "/");
                    return p.startsWith("_") ? p.substring(1) : p;
                })
                .collect(Collectors.toList());
    }

    private List<String> filter(List<String> options, String current) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }

    private int parseInt(String val, int def) {
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return def; }
    }
    
    private void handleList(Player player, int page) {
        java.util.Collection<Template> templates = templateManager.getTemplates();
        if (templates.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No templates loaded.");
            return;
        }
        
        List<Template> sorted = templates.stream()
                .sorted((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()))
                .collect(Collectors.toList());
        
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) sorted.size() / pageSize);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        player.sendMessage(ChatColor.GOLD + "=== Templates (Page " + page + "/" + totalPages + ") ===");
        
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, sorted.size());
        
        for (int i = start; i < end; i++) {
            Template t = sorted.get(i);
            String displayPath = t.getPath().replace("/_", "/");
            if (displayPath.startsWith("_")) displayPath = displayPath.substring(1);
            
            player.sendMessage(ChatColor.YELLOW + "- " + ChatColor.WHITE + t.getName() + 
                    ChatColor.GRAY + " (" + displayPath + ") Ver: " + t.getLatestVersion().getVersionId());
        }
    }

    private void handleListInstances(Player player, String path, int page) {
        Template template = templateManager.getTemplate(path);
        if (template == null) {
            player.sendMessage(ChatColor.RED + "Template not found: " + path);
            return;
        }
        
        List<Instance> instances = instanceRepository.getAllLoadedInstances().stream()
                .filter(i -> i.getTemplateId().equals(template.getId()))
                .collect(Collectors.toList());
        
        if (instances.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No loaded instances found for template: " + template.getName());
            return;
        }

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) instances.size() / pageSize);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        player.sendMessage(ChatColor.GOLD + "=== Instances of " + template.getName() + " (Page " + page + "/" + totalPages + ") ===");
        
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, instances.size());
        
        for (int i = start; i < end; i++) {
            Instance inst = instances.get(i);
            org.bukkit.World w = Bukkit.getWorld(inst.getWorldId());
            String wName = w != null ? w.getName() : "Unknown";
            
            // Short ID
            String shortId = inst.getId().substring(0, 8);
            
            player.sendMessage(ChatColor.YELLOW + "- ID:" + ChatColor.WHITE + shortId + "..." + 
                    ChatColor.GRAY + " @ " + wName + " (" + inst.getX() + "," + inst.getY() + "," + inst.getZ() + ")");
        }
    }
    
    private void handleTpInstance(Player player, String instanceId) {
        // Try exact match first, then startsWith if not found
        Instance target = instanceRepository.getAllLoadedInstances().stream()
                .filter(i -> i.getId().equals(instanceId))
                .findFirst()
                .orElse(null);
        
        if (target == null) {
            // Try prefix match
             target = instanceRepository.getAllLoadedInstances().stream()
                .filter(i -> i.getId().startsWith(instanceId))
                .findFirst()
                .orElse(null);
        }
        
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Instance not found: " + instanceId);
            return;
        }
        
        org.bukkit.World w = Bukkit.getWorld(target.getWorldId());
        if (w == null) {
            player.sendMessage(ChatColor.RED + "Instance world is not loaded.");
            return;
        }
        
        player.teleport(new Location(w, target.getX(), target.getY(), target.getZ()));
        player.sendMessage(ChatColor.GREEN + "Teleported to instance " + target.getId());
    }

    private void handlePlace(Player player, String path, int rot, boolean flipX, boolean flipY, boolean flipZ) {
        player.sendMessage(ChatColor.YELLOW + "Placing template...");
        
        Template template = templateManager.getTemplate(path);
        if (template == null) {
            player.sendMessage(ChatColor.RED + "Template not found: " + path);
            return;
        }

        try {
            instanceManager.placeInstance(player, template, player.getLocation(), rot, flipX, flipY, flipZ);
            player.sendMessage(ChatColor.GREEN + "Template placed successfully!");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error placing template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSave(Player player, String name, String path) {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
        ClipboardHolder holder;
        try {
            holder = session.getClipboard();
        } catch (Exception e) { // EmptyClipboardException is not public in all versions, catch generic
            player.sendMessage(ChatColor.RED + "Your clipboard is empty. Copy something first!");
            return;
        }
        
        Clipboard clipboard = holder.getClipboard();

        try {
            templateManager.createTemplate(player, name, path, clipboard);
            player.sendMessage(ChatColor.GREEN + "Template '" + name + "' saved successfully!");
        } catch (SecurityException e) {
            player.sendMessage(ChatColor.RED + "Permission denied: " + e.getMessage());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error saving template: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleCommit(Player player, String path, String message) {
        // 1. Get Template
        Template template = templateManager.getTemplate(path);
        if (template == null) {
            player.sendMessage(ChatColor.RED + "Template not found: " + path);
            return;
        }

        // 2. Validate World and Permissions
        com.jackyblackson.idunntemplates.core.domain.TemplateMetadata meta = template.getMetadata();
        org.bukkit.World sourceWorld = Bukkit.getWorld(meta.getWorldId());
        if (sourceWorld == null) {
            player.sendMessage(ChatColor.RED + "The source world (" + meta.getWorldId() + ") is not loaded.");
            return;
        }

        // 3. Calculate Region and Origin
        BlockVector3 min = BlockVector3.at(meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
        BlockVector3 max = min.add(meta.getWidth() - 1, meta.getHeight() - 1, meta.getLength() - 1);
        
        // Recover origin offset from previous version
        BlockVector3 originOffset = BlockVector3.ZERO;
        TemplateVersion latest = template.getLatestVersion();
        if (latest != null) {
            try {
                java.io.File schemFile = new java.io.File(template.getDirectory(), latest.getVersionId() + ".schem");
                if (schemFile.exists()) {
                    com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat format = com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats.findByFile(schemFile);
                    if (format == null) format = com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats.findByAlias("sponge");
                    if (format != null) {
                        try (com.sk89q.worldedit.extent.clipboard.io.ClipboardReader reader = format.getReader(new java.io.FileInputStream(schemFile))) {
                            Clipboard oldClip = reader.read();
                            originOffset = oldClip.getOrigin().subtract(oldClip.getRegion().getMinimumPoint());
                        }
                    }
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.YELLOW + "Warning: Could not read previous version to recover origin offset. Resetting origin to min point.");
                e.printStackTrace();
            }
        }
        
        BlockVector3 newOrigin = min.add(originOffset);

        // 4. Capture
        com.sk89q.worldedit.regions.CuboidRegion region = new com.sk89q.worldedit.regions.CuboidRegion(
            BukkitAdapter.adapt(sourceWorld), min, max
        );
        
        com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard clipboard = new com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard(region);
        clipboard.setOrigin(newOrigin);

        try (com.sk89q.worldedit.EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(sourceWorld))) {
             com.sk89q.worldedit.function.operation.ForwardExtentCopy copy = new com.sk89q.worldedit.function.operation.ForwardExtentCopy(
                 editSession, region, clipboard, region.getMinimumPoint()
             );
             com.sk89q.worldedit.function.operation.Operations.complete(copy);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to capture template: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 5. Commit
        try {
            templateManager.commitTemplate(player, template, message, clipboard);
            player.sendMessage(ChatColor.GREEN + "Template version committed from source location!");
        } catch (Exception e) {
             player.sendMessage(ChatColor.RED + "Error committing: " + e.getMessage());
             e.printStackTrace();
        }
    }
}