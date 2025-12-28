package com.jackyblackson.idunntemplates.core.domain.brush;

import com.jackyblackson.idunntemplates.core.set.TemplateSet;

public class BrushSettings {
    
    private TemplateSet content;
    private RotationMode rotation = RotationMode.FIXED_0;
    private FlipMode flipX = FlipMode.FALSE;
    private FlipMode flipZ = FlipMode.FALSE;
    private boolean noAir = true;
    private boolean emptyOnly = true;

    public BrushSettings() {
        this.content = new TemplateSet();
    }

    public TemplateSet getContent() {
        return content;
    }

    public void setContent(TemplateSet content) {
        this.content = content;
    }

    public RotationMode getRotation() {
        return rotation;
    }

    public void setRotation(RotationMode rotation) {
        this.rotation = rotation;
    }

    public FlipMode getFlipX() {
        return flipX;
    }

    public void setFlipX(FlipMode flipX) {
        this.flipX = flipX;
    }

    public FlipMode getFlipZ() {
        return flipZ;
    }

    public void setFlipZ(FlipMode flipZ) {
        this.flipZ = flipZ;
    }

    public boolean isNoAir() {
        return noAir;
    }

    public void setNoAir(boolean noAir) {
        this.noAir = noAir;
    }

    public boolean isEmptyOnly() {
        return emptyOnly;
    }

    public void setEmptyOnly(boolean emptyOnly) {
        this.emptyOnly = emptyOnly;
    }

    public enum RotationMode {
        FIXED_0, FIXED_90, FIXED_180, FIXED_270, RANDOM, INHERIT
    }

    public enum FlipMode {
        TRUE, FALSE, RANDOM, INHERIT
    }
}
