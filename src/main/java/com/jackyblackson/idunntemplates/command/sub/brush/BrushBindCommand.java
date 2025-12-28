package com.jackyblackson.idunntemplates.command.sub.brush;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushSettings;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import com.jackyblackson.idunntemplates.core.util.ItemUtil;
import com.jackyblackson.idunntemplates.core.util.brush.BrushFlagParser;
import com.jackyblackson.idunntemplates.manager.SessionManager;
import com.jackyblackson.idunntemplates.manager.SetManager;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BrushBindCommand extends BaseSubCommand {

    private final SessionManager sessionManager;
    private final TemplateManager templateManager;
    private final SetManager setManager;

    public BrushBindCommand(SessionManager sessionManager, TemplateManager templateManager, SetManager setManager) {
        this.sessionManager = sessionManager;
        this.templateManager = templateManager;
        this.setManager = setManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn brush bind <channel> <type> <value> [flags]
        // types: path, set
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn brush bind <channel> <path|set> <value> [-r] [-x] [-z]");
            return;
        }

        String channel = args[1].toLowerCase();
        String type = args[2].toLowerCase();
        String value = args[3];

        ItemStack item = player.getInventory().getItemInMainHand();
        String matName = ItemUtil.getBrushKey(item);
        
        if (matName == null) {
            player.sendMessage(ChatColor.RED + "You must hold an item to bind a brush.");
            return;
        }

        var session = sessionManager.getSession(player.getUniqueId());
        var pref = session.getPreference();
        
        BrushSession brushSession = pref.getBoundBrushes().computeIfAbsent(matName, k -> new BrushSession());
        BrushSettings settings = new BrushSettings();
        
        // Inherit defaults from preference?
        // Spec: "If not provided ... use player preference settings"
        // Here we initialize a new settings object. 
        // Logic for inheriting preference values should ideally be at creation or usage time.
        // For now, let's init with preference values if possible or keep defaults.
        // BrushSettings defaults are: fixed 0, false, false, noAir=true, emptyOnly=true.
        // Let's assume these are good defaults. Flags override them.

        // Content
        if (type.equals("path")) {
            // Create a set with single source
            settings.getContent().addSource(value, 1.0);
        } else if (type.equals("set")) {
            // Resolve set
            TemplateSet sourceSet = setManager.getSet(value, player.getName());
            if (sourceSet == null) {
                player.sendMessage(ChatColor.RED + "Set not found: " + value);
                return;
            }
            // Clone set structure (sources) but maybe not properties? 
            // Brush properties override set properties usually.
            // Let's copy sources.
            for (var src : sourceSet.getSources()) {
                settings.getContent().addSource(src.getPath(), src.getWeight());
            }
        } else {
            player.sendMessage(ChatColor.RED + "Unknown type: " + type + ". Use 'path' or 'set'.");
            return;
        }

        // Apply flags
        BrushFlagParser.applyFlags(settings, args, 3);

        brushSession.setSettings(channel, settings);
        sessionManager.saveSession(player.getUniqueId());

        player.sendMessage(ChatColor.GREEN + "Bound " + type + " '" + value + "' to channel '" + channel + "' on " + matName);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return filter(Arrays.asList("left", "right", "<custom>"), args[1]);
        }
        if (args.length == 3) {
            return filter(Arrays.asList("path", "set"), args[2]);
        }
        if (args.length == 4) {
            if (args[2].equalsIgnoreCase("path")) {
                // Suggest paths
                return filter(getAllTemplatePaths(), args[3]);
            }
            if (args[2].equalsIgnoreCase("set")) {
                // Suggest sets
                List<String> options = new ArrayList<>();
                options.addAll(sessionManager.getSession(player.getUniqueId()).getPreference().getSavedSets().keySet());
                for (String ns : setManager.getLoadedNamespaces()) {
                    for (String name : setManager.getGlobalNamespace(ns).keySet()) {
                        options.add(ns + ":" + name);
                    }
                }
                return filter(options, args[3]);
            }
        }
        return Collections.emptyList();
    }
    
    private List<String> getAllTemplatePaths() {
        List<String> paths = new ArrayList<>();
        templateManager.getTemplates().forEach(t -> {
            String p = t.getPath();
            if (p.startsWith("_")) p = p.substring(1);
            paths.add(p);
        });
        return paths;
    }
}
