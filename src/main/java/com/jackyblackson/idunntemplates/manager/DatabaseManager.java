package com.jackyblackson.idunntemplates.manager;

import com.j256.ormlite.support.DatabaseConnection;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.dao.TemplateDao;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {

    private final IdunnTemplates plugin;
    private HikariDataSource dataSource;
    private ConnectionSource connectionSource;

    // DAOs
    private Dao<Instance, String> instanceDao;
    private TemplateDao templateDao;

    // 如果你有其他实体，继续在这里添加
    // private Dao<TemplateData, UUID> templateDao;

    public DatabaseManager(IdunnTemplates plugin) {
        this.plugin = plugin;
    }

    public void init() throws SQLException {
        FileConfiguration config = plugin.getConfig();

        // 读取配置类型: sqlite, mysql, postgresql
        String storageType = config.getString("storage.type", "sqlite").toLowerCase();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("IdunnTemplates-Pool");

        // --- 数据库类型分发逻辑 ---
        switch (storageType) {
            case "mysql":
            case "mariadb":
                configureMySQL(hikariConfig, config);
                break;
            case "postgresql":
            case "postgres":
                configurePostgres(hikariConfig, config);
                break;
            case "sqlite":
            default:
                configureSQLite(hikariConfig);
                break;
        }

        // --- 通用连接池配置 ---
        // 建议：最大连接数不要设置太大，Minecraft 插件通常 10 个够用了
        int poolSize = config.getInt("storage.pool-size", 10);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setConnectionTimeout(30000); // 30秒超时
        hikariConfig.setIdleTimeout(600000);      // 10分钟空闲断开
        hikariConfig.setMaxLifetime(1800000);     // 30分钟最大生命周期

        // --- 初始化连接池 ---
        plugin.getLogger().info("Connecting to database (" + storageType + ")...");
        this.dataSource = new HikariDataSource(hikariConfig);

        // --- 绑定 ORMLite ---
        // 使用 DataSourceConnectionSource 将 Hikari 桥接到 ORMLite
        this.connectionSource = new DataSourceConnectionSource(dataSource, dataSource.getJdbcUrl());

        // --- 初始化 DAOs ---
        initDAOs();

        // --- [关键修复] 自动建表 ---
        // 必须显式为每一个 Entity 类调用 createTableIfNotExists

        // 1. 基础表 (Metadata 被 Template 引用，理论上应该先建，虽然 ORMLite 会处理延迟)
        TableUtils.createTableIfNotExists(connectionSource, TemplateMetadata.class);

        // 2. 核心表
        TableUtils.createTableIfNotExists(connectionSource, Template.class);

        // 3. 关联表
        TableUtils.createTableIfNotExists(connectionSource, TemplateVersion.class);
        TableUtils.createTableIfNotExists(connectionSource, Instance.class);

//        plugin.getLogger().info("Database initialized successfully.");

        plugin.getLogger().info("Database initialized successfully.");
    }

    private void configureSQLite(HikariConfig config) {
        File dbFile = new File(plugin.getDataFolder(), "data.db");
        // 自动创建父目录
        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());

        // SQLite 不需要复杂的池配置，甚至 1 个连接也可以，但 Hikari 会处理好
        config.setConnectionTestQuery("SELECT 1");
    }

    private void configureMySQL(HikariConfig config, FileConfiguration fileConfig) {
        String host = fileConfig.getString("storage.host", "localhost");
        String port = fileConfig.getString("storage.port", "3306");
        String database = fileConfig.getString("storage.database", "minecraft");
        String username = fileConfig.getString("storage.username", "root");
        String password = fileConfig.getString("storage.password", "");
        boolean useSSL = fileConfig.getBoolean("storage.use-ssl", false);

        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 拼接 MySQL URL，包含一些必要的性能参数
        String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=%b&autoReconnect=true&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8",
                host, port, database, useSSL);

        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        // MySQL 性能优化参数
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
    }

    private void configurePostgres(HikariConfig config, FileConfiguration fileConfig) {
        String host = fileConfig.getString("storage.host", "localhost");
        String port = fileConfig.getString("storage.port", "5432");
        String database = fileConfig.getString("storage.database", "minecraft");
        String username = fileConfig.getString("storage.username", "postgres");
        String password = fileConfig.getString("storage.password", "");

        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s", host, port, database));
        config.setUsername(username);
        config.setPassword(password);

        // Postgres 特有测试语句
        config.setConnectionTestQuery("SELECT 1");
    }

    private void initDAOs() throws SQLException {
        // 创建 DAO 实例
        this.instanceDao = DaoManager.createDao(connectionSource, Instance.class);
        this.templateDao = DaoManager.createDao(connectionSource, Template.class);

        // 开启 ORMLite 的自动提交控制，提高批量操作性能
//        instanceDao.setAutoCommit(connectionSource.getReadOnlyConnection(), true);
//        templateDao.setAutoCommit((DatabaseConnection) connectionSource, true);
    }

    public void close() {
        if (connectionSource != null) {
            try {
                connectionSource.close(); // 关闭 ORMLite 连接源
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error closing connection source", e);
            }
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close(); // 关闭 HikariCP 线程池
        }
    }

    // --- Getters ---

    public Dao<Instance, String> getInstanceDao() {
        return instanceDao;
    }

    public TemplateDao getTemplateDao() {
        return templateDao;
    }
}
