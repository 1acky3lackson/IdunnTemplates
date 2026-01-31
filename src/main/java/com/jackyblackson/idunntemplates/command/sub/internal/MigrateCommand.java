package com.jackyblackson.idunntemplates.command.sub.internal;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.command.IdunnSubCommand;
import com.jackyblackson.idunntemplates.core.migration.MigrationTool;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class MigrateCommand implements IdunnSubCommand {

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("idunn.admin.migrate")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Starting migration... Check console for details.");

        // Run async to avoid blocking main thread
        IdunnTemplates.getInstance().getServer().getScheduler().runTaskAsynchronously(IdunnTemplates.getInstance(), () -> {
            try {
                new MigrationTool(IdunnTemplates.getInstance()).migrateToDatabase();
                player.sendMessage(ChatColor.GREEN + "Migration finished! Check console for full report.");
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Migration failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
