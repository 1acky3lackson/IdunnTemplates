//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.moonaticks.resizeReworked.commands;

import dev.moonaticks.resizeReworked.ResizeReworked;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ResizeCommand implements CommandExecutor, TabCompleter {
    private final ResizeReworked plugin;

    public ResizeCommand(ResizeReworked plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("resizeplugin.reload")) {
            this.plugin.getConfigManager().reloadConfig();
            this.plugin.getLanguageManager().reloadLanguage();
            sender.sendMessage(this.plugin.getLanguageManager().formatMessageComponent(this.plugin.getLanguageManager().getMessageComponent("config_reloaded", new String[0])));
            return true;
        } else {
            Player targetPlayer;
            double scale;
            if (args.length >= 2 && sender.hasPermission("resizeplugin.resize.others")) {
                targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    sender.sendMessage(this.plugin.getLanguageManager().formatMessageComponent(this.plugin.getLanguageManager().getMessageComponent("player_not_found", new String[]{"player", args[1]})));
                    return true;
                }

                try {
                    scale = Double.parseDouble(args[0]);
                } catch (NumberFormatException var16) {
                    return this.sendMessage(sender, "invalid_number");
                }
            } else {
                if (args.length != 1) {
                    return this.sendMessage(sender, "usage");
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(this.plugin.getLanguageManager().formatMessageComponent(this.plugin.getLanguageManager().getMessageComponent("only_player", new String[0])));
                    return true;
                }

                Player player = (Player)sender;
                targetPlayer = player;

                try {
                    scale = Double.parseDouble(args[0]);
                } catch (NumberFormatException var15) {
                    return this.sendMessage(player, "invalid_number");
                }
            }

            boolean hasExtendedPermission = sender.hasPermission("resizeplugin.resize.extended");
            double minScale = this.plugin.getResizeManager().getMinScale(hasExtendedPermission);
            double maxScale = this.plugin.getResizeManager().getMaxScale(hasExtendedPermission);
            if (!this.plugin.getResizeManager().isValidScale(scale, hasExtendedPermission)) {
                return this.sendMessage(sender, "scale_range", "min", String.valueOf(minScale), "max", String.valueOf(maxScale));
            } else if (this.plugin.getResizeManager().hasCooldown(targetPlayer)) {
                long remainingTime = this.plugin.getResizeManager().getRemainingCooldown(targetPlayer);
                sender.sendMessage(this.plugin.getLanguageManager().formatMessageComponent(this.plugin.getLanguageManager().getMessageComponent("cooldown", new String[]{"time", String.valueOf(remainingTime)})));
                return true;
            } else {
                this.plugin.getResizeManager().updateLastUsage(targetPlayer);
                this.plugin.getResizeManager().smoothlyResizePlayer(targetPlayer, scale);
                return this.sendMessage(sender, "resized", "scale", String.valueOf(scale));
            }
        }
    }

    private boolean sendMessage(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(this.plugin.getLanguageManager().formatMessageComponent(this.plugin.getLanguageManager().getMessageComponent(key, placeholders)));
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("resizeplugin.reload") && "reload".startsWith(args[0].toLowerCase())) {
                return List.of("reload");
            } else {
                return sender.hasPermission("resizeplugin.resize.extended") ? List.of("0.8", "1.0", "1.15", "0.0625", "16.0") : List.of("0.8", "1.0", "1.15");
            }
        } else {
            return args.length == 2 && sender.hasPermission("resizeplugin.resize.others") ? (List)Bukkit.getOnlinePlayers().stream().map(Player::getName).filter((name) -> {
                return name.toLowerCase().startsWith(args[1].toLowerCase());
            }).collect(Collectors.toList()) : Collections.emptyList();
        }
    }
}
