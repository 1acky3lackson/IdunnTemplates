package com.jackyblackson.idunntemplates;

import com.jackyblackson.idunntemplates.command.IdunnCommand;
import com.jackyblackson.idunntemplates.core.calc.BlockComparator;
import com.jackyblackson.idunntemplates.core.store.FileInstanceRepository;
import com.jackyblackson.idunntemplates.core.store.FileTemplateStorage;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.jackyblackson.idunntemplates.listener.ChunkListener;
import com.jackyblackson.idunntemplates.manager.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class IdunnTemplates extends JavaPlugin {

    private static IdunnTemplates INSTANCE = null;

    private TemplateStorage templateStorage;
    private TemplateManager templateManager;
    private InstanceRepository instanceRepository;
    private InstanceManager instanceManager;
    private SessionManager sessionManager;
    private BlockComparator blockComparator;
    private TemplateUpdater templateUpdater;
    private com.jackyblackson.idunntemplates.manager.EffectManager effectManager;
    private com.jackyblackson.idunntemplates.manager.SetManager setManager;

    public static IdunnTemplates getInstance() { return INSTANCE; }

    public TemplateStorage getTemplateStorage() {
        return templateStorage;
    }
    
    public com.jackyblackson.idunntemplates.manager.SetManager getSetManager() {
        return setManager;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public InstanceRepository getInstanceRepository() {
        return instanceRepository;
    }

    public InstanceManager getInstanceManager() {
        return instanceManager;
    }
    
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public BlockComparator getBlockComparator() {
        return blockComparator;
    }

    public TemplateUpdater getTemplateUpdater() {
        return templateUpdater;
    }

    @Override
    public void onEnable() {
        getLogger().info("""
                \n\s
                \n\s
                ██╗██████╗ ██╗   ██╗███╗   ██╗███╗   ██╗\s
                ██║██╔══██╗██║   ██║████╗  ██║████╗  ██║\s
                ██║██║  ██║██║   ██║██╔██╗ ██║██╔██╗ ██║\s
                ██║██║  ██║██║   ██║██║╚██╗██║██║╚██╗██║\s
                ██║██████╔╝╚██████╔╝██║ ╚████║██║ ╚████║\s
                ╚═╝╚═════╝  ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═══╝\s
                                                        \s
                                       By Jacky_Blackson\s
                                                                               \s
                """.stripIndent().indent(4)
        );

        // 0. setup instance
        INSTANCE = this;

        // 1. Setup Config
        saveDefaultConfig();
        
        // 2. Setup Storage
        File templateDir = new File(getDataFolder(), "templates");
        File instancesDir = new File(getDataFolder(), "instances");
        File playerDir = new File(getDataFolder(), "player_data");
        File setsDir = new File(getDataFolder(), "sets");
        
        if (!templateDir.exists()) templateDir.mkdirs();
        if (!instancesDir.exists()) instancesDir.mkdirs();
        if (!playerDir.exists()) playerDir.mkdirs();
        if (!setsDir.exists()) setsDir.mkdirs();
        
        this.templateStorage = new FileTemplateStorage(templateDir);
        this.instanceRepository = new FileInstanceRepository(instancesDir, getLogger());
        
        // 3. Setup Core Logic
        this.blockComparator = new BlockComparator(getConfig());
        this.templateUpdater = new TemplateUpdater(templateStorage, instanceRepository, blockComparator, getLogger());

        // 4. Setup Manager
        this.templateManager = new TemplateManager(templateStorage);
        this.templateManager.setUpdater(templateUpdater);
        
        java.util.List<String> defaultEmptyBlocks = getConfig().getStringList("emptyBlocks");

        this.sessionManager = new com.jackyblackson.idunntemplates.manager.SessionManager(playerDir, getLogger(), defaultEmptyBlocks);
        this.instanceManager = new InstanceManager(templateStorage, instanceRepository, getLogger(), sessionManager);

        // Inject sessionManager into instanceManager via setter or reflection if constructor not updated here?
        // Wait, I updated InstanceManager constructor in previous turn but I need to update the call here.
        // I updated InstanceManager constructor in Turn 5, but I updated the call in IdunnTemplates in Turn 5 too?
        // Let's check IdunnTemplates current content in Turn 5.
        // I did "Inject SessionManager into InstanceManager constructor." in Turn 5.
        // So the line is: this.instanceManager = new InstanceManager(..., sessionManager);
        
        // I need to update it again to match.
        this.instanceManager = new InstanceManager(templateStorage, instanceRepository, getLogger(), sessionManager);
        
        this.setManager = new com.jackyblackson.idunntemplates.manager.SetManager(setsDir, sessionManager, getLogger());
        
        // Inject back into SessionManager
        this.sessionManager.setTemplateManager(templateManager);
        this.sessionManager.setSetManager(setManager);
        
        this.effectManager = new EffectManager(templateManager, instanceRepository, sessionManager, setManager);
        
        BrushManager brushManager = new BrushManager(sessionManager, templateManager, instanceManager, setManager);

        // 5. Register Commands
        Objects.requireNonNull(getCommand("idunn")).setExecutor(new IdunnCommand(templateManager, instanceManager, instanceRepository, sessionManager, setManager));
        
        // 6. Register Listeners
        getServer().getPluginManager().registerEvents(new ChunkListener(instanceRepository, templateManager, templateUpdater, getLogger()), this);
        getServer().getPluginManager().registerEvents(sessionManager, this);
        getServer().getPluginManager().registerEvents(effectManager, this);
        getServer().getPluginManager().registerEvents(brushManager, this);
        
        // 7. Tasks
        // Run particle effects every 10 ticks (0.5s)
        effectManager.runTaskTimer(this, 20L, 10L);

        // 8. Load Sessions for Online Players (Handle Reloads)
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            sessionManager.onPlayerJoin(new org.bukkit.event.player.PlayerJoinEvent(p, null));
        }

        getLogger().info("IdunnTemplates has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (instanceRepository instanceof FileInstanceRepository) {
            ((FileInstanceRepository) instanceRepository).shutdown();
        }
        
        if (sessionManager != null) {
            // Save all sessions
             for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                 sessionManager.saveSession(p.getUniqueId());
             }
        }
    }
}