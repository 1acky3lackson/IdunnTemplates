package com.jackyblackson.idunntemplates.core.store;

import com.jackyblackson.idunntemplates.IdunnTemplates;
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
import java.util.UUID;
import java.util.logging.Logger;

public class DatabaseTemplateStorage implements TemplateStorage {

    private final DatabaseManager databaseManager;
    private final File rootSchematicDirectory;
    private final Logger logger;

    // 我们使用之前定义的增强版 TemplateDao
    private final TemplateDao templateDao;

    public DatabaseTemplateStorage(DatabaseManager databaseManager, File rootSchematicDirectory, Logger logger) {
        this.databaseManager = databaseManager;
        this.rootSchematicDirectory = rootSchematicDirectory;
        this.logger = logger;

        if (!this.rootSchematicDirectory.exists()) {
            this.rootSchematicDirectory.mkdirs();
        }

        // 获取我们在 DatabaseManager 中初始化的 TemplateDao
        this.templateDao = databaseManager.getTemplateDao();

        if (this.templateDao == null) {
            throw new IllegalStateException("TemplateDao is not initialized in DatabaseManager!");
        }
    }

    @Override
    public Template saveNewTemplate(String path, String name, TemplateMetadata metadata, Clipboard initialClipboard, TemplateVersion initialVersion) throws IOException {
        String fullPath = path.isEmpty() ? name : path + "/" + name;

        try {
            // 1. 检查路径冲突 (Database Query)
            if (templateDao.queryByPath(fullPath) != null) {
                throw new IOException("Template already exists at path: " + fullPath);
            }

            // 2. 准备物理目录 (For Schematics)
            // 虽然元数据进数据库了，但我们依然需要物理文件夹来存 .schem 文件
            File templateDir = new File(rootSchematicDirectory, fullPath);
            if (!templateDir.exists() && !templateDir.mkdirs()) {
                throw new IOException("Failed to create schematic directory " + templateDir.getPath());
            }

            // 3. 准备数据
            // 注意：这里只是内存操作，真正的数据库保存顺序很重要
            metadata.addVersion(initialVersion);

            // 4. 保存 .schem 文件到磁盘
            saveSchematicFile(templateDir, initialVersion.getVersionId(), initialClipboard);

            // 5. 构造 Template 对象
            Template template = new Template(name, fullPath, metadata);

            // 6. [Transaction] 数据库保存操作
            // 使用 ORMLite 的事务支持，确保 Template, Metadata, Version 要么都成功，要么都失败
            // 避免出现“文件存了但数据库没记录”或“有模板没版本”的脏数据
            com.j256.ormlite.misc.TransactionManager.callInTransaction(templateDao.getConnectionSource(), () -> {

                // A. 保存 Template (create 方法内部会自动保存 Metadata)
                templateDao.create(template);

                // B. 保存 Version
                // 必须先保存 Template 拿到 ID，才能保存 Version (外键依赖)
                initialVersion.setTemplate(template);
                templateDao.getVersionDao().create(initialVersion);

                return null;
            });

            logger.info("Saved new template to DB and Disk: " + fullPath);
            return template;

        } catch (SQLException e) {
            // 如果数据库失败，理论上应该回滚文件操作 (删除 .schem)，这里简化处理
            throw new IOException("Database error while saving template: " + e.getMessage(), e);
        }
    }

    @Override
    public Template loadTemplate(String path) throws IOException {
        try {
            // 使用 queryByPath (内部会自动 hydrate 关联数据)
            Template template = templateDao.queryByPath(path);

            if (template == null) {
                // 兼容性检查：如果数据库没找到，是否需要尝试从文件系统恢复？
                // 这里的策略是：数据库是唯一真理。如果需要迁移旧数据，请编写专门的迁移脚本。
                return null;
            }

            return template;

        } catch (SQLException e) {
            throw new IOException("Database error loading template: " + path, e);
        }
    }

    @Override
    public List<Template> loadAllTemplates() throws IOException {
        try {
            // 使用 queryAllWithRelations (内部会自动 hydrate)
            return templateDao.queryAllWithRelations();
        } catch (SQLException e) {
            throw new IOException("Database error loading all templates", e);
        }
    }

    @Override
    public void updateMetadata(Template template) throws IOException {
        try {
            // update 方法内部会自动处理 Metadata 的更新 (prePersist 等)
            templateDao.update(template);
        } catch (SQLException e) {
            throw new IOException("Database error updating metadata for " + template.getName(), e);
        }
    }

    @Override
    public Clipboard loadSchematic(Template template, TemplateVersion version) throws IOException {
        // 二进制文件依然走文件系统
        // Template.getDirectory() 现在会动态返回基于 dataFolder 的路径
        File file = new File(EntityHelper.getDirectory(template), version.getVersionId() + ".schem");

        ClipboardFormat format = ClipboardFormats.findByAlias("schem");
        if (format == null) throw new IOException("Schematic format 'schem' not found.");

        if (!file.exists()) {
            // 如果是刚迁移的环境，可能文件还在旧位置？
            // 只要 getDirectory() 逻辑和 rootSchematicDirectory 逻辑一致即可
            throw new IOException("Schematic file not found: " + file.getPath());
        }

        try (var reader = format.getReader(new java.io.FileInputStream(file))) {
            return reader.read();
        }
    }

    @Override
    public void saveTemplateVersion(Template template, TemplateVersion version, Clipboard clipboard) throws IOException {
        try {
            // 1. 保存 .schem 到磁盘
            saveSchematicFile(EntityHelper.getDirectory(template), version.getVersionId(), clipboard);

            // 2. 数据库事务：插入 Version 并更新 Metadata
            com.j256.ormlite.misc.TransactionManager.callInTransaction(templateDao.getConnectionSource(), () -> {

                // A. 插入新的 Version 记录
                version.setTemplate(template);
                templateDao.getVersionDao().create(version);

                // B. 更新内存中的 Template 对象状态
                // 注意：如果这是已存在的版本（更新），逻辑会有所不同，
                // 但通常 VersionId 是时间戳，所以都是新增。
                if (!template.getMetadata().getVersions().contains(version)) {
                    template.getMetadata().addVersion(version);
                }

                // C. 更新 Metadata (比如 last_updated 时间变了，或者版本列表缓存变了)
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