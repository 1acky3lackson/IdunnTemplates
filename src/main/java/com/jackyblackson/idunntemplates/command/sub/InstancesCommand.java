package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InstancesCommand extends BaseSubCommand {

    private final TemplateManager templateManager;
    private final InstanceRepository instanceRepository;

    public InstancesCommand(TemplateManager templateManager, InstanceRepository instanceRepository) {
        this.templateManager = templateManager;
        this.instanceRepository = instanceRepository;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn instances <templatePath> [page]
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn instances <templatePath> [page]");
            return;
        }
        String instPath = args[1];
        int instPage = args.length > 2 ? parseInt(args[2], 1) : 1;
        
        Template template = templateManager.getTemplate(instPath);
        if (template == null) {
            player.sendMessage(ChatColor.RED + "Template not found: " + instPath);
            return;
        }

        List<Instance> instances = instanceRepository.getAllLoadedInstances().stream()
                .filter(i -> i.getTemplateId().equals(template.getId()))
                .collect(Collectors.toList());

        if (instances.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No loaded instances found for template: " + template.getName());
            return;
        }

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) instances.size() / pageSize);
        if (instPage < 1) instPage = 1;
        if (instPage > totalPages) instPage = totalPages;

        player.sendMessage(ChatColor.GOLD + "=== Instances of " + template.getName() + " (Page " + instPage + "/" + totalPages + ") ===");

        int start = (instPage - 1) * pageSize;
        int end = Math.min(start + pageSize, instances.size());

        for (int i = start; i < end; i++) {
            Instance inst = instances.get(i);
            org.bukkit.World w = Bukkit.getWorld(inst.getWorldId());
            String wName = w != null ? w.getName() : "Unknown";

            // Short ID
            String shortId = inst.getId().substring(0, 8);

            player.sendMessage(ChatColor.YELLOW + "- ID:" + ChatColor.WHITE + shortId + "..." +
                    ChatColor.GRAY + " @ " + wName + " (" + inst.getX() + "," + inst.getY() + "," + inst.getZ() + ")");
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return filter(getTemplatePaths(), args[1]);
        }
        return Collections.emptyList();
    }
    
    private List<String> getTemplatePaths() {
        return templateManager.getTemplates().stream()
                .map(t -> {
                    String p = t.getPath().replace("/_", "/");
                    return p.startsWith("_") ? p.substring(1) : p;
                })
                .collect(Collectors.toList());
    }
}
