package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SmartCommitCommand extends BaseSubCommand {

    private final TemplateManager templateManager;
    private final CommitCommand commitCommand;

    public SmartCommitCommand(TemplateManager templateManager) {
        this.templateManager = templateManager;
        this.commitCommand = new CommitCommand(templateManager);
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn commit [path] <message>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn commit [templatePath] <message>");
            return;
        }

        String possiblePath = args[1];
        Template explicit = templateManager.getTemplate(possiblePath);
        
        if (explicit != null) {
            // Explicit mode: /idunn commit path message...
            commitCommand.execute(player, args);
            return;
        }
        
        // Smart mode: /idunn commit message... (where first word is NOT a path)
        // Find template player is in
        Location loc = player.getLocation();
        Template target = null;
        
        for (Template t : templateManager.getTemplates()) {
            if (!t.getMetadata().getWorldId().equals(loc.getWorld().getUID())) continue;
            
            TemplateMetadata meta = t.getMetadata();
            // AABB check
            double minX = meta.getAnchorX();
            double minY = meta.getAnchorY();
            double minZ = meta.getAnchorZ();
            double maxX = minX + meta.getWidth();
            double maxY = minY + meta.getHeight();
            double maxZ = minZ + meta.getLength();
            
            if (loc.getX() >= minX && loc.getX() < maxX &&
                loc.getY() >= minY && loc.getY() < maxY &&
                loc.getZ() >= minZ && loc.getZ() < maxZ) {
                target = t;
                break;
            }
        }
        
        if (target == null) {
            player.sendMessage(ChatColor.RED + "You are not inside any template's master region, this smart commit command have to be execute in a template's master region. You can use '/idunn template commit <path> <commit message>' instead to manually specific which template should we commit.");
            return;
        }
        
        // Construct new args: ["commit", targetPath, args[1], args[2]...]
        // args[0] is "commit". args[1...] is message.
        String[] newArgs = new String[args.length + 1];
        newArgs[0] = args[0]; // "commit"
        newArgs[1] = target.getPath();
        System.arraycopy(args, 1, newArgs, 2, args.length - 1);
        
        player.sendMessage(ChatColor.YELLOW + "Smart Commit detected template: " + target.getName());
        commitCommand.execute(player, newArgs);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return List.of("<commit message>");
    }
}
