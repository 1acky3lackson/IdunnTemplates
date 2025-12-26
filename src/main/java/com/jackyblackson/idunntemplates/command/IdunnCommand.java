package com.jackyblackson.idunntemplates.command;

import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IdunnCommand implements CommandExecutor {

    private final TemplateManager templateManager;

    public IdunnCommand(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            return false;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("save")) {
            // /idunn save <name> [path]
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /idunn save <name> [path]");
                return true;
            }
            String name = args[1];
            String path = args.length > 2 ? args[2] : null;

            handleSave(player, name, path);
            return true;
        }

        return false;
    }

    private void handleSave(Player player, String name, String path) {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
        ClipboardHolder holder;
        try {
            holder = session.getClipboard();
        } catch (Exception e) { // EmptyClipboardException is not public in all versions, catch generic
            player.sendMessage(ChatColor.RED + "Your clipboard is empty. Copy something first!");
            return;
        }
        
        Clipboard clipboard = holder.getClipboard();

        try {
            templateManager.createTemplate(player, name, path, clipboard);
            player.sendMessage(ChatColor.GREEN + "Template '" + name + "' saved successfully!");
        } catch (SecurityException e) {
            player.sendMessage(ChatColor.RED + "Permission denied: " + e.getMessage());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error saving template: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
