package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.util.PermissionUtil;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.jackyblackson.idunntemplates.permission.PermissionNames;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommitCommand extends BaseSubCommand {

    private final TemplateManager templateManager;

    public CommitCommand(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn commit <templatePath> <message>
        if (args.length < 3) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.usage"));
            return;
        }
        String commitPath = args[1];
        StringBuilder msg = new StringBuilder();
        for (int i = 2; i < args.length; i++) msg.append(args[i]).append(" ");
        String message = msg.toString().trim();

        boolean hasPerm = commitPath.startsWith("users/" + player.getName())
                || PermissionUtil.hasRecursivePermission(
                        player,
                        PermissionNames.Templates.commitToPath$R,
                        commitPath
                );

        if (!hasPerm) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.no_perm", commitPath));
            return;
        }

        // 1. Get Template
        Template template = templateManager.getTemplate(commitPath);
        if (template == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.not_found", commitPath));
            return;
        }

        // 2. Validate World and Permissions
        TemplateMetadata meta = template.getMetadata();
        org.bukkit.World sourceWorld = Bukkit.getWorld(meta.getWorldId());
        if (sourceWorld == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.world_not_loaded", meta.getWorldId().toString()));
            return;
        }

        // 3. Calculate Region and Origin
        BlockVector3 min = BlockVector3.at(meta.getAnchorX(), meta.getAnchorY(), meta.getAnchorZ());
        BlockVector3 max = min.add(meta.getWidth() - 1, meta.getHeight() - 1, meta.getLength() - 1);

        // Recover origin offset from previous version
        BlockVector3 originOffset = BlockVector3.ZERO;
        TemplateVersion latest = template.getLatestVersion();
        if (latest != null) {
            try {
                java.io.File schemFile = new java.io.File(template.getDirectory(), latest.getVersionId() + ".schem");
                if (schemFile.exists()) {
                    com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat format = com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats.findByFile(schemFile);
                    if (format == null) format = com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats.findByAlias("sponge");
                    if (format != null) {
                        try (com.sk89q.worldedit.extent.clipboard.io.ClipboardReader reader = format.getReader(new java.io.FileInputStream(schemFile))) {
                            Clipboard oldClip = reader.read();
                            originOffset = oldClip.getOrigin().subtract(oldClip.getRegion().getMinimumPoint());
                        }
                    }
                }
            } catch (Exception e) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.warning_origin"));
                e.printStackTrace();
            }
        }

        BlockVector3 newOrigin = min.add(originOffset);

        // 4. Capture
        com.sk89q.worldedit.regions.CuboidRegion region = new com.sk89q.worldedit.regions.CuboidRegion(
                BukkitAdapter.adapt(sourceWorld), min, max
        );

        com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard clipboard = new com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard(region);
        clipboard.setOrigin(newOrigin);

        try (com.sk89q.worldedit.EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(sourceWorld))) {
            com.sk89q.worldedit.function.operation.ForwardExtentCopy copy = new com.sk89q.worldedit.function.operation.ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            com.sk89q.worldedit.function.operation.Operations.completeLegacy(copy);
        } catch (Exception e) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.failed_capture", e.getMessage()));
            e.printStackTrace();
            return;
        }

        // 5. Commit
        try {
            templateManager.commitTemplate(player, template, message, clipboard);
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.success"));
        } catch (Exception e) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "commit.error", e.getMessage()));
            e.printStackTrace();
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
