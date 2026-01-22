package com.jackyblackson.idunntemplates.core.util;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.manager.LanguageManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class MessageUtil {
    
    private static LanguageManager languageManager;

    public static void setLanguageManager(LanguageManager languageManager) {
        MessageUtil.languageManager = languageManager;
    }
    
    public static String getMessage(Player player, String key, String... args) {
        if (languageManager == null) return key;
        return languageManager.getMessage(player, key, args);
    }

    public static void sendMessageAfterPlace(Instance inst, Player player) {
        var messages = generateMessageAfterPlace(inst, player);
        for (TextComponent msg : messages) {
            player.spigot().sendMessage(msg);
        }
    }

    private static TextComponent[] generateMessageAfterPlace(Instance inst, Player player) {
        var template = inst.getTemplate();
        // Generate clickable message
        TextComponent msg = new TextComponent(getMessage(player, "message.placed", template.getPath(), inst.getId().substring(0,8)));
        // Color is handled by getMessage (via config)

        TextComponent tpTemplate = new TextComponent(getMessage(player, "message.tp_template.label"));
        tpTemplate.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn template tp " + template.getPath()));
        tpTemplate.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(getMessage(player, "message.tp_template.hover")).create()));

        TextComponent tpInstance = new TextComponent(getMessage(player, "message.tp_instance.label"));
        tpInstance.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn instance tp " + inst.getId()));
        tpInstance.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(getMessage(player, "message.tp_instance.hover")).create()));

        TextComponent undo = new TextComponent(getMessage(player, "message.delete.label"));
        undo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn instance delete " + inst.getId()));
        undo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(getMessage(player, "message.delete.hover")).create()));

        TextComponent extraMethods = new TextComponent("");
        extraMethods.addExtra(tpTemplate);
        extraMethods.addExtra(tpInstance);
        extraMethods.addExtra(undo);

        return new TextComponent[]{msg, extraMethods};
    }
}
