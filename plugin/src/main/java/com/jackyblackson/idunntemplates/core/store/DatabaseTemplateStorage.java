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

            // [新增] 生成初始预览图
            takeSnapshot(templateDir, initialClipboard);

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

            // [新增] 检查并补全缩略图
            checkAndGenerateMissingThumbnails(templates);

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

            // [新增] 更新预览图 (使用最新版本的 Clipboard)
            takeSnapshot(templateDir, clipboard);

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

    /**
     * [新增] 生成预览图
     * 使用 FAWE 或其他渲染逻辑生成 thumbnail.png
     */
    private void takeSnapshot(File templateDir, Clipboard clipboard) {
        File snapshotFile = new File(templateDir, "thumbnail.png");

        // 如果文件已存在，先删除旧的，保证是最新的预览图
        if (snapshotFile.exists()) {
            snapshotFile.delete();
        }

        try {
            // 这里调用具体的渲染逻辑，为了不让这个类太臃肿，建议抽离出去
            // 如果你要在这里写 FAWE 逻辑，需要依赖 FAWE-Bukkit 或 FAWE-Core
            SnapshotGenerator.generate(clipboard, snapshotFile);
            logger.info("Generated thumbnail for " + templateDir.getName());
        } catch (Exception e) {
            // 生成图片失败不应该打断主流程，记录错误即可
            logger.log(Level.WARNING, "Failed to generate thumbnail for " + templateDir.getName(), e);
        }
    }

    /**
     * [新增] 批量检查缺失的缩略图
     * 这是一个可能耗时的操作，建议在异步线程中运行，或者只检查前 N 个
     */
    private void checkAndGenerateMissingThumbnails(List<Template> templates) {
        // 为了不卡死主线程/启动流程，建议放到后台线程执行
        new Thread(() -> {
            for (Template template : templates) {
                File dir = EntityHelper.getDirectory(template);
                File snapshotFile = new File(dir, "thumbnail.png");

                if (!snapshotFile.exists()) {
                    try {
                        // 如果没有图片，尝试加载最新版本的 schematic 并渲染
                        TemplateVersion latestVersion = template.getLatestVersion();
                        if (latestVersion != null) {
                            Clipboard clipboard = loadSchematic(template, latestVersion);
                            takeSnapshot(dir, clipboard);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Auto-generation of missing thumbnail failed for " + template.getName(), e);
                    }
                }
            }
        }).start();
    }

    // 内部类或外部工具类：封装 FAWE 渲染逻辑
    // 注意：FAWE 的渲染 API 可能会随版本变动
    private static class SnapshotGenerator {
        public static void generate(Clipboard clipboard, File outputFile) throws Exception {
//            if (!outputFile.exists()) outputFile.mkdirs();

            ClipboardFormat format = ClipboardFormats.findByAlias("png");
            if (format == null) throw new IOException("Schematic format 'png' not found.");

            try (ClipboardWriter writer = format.getWriter(new FileOutputStream(outputFile))) {
                writer.write(clipboard);
            }
        }
    }
}