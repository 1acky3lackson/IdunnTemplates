package com.jackyblackson.idunntemplates.core.util;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class MessageUtil {
    public static void sendMessageAfterPlace(Instance inst, Player player) {
        var messages = generateMessageAfterPlace(inst, player);
        for (TextComponent msg : messages) {
            player.spigot().sendMessage(msg);
        }
    }

    private static TextComponent[] generateMessageAfterPlace(Instance inst, Player player) {
        var template = inst.getTemplate();
        // Generate clickable message
        TextComponent msg = new TextComponent("Placed " + template.getPath() + " (" + inst.getId().substring(0,8) + ") ");
        msg.setColor(net.md_5.bungee.api.ChatColor.GREEN);

        TextComponent tpTemplate = new TextComponent("    [->TEMPLATE]");
        tpTemplate.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        tpTemplate.setBold(true);
        tpTemplate.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn template tp " + template.getPath()));
        tpTemplate.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport to Master of the Template").create()));

        TextComponent tpInstance = new TextComponent(" [->INSTANCE]");
        tpInstance.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        tpInstance.setBold(true);
        tpInstance.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn instance tp " + inst.getId()));
        tpInstance.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport to Instance").create()));

        TextComponent undo = new TextComponent(" [UNDO]");
        undo.setColor(net.md_5.bungee.api.ChatColor.RED);
        undo.setBold(true);
        undo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn instance undo " + inst.getId()));
        undo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Delete instance and undo blocks").create()));

        TextComponent extraMethods = new TextComponent("");
        extraMethods.addExtra(tpTemplate);
        extraMethods.addExtra(tpInstance);
        extraMethods.addExtra(undo);

        return new TextComponent[]{msg, extraMethods};
    }
}
