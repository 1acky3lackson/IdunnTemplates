package com.jackyblackson.idunntemplates.command;

import com.jackyblackson.idunntemplates.command.sub.*;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.manager.InstanceManager;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class IdunnCommand implements TabExecutor {

    private final Map<String, IdunnSubCommand> subCommands = new HashMap<>();

    public IdunnCommand(TemplateManager templateManager, InstanceManager instanceManager, InstanceRepository instanceRepository, SessionManager sessionManager) {
        subCommands.put("save", new SaveCommand(templateManager));
        subCommands.put("place", new PlaceCommand(templateManager, instanceManager));
        subCommands.put("commit", new CommitCommand(templateManager));
        subCommands.put("list", new ListCommand(templateManager));
        subCommands.put("reload", new ReloadCommand(templateManager));
        subCommands.put("instances", new InstancesCommand(templateManager, instanceRepository));
        subCommands.put("tp", new TpCommand(instanceRepository));
        subCommands.put("pref", new PrefCommand(sessionManager));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            return false;
        }

        String subName = args[0].toLowerCase();
        IdunnSubCommand subCmd = subCommands.get(subName);

        if (subCmd != null) {
            subCmd.execute(player, args);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Unknown subcommand: " + subName);
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        Player player = (Player) sender;

        if (args.length == 1) {
            return filter(new ArrayList<>(subCommands.keySet()), args[0]);
        }

        String subName = args[0].toLowerCase();
        IdunnSubCommand subCmd = subCommands.get(subName);

        if (subCmd != null) {
            return subCmd.tabComplete(player, args);
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
