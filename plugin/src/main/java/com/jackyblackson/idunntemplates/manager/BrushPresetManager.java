package com.jackyblackson.idunntemplates.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jackyblackson.idunntemplates.core.domain.brush.BrushPreset;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class BrushPresetManager {

    private final File presetsFolder;
    private final Logger logger;
    private final Gson gson;

    // Namespace -> (PresetName -> BrushPreset)
    private final Map<String, Map<String, BrushPreset>> presets = new ConcurrentHashMap<>();

    public BrushPresetManager(File dataFolder, Logger logger) {
        this.presetsFolder = new File(dataFolder, "presets/brushes");
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        if (!presetsFolder.exists()) {
            presetsFolder.mkdirs();
        }

        loadAllPresets();
    }

    public void loadAllPresets() {
        presets.clear();
        File[] files = presetsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File f : files) {
            String namespace = f.getName().replace(".json", "");
            try (FileReader reader = new FileReader(f)) {
                Type type = new TypeToken<Map<String, BrushPreset>>(){}.getType();
                Map<String, BrushPreset> loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    presets.put(namespace, new ConcurrentHashMap<>(loaded));
                }
            } catch (IOException e) {
                logger.severe("Failed to load brush preset namespace " + namespace + ": " + e.getMessage());
            }
        }
    }

    public void saveNamespace(String namespace) {
        if (!presets.containsKey(namespace)) return;

        File f = new File(presetsFolder, namespace + ".json");
        try (FileWriter writer = new FileWriter(f)) {
            gson.toJson(presets.get(namespace), writer);
        } catch (IOException e) {
            logger.severe("Failed to save brush preset namespace " + namespace + ": " + e.getMessage());
        }
    }

    public void savePreset(String namespace, BrushPreset preset) {
        presets.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>()).put(preset.getName(), preset);
        saveNamespace(namespace);
    }
    
    public BrushPreset getPreset(String namespace, String name) {
        Map<String, BrushPreset> nsPresets = presets.get(namespace);
        if (nsPresets == null) return null;
        return nsPresets.get(name);
    }

    public BrushPreset resolvePreset(String query, Player player) {
        String namespace;
        String name;

        if (query.contains(":")) {
            String[] parts = query.split(":", 2);
            namespace = parts[0];
            name = parts[1];
        } else {
            namespace = "player." + player.getName();
            name = query;
        }

        return getPreset(namespace, name);
    }
    
    public String resolveNamespace(String query, Player player) {
        if (query.contains(":")) {
            return query.split(":", 2)[0];
        }
        return "player." + player.getName();
    }
    
    public String resolveName(String query) {
        if (query.contains(":")) {
            return query.split(":", 2)[1];
        }
        return query;
    }

    public Set<String> getNamespaces() {
        return Collections.unmodifiableSet(presets.keySet());
    }
    
    public Map<String, BrushPreset> getPresetsInNamespace(String namespace) {
        return Collections.unmodifiableMap(presets.getOrDefault(namespace, Collections.emptyMap()));
    }
}
