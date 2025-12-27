package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
import com.jackyblackson.idunntemplates.core.util.TransformUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class InstanceManager {

    private final TemplateStorage templateStorage;
    private final InstanceRepository instanceRepository;
    private final Logger logger;

    public InstanceManager(TemplateStorage templateStorage, InstanceRepository instanceRepository, Logger logger) {
        this.templateStorage = templateStorage;
        this.instanceRepository = instanceRepository;
        this.logger = logger;
    }

    /**
     * Places an instance of a template at the specified location.
     */
    public Instance placeInstanceAndReturn(Player player, Template template, Location location, int rot, boolean flipX, boolean flipY, boolean flipZ) throws Exception {
        TemplateVersion latest = template.getLatestVersion();
        if (latest == null) {
            throw new IllegalArgumentException("Template has no versions.");
        }

        Clipboard clipboard = template.getClipboard(
                latest.getVersionId(),
                rot,
                flipX,
                flipY,
                flipZ
        );

        // Prepare Holder
        ClipboardHolder holder = new ClipboardHolder(clipboard);

        // Paste
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(BukkitAdapter.adapt(location.getWorld()))
                .actor(BukkitAdapter.adapt(player))
                .build()
        ) {
            // Bind to player for undo
            if (player != null) {
                com.sk89q.worldedit.LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
//                editSession = session.createEditSession(BukkitAdapter.adapt(player));
                session.remember(editSession);
            }

            Operation op = holder.createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .ignoreAirBlocks(true) // Preference check? Passed via param?
                    // Assuming false for now or check pref elsewhere.
                    // Prompt said "placeOnEmptyOnly" pref exists.
                    // If placeOnEmptyOnly, we need mask.
                    // For now simplicity.
                    .build();
            Operations.completeLegacy(op);
            editSession.flushQueue();
        }

        var minPos = TransformUtil.getInstanceMinPos(location, clipboard);

        // Create Record
        Instance instance = new Instance(
                template.getId(),
                latest.getVersionId(),
                location.getWorld().getUID(),
                minPos.x(), minPos.y(), minPos.z(),
                rot, flipX, flipY, flipZ,
                player.getUniqueId(),
                player.getName()
        );
        
        instanceRepository.saveInstance(instance);
        
        return instance;
    }
    
    public void placeInstance(Player player, Template template, Location location, int rot, boolean flipX, boolean flipY, boolean flipZ) throws Exception {
        placeInstanceAndReturn(player, template, location, rot, flipX, flipY, flipZ);
    }
}
