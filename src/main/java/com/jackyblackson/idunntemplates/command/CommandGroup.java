package com.jackyblackson.idunntemplates.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CommandGroup implements IdunnSubCommand {

    private final Map<String, IdunnSubCommand> subCommands = new HashMap<>();

    public void register(String name, IdunnSubCommand command) {
        subCommands.put(name.toLowerCase(), command);
    }

    @Override
    public void execute(Player player, String[] args) {
        // args[0] is the group name (e.g., "template")
        // We need args[1] as the subcommand
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Available commands: " + String.join(", ", subCommands.keySet()));
            return;
        }

        String subName = args[1].toLowerCase();
        IdunnSubCommand cmd = subCommands.get(subName);

        if (cmd != null) {
            // Shift args: ["template", "list", "1"] -> ["list", "1"]
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            cmd.execute(player, newArgs);
        } else {
            player.sendMessage(ChatColor.RED + "Unknown command: " + subName);
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        // args[0] is group name.
        
        if (args.length == 2) {
            return filter(new ArrayList<>(subCommands.keySet()), args[1]);
        }
        
        if (args.length > 2) {
            String subName = args[1].toLowerCase();
            IdunnSubCommand cmd = subCommands.get(subName);
            if (cmd != null) {
                String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
                return cmd.tabComplete(player, newArgs);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String current) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }
}
