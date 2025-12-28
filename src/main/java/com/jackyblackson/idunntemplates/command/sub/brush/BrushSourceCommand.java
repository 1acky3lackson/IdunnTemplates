package com.jackyblackson.idunntemplates.command.sub.brush;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSettings;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BrushSourceCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final TemplateManager templateManager;
    private final SetManager setManager;

    public BrushSourceCommand(SessionManager sessionManager, TemplateManager templateManager, SetManager setManager) {
        this.sessionManager = sessionManager;
        this.templateManager = templateManager;
        this.setManager = setManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn brush source <channel> <action> ...
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn brush source <channel> <add|list|remove> ...");
            return;
        }

        String channel = args[1];
        String action = args[2].toLowerCase();

        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);

        if (matName == null) {
            player.sendMessage(ChatColor.RED + "You must hold an item.");
            return;
        }

        var session = sessionManager.getSession(player.getUniqueId());
        BrushSession brushSession = session.getPreference().getBoundBrushes().get(matName);
        if (brushSession == null || brushSession.getSettings(channel) == null) {
            player.sendMessage(ChatColor.RED + "No brush bound to channel '" + channel + "'.");
            return;
        }

        BrushSettings settings = brushSession.getSettings(channel);
        TemplateSet content = settings.getContent();

        switch (action) {
            case "add":
                handleAdd(player, args, content);
                break;
            case "list":
                handleList(player, content, channel);
                break;
            case "remove":
                handleRemove(player, args, content, channel);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown action: " + action);
        }
        
        sessionManager.saveSession(player.getUniqueId());
    }

    private void handleAdd(Player player, String[] args, TemplateSet content) {
        // args: source <channel> add <type> <value> [weight]
        // indicies: 0     1      2    3      4       5
        if (args.length < 5) {
            player.sendMessage(ChatColor.RED + "Usage: ... add <path|set> <value> [weight]");
            return;
        }
        
        String type = args[3].toLowerCase();
        String value = args[4];
        double weight = 1.0;
        
        if (args.length > 5) {
            try {
                weight = Double.parseDouble(args[5]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid weight.");
                return;
            }
        }
        
        if (type.equals("path")) {
            content.addSource(value, weight);
            player.sendMessage(ChatColor.GREEN + "Added path source: " + value);
        } else if (type.equals("set")) {
             TemplateSet sourceSet = setManager.getSet(value, player.getName());
            if (sourceSet == null) {
                player.sendMessage(ChatColor.RED + "Set not found: " + value);
                return;
            }
            // Add all sources from the set? Or add the set as a meta-source?
            // Currently TemplateSet stores list of sources (path, weight).
            // It doesn't support nested sets directly unless we flatten them.
            // Documentation says "add new path/set source".
            // Implementation of TemplateSet usually flattens.
            // SetsAddSubsetCommand logic:
            // "For each source in subset, add to current set."
            
            for (var src : sourceSet.getSources()) {
                content.addSource(src.getPath(), src.getWeight() * weight); // Multiply weight? Or just use src weight? 
                // Usually we might want to scale, but simple add is safer.
            }
            player.sendMessage(ChatColor.GREEN + "Added contents of set: " + value);
        } else {
            player.sendMessage(ChatColor.RED + "Unknown type: " + type);
        }
    }

    private void handleList(Player player, TemplateSet content, String channel) {
        player.sendMessage(ChatColor.GOLD + "Brush Sources:");
        var sources = content.getSources();
        for (int i = 0; i < sources.size(); i++) {
            var src = sources.get(i);
            var method = new TextComponent("[X] ");
            method.setColor(net.md_5.bungee.api.ChatColor.RED);
            method.setBold(true);
            method.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/idunn brush source " + channel + " remove " + i
            ));
            method.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Delete this source").create()));
            var msg = new TextComponent(ChatColor.YELLOW + "" + i + ". " + ChatColor.WHITE + src.getPath() + ChatColor.GRAY + " (w: " + src.getWeight() + ")");
            method.addExtra(msg);
            player.spigot().sendMessage(method);
        }
    }

    private void handleRemove(Player player, String[] args, TemplateSet content, String channel) {
        // args: source <channel> remove <index>
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: ... remove <index>");
            return;
        }
        
        try {
            int index = Integer.parseInt(args[3]);
            if (index < 0 || index >= content.getSources().size()) {
                player.sendMessage(ChatColor.RED + "Invalid index.");
                return;
            }
            var removed = content.getSources().remove(index);
            player.sendMessage(ChatColor.GREEN + "Removed source: " + removed.getPath() + ". Sources now:");
            handleList(player, content, channel);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid index.");
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        // source <channel> <action> ...
        if (args.length == 2) {
             ItemStack item = player.getInventory().getItemInMainHand();
             String matName = ItemUtil.getBrushKey(item);
             if (matName != null) {
                 var session = sessionManager.getSession(player.getUniqueId());
                 BrushSession bs = session.getPreference().getBoundBrushes().get(matName);
                 if (bs != null) {
                     return filter(new ArrayList<>(bs.getChannels().keySet()), args[1]);
                 }
             }
             return filter(Arrays.asList("left", "right"), args[1]);
        }
        if (args.length == 3) {
            return filter(Arrays.asList("add", "list", "remove"), args[2]);
        }
        if (args.length == 4 && args[2].equalsIgnoreCase("remove")) {
            return List.of("<index>");
        }
        if (args.length == 4 && args[2].equalsIgnoreCase("add")) {
            return filter(Arrays.asList("path", "set"), args[3]);
        }
        if (args.length == 5 && args[2].equalsIgnoreCase("add")) {
            if (args[3].equalsIgnoreCase("path")) {
                List<String> paths = new ArrayList<>();
                templateManager.getTemplates().forEach(t -> {
                    String p = t.getPath();
                    if (p.startsWith("_")) p = p.substring(1);
                    paths.add(p);
                });
                return filter(paths, args[4]);
            }
            if (args[3].equalsIgnoreCase("set")) {
                List<String> options = new ArrayList<>();
                options.addAll(sessionManager.getSession(player.getUniqueId()).getPreference().getSavedSets().keySet());
                for (String ns : setManager.getLoadedNamespaces()) {
                    for (String name : setManager.getGlobalNamespace(ns).keySet()) {
                        options.add(ns + ":" + name);
                    }
                }
                return filter(options, args[4]);
            }
        }
        
        return Collections.emptyList();
    }
}
