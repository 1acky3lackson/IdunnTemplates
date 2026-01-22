package com.jackyblackson.idunntemplates.command;

import com.jackyblackson.idunntemplates.command.sub.brush.preset.BrushPresetLoadCommand;
import com.jackyblackson.idunntemplates.command.sub.brush.preset.BrushPresetSaveCommand;
import com.jackyblackson.idunntemplates.command.sub.brush.preset.BrushPresetUpdateCommand;
import com.jackyblackson.idunntemplates.command.sub.brush.*;
import com.jackyblackson.idunntemplates.command.sub.internal.DeleteInstanceCommand;
import com.jackyblackson.idunntemplates.command.sub.sets.*;
import com.jackyblackson.idunntemplates.command.sub.*; // Restore this
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class IdunnCommand implements TabExecutor {

    private final Map<String, IdunnSubCommand> subCommands = new HashMap<>();

    public IdunnCommand(TemplateManager templateManager, InstanceManager instanceManager, InstanceRepository instanceRepository, SessionManager sessionManager, SetManager setManager, BrushManager brushManager, BrushPresetManager brushPresetManager) {
        // Template Group
        CommandGroup templateGroup = new CommandGroup();
        templateGroup.register("list", new ListCommand(templateManager));
        templateGroup.register("create", new SaveCommand(templateManager));
        templateGroup.register("commit", new CommitCommand(templateManager));
        templateGroup.register("tp", new TemplateTpCommand(templateManager));
        templateGroup.register("place", new PlaceCommand(templateManager, instanceManager));
        subCommands.put("template", templateGroup);

        // Instance Group
        CommandGroup instanceGroup = new CommandGroup();
        instanceGroup.register("list", new InstancesCommand(templateManager, instanceRepository));
        instanceGroup.register("tp", new TpCommand(instanceRepository));
        instanceGroup.register("delete", new DeleteInstanceCommand(instanceRepository, templateManager));
        subCommands.put("instance", instanceGroup);
        
        // Sets Group
        CommandGroup setsGroup = new CommandGroup();
        
        CommandGroup setsAddGroup = new CommandGroup();
        setsAddGroup.register("path", new SetsAddPathCommand(sessionManager, templateManager));
        setsAddGroup.register("subset", new SetsAddSubsetCommand(sessionManager, setManager));
        setsGroup.register("add", setsAddGroup);
        
        setsGroup.register("remove", new SetsRemoveCommand(sessionManager));
        setsGroup.register("clear", new SetsClearCommand(sessionManager));
        setsGroup.register("prop", new SetsPropCommand(sessionManager));
        setsGroup.register("save", new SetsSaveCommand(sessionManager, setManager));
        setsGroup.register("load", new SetsLoadCommand(sessionManager, setManager));
        setsGroup.register("list", new SetsListCommand(sessionManager, setManager));
        setsGroup.register("place", new SetsPlaceCommand(sessionManager, templateManager, instanceManager, setManager));
        setsGroup.register("view", new SetsViewCommand(sessionManager));
        setsGroup.register("update", new SetsUpdateCommand(sessionManager, setManager));
        setsGroup.register("transferToGlobal", new SetsTransferCommand(sessionManager, setManager));
        subCommands.put("set", setsGroup);
        
        // Brush Group
        CommandGroup brushGroup = new CommandGroup();
        brushGroup.register("bind", new BrushBindCommand(sessionManager, templateManager, setManager));
        brushGroup.register("unbind", new BrushUnbindCommand(sessionManager));
        brushGroup.register("trigger", new BrushTriggerCommand(brushManager, sessionManager));
        brushGroup.register("modify", new BrushModifyCommand(sessionManager));
        brushGroup.register("source", new BrushSourceCommand(sessionManager, templateManager, setManager));
        brushGroup.register("list", new BrushListCommand(sessionManager));
        
        CommandGroup presetGroup = new CommandGroup();
        presetGroup.register("save", new BrushPresetSaveCommand(brushPresetManager, sessionManager));
        presetGroup.register("update", new BrushPresetUpdateCommand(brushPresetManager, sessionManager));
        presetGroup.register("load", new BrushPresetLoadCommand(brushPresetManager, sessionManager));
        brushGroup.register("preset", presetGroup);
        
        subCommands.put("brush", brushGroup);

        // Root Commands
        subCommands.put("reload", new ReloadCommand(templateManager));
        subCommands.put("pref", new PrefCommand(sessionManager));
        subCommands.put("tp", new SmartTpCommand(templateManager, instanceRepository));
        subCommands.put("commit", new SmartCommitCommand(templateManager));
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
