package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InstanceMaskCommand extends BaseSubCommand {

    private final InstanceRepository instanceRepository;

    public InstanceMaskCommand(InstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    @Override
    public void execute(Player player, String[] args) {
        // args: <id> <face> <amount>
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn instance mask <id> <face> <amount>");
            player.sendMessage(ChatColor.GRAY + "Faces: x+, x-, y+, y-, z+, z-");
            return;
        }

        String id = args[1];
        String face = args[2].toLowerCase();
        int amount = 0;
        
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount number.");
            return;
        }

        Instance instance = instanceRepository.getAllLoadedInstances().stream()
                .filter(i -> i.getId().startsWith(id)) // Allow partial match
                .findFirst()
                .orElse(null);

        if (instance == null) {
            player.sendMessage(MessageUtil.getMessage(player, "instance.not_found", id));
            return;
        }

        switch (face) {
            case "x+": instance.setMaskXPos(amount); break;
            case "x-": instance.setMaskXNeg(amount); break;
            case "y+": instance.setMaskYPos(amount); break;
            case "y-": instance.setMaskYNeg(amount); break;
            case "z+": instance.setMaskZPos(amount); break;
            case "z-": instance.setMaskZNeg(amount); break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid face. Use x+, x-, y+, y-, z+, z-");
                return;
        }
        
        instanceRepository.saveInstance(instance);
        player.sendMessage(ChatColor.GREEN + "Instance mask updated. Note: This does not affect existing blocks unless you perform an update/cut.");
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
             return instanceRepository.getAllLoadedInstances().stream()
                     .map(Instance::getId)
                     .map(s -> s.substring(0, 8))
                     .collect(Collectors.toList());
        }
        if (args.length == 3) {
            return Arrays.asList("x+", "x-", "y+", "y-", "z+", "z-");
        }
        return Collections.emptyList();
    }
}
