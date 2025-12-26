package com.jackyblackson.idunntemplates.core.domain;

import java.io.File;

public class Template {
    private final String name;
    private final File directory;
    private final TemplateMetadata metadata;

    public Template(String name, File directory, TemplateMetadata metadata) {
        this.name = name;
        this.directory = directory;
        this.metadata = metadata;
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
