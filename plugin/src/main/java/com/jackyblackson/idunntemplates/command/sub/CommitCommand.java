package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.util.PermissionUtil;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommitCommand extends BaseSubCommand {

    private final TemplateManager templateManager;

    public CommitCommand(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn commit <templatePath> <message>
        if (args.length < 3) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.usage"));
            return;
        }
        String commitPath = args[1];
        StringBuilder msg = new StringBuilder();
        for (int i = 2; i < args.length; i++) msg.append(args[i]).append(" ");
        String message = msg.toString().trim();

        boolean hasPerm = commitPath.startsWith("users/" + player.getName())
                || PermissionUtil.hasRecursivePermission(
                        player,
                        PermissionNames.Templates.commitToPath$R,
                        commitPath
                );

        if (!hasPerm) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.no_perm", commitPath));
            return;
        }

        // 1. Get Template
        Template template = templateManager.getTemplate(commitPath);
        if (template == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.not_found", commitPath));
            return;
        }

        // 2. Validate World and Permissions
        TemplateMetadata meta = template.getMetadata();
        org.bukkit.World sourceWorld = Bukkit.getWorld(meta.getWorldId());
        if (sourceWorld == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.world_not_loaded", meta.getWorldId().toString()));
            return;
        }



        // 5. Commit
        try {
            templateManager.commitTemplate(player, template, message);
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.success"));
        } catch (Exception e) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.error", e.getMessage()));
            e.printStackTrace();
        }
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
