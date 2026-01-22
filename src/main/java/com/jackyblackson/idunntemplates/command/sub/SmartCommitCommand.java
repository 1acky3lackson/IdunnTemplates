package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.util.PermissionUtil;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.jackyblackson.idunntemplates.permission.PermissionNames;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

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
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.smart.usage"));
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
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.smart.not_in_region"));
            return;
        }

        boolean hasPerm = target.getPath().startsWith("users/" + player.getName())
                || PermissionUtil.hasRecursivePermission(
                player,
                PermissionNames.Templates.commitToPath$R,
                target.getPath()
        );

        if (!hasPerm) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.smart.no_perm", target.getPath()));
            return;
        }
        
        // Construct new args: ["commit", targetPath, args[1], args[2]...]
        // args[0] is "commit". args[1...] is message.
        String[] newArgs = new String[args.length + 1];
        newArgs[0] = args[0]; // "commit"
        newArgs[1] = target.getPath();
        System.arraycopy(args, 1, newArgs, 2, args.length - 1);
        
        player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.smart.detected", target.getName()));
        commitCommand.execute(player, newArgs);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return List.of("<commit message>");
    }
}
