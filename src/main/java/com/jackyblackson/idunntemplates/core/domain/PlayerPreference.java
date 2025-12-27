package com.jackyblackson.idunntemplates.core.domain;

import org.bukkit.Material;

public class PlayerPreference {
    
    private String wandMaterialName;
    private boolean placeOnEmptyOnly;

    public PlayerPreference() {
        // Defaults
        this.wandMaterialName = Material.NETHERITE_HOE.name();
        this.placeOnEmptyOnly = true;
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
}
