package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.manager.SessionManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrefCommand extends BaseSubCommand {

    private final SessionManager sessionManager;

    public PrefCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn pref <subcmd> ...
        if (args.length < 2) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.usage"));
            return;
        }
        String prefSub = args[1].toLowerCase();
        
        com.jackyblackson.idunntemplates.core.domain.PlayerSession session = sessionManager.getSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.session_missing"));
            return;
        }
        com.jackyblackson.idunntemplates.core.domain.PlayerPreference pref = session.getPreference();

        if (prefSub.equals("wand")) {
            if (args.length > 2 && args[2].equalsIgnoreCase("bind")) {
                org.bukkit.Material type = player.getInventory().getItemInMainHand().getType();
                if (type == org.bukkit.Material.AIR) {
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.wand.air"));
                    return;
                }
                pref.setWandMaterialName(type.name());
                sessionManager.saveSession(player.getUniqueId());
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.wand.bound", type.name()));
            } else {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.wand.current", pref.getWandMaterialName()));
            }
        } else if (prefSub.equalsIgnoreCase("placeonemptyonly")) {
            if (args.length < 3) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.place_on_empty.usage"));
                return;
            }
            boolean val = Boolean.parseBoolean(args[2]);
            pref.setPlaceOnEmptyOnly(val);
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.place_on_empty.set", String.valueOf(val)));
        } else if (prefSub.equalsIgnoreCase("actionbar")) {
            if (args.length < 3) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.action_bar.usage"));
                return;
            }
            boolean val = Boolean.parseBoolean(args[2]);
            pref.setShowActionBar(val);
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.action_bar.set", String.valueOf(val)));
        } else if (prefSub.equalsIgnoreCase("emptyblocks")) {
            if (args.length < 3) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.empty_blocks.usage"));
                return;
            }
            String action = args[2].toLowerCase();
            if (action.equals("list")) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.empty_blocks.header"));
                for (String block : pref.getEmptyBlocks()) {
                    TextComponent msg = new TextComponent("- " + block + " ");
                    msg.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                    
                    TextComponent del = new TextComponent("[X]");
                    del.setColor(net.md_5.bungee.api.ChatColor.RED);
                    del.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/idunn pref emptyBlocks remove " + block));
                    del.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.empty_blocks.remove_hover")).create()));
                    
                    msg.addExtra(del);
                    player.spigot().sendMessage(msg);
                }
            } else if (action.equals("add")) {
                if (args.length < 4) {
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.empty_blocks.add_usage"));
                    return;
                }
                String mat = args[3].toUpperCase();
                try {
                    org.bukkit.Material.valueOf(mat); // Validate
                } catch (IllegalArgumentException e) {
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.empty_blocks.invalid_mat", mat));
                    return;
                }
                if (!pref.getEmptyBlocks().contains(mat)) {
                    pref.getEmptyBlocks().add(mat);
                    sessionManager.saveSession(player.getUniqueId());
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.empty_blocks.added", mat));
                } else {
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.empty_blocks.already_in"));
                }
            } else if (action.equals("remove")) {
                if (args.length < 4) {
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.empty_blocks.remove_usage"));
                    return;
                }
                String mat = args[3].toUpperCase();
                if (pref.getEmptyBlocks().remove(mat)) {
                    sessionManager.saveSession(player.getUniqueId());
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.empty_blocks.removed", mat));
                } else {
                    player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.empty_blocks.not_in"));
                }
            }
        } else if (prefSub.equalsIgnoreCase("particles")) {
            if (args.length < 4) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.particles.usage"));
                return;
            }
            String type = args[2].toLowerCase();
            boolean val = Boolean.parseBoolean(args[3]);
            boolean found = true;
            switch (type) {
                case "template" -> pref.setParticleTemplateBoundaries(val);
                case "instance" -> pref.setParticleInstanceBoundaries(val);
                case "project" -> pref.setParticleProjectBoundaries(val);
                case "wand" -> pref.setParticleWand(val);
                default -> found = false;
            }
            if (!found) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.particles.unknown_type"));
                return;
            }
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.particles.set", type, String.valueOf(val)));
        } else if (prefSub.equalsIgnoreCase("bossbar")) {
            if (args.length < 4) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.bossbar.usage"));
                return;
            }
            String type = args[2].toLowerCase();
            boolean val = Boolean.parseBoolean(args[3]);
            boolean found = true;
            switch (type) {
                case "template" -> pref.setBossBarTemplate(val);
                case "instance" -> pref.setBossBarInstance(val);
                case "project" -> pref.setBossBarProject(val);
                case "set" -> pref.setBossBarSet(val);
                default -> found = false;
            }
            if (!found) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.bossbar.unknown_type"));
                return;
            }
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.bossbar.set", type, String.valueOf(val)));
        } else {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "pref.unknown_option"));
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            List<String> opts = new ArrayList<>();
            opts.add("wand");
            opts.add("placeOnEmptyOnly");
            opts.add("particles");
            opts.add("bossbar");
            opts.add("emptyBlocks");
            opts.add("actionBar");
            return filter(opts, args[1]);
        }
        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("wand")) {
                return filter(Collections.singletonList("bind"), args[2]);
            }
            if (args[1].equalsIgnoreCase("placeOnEmptyOnly") || args[1].equalsIgnoreCase("actionBar")) {
                List<String> bools = new ArrayList<>();
                bools.add("true"); bools.add("false");
                return filter(bools, args[2]);
            }
            if (args[1].equalsIgnoreCase("particles")) {
                List<String> types = new ArrayList<>();
                types.add("template"); types.add("instance"); types.add("project"); types.add("wand");
                return filter(types, args[2]);
            }
            if (args[1].equalsIgnoreCase("bossbar")) {
                List<String> types = new ArrayList<>();
                types.add("template"); types.add("instance"); types.add("project"); types.add("set");
                return filter(types, args[2]);
            }
            if (args[1].equalsIgnoreCase("emptyBlocks")) {
                List<String> opts = new ArrayList<>();
                opts.add("list"); opts.add("add"); opts.add("remove");
                return filter(opts, args[2]);
            }
        }
        if (args.length == 4) {
            if (args[1].equalsIgnoreCase("particles") || args[1].equalsIgnoreCase("bossbar")) {
                List<String> bools = new ArrayList<>();
                bools.add("true"); bools.add("false");
                return filter(bools, args[3]);
            }
            if (args[1].equalsIgnoreCase("emptyBlocks") && args[2].equalsIgnoreCase("remove")) {
                 com.jackyblackson.idunntemplates.core.domain.PlayerSession session = sessionManager.getSession(player.getUniqueId());
                 if (session != null) {
                     return filter(session.getPreference().getEmptyBlocks(), args[3]);
                 }
            }
        }
        return Collections.emptyList();
    }
}
