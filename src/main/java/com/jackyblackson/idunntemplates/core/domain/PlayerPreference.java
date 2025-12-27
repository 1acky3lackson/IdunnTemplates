package com.jackyblackson.idunntemplates.core.domain;

import com.jackyblackson.idunntemplates.core.set.TemplateSet;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class PlayerPreference {
    
    private String wandMaterialName;
    private boolean placeOnEmptyOnly;
    
    private TemplateSet currentSet = new TemplateSet();
    private Map<String, TemplateSet> savedSets = new HashMap<>();

    public PlayerPreference() {
        // Defaults
        this.wandMaterialName = Material.GOLDEN_HOE.name();
        this.placeOnEmptyOnly = false;
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
    
    public TemplateSet getCurrentSet() {
        return currentSet;
    }
    
    public void setCurrentSet(TemplateSet currentSet) {
        this.currentSet = currentSet;
    }
    
    public Map<String, TemplateSet> getSavedSets() {
        return savedSets;
    }
}
