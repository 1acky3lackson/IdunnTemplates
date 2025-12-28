package com.jackyblackson.idunntemplates.core.domain.brush;

import java.util.HashMap;
import java.util.Map;

public class BrushPreset {
    
    private String name;
    private String creator;
    private String description;
    private Map<String, BrushSettings> channels = new HashMap<>();

    public BrushPreset() {}

    public BrushPreset(String name, String creator, String description, Map<String, BrushSettings> channels) {
        this.name = name;
        this.creator = creator;
        this.description = description;
        this.channels = channels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, BrushSettings> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, BrushSettings> channels) {
        this.channels = channels;
    }
}
