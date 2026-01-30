package com.jackyblackson.idunntemplates.command.sub.internal;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.calc.BlockComparator;
import com.jackyblackson.idunntemplates.core.calc.DiffCalculator;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;

public class DeleteInstanceCommand extends BaseSubCommand {

    private final InstanceRepository instanceRepository;
    private final TemplateManager templateManager;
    private final DiffCalculator diffCalculator;
    private final BlockComparator blockComparator;

    public DeleteInstanceCommand(InstanceRepository instanceRepository, TemplateManager templateManager) {
        this.instanceRepository = instanceRepository;
        this.templateManager = templateManager;
        
        // Initialize helpers
        this.blockComparator = new BlockComparator(IdunnTemplates.getInstance().getConfig());
        this.diffCalculator = new DiffCalculator(blockComparator, IdunnTemplates.getInstance().getLogger());
    }

    @Override
    public void execute(Player player, String[] args) {
        // /idunn instance delete <id> [-keep]
        if (args.length < 2) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "instance.delete.usage"));
            return;
        }
        
        String id = args[1];
        boolean keepBlocks = false;
        
        if (args.length > 2) {
            if (args[2].equalsIgnoreCase("-keep")) {
                keepBlocks = true;
            }
        }
        
        // Find instance
        Instance target = instanceRepository.getAllLoadedInstances().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElse(null);
                
        if (target == null) {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "instance.delete.not_found", id));
            return;
        }
        
        if (!keepBlocks) {
            try {
                int count = IdunnTemplates.getInstance().getInstanceManager().removeInstanceBlocks(target, player);
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "instance.delete.removed_blocks", String.valueOf(count)));
            } catch (Exception e) {
                player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "instance.delete.error_blocks", e.getMessage()));
                e.printStackTrace();
                return;
            }
            
            // Hard delete record
            instanceRepository.hardDelete(target);
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "instance.delete.hard_deleted"));
        } else {
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "instance.delete.skip_blocks"));

             // Soft delete record
            target.setDeletedTimestamp(System.currentTimeMillis());
            instanceRepository.saveInstance(target);
            player.sendMessage(com.jackyblackson.idunntemplates.core.util.MessageUtil.getMessage(player, "instance.delete.soft_deleted"));
        }
    }

//    private int removeInstanceBlocks(Instance instance, Player player) throws IOException {
//        Template template = templateManager.getTemplate(instance.getTemplateId());
//        if (template == null) {
//            throw new IOException("Template not found for this instance.");
//        }
//
//        TemplateVersion version = template.getMetadata().getVersions().stream()
//                .filter(v -> v.getVersionId().equals(instance.getCurrentVersionId()))
//                .findFirst()
//                .orElse(null);
//
//        if (version == null) {
//            throw new IOException("Version info missing for this instance.");
//        }
//
//        // Load Variations
//        int rot = instance.getRotationY();
//        boolean flipX = instance.isFlipX();
//        boolean flipY = instance.isFlipY();
//        boolean flipZ = instance.isFlipZ();
//        Clipboard clipboard = template.getClipboard(version.getVersionId(), rot, flipX, flipY, flipZ);
//        if (clipboard == null) {
//            throw new IOException("Failed to load clipboard.");
//        }
//
//        World world = Bukkit.getWorld(instance.getWorldId());
//        if (world == null) {
//            throw new IOException("World not loaded.");
//        }
//
//
//
//        // 1. Construct Transform
//        AffineTransform transform = new AffineTransform();
////        transform = transform.rotateY(instance.getRotationY());
////        if (instance.isFlipX()) transform = transform.scale(BlockVector3.at(-1, 1, 1).toVector3());
////        if (instance.isFlipY()) transform = transform.scale(BlockVector3.at(1, -1, 1).toVector3());
////        if (instance.isFlipZ()) transform = transform.scale(BlockVector3.at(1, 1, -1).toVector3());
//
//        // 2. Origin
//        BlockVector3 origin = BlockVector3.at(instance.getX(), instance.getY(), instance.getZ());
//
//        // 3. Get managed blocks using DiffCalculator
//        Set<BlockVector3> managedBlocks = diffCalculator.calculateManagedBlocks(clipboard, transform, origin, world, instance);
//
//        if (managedBlocks.isEmpty()) {
//            return 0;
//        }
//
//        // 4. Remove blocks using WorldEdit
//        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
//
//            for (BlockVector3 pos : managedBlocks) {
//                editSession.setBlock(pos, BlockTypes.AIR.getDefaultState());
//            }
//            editSession.flushSession();
//        } catch (Exception e) {
//            throw new IOException("WorldEdit error: " + e.getMessage(), e);
//        }
//
//        return managedBlocks.size();
//    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return instanceRepository.getAllLoadedInstances().stream()
                    .map(Instance::getId)
                    .collect(java.util.stream.Collectors.toList());         }
        if (args.length == 3) {
            return Collections.singletonList("-keep");
        }
        return Collections.emptyList();
    }
}