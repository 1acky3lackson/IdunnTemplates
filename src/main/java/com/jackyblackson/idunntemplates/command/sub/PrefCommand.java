package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.manager.SessionManager;
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
            player.sendMessage(ChatColor.RED + "Usage: /idunn pref <wand|placeOnEmptyOnly>");
            return;
        }
        String prefSub = args[1].toLowerCase();
        
        com.jackyblackson.idunntemplates.core.domain.PlayerSession session = sessionManager.getSession(player.getUniqueId());
        if (session == null) {
            // Should not happen if listener works, but just in case
            player.sendMessage(ChatColor.RED + "Session not found. Please rejoin.");
            return;
        }
        com.jackyblackson.idunntemplates.core.domain.PlayerPreference pref = session.getPreference();

        if (prefSub.equals("wand")) {
            if (args.length > 2 && args[2].equalsIgnoreCase("bind")) {
                // /idunn pref wand bind
                org.bukkit.Material type = player.getInventory().getItemInMainHand().getType();
                if (type == org.bukkit.Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You cannot bind Air as a wand.");
                    return;
                }
                pref.setWandMaterialName(type.name());
                sessionManager.saveSession(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Bound magic wand to: " + type.name());
            } else {
                // /idunn pref wand
                player.sendMessage(ChatColor.GOLD + "Current Magic Wand: " + ChatColor.WHITE + pref.getWandMaterialName());
            }
        } else if (prefSub.equalsIgnoreCase("placeonemptyonly")) {
            // /idunn pref placeOnEmptyOnly <true|false>
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Usage: /idunn pref placeOnEmptyOnly <true|false>");
                return;
            }
            boolean val = Boolean.parseBoolean(args[2]);
            pref.setPlaceOnEmptyOnly(val);
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Set placeOnEmptyOnly to: " + val);
        } else if (prefSub.equalsIgnoreCase("particles")) {
            // /idunn pref particles <type> <true|false>
            if (args.length < 4) {
                player.sendMessage(ChatColor.RED + "Usage: /idunn pref particles <template|instance|wand> <true|false>");
                return;
            }
            String type = args[2].toLowerCase();
            boolean val = Boolean.parseBoolean(args[3]);
            boolean found = true;
            switch (type) {
                case "template" -> pref.setParticleTemplateBoundaries(val);
                case "instance" -> pref.setParticleInstanceBoundaries(val);
                case "wand" -> pref.setParticleWand(val);
                default -> found = false;
            }
            if (!found) {
                player.sendMessage(ChatColor.RED + "Unknown particle type. Options: template, instance, wand");
                return;
            }
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Set particle preference '" + type + "' to: " + val);
        } else if (prefSub.equalsIgnoreCase("bossbar")) {
            // /idunn pref bossbar <type> <true|false>
            if (args.length < 4) {
                player.sendMessage(ChatColor.RED + "Usage: /idunn pref bossbar <template|instance|set> <true|false>");
                return;
            }
            String type = args[2].toLowerCase();
            boolean val = Boolean.parseBoolean(args[3]);
            boolean found = true;
            switch (type) {
                case "template" -> pref.setBossBarTemplate(val);
                case "instance" -> pref.setBossBarInstance(val);
                case "set" -> pref.setBossBarSet(val);
                default -> found = false;
            }
            if (!found) {
                player.sendMessage(ChatColor.RED + "Unknown bossbar type. Options: template, instance, set");
                return;
            }
            sessionManager.saveSession(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Set bossbar preference '" + type + "' to: " + val);
        } else {
            player.sendMessage(ChatColor.RED + "Unknown preference option.");
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
            return filter(opts, args[1]);
        }
        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("wand")) {
                return filter(Collections.singletonList("bind"), args[2]);
            }
            if (args[1].equalsIgnoreCase("placeOnEmptyOnly")) {
                List<String> bools = new ArrayList<>();
                bools.add("true"); bools.add("false");
                return filter(bools, args[2]);
            }
            if (args[1].equalsIgnoreCase("particles")) {
                List<String> types = new ArrayList<>();
                types.add("template"); types.add("instance"); types.add("wand");
                return filter(types, args[2]);
            }
            if (args[1].equalsIgnoreCase("bossbar")) {
                List<String> types = new ArrayList<>();
                types.add("template"); types.add("instance"); types.add("set");
                return filter(types, args[2]);
            }
        }
        if (args.length == 4) {
            if (args[1].equalsIgnoreCase("particles") || args[1].equalsIgnoreCase("bossbar")) {
                List<String> bools = new ArrayList<>();
                bools.add("true"); bools.add("false");
                return filter(bools, args[3]);
            }
        }
        return Collections.emptyList();
    }
}
