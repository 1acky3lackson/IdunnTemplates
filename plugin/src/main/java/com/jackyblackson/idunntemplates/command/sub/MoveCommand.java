package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Collections;

public class MoveCommand extends BaseSubCommand {
    private final TemplateManager templateManager;

    public MoveCommand(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn template move <template> <newPath>
        if (args.length < 3) {
            player.sendMessage("Usage: /idunn template move <template> <newPath>");
            return;
        }

        String templateNameOrPath = args[1];
        String newPath = args[2];

        Template template = templateManager.getTemplate(templateNameOrPath);
        if (template == null) {
            template = templateManager.getTemplate(templateNameOrPath.replace(".", "/"));
        }
        if (template == null) {
            template = templateManager.getTemplate(templateNameOrPath.replace("/", "."));
        }

        if (template == null) {
             player.sendMessage("Template not found.");
             return;
        }

        try {
            templateManager.moveTemplate(player, template, newPath);
            player.sendMessage("Template moved successfully to " + newPath);
        } catch (Exception e) {
            player.sendMessage("Failed to move template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
             return templateManager.getNextPathsFor(args[1]);
        } else if (args.length == 3) {
             // For the new path argument, suggest existing paths to help user navigate folders
             return templateManager.getNextPathsFor(args[2]);
        }
        return Collections.emptyList();
    }
}
