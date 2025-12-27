package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
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
        Region region;
        try {
            region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (IncompleteRegionException e) {
            player.sendMessage(ChatColor.RED + "Please make a WorldEdit selection first.");
            return;
        }

        if (!(region instanceof CuboidRegion)) {
            player.sendMessage(ChatColor.RED + "Only cuboid selections are supported for saving templates.");
            return;
        }

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(BukkitAdapter.asBlockVector(player.getLocation()));

        try (com.sk89q.worldedit.EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getWorld()))) {
            ForwardExtentCopy copy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            Operations.completeLegacy(copy);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to capture selection: " + e.getMessage());
            e.printStackTrace();
            return;
        }

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
