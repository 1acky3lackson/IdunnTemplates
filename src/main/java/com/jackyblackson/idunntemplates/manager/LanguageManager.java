package com.jackyblackson.idunntemplates.manager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageManager {

    private final JavaPlugin plugin;
    private final File langFolder;
    private final Map<String, YamlConfiguration> languages = new HashMap<>();
    private String defaultLanguage = "en-us";

    public LanguageManager(JavaPlugin plugin, String defaultLanguage) {
        this.plugin = plugin;
        this.langFolder = new File(plugin.getDataFolder(), "lang");
        if (defaultLanguage != null) {
            this.defaultLanguage = defaultLanguage.toLowerCase();
        }
        loadLanguages();
    }

    public void loadLanguages() {
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Save/Update defaults
        String[] defaults = {"en-us.yml", "zh-cn.yml", "zh-tw.yml"};
        for (String def : defaults) {
            updateLanguageFile(def);
        }

        languages.clear();
        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String langCode = file.getName().replace(".yml", "").toLowerCase();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            languages.put(langCode, config);
            plugin.getLogger().info("Loaded language: " + langCode);
        }
    }

    private void updateLanguageFile(String filename) {
        File file = new File(langFolder, filename);
        if (!file.exists()) {
            try {
                plugin.saveResource("lang/" + filename, false);
            } catch (IllegalArgumentException e) {
                // Resource not found in jar
            }
            return;
        }

        try (java.io.InputStream in = plugin.getResource("lang/" + filename)) {
            if (in == null) return;

            YamlConfiguration internalConfig = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            YamlConfiguration diskConfig = YamlConfiguration.loadConfiguration(file);

            boolean modified = false;
            for (String key : internalConfig.getKeys(true)) {
                if (!diskConfig.contains(key)) {
                    diskConfig.set(key, internalConfig.get(key));
                    modified = true;
                }
            }

            if (modified) {
                diskConfig.save(file);
                plugin.getLogger().info("Updated language file: " + filename);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update language file " + filename + ": " + e.getMessage());
        }
    }

    public String getMessage(Player player, String key, String... args) {
        String locale = player.getLocale().toLowerCase();
        // Bukkit locales are usually en_us, but we might name files en-us or en_us.
        // Let's normalize _ to - for consistency if we want, or just support both.
        // Standard convention in web is often -, Java/Bukkit often _.
        // Let's try direct match first, then normalized.
        
        String msg = getRawMessage(locale, key);
        if (msg == null) {
            msg = getRawMessage(locale.replace("_", "-"), key);
        }
        if (msg == null) {
            msg = getRawMessage(defaultLanguage, key);
        }
        
        if (msg == null) {
            return "Missing translation: " + key;
        }

        return format(msg, args);
    }
    
    public String getMessage(String lang, String key, String... args) {
        String locale = lang.toLowerCase();
        String msg = getRawMessage(locale, key);
        if (msg == null) {
            msg = getRawMessage(defaultLanguage, key);
        }
        
        if (msg == null) {
            return "Missing translation: " + key;
        }

        return format(msg, args);
    }

    private String getRawMessage(String lang, String key) {
        YamlConfiguration config = languages.get(lang);
        if (config == null) return null;
        return config.getString(key);
    }

    private String format(String msg, String... args) {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        for (int i = 0; i < args.length; i++) {
            // Replace %1%, %2% etc.
            // args[0] -> %1%
            String placeholder = "%" + (i + 1) + "%";
            msg = msg.replace(placeholder, args[i]);
        }
        return msg;
    }
}
