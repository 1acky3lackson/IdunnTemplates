package com.jackyblackson.idunntemplates.voxelwind;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.BlockFactory;
import com.sk89q.worldedit.internal.registry.AbstractFactory;
import com.sk89q.worldedit.internal.registry.InputParser;

import java.lang.reflect.Field;
import java.util.List;

public class VoxelWindInitializer {

    /**
     * Replaces the default BlockFactory parsers with our VoxelWindBlockParser.
     * This ensures VoxelWind logic runs before (or as a wrapper to) the default logic.
     */
    @SuppressWarnings("unchecked")
    public static void init() {
        try {
            WorldEdit we = WorldEdit.getInstance();
            BlockFactory factory = we.getBlockFactory();

            // 1. Access the 'parsers' field in AbstractFactory
            Field parsersField = AbstractFactory.class.getDeclaredField("parsers");
            parsersField.setAccessible(true);

            // 2. Get the current list (which contains DefaultBlockParser)
            List<InputParser<?>> parsers = (List<InputParser<?>>) parsersField.get(factory);

            // 3. Clear and inject our new hybrid parser
            parsers.clear();
            parsers.add(new VoxelWindBlockParser(we));

            IdunnTemplates.getInstance().getLogger().info("VoxelWind: Successfully injected VoxelWindBlockParser into BlockFactory.");
        } catch (Exception e) {
            IdunnTemplates.getInstance().getLogger().info("VoxelWind: Failed to inject BlockParser via reflection!" + e.getMessage());
        }
    }
}