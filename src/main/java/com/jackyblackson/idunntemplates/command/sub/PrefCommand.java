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
        }
        return Collections.emptyList();
    }
}
