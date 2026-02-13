package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Collections;

public class TransferCommand extends BaseSubCommand {
    private final TemplateManager templateManager;

    public TransferCommand(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn template transfer <template> <newOwner>
        if (args.length < 3) {
            player.sendMessage("Usage: /idunn template transfer <template> <newOwner>");
            return;
        }

        String templateNameOrPath = args[1];
        String newOwner = args[2];

        Template template = templateManager.getTemplate(templateNameOrPath);
        if (template == null) {
            template = templateManager.getTemplate(templateNameOrPath.replace(".", "/"));
        }

        if (template == null) {
             player.sendMessage("Template not found.");
             return;
        }

        try {
            templateManager.transferTemplate(player, template, newOwner);
            player.sendMessage("Template ownership transferred to " + newOwner);
        } catch (Exception e) {
            player.sendMessage("Failed to transfer template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
             return templateManager.getNextPathsFor(args[1]);
        } else if (args.length == 3) {
             return null; // Return null to let Bukkit suggest online players
        }
        return Collections.emptyList();
    }
}
