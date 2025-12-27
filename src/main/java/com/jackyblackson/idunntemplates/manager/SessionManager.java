package com.jackyblackson.idunntemplates.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jackyblackson.idunntemplates.core.domain.PlayerPreference;
import com.jackyblackson.idunntemplates.core.domain.PlayerSession;
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

    public SessionManager(File dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerSession getSession(UUID playerId) {
        return sessions.get(playerId);
    }
    
    public void saveSession(UUID playerId) {
        PlayerSession session = sessions.get(playerId);
        if (session != null) {
            savePreference(playerId, session.getPreference());
        }
    }

    private PlayerPreference loadPreference(UUID playerId) {
        File file = new File(dataFolder, playerId.toString() + ".json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, PlayerPreference.class);
            } catch (IOException e) {
                logger.severe("Failed to load preference for " + playerId + ": " + e.getMessage());
            }
        }
        return new PlayerPreference(); // Default
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
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID pid = event.getPlayer().getUniqueId();
        // Save on quit
        saveSession(pid);
        sessions.remove(pid);
    }
}
