package com.jackyblackson.idunntemplates.voxelwind;

import com.jackyblackson.idunntemplates.IdunnTemplates;

public class VoxelWind {

    public static void init() {
        // 1. 加载配置
        // 加载/重载配置文件
        VoxelWindConfig.get().reload();

        VoxelWindInitializer.init();

        IdunnTemplates.getInstance().getLogger().info("[VoxelWind] Registered pattern parser (No prefix required).");
    }
}