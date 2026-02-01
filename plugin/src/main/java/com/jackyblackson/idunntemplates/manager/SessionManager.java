package com.jackyblackson.idunntemplates.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jackyblackson.idunntemplates.core.domain.PlayerPreference;
import com.jackyblackson.idunntemplates.core.domain.PlayerSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SessionManager implements Listener {

    private final File dataFolder;
    private final Logger logger;
    private final Gson gson;
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final java.util.List<String> defaultEmptyBlocks;
    
    // Dependencies for generating placements
    private TemplateManager templateManager;
    private SetManager setManager;

    public SessionManager(File dataFolder, Logger logger, java.util.List<String> defaultEmptyBlocks) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.defaultEmptyBlocks = defaultEmptyBlocks;
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }
    
    public void setSetManager(SetManager setManager) {
        this.setManager = setManager;
    }

    public PlayerSession getSession(UUID playerId) {
        return sessions.get(playerId);
    }
    
    public void regenerateNextPlacement(UUID playerId) {
        PlayerSession session = sessions.get(playerId);
        if (session == null || templateManager == null || setManager == null) return;
        
        var set = session.getPreference().getCurrentSet();
        if (set == null || set.getSources().isEmpty()) {
            session.setNextPlacement(null);
            return;
        }
        
        // Resolve random
        // Need name to set resolver
        String playerName = org.bukkit.Bukkit.getOfflinePlayer(playerId).getName(); 
        // Might be null if offline, but session implies online/active.
        // Assuming online for simplicity or pass name.
        
        com.jackyblackson.idunntemplates.core.domain.Template t = set.pickRandom(templateManager, name -> setManager.getSet(name, playerName), false);
        if (t == null) {
            session.setNextPlacement(null);
            return;
        }
        
        int rot = set.resolveRotation();
        boolean fx = set.resolveFlipX();
        boolean fz = set.resolveFlipZ();
        
        // Spec says "flipy" in TemplateSet maps to Z?
        // SetsPlaceCommand used:
        // fz = fy; fy = false;
        // TemplateSet has flipX, flipZ (mapped to flipY field string).
        // Let's check TemplateSet.java to be sure what getFlipZ returns.
        // It returns flipZ.
        // So here we use flipZ.
        
        session.setNextPlacement(new PlayerSession.NextPlacement(t, rot, fx, fz));
    }
    
    public PlayerPreference getOrLoadPreference(UUID playerId) {
        if (sessions.containsKey(playerId)) {
            return sessions.get(playerId).getPreference();
        }
        return loadPreference(playerId);
    }
    
    public void saveSession(UUID playerId) {
        PlayerSession session = sessions.get(playerId);
        if (session != null) {
            savePreference(playerId, session.getPreference());
        }
    }

    private PlayerPreference loadPreference(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        PlayerPreference pref = null;
        File file = new File(dataFolder, playerId.toString() + ".json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                pref = gson.fromJson(reader, PlayerPreference.class);
                if (player != null) {
                    pref.setPlayerName(player.getName());
                    savePreference(playerId, pref);
                }
            } catch (IOException e) {
                logger.severe("Failed to load preference for " + playerId + ": " + e.getMessage());
            }
        }
        
        if (pref == null) {
            pref = new PlayerPreference();
            if (player != null) {
                pref.setPlayerName(player.getName());
            }
        }
        
        // Apply defaults if empty (and not just created empty, but if it was missing or new)
        // If loaded but emptyBlocks is null/empty, should we fill it?
        // Maybe checking if it's null is safer to distinguish "user cleared it" vs "never set".
        // But gson initializes to null if missing.
        // My PlayerPreference getter lazy inits to empty list now.
        // Let's set it if empty/null.
        if (pref.getEmptyBlocks().isEmpty()) {
            pref.setEmptyBlocks(new java.util.ArrayList<>(defaultEmptyBlocks));
        }
        
        return pref;
    }

    private void savePreference(UUID playerId, PlayerPreference pref) {
        File file = new File(dataFolder, playerId.toString() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(pref, writer);
        } catch (IOException e) {
            logger.severe("Failed to save preference for " + playerId + ": " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID pid = event.getPlayer().getUniqueId();
        PlayerPreference pref = loadPreference(pid);
        PlayerSession session = new PlayerSession(pid, pref);
        sessions.put(pid, session);
        
        // Regenerate state
        regenerateNextPlacement(pid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID pid = event.getPlayer().getUniqueId();
        // Save on quit
        saveSession(pid);
        sessions.remove(pid);
    }
}
