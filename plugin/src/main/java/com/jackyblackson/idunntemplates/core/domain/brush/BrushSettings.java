package com.jackyblackson.idunntemplates.core.domain.brush;

import com.jackyblackson.idunntemplates.core.domain.PlayerSession;
import com.jackyblackson.idunntemplates.core.set.TemplateSet;

public class BrushSettings implements Cloneable {
    
    private TemplateSet content;
    private RotationMode rotation = RotationMode.RANDOM;
    private FlipMode flipX = FlipMode.RANDOM;
    private FlipMode flipZ = FlipMode.RANDOM;
    private boolean noAir = true;
    private boolean emptyOnly = true;
    private Long collectionId = null;
    
    private transient PlayerSession.NextPlacement nextPlacement;
    private transient boolean isFetchingNext = false;

    public BrushSettings() {
        this.content = new TemplateSet();
    }
    
    public BrushSettings(BrushSettings other) {
        this.rotation = other.rotation;
        this.flipX = other.flipX;
        this.flipZ = other.flipZ;
        this.noAir = other.noAir;
        this.emptyOnly = other.emptyOnly;
        this.collectionId = other.collectionId;
        
        // Deep copy content
        this.content = new TemplateSet();
        if (other.content != null) {
            for (var src : other.content.getSources()) {
                this.content.addSource(src.getPath(), src.getWeight());
            }
        }
        // nextPlacement is transient, start null
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
    
    public Long getCollectionId() {
        return collectionId;
    }
    
    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }
    
    public boolean isFetchingNext() {
        return isFetchingNext;
    }
    
    public void setFetchingNext(boolean fetchingNext) {
        this.isFetchingNext = fetchingNext;
    }
    
    public PlayerSession.NextPlacement getNextPlacement() {
        return nextPlacement;
    }

    public void setNextPlacement(PlayerSession.NextPlacement nextPlacement) {
        this.nextPlacement = nextPlacement;
    }
    
    @Override
    public BrushSettings clone() {
        return new BrushSettings(this);
    }

    public enum RotationMode {
        FIXED_0, FIXED_90, FIXED_180, FIXED_270, RANDOM, INHERIT
    }

    public enum FlipMode {
        TRUE, FALSE, RANDOM, INHERIT
    }
}
