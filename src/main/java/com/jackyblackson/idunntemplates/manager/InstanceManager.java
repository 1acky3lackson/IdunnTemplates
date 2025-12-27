package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.store.TemplateStorage;
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
    public void placeInstance(Player player, Template template, Location location, int rotationY, boolean flipX, boolean flipY, boolean flipZ) throws Exception {
        TemplateVersion latestVersion = template.getLatestVersion();
        if (latestVersion == null) {
            throw new IllegalStateException("Template has no versions.");
        }

        // 2. Load Schematic for specific variation
        // Format: <versionId>_<rot>_<flipX>_<flipZ>.schem in 'variations' subfolder
        File variationsDir = new File(template.getDirectory(), "variations");
        String filename = latestVersion.getVersionId() + "_" + rotationY + "_" + flipX + "_" + flipZ + ".schem";
        File schemFile = new File(variationsDir, filename);
        
        if (!schemFile.exists()) {
             // Fallback for backward compatibility or error?
             // Prompt asked to simplify logic, so we assume variations exist.
             throw new java.io.FileNotFoundException("Variation schematic not found: " + filename + ". Please re-commit the template to generate variations.");
        }

        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
        if (format == null) format = ClipboardFormats.findByAlias("schem");
        
        try (ClipboardReader reader = format.getReader(new FileInputStream(schemFile))) {
            clipboard = reader.read();
        }

        // 3. Paste to World (No transformation needed as schematic is pre-transformed)
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
            ClipboardHolder holder = new ClipboardHolder(clipboard);
            // No transform applied here!
            
            Operation operation = holder
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .ignoreAirBlocks(false) 
                    .build();
            
            Operations.complete(operation);
        } catch (WorldEditException e) {
            throw new RuntimeException("WorldEdit paste failed: " + e.getMessage(), e);
        }

        var clipboardOffset = clipboard.getRegion().getMinimumPoint();
        var clipboardOrigin = clipboard.getOrigin();
        var instanceRoot = BlockVector3.at(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        ).subtract(
                BlockVector3.at(
                        clipboardOrigin.x(),
                        clipboardOrigin.y(),
                        clipboardOrigin.z()
                ).subtract(
                        clipboardOffset
                )
        );

        // 4. Create Instance Record
        Instance instance = new Instance(
                template.getId(),
                latestVersion.getVersionId(),
                location.getWorld().getUID(),
                instanceRoot.x(),
                instanceRoot.y(),
                instanceRoot.z(),
                rotationY,
                flipX, flipY, flipZ,
                player.getUniqueId(),
                player.getName()
        );
        
        // 5. Save Instance
        instanceRepository.saveInstance(instance);
        logger.info("Instance placed and saved: " + instance.getId());
    }
}
