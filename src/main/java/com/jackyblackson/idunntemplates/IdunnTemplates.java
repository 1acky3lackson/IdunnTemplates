package com.jackyblackson.idunntemplates;

import com.jackyblackson.idunntemplates.command.IdunnCommand;
import com.jackyblackson.idunntemplates.core.store.FileTemplateStorage;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class IdunnTemplates extends JavaPlugin {

    private TemplateStorage templateStorage;
    private TemplateManager templateManager;

    @Override
    public void onEnable() {
        // 1. Setup Storage
        File templateDir = new File(getDataFolder(), "templates");
        if (!templateDir.exists()) {
            templateDir.mkdirs();
        }
        
        this.templateStorage = new FileTemplateStorage(templateDir);

        // 2. Setup Manager
        this.templateManager = new TemplateManager(templateStorage);

        // 3. Register Commands
        Objects.requireNonNull(getCommand("idunn")).setExecutor(new IdunnCommand(templateManager));

        getLogger().info("IdunnTemplates has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
