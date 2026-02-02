package com.jackyblackson.idunntemplates.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DataSourceConfig {

    @Value("${app.datasource.type}")
    private String databaseType;

    @Value("${app.datasource.sqlite.file-path}")
    private String sqliteFilePath;

    @Value("${app.datasource.postgres.url}")
    private String postgresUrl;

    @Value("${app.datasource.postgres.username}")
    private String postgresUsername;

    @Value("${app.datasource.postgres.password}")
    private String postgresPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        if ("postgres".equalsIgnoreCase(databaseType)) {
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(postgresUrl)
                    .username(postgresUsername)
                    .password(postgresPassword)
                    .build();
        } else {
            // Default to SQLite
            String url = "jdbc:sqlite:" + sqliteFilePath;
            return DataSourceBuilder.create()
                    .driverClassName("org.sqlite.JDBC")
                    .url(url)
                    .build();
        }
    }

    // We can rely on Spring Boot's auto-configuration for EntityManagerFactory
    // if we just provide the DataSource. However, for SQLite dialect we might need
    // to customize properties if auto-detection fails.
    // Spring Boot usually detects dialect from JDBC URL.
    // But for SQLite with Hibernate 6, we might need to be explicit if using community dialect.

    // Let's add a configuration customizer to set dialect if needed.

    /*
    @Bean
    public JpaProperties jpaProperties() {
        // ...
    }
    */
}
