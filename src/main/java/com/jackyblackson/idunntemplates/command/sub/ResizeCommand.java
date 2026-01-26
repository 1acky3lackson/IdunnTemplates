package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.util.MessageUtil;
import com.jackyblackson.idunntemplates.manager.LanguageManager;
import com.jackyblackson.idunntemplates.manager.ResizeConfigManager;
import com.jackyblackson.idunntemplates.manager.ResizeManager;
import com.jackyblackson.idunntemplates.permission.PermissionNames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ResizeCommand extends BaseSubCommand {

    private final ResizeManager resizeManager;
    private final ResizeConfigManager resizeConfigManager;
    private final LanguageManager languageManager;

    public ResizeCommand(ResizeManager resizeManager, ResizeConfigManager resizeConfigManager, LanguageManager languageManager) {
        this.resizeManager = resizeManager;
        this.resizeConfigManager = resizeConfigManager;
        this.languageManager = languageManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // args[0] is "resize"

        if (args.length >= 2 && args[1].equalsIgnoreCase("reload") && player.hasPermission(PermissionNames.Resizes.reload)) {
            resizeConfigManager.reloadConfig();
            languageManager.loadLanguages();
            player.sendMessage(MessageUtil.getMessage(player, "resize.config_reloaded"));
            return;
        }

        Player targetPlayer;
        double scale;

        if (args.length >= 3 && player.hasPermission(PermissionNames.Resizes.resizeOthers)) {
            targetPlayer = Bukkit.getPlayer(args[2]);
            if (targetPlayer == null) {
                player.sendMessage(MessageUtil.getMessage(player, "resize.player_not_found", args[2]));
                return;
            }
            try {
                scale = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtil.getMessage(player, "resize.invalid_number"));
                return;
            }
        } else {
            if (args.length != 2) {
                player.sendMessage(MessageUtil.getMessage(player, "resize.usage"));
                return;
            }
            targetPlayer = player;
            try {
                scale = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtil.getMessage(player, "resize.invalid_number"));
                return;
            }
        }

        boolean hasExtendedPermission = player.hasPermission(PermissionNames.Resizes.extendedResize);
        double minScale = resizeManager.getMinScale(hasExtendedPermission);
        double maxScale = resizeManager.getMaxScale(hasExtendedPermission);

        if (!resizeManager.isValidScale(scale, hasExtendedPermission)) {
            player.sendMessage(MessageUtil.getMessage(player, "resize.scale_range", String.valueOf(minScale), String.valueOf(maxScale)));
        } else if (resizeManager.hasCooldown(targetPlayer)) {
            long remainingTime = resizeManager.getRemainingCooldown(targetPlayer);
            player.sendMessage(MessageUtil.getMessage(player, "resize.cooldown", String.valueOf(remainingTime)));
        } else {
            resizeManager.updateLastUsage(targetPlayer);
            resizeManager.smoothlyResizePlayer(targetPlayer, scale);
            player.sendMessage(MessageUtil.getMessage(player, "resize.resized", String.valueOf(scale)));
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        // args[0] is "resize"
        if (args.length == 2) { // "resize <arg>"
            if (player.hasPermission(PermissionNames.Resizes.reload) && "reload".startsWith(args[1].toLowerCase())) {
                return List.of("reload");
            } else {
                return player.hasPermission(PermissionNames.Resizes.extendedResize) ? List.of("0.8", "1.0", "1.15", "0.0625", "16.0") : List.of("0.8", "1.0", "1.15");
            }
        } else if (args.length == 3 && player.hasPermission(PermissionNames.Resizes.resizeOthers)) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
