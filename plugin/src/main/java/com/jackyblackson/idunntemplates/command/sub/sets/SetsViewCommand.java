package com.jackyblackson.idunntemplates.command.sub.sets;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.core.set.TemplateSetSource;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetsViewCommand extends BaseSubCommand {

    private final SessionManager sessionManager;

    public SetsViewCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        TemplateSet set = sessionManager.getSession(player.getUniqueId()).getPreference().getCurrentSet();
        List<TemplateSetSource> sources = set.getSources();
        
        if (sources.isEmpty()) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.view.empty"));
            return;
        }
        
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.view.header"));
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.view.props", set.getRotate().toString(), set.getFlipX().toString(), set.getFlipZ().toString()));
        
        for (TemplateSetSource src : sources) {
            TextComponent msg = new TextComponent("- " + src.getPath() + " (w:" + src.getWeight() + ") ");
            msg.setColor(net.md_5.bungee.api.ChatColor.WHITE);
            
            TextComponent remove = new TextComponent(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.view.remove_label"));
            remove.setColor(net.md_5.bungee.api.ChatColor.RED);
            remove.setBold(true);
            remove.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn set remove " + src.getPath()));
            remove.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "sets.view.remove_hover")).create()));
            
            msg.addExtra(remove);
            player.spigot().sendMessage(msg);
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
