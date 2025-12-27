package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SaveCommand extends BaseSubCommand {

    private final TemplateManager templateManager;

    public SaveCommand(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn save <name> [path]
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn save <name> [path]");
            return;
        }
        String saveName = args[1];
        String savePath = args.length > 2 ? args[2] : null;

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
            templateManager.createTemplate(player, saveName, savePath, clipboard);
            player.sendMessage(ChatColor.GREEN + "Template '" + saveName + "' saved successfully!");
        } catch (SecurityException e) {
            player.sendMessage(ChatColor.RED + "Permission denied: " + e.getMessage());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error saving template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
