package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand extends BaseSubCommand {

    private final TemplateManager templateManager;

    public ListCommand(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn list [page]
        int page = args.length > 1 ? parseInt(args[1], 1) : 1;
        
        java.util.Collection<Template> templates = templateManager.getTemplates();
        if (templates.isEmpty()) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "list.empty"));
            return;
        }

        List<Template> sorted = templates.stream()
                .sorted((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()))
                .collect(Collectors.toList());

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) sorted.size() / pageSize);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "list.header", String.valueOf(page), String.valueOf(totalPages)));

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, sorted.size());

        for (int i = start; i < end; i++) {
            Template t = sorted.get(i);
            String displayPath = t.getPath().replace("/_", "/");
            if (displayPath.startsWith("_")) displayPath = displayPath.substring(1);

            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "list.entry", t.getName(), displayPath, t.getLatestVersion().getVersionId()));
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
