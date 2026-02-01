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

        languages.clear();

        // Load built-in languages from resources
        String[] defaults = {"en-us.yml", "zh-cn.yml", "zh-tw.yml"};
        for (String filename : defaults) {
            try (java.io.InputStream in = plugin.getResource("lang/" + filename)) {
                if (in != null) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
                    String langCode = filename.replace(".yml", "").toLowerCase();
                    languages.put(langCode, config);
                    plugin.getLogger().info("Loaded built-in language: " + langCode);

                    // Save to disk if not exists (for reference), but do not read from it
                    File file = new File(langFolder, filename);
                    if (!file.exists()) {
                        try {
                            plugin.saveResource("lang/" + filename, false);
                        } catch (IllegalArgumentException e) {
                            // Resource not found
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load built-in language " + filename + ": " + e.getMessage());
            }
        }

        // Load custom languages from disk
        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String langCode = file.getName().replace(".yml", "").toLowerCase();
            // Skip if already loaded (built-ins)
            if (languages.containsKey(langCode)) {
                continue;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            languages.put(langCode, config);
            plugin.getLogger().info("Loaded custom language: " + langCode);
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
