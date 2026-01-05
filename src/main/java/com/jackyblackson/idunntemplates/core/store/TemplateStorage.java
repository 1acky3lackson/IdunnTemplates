package com.jackyblackson.idunntemplates.core.store;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.sk89q.worldedit.extent.clipboard.Clipboard;

import java.io.IOException;

public interface TemplateStorage {
    
    /**
     * Saves a new template to disk.
     * @param path The relative path (e.g. "users/jacky/mybuild")
     * @param metadata The initial metadata
     * @param initialClipboard The initial schematic clipboard
     * @param initialVersion The initial version info
     * @return The created Template object
     */
    Template saveNewTemplate(String path, String name, TemplateMetadata metadata, Clipboard initialClipboard, TemplateVersion initialVersion) throws IOException;

    /**
     * Loads a template from a specific directory.
     * @param path The relative path to the template directory
     */
    Template loadTemplate(String path) throws IOException;

    /**
     * Loads all available templates from the storage.
     * @return A list of all loaded templates.
     */
    java.util.List<Template> loadAllTemplates() throws IOException;

    /**
     * Updates the metadata on disk.
     */
    void updateMetadata(Template template) throws IOException;
    
    /**
     * Loads the clipboard (schematic) for a specific version.
     */
    Clipboard loadSchematic(Template template, TemplateVersion version) throws IOException;

    /**
     * Saves a new schematic version for an existing template.
     */
    void saveTemplateVersion(Template template, TemplateVersion version, Clipboard clipboard) throws IOException;
}
