package com.jackyblackson.idunntemplates.util;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.util.TransformUtil;
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

public class EntityHelper {

    private static final Map<String, Clipboard> cachedClipboard = new HashMap<>();
    private static final Map<UUID, org.bukkit.util.Vector> cachedOriginOffset = new HashMap<>();

    // --- Template Helpers ---

    public static File getDirectory(Template template) {
        File templatesRoot = new File(IdunnTemplates.getInstance().getStorageRootFolder(), "templates");
        return new File(templatesRoot, template.getPath());
    }

    public static Clipboard getClipboard(Template template, String versionId) {
        String cacheKey = template.getId() + ":" + versionId;
        if (cachedClipboard.containsKey(cacheKey)) {
            return cachedClipboard.get(cacheKey);
        }
        File file = new File(getDirectory(template), versionId + ".schem");
        if (!file.exists()) return null;

        ClipboardFormat format = ClipboardFormats.findByAlias("schem");
        if (format == null) format = ClipboardFormats.findByAlias("sponge");

        try {
            assert format != null;
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                var result = reader.read();
                cachedClipboard.put(cacheKey, result);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Clipboard getClipboard(Template template, String versionId, int rotation, boolean flipX, boolean flipY, boolean flipZ) {
        return TransformUtil.transformClipboard(getClipboard(template, versionId), rotation, flipX, flipY, flipZ);
    }

    public static org.bukkit.util.Vector getOriginOffset(Template template) {
        if (cachedOriginOffset.containsKey(template.getId())) return cachedOriginOffset.get(template.getId());

        TemplateVersion latest = template.getLatestVersion();
        if (latest == null) return new org.bukkit.util.Vector(0,0,0);
        Clipboard clip = getClipboard(template, latest.getVersionId());
        if (clip == null) return new org.bukkit.util.Vector(0,0,0);

        BlockVector3 min = clip.getRegion().getMinimumPoint();
        BlockVector3 origin = clip.getOrigin();

        org.bukkit.util.Vector offset = new org.bukkit.util.Vector(min.x() - origin.x(), min.y() - origin.y(), min.z() - origin.z());
        cachedOriginOffset.put(template.getId(), offset);
        return offset;
    }

    // --- Instance Helpers ---

    public static Template getTemplate(Instance instance) {
        if (IdunnTemplates.getInstance() == null) return null;
        return IdunnTemplates.getInstance().getTemplateManager().getTemplate(instance.getTemplateId());
    }

    public static Template getEmbeddedTemplate(Instance instance) {
        if (instance.isWild() || IdunnTemplates.getInstance() == null) {
            return null;
        }
        return IdunnTemplates.getInstance().getTemplateManager().getTemplate(instance.getEmbeddedInTemplateId());
    }

    public static boolean canUpdate(Instance instance) {
        return (instance.isWild()) || ((instance.getEmbeddedInTemplateId() != null) && (!getEmbeddedTemplate(instance).isLocked()));
    }
}
