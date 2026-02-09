package com.jackyblackson.idunntemplates.voxelwind;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class VoxelWindConfig {
    private final Map<String, String> materialMap = new HashMap<>();
    private final Map<String, String> shapeMap = new HashMap<>();
    private final Map<String, Set<String>> allowedCombinations = new HashMap<>();
    // 新增：直白映射表
    private final Map<String, String> directMappings = new HashMap<>();

    private String separator = "-";
    private static VoxelWindConfig instance;

    public static VoxelWindConfig get() {
        if (instance == null) instance = new VoxelWindConfig();
        return instance;
    }

    public void reload() {
        materialMap.clear();
        shapeMap.clear();
        allowedCombinations.clear();
        directMappings.clear();

        IdunnTemplates plugin = IdunnTemplates.getInstance();
        File file = new File(plugin.getDataFolder(), "voxelwind.yml");

        if (!file.exists()) {
            plugin.saveResource("voxelwind.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.separator = config.getString("separator", "-");

        // 1. Load Definitions
        loadMap(config.getConfigurationSection("definitions.materials"), materialMap);
        loadMap(config.getConfigurationSection("definitions.shapes"), shapeMap);

        // 2. Load Rules
        ConfigurationSection rules = config.getConfigurationSection("rules");
        if (rules != null) {
            for (String prefix : rules.getKeys(false)) {
                List<String> suffixes = rules.getStringList(prefix);
                allowedCombinations.put(prefix, new HashSet<>(suffixes));
            }
        }

        // 3. Load Direct Mappings
        loadMap(config.getConfigurationSection("direct_mappings"), directMappings);

        plugin.getLogger().log(Level.INFO, "[VoxelWind] Loaded " + allowedCombinations.size() + " rules and " + directMappings.size() + " direct mappings.");
    }

    private void loadMap(ConfigurationSection section, Map<String, String> target) {
        if (section != null) {
            for (String key : section.getKeys(false)) {
                target.put(key.toLowerCase(), section.getString(key));
            }
        }
    }

    public String getMaterial(String prefix) { return materialMap.get(prefix); }
    public String getShape(String suffix) { return shapeMap.get(suffix); }
    public String getSeparator() { return separator; }

    /**
     * 获取直白映射的结果
     */
    public String getDirectMapping(String input) {
        return directMappings.get(input.toLowerCase());
    }

    public Set<String> getDirectMappingKeys() {
        return directMappings.keySet();
    }

    public boolean isValidCombination(String prefix, String suffix) {
        return allowedCombinations.containsKey(prefix) &&
                allowedCombinations.get(prefix).contains(suffix);
    }

    public Set<String> getAllowedSuffixes(String prefix) {
        return allowedCombinations.getOrDefault(prefix, Collections.emptySet());
    }

    public Set<String> getValidPrefixes() {
        return allowedCombinations.keySet();
    }
}