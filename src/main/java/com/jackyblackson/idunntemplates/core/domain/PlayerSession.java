package com.jackyblackson.idunntemplates.core.domain;

import java.util.UUID;

public class PlayerSession {
    
    private final UUID playerId;
    private PlayerPreference preference;

    public PlayerSession(UUID playerId, PlayerPreference preference) {
        this.playerId = playerId;
        this.preference = preference;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public PlayerPreference getPreference() {
        return preference;
    }

    public void setPreference(PlayerPreference preference) {
        this.preference = preference;
    }
}
