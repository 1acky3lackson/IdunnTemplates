package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ReloadCommand extends BaseSubCommand {

    private final TemplateManager templateManager;

    public ReloadCommand(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        templateManager.reloadTemplates();
        player.sendMessage(ChatColor.GREEN + "Templates reloaded from disk.");
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
