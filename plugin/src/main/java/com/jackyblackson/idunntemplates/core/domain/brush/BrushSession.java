package com.jackyblackson.idunntemplates.core.domain.brush;

import java.util.HashMap;
import java.util.Map;

public class BrushSession {
    
    private Map<String, BrushSettings> channels = new HashMap<>();

    public BrushSession() {}

    public Map<String, BrushSettings> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, BrushSettings> channels) {
        this.channels = channels;
    }
    
    public BrushSettings getSettings(String channel) {
        return channels.get(channel);
    }
    
    public void setSettings(String channel, BrushSettings settings) {
        channels.put(channel, settings);
    }
    
    public void removeSettings(String channel) {
        channels.remove(channel);
    }
}
