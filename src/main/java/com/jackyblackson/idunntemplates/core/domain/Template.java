package com.jackyblackson.idunntemplates.core.domain;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.util.TransformUtil;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;

import javax.sound.sampled.Clip;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Template {
    private final String name;
    private final String path; // Relative path for display/commands
    private final File directory;
    private final TemplateMetadata metadata;

    private final UUID id;

    private final Map<String, Clipboard> cachedClipboard = new HashMap<>();
    
    // Cache for particle effects
    private org.bukkit.util.Vector cachedOriginOffset = null;

    public boolean isLocked() { return this.getMetadata().isLocked(); }

    public org.bukkit.util.Vector getOriginOffset() {
        if (cachedOriginOffset != null) return cachedOriginOffset;
        
        TemplateVersion latest = getLatestVersion();
        if (latest == null) return new org.bukkit.util.Vector(0,0,0);
        
        Clipboard clip = getClipboard(latest.getVersionId());
        if (clip == null) return new org.bukkit.util.Vector(0,0,0);
        
        com.sk89q.worldedit.math.BlockVector3 min = clip.getRegion().getMinimumPoint();
        com.sk89q.worldedit.math.BlockVector3 origin = clip.getOrigin();
        
        cachedOriginOffset = new org.bukkit.util.Vector(
            min.x() - origin.x(),
            min.y() - origin.y(),
            min.z() - origin.z()
        );
        return cachedOriginOffset;
    }

    public Clipboard getClipboard(String versionId) {
        if (this.cachedClipboard.containsKey(versionId)) {
            return this.cachedClipboard.get(versionId);
        }
        File file = new File(this.directory, versionId + ".schem");
        if (!file.exists()) return null;

        ClipboardFormat format = ClipboardFormats.findByAlias("schem");
        if (format == null) format = ClipboardFormats.findByAlias("sponge");

        try {
            assert format != null;
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                var result = reader.read();
                this.cachedClipboard.put(versionId, result);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Clipboard getClipboard(String versionId, int rotation, boolean flipX, boolean flipY, boolean flipZ) {
        return TransformUtil.transformClipboard(
                this.getClipboard(versionId),
                rotation,
                flipX,
                flipY,
                flipZ
        );
    }

    public Template(String name, String path, File directory, TemplateMetadata metadata) {
        this.name = name;
        this.path = path;
        this.directory = directory;
        this.metadata = metadata;
        this.id = metadata.getTemplateId();
    }
    
    public String getPath() {
        return path;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public File getDirectory() {
        return directory;
    }

    public TemplateMetadata getMetadata() {
        return metadata;
    }
    
    public TemplateVersion getLatestVersion() {
        if (metadata.getVersions().isEmpty()) return null;
        return metadata.getVersions().get(metadata.getVersions().size() - 1);
    }
}
