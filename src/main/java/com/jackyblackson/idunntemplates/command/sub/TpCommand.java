package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TpCommand extends BaseSubCommand {

    private final InstanceRepository instanceRepository;

    public TpCommand(InstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn tp <instanceId>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn tp <instanceId>");
            return;
        }
        String instanceId = args[1];

        // Try exact match first, then startsWith if not found
        Instance target = instanceRepository.getAllLoadedInstances().stream()
                .filter(i -> i.getId().equals(instanceId))
                .findFirst()
                .orElse(null);

        if (target == null) {
            // Try prefix match
            target = instanceRepository.getAllLoadedInstances().stream()
                    .filter(i -> i.getId().startsWith(instanceId))
                    .findFirst()
                    .orElse(null);
        }

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Instance not found: " + instanceId);
            return;
        }

        org.bukkit.World w = Bukkit.getWorld(target.getWorldId());
        if (w == null) {
            player.sendMessage(ChatColor.RED + "Instance world is not loaded.");
            return;
        }

        player.teleport(new Location(w, target.getX(), target.getY(), target.getZ()));
        player.sendMessage(ChatColor.GREEN + "Teleported to instance " + target.getId());
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
             List<String> ids = instanceRepository.getAllLoadedInstances().stream()
                     .map(Instance::getId)
                     .collect(Collectors.toList());
             return filter(ids, args[1]);
        }
        return Collections.emptyList();
    }
}
