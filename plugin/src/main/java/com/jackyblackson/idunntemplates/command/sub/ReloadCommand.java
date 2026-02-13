package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.jackyblackson.idunntemplates.voxelwind.VoxelWindConfig;
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
        // 清理资源
//        IdunnTemplates.getInstance().getDatabaseManager().close();
//        IdunnTemplates.getInstance().getPermissionServerManager().close();
//
//        // 重载配置
//        IdunnTemplates.getInstance().reloadConfig();
//
//        // 重新初始化
//        IdunnTemplates.getInstance().onEnable();

        // 重载 VoxelWind 配置
        VoxelWindConfig.get().reload();

        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "reload.success"));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
