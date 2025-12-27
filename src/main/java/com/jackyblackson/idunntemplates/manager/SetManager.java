package com.jackyblackson.idunntemplates.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SetManager {

    private final File setsFolder;
    private final Logger logger;
    private final Gson gson;
    private final SessionManager sessionManager;

    // Cache: Namespace -> (SetName -> TemplateSet)
    private final Map<String, Map<String, TemplateSet>> globalSets = new ConcurrentHashMap<>();

    public SetManager(File setsFolder, SessionManager sessionManager, Logger logger) {
        this.setsFolder = setsFolder;
        this.sessionManager = sessionManager;
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        if (!setsFolder.exists()) {
            setsFolder.mkdirs();
        }
        
        loadGlobalSets();
    }

    private void loadGlobalSets() {
        // Load all .json files in setsFolder
        File[] files = setsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;
        
        for (File f : files) {
            String namespace = f.getName().replace(".json", "");
            try (FileReader reader = new FileReader(f)) {
                Type type = new TypeToken<Map<String, TemplateSet>>(){}.getType();
                Map<String, TemplateSet> sets = gson.fromJson(reader, type);
                if (sets != null) {
                    globalSets.put(namespace, new ConcurrentHashMap<>(sets));
                }
            } catch (IOException e) {
                logger.severe("Failed to load set namespace " + namespace + ": " + e.getMessage());
            }
        }
    }
    
    public void saveNamespace(String namespace) {
        if (!globalSets.containsKey(namespace)) return;
        
        File f = new File(setsFolder, namespace + ".json");
        try (FileWriter writer = new FileWriter(f)) {
             gson.toJson(globalSets.get(namespace), writer);
        } catch (IOException e) {
             logger.severe("Failed to save set namespace " + namespace + ": " + e.getMessage());
        }
    }

    /**
     * Resolves a set by "namespace:name" or "name".
     * Namespace logic:
     * 1. If explicit "ns:name", look in ns.
     * 2. If just "name" (namespace null/empty), look in "player.<name>" (implied context?),
     *    Wait, context is needed.
     *    The requirement says: "If no namespace provided, try player.<name>, then global."
     *    But here we don't know who "player" is unless passed.
     *    So this method should take an optional context player.
     */
    public TemplateSet getSet(String rawName, String contextPlayerName) {
        String namespace = null;
        String name = rawName;
        
        if (rawName.contains(":")) {
            String[] parts = rawName.split(":", 2);
            namespace = parts[0];
            name = parts[1];
        }
        
        if (namespace != null) {
            return getSetExact(namespace, name);
        }
        
        // No namespace provided
        // 1. Try player.<contextPlayerName>
        if (contextPlayerName != null) {
            TemplateSet set = getSetExact("player." + contextPlayerName, name);
            if (set != null) return set;
        }
        
        // 2. Try global
        return getSetExact("global", name);
    }
    
    public TemplateSet getSetExact(String namespace, String name) {
        if (namespace.startsWith("player.")) {
            String playerName = namespace.substring(7);
            // Load from SessionManager
            UUID uuid = getUUID(playerName);
            if (uuid == null) return null; // Player never played?
            
            var pref = sessionManager.getOrLoadPreference(uuid);
            if (pref == null) return null;
            return pref.getSavedSets().get(name);
        } else {
            // Check loaded global sets
            Map<String, TemplateSet> nsSets = globalSets.get(namespace);
            if (nsSets == null) return null;
            return nsSets.get(name);
        }
    }
    
    public void saveGlobalSet(String namespace, String name, TemplateSet set) {
        if (namespace.startsWith("player.")) {
             throw new IllegalArgumentException("Cannot save to player namespace via global manager directly.");
        }
        globalSets.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>()).put(name, set);
        saveNamespace(namespace);
    }
    
    public Map<String, TemplateSet> getGlobalNamespace(String namespace) {
        return Collections.unmodifiableMap(globalSets.getOrDefault(namespace, Collections.emptyMap()));
    }
    
    public java.util.Set<String> getLoadedNamespaces() {
        return Collections.unmodifiableSet(globalSets.keySet());
    }
    
    private UUID getUUID(String name) {
        // Try online
        org.bukkit.entity.Player p = Bukkit.getPlayer(name);
        if (p != null) return p.getUniqueId();
        // Try offline
        OfflinePlayer op = Bukkit.getOfflinePlayer(name);
        if (op.hasPlayedBefore()) return op.getUniqueId();
        return null;
    }
}
