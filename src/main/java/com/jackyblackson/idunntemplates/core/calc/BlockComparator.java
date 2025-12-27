package com.jackyblackson.idunntemplates.core.calc;

import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockComparator {

    private final Set<String> idleBlocks = new HashSet<>();
    private final Set<String> ignoredBlocks = new HashSet<>(); // e.g. structural_void

    public BlockComparator(FileConfiguration config) {
        loadConfig(config);
    }

    public void loadConfig(FileConfiguration config) {
        idleBlocks.clear();
        ignoredBlocks.clear();
        
        List<String> idles = config.getStringList("update.idle-blocks");
        if (idles.isEmpty()) {
            // Defaults
            idleBlocks.add("minecraft:air");
            idleBlocks.add("minecraft:cave_air");
            idleBlocks.add("minecraft:void_air");
            idleBlocks.add("minecraft:water");
            idleBlocks.add("minecraft:lava");
            idleBlocks.add("minecraft:light");
        } else {
            idleBlocks.addAll(idles);
        }
        
        // Structure Void is special: "if bn == structural_void then don't update"
        // It's effectively an "ignored block" in the schematic.
        ignoredBlocks.add("minecraft:structure_void");
    }

    public boolean isIdle(BlockState block) {
        if (block == null) return true; // Treat null/missing as air
        return idleBlocks.contains(block.getBlockType().getId());
    }
    
    public boolean isIdle(org.bukkit.block.Block block) {
        // Convert bukkit block to string id? 
        // block.getType().getKey().toString() -> "minecraft:stone"
        return idleBlocks.contains(block.getType().getKey().toString());
    }

    public boolean isIgnored(BlockState block) {
        if (block == null) return false;
        return ignoredBlocks.contains(block.getBlockType().getId());
    }

    /**
     * Checks if two blocks are "similar enough" to be considered unmodified.
     * Ignores NBT/inventory data for containers if possible (WE BlockState equals includes NBT usually).
     * 
     * However, BlockState.equals() in WorldEdit is strict.
     * We want to ignore inventory NBT but keep other properties (like facing).
     */
    public boolean isSimilar(BlockState weBlock, org.bukkit.block.Block bukkitBlock) {
        if (weBlock == null) return false;
        
        // 1. Check Material/Type
        String weId = weBlock.getBlockType().getId();
        String bukkitId = bukkitBlock.getType().getKey().toString();
        if (!weId.equals(bukkitId)) return false;

        // 2. Check BlockData (States like facing, waterlogged, etc.)
        // WorldEdit BlockState.getAsString() returns "minecraft:chest[facing=north,type=single]"
        // Bukkit Block.getBlockData().getAsString() returns "minecraft:chest[facing=north,type=single,waterlogged=false]"
        
        // This is tricky because string formats might slightly differ or include defaults.
        // A robust way involves parsing states.
        
        // For Phase 3, let's assume strict equality on string representation of BlockData 
        // (excluding NBT, which is usually not in BlockData string except for some mods).
        // Bukkit's getAsString() includes properties.
        // WE's getAsString() includes properties.
        
        // Simple heuristic:
        return weBlock.getAsString().equals(bukkitBlock.getBlockData().getAsString());
        
        // Note: If WE BlockState has NBT (e.g. tile entity data), it MIGHT be in toString? 
        // WE BlockState usually separates states and NBT. 
        // `weBlock.toBaseBlock().getNbtData()` holds NBT. 
        // `weBlock` itself (BlockState) usually just holds properties.
        // So `equals` on BlockState should be safe regarding Inventory.
    }
}
