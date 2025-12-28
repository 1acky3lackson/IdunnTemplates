package com.jackyblackson.idunntemplates.core.domain;

import com.jackyblackson.idunntemplates.core.domain.brush.BrushSession;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerPreference {
    
    private String wandMaterialName;
    private boolean placeOnEmptyOnly;
    private List<String> emptyBlocks;

    // Effect Preferences
    private boolean particleTemplateBoundaries = true;
    private boolean particleInstanceBoundaries = true;
    private boolean particleWand = true;

    // BossBar Preferences
    private boolean bossBarTemplate = true;
    private boolean bossBarInstance = true;
    private boolean bossBarSet = true;
    
    // Action Bar Preference
    private boolean showActionBar = true;
    
    private TemplateSet currentSet = new TemplateSet();
    private Map<String, TemplateSet> savedSets = new HashMap<>();
    
    // Key: Material Name, Value: BrushSession
    private Map<String, BrushSession> boundBrushes = new HashMap<>();

    public PlayerPreference() {
        // Defaults
        this.wandMaterialName = Material.GOLDEN_HOE.name();
        this.placeOnEmptyOnly = false;
        // emptyBlocks initialized lazily or via setter
    }

    public boolean isShowActionBar() {
        return showActionBar;
    }

    public void setShowActionBar(boolean showActionBar) {
        this.showActionBar = showActionBar;
    }

    public String getWandMaterialName() {
        return wandMaterialName;
    }

    public void setWandMaterialName(String wandMaterialName) {
        this.wandMaterialName = wandMaterialName;
    }

    public boolean isPlaceOnEmptyOnly() {
        return placeOnEmptyOnly;
    }

    public void setPlaceOnEmptyOnly(boolean placeOnEmptyOnly) {
        this.placeOnEmptyOnly = placeOnEmptyOnly;
    }
    
    public List<String> getEmptyBlocks() {
        if (emptyBlocks == null) {
            emptyBlocks = new ArrayList<>();
        }
        return emptyBlocks;
    }
    
    public void setEmptyBlocks(List<String> emptyBlocks) {
        this.emptyBlocks = emptyBlocks;
    }

    public boolean isParticleTemplateBoundaries() {
        return particleTemplateBoundaries;
    }

    public void setParticleTemplateBoundaries(boolean particleTemplateBoundaries) {
        this.particleTemplateBoundaries = particleTemplateBoundaries;
    }

    public boolean isParticleInstanceBoundaries() {
        return particleInstanceBoundaries;
    }

    public void setParticleInstanceBoundaries(boolean particleInstanceBoundaries) {
        this.particleInstanceBoundaries = particleInstanceBoundaries;
    }

    public boolean isParticleWand() {
        return particleWand;
    }

    public void setParticleWand(boolean particleWand) {
        this.particleWand = particleWand;
    }

    public boolean isBossBarTemplate() {
        return bossBarTemplate;
    }

    public void setBossBarTemplate(boolean bossBarTemplate) {
        this.bossBarTemplate = bossBarTemplate;
    }

    public boolean isBossBarInstance() {
        return bossBarInstance;
    }

    public void setBossBarInstance(boolean bossBarInstance) {
        this.bossBarInstance = bossBarInstance;
    }

    public boolean isBossBarSet() {
        return bossBarSet;
    }

    public void setBossBarSet(boolean bossBarSet) {
        this.bossBarSet = bossBarSet;
    }

    public TemplateSet getCurrentSet() {
        return currentSet;
    }
    
    public void setCurrentSet(TemplateSet currentSet) {
        this.currentSet = currentSet;
    }
    
    public Map<String, TemplateSet> getSavedSets() {
        return savedSets;
    }
    
    public Map<String, BrushSession> getBoundBrushes() {
        return boundBrushes;
    }
    
    public void setBoundBrushes(Map<String, BrushSession> boundBrushes) {
        this.boundBrushes = boundBrushes;
    }
}
