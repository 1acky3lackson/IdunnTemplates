package com.jackyblackson.idunntemplates.core.domain;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.store.dao.InstanceDao;
import com.jackyblackson.idunntemplates.core.store.dao.TemplateDao;
import com.jackyblackson.idunntemplates.core.util.TransformUtil;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DatabaseTable(tableName = "idunn_templates", daoClass = TemplateDao.class)
public class Template {

    @DatabaseField(id = true)
    private UUID id;

    @DatabaseField(unique = true, canBeNull = false)
    private String path;

    @DatabaseField(index = true)
    private String name;

    // 外键关联：TemplateMetadata
    // foreignAutoRefresh = true 会自动加载 metadata 数据
    // foreignAutoCreate = true 会在保存 template 时自动保存 metadata
    @DatabaseField(foreign = true, foreignAutoRefresh = true, foreignAutoCreate = true, columnName = "metadata_id")
    private TemplateMetadata metadata;

    // --- Transient (Runtime Cache) ---
    private final transient Map<String, Clipboard> cachedClipboard = new HashMap<>();
    private transient org.bukkit.util.Vector cachedOriginOffset = null;

    // ORM required
    public Template() {}

    public Template(String name, String path, TemplateMetadata metadata) {
        this.name = name;
        this.path = path;
        this.metadata = metadata;
        this.id = metadata.getTemplateId();
    }

    public Template(String name, String filePath, File templateDir, TemplateMetadata metadata) {
    }

    // --- Business Logic ---

    public File getDirectory() {
        File templatesRoot = new File(IdunnTemplates.getInstance().getDataFolder(), "templates");
        return new File(templatesRoot, path);
    }

    public Clipboard getClipboard(String versionId) {
        if (this.cachedClipboard.containsKey(versionId)) {
            return this.cachedClipboard.get(versionId);
        }
        File file = new File(this.getDirectory(), versionId + ".schem");
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

    // 代理方法
    public Clipboard getClipboard(String versionId, int rotation, boolean flipX, boolean flipY, boolean flipZ) {
        return TransformUtil.transformClipboard(this.getClipboard(versionId), rotation, flipX, flipY, flipZ);
    }

    public boolean isLocked() { return getMetadata().isLocked(); }

    public org.bukkit.util.Vector getOriginOffset() {
        if (cachedOriginOffset != null) return cachedOriginOffset;
        TemplateVersion latest = getLatestVersion();
        if (latest == null) return new org.bukkit.util.Vector(0,0,0);
        Clipboard clip = getClipboard(latest.getVersionId());
        if (clip == null) return new org.bukkit.util.Vector(0,0,0);

        BlockVector3 min = clip.getRegion().getMinimumPoint();
        BlockVector3 origin = clip.getOrigin();

        cachedOriginOffset = new org.bukkit.util.Vector(min.x() - origin.x(), min.y() - origin.y(), min.z() - origin.z());
        return cachedOriginOffset;
    }

    // --- Getters ---
    public UUID getId() { return id; }
    public String getPath() { return path; }
    public String getName() { return name; }
    public TemplateMetadata getMetadata() { return metadata; }
    public void setMetadata(TemplateMetadata metadata) { this.metadata = metadata; }

    public TemplateVersion getLatestVersion() {
        if (metadata.getVersions().isEmpty()) return null;
        return metadata.getVersions().get(metadata.getVersions().size() - 1);
    }
}