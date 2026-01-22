package com.jackyblackson.idunntemplates.command.sub;

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
        // /idunn place <path> [rot] [flipX] [flipY] [flipZ]
        if (args.length < 2) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "place.usage"));
            return;
        }
        String placePath = args[1];
        int rot = args.length > 2 ? parseInt(args[2], 0) : 0;
        boolean flipX = args.length > 3 && Boolean.parseBoolean(args[3]);
        boolean flipY = args.length > 4 && Boolean.parseBoolean(args[4]);
        boolean flipZ = args.length > 5 && Boolean.parseBoolean(args[5]);

        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "place.placing"));

        Template template = templateManager.getTemplate(placePath);
        if (template == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "place.not_found", placePath));
            return;
        }

        try {
            com.jackyblackson.idunntemplates.core.domain.Instance inst = 
                instanceManager.placeInstanceAndReturn(player, template, player.getLocation(), rot, flipX, flipY, flipZ);

            MessageUtil.sendMessageAfterPlace(inst, player);

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
