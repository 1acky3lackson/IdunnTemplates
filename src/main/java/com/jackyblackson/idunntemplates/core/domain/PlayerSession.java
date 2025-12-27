package com.jackyblackson.idunntemplates.core.domain;

import java.util.UUID;

public class PlayerSession {
    
    private final UUID playerId;
    private PlayerPreference preference;
    private NextPlacement nextPlacement;

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
    
    public NextPlacement getNextPlacement() {
        return nextPlacement;
    }
    
    public void setNextPlacement(NextPlacement nextPlacement) {
        this.nextPlacement = nextPlacement;
    }
    
    public static class NextPlacement {
        private final Template template;
        private final int rotation;
        private final boolean flipX;
        private final boolean flipZ;
        
        public NextPlacement(Template template, int rotation, boolean flipX, boolean flipZ) {
            this.template = template;
            this.rotation = rotation;
            this.flipX = flipX;
            this.flipZ = flipZ;
        }

        public Template getTemplate() {
            return template;
        }

        public int getRotation() {
            return rotation;
        }

        public boolean isFlipX() {
            return flipX;
        }

        public boolean isFlipZ() {
            return flipZ;
        }
    }
}
