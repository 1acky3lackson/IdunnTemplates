package com.jackyblackson.idunntemplates.command.sub.internal;

import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class UndoInstanceCommand extends BaseSubCommand {

    private final InstanceRepository instanceRepository;

    public UndoInstanceCommand(InstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn instance undo <id>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn instance undo <id>");
            return;
        }
        
        String id = args[1];
        
        // Find instance
        Instance target = instanceRepository.getAllLoadedInstances().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElse(null);
                
        if (target != null) {
            // Delete record
            instanceRepository.hardDelete(target);
        }
        
        // Execute WorldEdit undo
        player.performCommand("/undo");
        
        player.sendMessage(ChatColor.GREEN + "Instance record deleted and undo attempted.");
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
