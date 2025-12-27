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
        // Template Group
        CommandGroup templateGroup = new CommandGroup();
        templateGroup.register("list", new ListCommand(templateManager));
        templateGroup.register("create", new SaveCommand(templateManager));
        templateGroup.register("commit", new CommitCommand(templateManager));
        templateGroup.register("tp", new TemplateTpCommand(templateManager));
        subCommands.put("template", templateGroup);

        // Instance Group
        CommandGroup instanceGroup = new CommandGroup();
        instanceGroup.register("place", new PlaceCommand(templateManager, instanceManager));
        instanceGroup.register("list", new InstancesCommand(templateManager, instanceRepository));
        instanceGroup.register("tp", new TpCommand(instanceRepository));
        subCommands.put("instance", instanceGroup);

        // Root Commands
        subCommands.put("reload", new ReloadCommand(templateManager));
        subCommands.put("pref", new PrefCommand(sessionManager));
        subCommands.put("tp", new SmartTpCommand(templateManager, instanceRepository));
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
            player.sendMessage(ChatColor.RED + "Unknown command: " + subName);
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
