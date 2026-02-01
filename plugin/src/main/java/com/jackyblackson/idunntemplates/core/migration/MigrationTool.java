package com.jackyblackson.idunntemplates.core.migration;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.store.dao.TemplateDao;
import com.jackyblackson.idunntemplates.manager.DatabaseManager;
import com.j256.ormlite.misc.TransactionManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class MigrationTool {

    private final IdunnTemplates plugin;
    private final Logger logger;
    private final Gson gson;
    private final InstanceRepository instanceRepository;
    private final DatabaseManager databaseManager;

    public MigrationTool(IdunnTemplates plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.instanceRepository = plugin.getInstanceRepository();
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void migrateToDatabase() {
        logger.info("Starting migration to database...");

        // Migrate Instances
        migrateInstances();

        // Migrate Templates
        migrateTemplates();

        logger.info("Migration finished.");
    }

    private void migrateInstances() {
        logger.info("Migrating instances...");
        File instancesDir = new File(plugin.getDataFolder(), "instances");
        if (!instancesDir.exists()) {
            logger.info("No instances directory found.");
            return;
        }

        File[] worldDirs = instancesDir.listFiles(File::isDirectory);
        if (worldDirs == null) return;

        int count = 0;
        for (File worldDir : worldDirs) {
            File[] partitionFiles = worldDir.listFiles((dir, name) -> name.startsWith("r.") && name.endsWith(".json"));
            if (partitionFiles == null) continue;

            for (File file : partitionFiles) {
                try (FileReader reader = new FileReader(file)) {
                    Type listType = new TypeToken<ArrayList<Instance>>(){}.getType();
                    List<Instance> list = gson.fromJson(reader, listType);

                    if (list != null) {
                        for (Instance instance : list) {
                            // Save to DB
                            // saveInstance uses createOrUpdate, so it's safe to run multiple times
                            instanceRepository.saveInstance(instance).join();
                            count++;
                        }
                    }
                } catch (IOException e) {
                    logger.severe("Failed to read instance file " + file.getPath() + ": " + e.getMessage());
                }
            }
        }
        logger.info("Migrated " + count + " instances.");
    }

    private void migrateTemplates() {
        logger.info("Migrating templates...");
        File templatesRoot = new File(plugin.getDataFolder(), "templates");
        if (!templatesRoot.exists()) {
            logger.info("No templates directory found.");
            return;
        }

        int count = scanAndMigrateTemplates(templatesRoot, templatesRoot);
        logger.info("Migrated " + count + " templates.");
    }

    private int scanAndMigrateTemplates(File directory, File root) {
        int count = 0;
        File metadataFile = new File(directory, "metadata.json");

        if (metadataFile.exists()) {
            // Found a template
            try {
                processTemplate(directory, metadataFile, root);
                count++;
            } catch (Exception e) {
                logger.severe("Failed to migrate template at " + directory.getPath() + ": " + e.getMessage());
                e.printStackTrace();
            }
            // Do not recurse inside a template directory
            return count;
        }

        // Recurse
        File[] files = directory.listFiles(File::isDirectory);
        if (files != null) {
            for (File subDir : files) {
                count += scanAndMigrateTemplates(subDir, root);
            }
        }
        return count;
    }

    private void processTemplate(File templateDir, File metadataFile, File root) throws Exception {
        // 1. Parse Metadata and Versions
        TemplateMetadata metadata;
        List<TemplateVersion> versions = new ArrayList<>();

        try (FileReader reader = new FileReader(metadataFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            // Deserialize Metadata (ignoring versions)
            metadata = gson.fromJson(json, TemplateMetadata.class);

            // Manually extract versions
            if (json.has("versions")) {
                JsonArray versionsArray = json.getAsJsonArray("versions");
                for (JsonElement element : versionsArray) {
                    TemplateVersion v = gson.fromJson(element, TemplateVersion.class);
                    versions.add(v);
                }
            }
        }

        // 2. Determine paths and names
        String dirName = templateDir.getName();
        String name = dirName.startsWith("_") ? dirName.substring(1) : dirName;

        // Calculate relative path of parent
        String relativePath = root.toURI().relativize(templateDir.getParentFile().toURI()).getPath();
        if (relativePath == null || relativePath.equals("")) {
             // templateDir is directly under root? No, parent is root.
             // URI relativize might return empty string if same.
             relativePath = "";
        }
        // URI paths end with / usually
        if (relativePath.endsWith("/")) relativePath = relativePath.substring(0, relativePath.length() - 1);

        String fullPath = relativePath.isEmpty() ? name : relativePath + "/" + name;

        // 3. Create Template object
        // Note: Template constructor might vary.
        // public Template(String name, String path, TemplateMetadata metadata)
        Template template = new Template(name, fullPath, metadata);

        // 4. Save to DB (Transaction)
        TemplateDao templateDao = databaseManager.getTemplateDao();

        TransactionManager.callInTransaction(templateDao.getConnectionSource(), () -> {
            // Check if exists
            if (templateDao.queryByPath(fullPath) == null) {
                // Save Template (cascades metadata)
                templateDao.create(template);

                // Save Versions
                for (TemplateVersion version : versions) {
                    version.setTemplate(template);
                    templateDao.getVersionDao().create(version);
                }
            } else {
                logger.warning("Template " + fullPath + " already exists in DB. Skipping DB insert.");
            }
            return null;
        });

        // 5. Rename Directory if needed
        if (dirName.startsWith("_")) {
            File newDir = new File(templateDir.getParentFile(), name);
            if (!newDir.exists()) {
                if (templateDir.renameTo(newDir)) {
                    logger.info("Renamed directory " + templateDir.getName() + " to " + newDir.getName());
                } else {
                    logger.warning("Failed to rename directory " + templateDir.getPath());
                }
            } else {
                 // If target exists and it's not the same dir (it shouldn't be), we have a collision.
                 // But since we are migrating, maybe we already migrated it?
                 logger.warning("Target directory " + newDir.getPath() + " already exists. Skipping rename.");
            }
        }
    }
}
