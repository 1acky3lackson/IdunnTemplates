package com.jackyblackson.idunntemplates.command.sub.brush;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.PlayerSession;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSettings;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.BrushManager;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.jackyblackson.idunntemplates.command.sub.brush.BrushCommandUtils.getBrushChanelTabCompleteForPlayer;

public class BrushChangeCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final BrushManager brushManager;

    public BrushChangeCommand(SessionManager sessionManager, BrushManager brushManager) {
        this.sessionManager = sessionManager;
        this.brushManager = brushManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // idunn brush change <channel>
        if (args.length < 2) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.change.usage"));
            return;
        }

        String channel = args[1].toLowerCase();

        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);

        if (matName == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.change.no_item"));
            return;
        }

        PlayerSession session = sessionManager.getSession(player.getUniqueId());
        BrushSession brushSession = session.getPreference().getBoundBrushes().get(matName);

        if (brushSession == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.change.not_bound", matName));
            return;
        }

        BrushSettings settings = brushSession.getSettings(channel);
        if (settings == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.change.channel_not_found", channel));
            return;
        }

        brushManager.updateNextPlacement(settings, player);
        sessionManager.saveSession(player.getUniqueId());

        PlayerSession.NextPlacement nextPlacement = settings.getNextPlacement();
        if (nextPlacement != null && nextPlacement.getTemplate() != null) {
            String templateName = nextPlacement.getTemplate().getName();
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.change.success", channel, templateName));
        } else {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "brush.change.no_template_found", channel));
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return filter(getBrushChanelTabCompleteForPlayer(sessionManager, player), args[1]);
        }
        return Collections.emptyList();
    }
}
