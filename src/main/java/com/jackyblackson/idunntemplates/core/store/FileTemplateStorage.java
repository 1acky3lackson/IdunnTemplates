package com.jackyblackson.idunntemplates.core.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;

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
        File templateDir = new File(parentDir, "_" + name);

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

        // Save schematic
        saveSchematicFile(templateDir, initialVersion.getVersionId(), initialClipboard);

        return new Template(name, templateDir, metadata);
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
            
            return new Template(name, templateDir, metadata);
        }
    }

    @Override
    public void updateMetadata(Template template) throws IOException {
        saveMetadataFile(template.getDirectory(), template.getMetadata());
    }

    @Override
    public void saveTemplateVersion(Template template, TemplateVersion version, Clipboard clipboard) throws IOException {
        saveSchematicFile(template.getDirectory(), version.getVersionId(), clipboard);
        template.getMetadata().addVersion(version);
        updateMetadata(template);
    }

    private void saveMetadataFile(File templateDir, TemplateMetadata metadata) throws IOException {
        File file = new File(templateDir, "metadata.json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(metadata, writer);
        }
    }

    private void saveSchematicFile(File templateDir, String versionId, Clipboard clipboard) throws IOException {
        File file = new File(templateDir, versionId + ".schem");
        ClipboardFormat format = ClipboardFormats.findByAlias("sponge"); // Standard schem format
        if (format == null) format = ClipboardFormats.findByAlias("schematic"); // Fallback

        try (ClipboardWriter writer = format.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        }
    }
}
