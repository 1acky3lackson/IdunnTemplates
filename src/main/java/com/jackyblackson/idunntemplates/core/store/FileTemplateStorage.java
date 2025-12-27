package com.jackyblackson.idunntemplates.core.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileTemplateStorage implements TemplateStorage {

    private final File rootDirectory;
    private final Gson gson;

    public FileTemplateStorage(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        if (!this.rootDirectory.exists()) {
            this.rootDirectory.mkdirs();
        }
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public Template saveNewTemplate(String path, String name, TemplateMetadata metadata, Clipboard initialClipboard, TemplateVersion initialVersion) throws IOException {
        // Construct the template directory: root + path + _name
        File parentDir = new File(rootDirectory, path);
        File templateDir = new File(parentDir, name);

        if (templateDir.exists()) {
            throw new IOException("Template already exists at " + templateDir.getPath());
        }
        if (!templateDir.mkdirs()) {
            throw new IOException("Failed to create directory " + templateDir.getPath());
        }

        // Add version to metadata
        metadata.addVersion(initialVersion);

        // Save metadata
        saveMetadataFile(templateDir, metadata);

        // Save schematic and variations
        String filePath = path.isEmpty() ? name : path + "/" + name;
        saveTemplateVersion(new Template(name, filePath, templateDir, metadata), initialVersion, initialClipboard);

        return new Template(name, filePath, templateDir, metadata);
    }

    @Override
    public Template loadTemplate(String path) throws IOException {
        File templateDir = new File(rootDirectory, path);
        if (!templateDir.exists() || !templateDir.isDirectory()) {
            return null;
        }
        
        File metadataFile = new File(templateDir, "metadata.json");
        if (!metadataFile.exists()) {
            throw new IOException("Missing metadata.json in " + templateDir.getPath());
        }

        try (FileReader reader = new FileReader(metadataFile)) {
            TemplateMetadata metadata = gson.fromJson(reader, TemplateMetadata.class);
            // Derive name from directory name (remove leading _)
            String dirName = templateDir.getName();
            String name = dirName.startsWith("_") ? dirName.substring(1) : dirName;

            IdunnTemplates.getInstance().getLogger().info(String.format(
                    "Load idunn template [%s] from %s with %d version(s)",
                    name,
                    dirName,
                    metadata.getVersions().size()
            ));
            
            return new Template(name, path, templateDir, metadata);
        }
    }

    @Override
    public java.util.List<Template> loadAllTemplates() throws IOException {
        java.util.List<Template> templates = new java.util.ArrayList<>();
        scanForTemplates(rootDirectory, templates);
        return templates;
    }

    private void scanForTemplates(File directory, java.util.List<Template> templates) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // Check if it's a template directory (starts with "_" and has metadata.json)
                if (new File(file, "metadata.json").exists()) {
                    try {
                        String relativePath = rootDirectory.toURI().relativize(file.toURI()).getPath();
                        if (relativePath.endsWith("/")) {
                            relativePath = relativePath.substring(0, relativePath.length() - 1);
                        }
                        
                        Template template = loadTemplate(relativePath);
                        if (template != null) {
                            templates.add(template);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    scanForTemplates(file, templates);
                }
            }
        }
    }

    @Override
    public void updateMetadata(Template template) throws IOException {
        saveMetadataFile(template.getDirectory(), template.getMetadata());
    }

    @Override
    public void saveTemplateVersion(Template template, TemplateVersion version, Clipboard clipboard) throws IOException {
        // Save base schematic
        saveSchematicFile(template.getDirectory(), version.getVersionId(), clipboard);
        
        // Save variations
        saveVariations(template.getDirectory(), version.getVersionId(), clipboard);

        // Update metadata
        if (!template.getMetadata().getVersions().contains(version)) {
            template.getMetadata().addVersion(version);
        }
        updateMetadata(template);
    }

    private void saveMetadataFile(File templateDir, TemplateMetadata metadata) throws IOException {
        File file = new File(templateDir, "metadata.json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(metadata, writer);
        }
    }

    private void saveSchematicFile(File dir, String filenameNoExt, Clipboard clipboard) throws IOException {
        File file = new File(dir, filenameNoExt + ".schem");
        ClipboardFormat format = ClipboardFormats.findByAlias("schem");
        if (format == null) throw new IOException("Schematic format 'schem' not found.");

        try (ClipboardWriter writer = format.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        }
    }
    
    private void saveVariations(File templateDir, String versionId, Clipboard original) throws IOException {
        int[] rotations = {0, 90, 180, 270};
        boolean[] flips = {false, true};

        for (int rot : rotations) {
            for (boolean flipX : flips) {
                for (boolean flipZ : flips) {
                    saveSingleVariation(templateDir, versionId, original, rot, flipX, flipZ);
                }
            }
        }
    }
    
    private void saveSingleVariation(File templateDir, String versionId, Clipboard original, int rot, boolean flipX, boolean flipZ) throws IOException {
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(rot);
        if (flipX) transform = transform.scale(BlockVector3.at(-1, 1, 1).toVector3());
        if (flipZ) transform = transform.scale(BlockVector3.at(1, 1, -1).toVector3());
        
        Clipboard transformed = createTransformedClipboard(original, transform);
        
        File varDir = new File(templateDir, "variations");
        if (!varDir.exists()) varDir.mkdirs();
        
        String filename = versionId + "_" + rot + "_" + flipX + "_" + flipZ; // saveSchematicFile adds .schem
        saveSchematicFile(varDir, filename, transformed);
    }
    
    private Clipboard createTransformedClipboard(Clipboard original, AffineTransform transform) {
        Region region = original.getRegion();
        BlockVector3 origin = original.getOrigin();
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        
        BlockVector3[] corners = new BlockVector3[8];
        corners[0] = min;
        corners[1] = BlockVector3.at(min.x(), min.y(), max.z());
        corners[2] = BlockVector3.at(min.x(), max.y(), min.z());
        corners[3] = BlockVector3.at(min.x(), max.y(), max.z());
        corners[4] = BlockVector3.at(max.x(), min.y(), min.z());
        corners[5] = BlockVector3.at(max.x(), min.y(), max.z());
        corners[6] = BlockVector3.at(max.x(), max.y(), min.z());
        corners[7] = max;
        
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockVector3 c : corners) {
            Vector3 v = c.toVector3().subtract(origin.toVector3());
            Vector3 t = transform.apply(v);
            BlockVector3 b = t.toBlockPoint();

            if (b.x() < minX) minX = b.x();
            if (b.y() < minY) minY = b.y();
            if (b.z() < minZ) minZ = b.z();
            if (b.x() > maxX) maxX = b.x();
            if (b.y() > maxY) maxY = b.y();
            if (b.z() > maxZ) maxZ = b.z();
        }
        
        BlockVector3 newMin = BlockVector3.at(minX, minY, minZ).add(origin);
        BlockVector3 newMax = BlockVector3.at(maxX, maxY, maxZ).add(origin);
        
        CuboidRegion newRegion = new CuboidRegion(null, newMin, newMax);
        BlockArrayClipboard target = new BlockArrayClipboard(newRegion);
        target.setOrigin(origin);
        
        ClipboardHolder holder = new ClipboardHolder(original);
        holder.setTransform(holder.getTransform().combine(transform));
        
        try {
            Operation op = holder.createPaste(target)
                .to(origin)
                .ignoreAirBlocks(false)
                .build();
            Operations.complete(op);
        } catch (Exception e) { e.printStackTrace(); }
        
        return target;
    }
}