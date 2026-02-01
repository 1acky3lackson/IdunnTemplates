package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateTpCommand extends BaseSubCommand {

    private final TemplateManager templateManager;

    public TemplateTpCommand(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn template tp <templatePath>
        if (args.length < 2) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "template.tp.usage"));
            return;
        }
        String path = args[1];

        Template template = templateManager.getTemplate(path);
        if (template == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "template.tp.not_found", path));
            return;
        }

        TemplateMetadata meta = template.getMetadata();
        org.bukkit.World world = Bukkit.getWorld(meta.getWorldId());
        if (world == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "template.tp.world_not_loaded"));
            return;
        }

        Location loc = new Location(world, meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
        player.teleport(loc);
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "template.tp.success", template.getName()));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return filter(getTemplatePaths(), args[1]);
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
