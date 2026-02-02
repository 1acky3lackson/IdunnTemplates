package com.jackyblackson.idunntemplates.core.store;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.dao.TemplateDao;
import com.jackyblackson.idunntemplates.util.EntityHelper;
import com.jackyblackson.idunntemplates.manager.DatabaseManager;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseTemplateStorage implements TemplateStorage {

    private final DatabaseManager databaseManager;
    private final File rootSchematicDirectory;
    private final Logger logger;
    private final TemplateDao templateDao;

    public DatabaseTemplateStorage(DatabaseManager databaseManager, File rootSchematicDirectory, Logger logger) {
        this.databaseManager = databaseManager;
        this.rootSchematicDirectory = rootSchematicDirectory;
        this.logger = logger;

        if (!this.rootSchematicDirectory.exists()) {
            this.rootSchematicDirectory.mkdirs();
        }

        this.templateDao = databaseManager.getTemplateDao();
        if (this.templateDao == null) {
            throw new IllegalStateException("TemplateDao is not initialized in DatabaseManager!");
        }
    }

    @Override
    public Template saveNewTemplate(String path, String name, TemplateMetadata metadata, Clipboard initialClipboard, TemplateVersion initialVersion) throws IOException {
        String fullPath = path.isEmpty() ? name : path + "/" + name;

        try {
            if (templateDao.queryByPath(fullPath) != null) {
                throw new IOException("Template already exists at path: " + fullPath);
            }

            File templateDir = new File(rootSchematicDirectory, fullPath);
            if (!templateDir.exists() && !templateDir.mkdirs()) {
                throw new IOException("Failed to create schematic directory " + templateDir.getPath());
            }

            metadata.addVersion(initialVersion);

            // 保存 .schem 文件
            saveSchematicFile(templateDir, initialVersion.getVersionId(), initialClipboard);

            Template template = new Template(name, fullPath, metadata);

            com.j256.ormlite.misc.TransactionManager.callInTransaction(templateDao.getConnectionSource(), () -> {
                templateDao.create(template);
                initialVersion.setTemplate(template);
                templateDao.getVersionDao().create(initialVersion);
                return null;
            });

            logger.info("Saved new template to DB and Disk: " + fullPath);
            return template;

        } catch (SQLException e) {
            throw new IOException("Database error while saving template: " + e.getMessage(), e);
        }
    }

    @Override
    public Template loadTemplate(String path) throws IOException {
        try {
            return templateDao.queryByPath(path);
        } catch (SQLException e) {
            throw new IOException("Database error loading template: " + path, e);
        }
    }

    @Override
    public List<Template> loadAllTemplates() throws IOException {
        try {
            List<Template> templates = templateDao.queryAllWithRelations();

            return templates;
        } catch (SQLException e) {
            throw new IOException("Database error loading all templates", e);
        }
    }

    @Override
    public void updateMetadata(Template template) throws IOException {
        try {
            templateDao.update(template);
        } catch (SQLException e) {
            throw new IOException("Database error updating metadata for " + template.getName(), e);
        }
    }

    @Override
    public Clipboard loadSchematic(Template template, TemplateVersion version) throws IOException {
        File file = new File(EntityHelper.getDirectory(template), version.getVersionId() + ".schem");
        ClipboardFormat format = ClipboardFormats.findByAlias("schem");
        if (format == null) throw new IOException("Schematic format 'schem' not found.");

        if (!file.exists()) {
            throw new IOException("Schematic file not found: " + file.getPath());
        }

        try (var reader = format.getReader(new java.io.FileInputStream(file))) {
            return reader.read();
        }
    }

    @Override
    public void saveTemplateVersion(Template template, TemplateVersion version, Clipboard clipboard) throws IOException {
        try {
            File templateDir = EntityHelper.getDirectory(template);

            // 1. 保存 .schem 到磁盘
            saveSchematicFile(templateDir, version.getVersionId(), clipboard);

            // 2. 数据库事务
            com.j256.ormlite.misc.TransactionManager.callInTransaction(templateDao.getConnectionSource(), () -> {
                version.setTemplate(template);
                templateDao.getVersionDao().create(version);

                if (!template.getMetadata().getVersions().contains(version)) {
                    template.getMetadata().addVersion(version);
                }
                templateDao.update(template);
                return null;
            });

        } catch (SQLException e) {
            throw new IOException("Failed to save template version to DB", e);
        }
    }

    // --- Helper Methods ---

    private void saveSchematicFile(File dir, String filenameNoExt, Clipboard clipboard) throws IOException {
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, filenameNoExt + ".schem");
        ClipboardFormat format = ClipboardFormats.findByAlias("schem");
        if (format == null) throw new IOException("Schematic format 'schem' not found.");

        try (ClipboardWriter writer = format.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        }
    }
}