package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.util.MessageUtil;
import com.jackyblackson.idunntemplates.manager.InstanceManager;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlaceCommand extends BaseSubCommand {

    private final TemplateManager templateManager;
    private final InstanceManager instanceManager;

    public PlaceCommand(TemplateManager templateManager, InstanceManager instanceManager) {
        this.templateManager = templateManager;
        this.instanceManager = instanceManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn place <path> [rot] [flipX] [flipY] [flipZ] [flags...] [-confirm]
        if (args.length < 2) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "place.usage"));
            return;
        }
        String placePath = args[1];
        
        int rot = 0;
        boolean flipX = false;
        boolean flipY = false;
        boolean flipZ = false;
        // boolean confirm = false; // confirm is now implicit if parentId is provided
        java.util.UUID parentId = null;
        
        int maskXNeg = 0;
        int maskXPos = 0;
        int maskYNeg = 0;
        int maskYPos = 0;
        int maskZNeg = 0;
        int maskZPos = 0;
        
        int posIndex = 0;
        for (int i = 2; i < args.length; i++) {
            String arg = args[i];
            // if (arg.equalsIgnoreCase("-confirm")) { confirm = true; } // deprecated
            if (arg.startsWith("-parent:")) {
                try {
                    parentId = java.util.UUID.fromString(arg.substring(8));
                } catch (IllegalArgumentException ignored) {}
            } else if (arg.startsWith("-") && arg.contains(":")) {
                // Parse flag: -face:val
                try {
                    String[] parts = arg.substring(1).split(":");
                    if (parts.length == 2) {
                        String face = parts[0].toLowerCase();
                        int val = Integer.parseInt(parts[1]);
                        
                        switch (face) {
                            case "x+": maskXPos = val; break;
                            case "x-": maskXNeg = val; break;
                            case "y+": maskYPos = val; break;
                            case "y-": maskYNeg = val; break;
                            case "z+": maskZPos = val; break;
                            case "z-": maskZNeg = val; break;
                        }
                    }
                } catch (NumberFormatException ignored) {}
            } else {
                // Positional arg
                switch (posIndex) {
                    case 0: rot = parseInt(arg, 0); break;
                    case 1: flipX = Boolean.parseBoolean(arg); break;
                    case 2: flipY = Boolean.parseBoolean(arg); break;
                    case 3: flipZ = Boolean.parseBoolean(arg); break;
                }
                posIndex++;
            }
        }

        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "place.placing"));

        Template template = templateManager.getTemplate(placePath);
        if (template == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "place.not_found", placePath));
            return;
        }

        // V2 UX: Warn if the template itself is locked (preventing updates from propagating if we place it elsewhere)
        // Note: This check refers to the template being placed (Child), not the Parent it is placed into.
        if (template.getMetadata().isLocked()) {
             player.sendMessage("");
             player.sendMessage(ChatColor.RED + "⚠ " + ChatColor.GOLD + "Template Locked!");
             player.sendMessage(ChatColor.GRAY + "The template " + ChatColor.WHITE + template.getName() + ChatColor.GRAY + " is currently locked for editing.");
             player.sendMessage(ChatColor.GRAY + "Updates to it will NOT be visible until the owner commits changes.");
             player.sendMessage(ChatColor.GRAY + "Please contact the owner to finish editing.");
             player.sendMessage("");
        }

        try {
            com.jackyblackson.idunntemplates.core.domain.Instance inst = 
                instanceManager.placeInstanceAndReturn(player, template, player.getLocation(), rot, flipX, flipY, flipZ,
                        maskXNeg, maskXPos, maskYNeg, maskYPos, maskZNeg, maskZPos, parentId); // Pass specific parent ID

            if (inst != null) {
                MessageUtil.sendMessageAfterPlace(inst, player);
            }

        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
        } catch (Exception e) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "place.error", e.getMessage()));
            e.printStackTrace();
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return filter(getTemplatePaths(), args[1]);
        }
        
        String current = args[args.length - 1];
        if (current.startsWith("-")) {
            List<String> flags = new ArrayList<>();
            flags.add("-x+:"); flags.add("-x-:");
            flags.add("-y+:"); flags.add("-y-:");
            flags.add("-z+:"); flags.add("-z-:");
            return filter(flags, current);
        }
        
        // Count positional args so far (ignoring flags)
        int posCount = 0;
        for (int i = 2; i < args.length - 1; i++) {
            if (!args[i].startsWith("-") || !args[i].contains(":")) {
                posCount++;
            }
        }
        
        if (posCount == 0) { // Expecting rot
            List<String> rots = new ArrayList<>();
            rots.add("0"); rots.add("90"); rots.add("180"); rots.add("270");
            return filter(rots, current);
        } else if (posCount >= 1 && posCount <= 3) { // Expecting bools
            List<String> bools = new ArrayList<>();
            bools.add("true"); bools.add("false");
            return filter(bools, current);
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
}
