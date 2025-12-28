package com.jackyblackson.idunntemplates.command.sub.brush;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSettings;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BrushModifyCommand extends BaseSubCommand {

    private final SessionManager sessionManager;

    public BrushModifyCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn brush modify <channel> <prop> <value>
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn brush modify <channel> <prop> <value>");
            return;
        }

        String channel = args[1];
        String prop = args[2].toLowerCase();
        String value = args[3].toUpperCase();

        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);

        if (matName == null) {
            player.sendMessage(ChatColor.RED + "You must hold an item.");
            return;
        }

        var session = sessionManager.getSession(player.getUniqueId());
        BrushSession brushSession = session.getPreference().getBoundBrushes().get(matName);
        if (brushSession == null || brushSession.getSettings(channel) == null) {
            player.sendMessage(ChatColor.RED + "No brush bound to channel '" + channel + "' on this item.");
            return;
        }

        BrushSettings settings = brushSession.getSettings(channel);

        try {
            switch (prop) {
                case "rotate":
                case "r":
                    settings.setRotation(BrushSettings.RotationMode.valueOf(value));
                    break;
                case "flipx":
                case "x":
                    settings.setFlipX(BrushSettings.FlipMode.valueOf(value));
                    break;
                case "flipz":
                case "z":
                    settings.setFlipZ(BrushSettings.FlipMode.valueOf(value));
                    break;
                case "noair":
                    settings.setNoAir(Boolean.parseBoolean(value));
                    break;
                case "emptyonly":
                    settings.setEmptyOnly(Boolean.parseBoolean(value));
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown property: " + prop);
                    return;
            }
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Updated " + prop + " to " + value + " for channel " + channel);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid value for " + prop + ": " + value);
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
             // Channel
             ItemStack item = player.getInventory().getItemInMainHand();
             String matName = ItemUtil.getBrushKey(item);
             if (matName != null) {
                 var session = sessionManager.getSession(player.getUniqueId());
                 BrushSession bs = session.getPreference().getBoundBrushes().get(matName);
                 if (bs != null) {
                     return filter(new java.util.ArrayList<>(bs.getChannels().keySet()), args[1]);
                 }
             }
             return filter(Arrays.asList("left", "right"), args[1]);
        }
        if (args.length == 3) {
            return filter(Arrays.asList("rotate", "flipx", "flipz", "noair", "emptyonly"), args[2]);
        }
        if (args.length == 4) {
            String prop = args[2].toLowerCase();
            switch (prop) {
                case "rotate":
                    return filter(enumToStrings(BrushSettings.RotationMode.values()), args[3]);
                case "flipx":
                case "flipz":
                    return filter(enumToStrings(BrushSettings.FlipMode.values()), args[3]);
                case "noair":
                case "emptyonly":
                    return filter(Arrays.asList("true", "false"), args[3]);
            }
        }
        return Collections.emptyList();
    }

    private List<String> enumToStrings(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).collect(Collectors.toList());
    }
}
