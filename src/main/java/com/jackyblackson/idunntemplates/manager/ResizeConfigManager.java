package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import org.bukkit.configuration.file.FileConfiguration;

public class ResizeConfigManager {
    private final IdunnTemplates plugin;
    private double defaultMinScale;
    private double defaultMaxScale;
    private double extendedMinScale;
    private double extendedMaxScale;
    private int cooldownSeconds;
    private int resizeSteps;
    private String language;
    private String prefix;

    public ResizeConfigManager(IdunnTemplates plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        this.plugin.saveDefaultConfig();
        FileConfiguration config = this.plugin.getConfig();
        this.defaultMinScale = config.getDouble("resize.default_min_scale", 0.8);
        this.defaultMaxScale = config.getDouble("resize.default_max_scale", 1.15);
        this.extendedMinScale = config.getDouble("resize.extended_min_scale", 0.0625);
        this.extendedMaxScale = config.getDouble("resize.extended_max_scale", 16.0);
        this.cooldownSeconds = config.getInt("resize.cooldown_seconds", 30);
        this.resizeSteps = config.getInt("resize.resize_steps", 20);
        this.language = config.getString("resize.language", "en_en");
        this.prefix = config.getString("resize.prefix", "§d[§IDUNN-Resized!§d] ");
    }

    public void reloadConfig() {
        this.plugin.reloadConfig();
        this.loadConfig();
    }

    public double getDefaultMinScale() {
        return this.defaultMinScale;
    }

    public double getDefaultMaxScale() {
        return this.defaultMaxScale;
    }

    public double getExtendedMinScale() {
        return this.extendedMinScale;
    }

    public double getExtendedMaxScale() {
        return this.extendedMaxScale;
    }

    public int getCooldownSeconds() {
        return this.cooldownSeconds;
    }

    public int getResizeSteps() {
        return this.resizeSteps;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getPrefix() {
        return this.prefix;
    }
}
