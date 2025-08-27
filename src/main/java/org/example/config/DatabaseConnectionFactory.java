// File: src/main/java/org/example/config/DatabaseConnectionFactory.java
package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Factory for database connections using HikariCP connection pool
 * Implements Singleton pattern for connection pool management
 */
public class DatabaseConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionFactory.class);
    private static DatabaseConnectionFactory instance;
    private HikariDataSource dataSource;

    private DatabaseConnectionFactory() {
        initializeDataSource();
    }

    public static synchronized DatabaseConnectionFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionFactory();
        }
        return instance;
    }

    private void initializeDataSource() {
        ConfigManager config = ConfigManager.getInstance();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getDatabaseUrl());
        hikariConfig.setUsername(config.getDatabaseUsername());
        hikariConfig.setPassword(config.getDatabasePassword());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool configuration for future concurrent access
        hikariConfig.setMaximumPoolSize(config.getIntProperty("db.pool.maximumPoolSize", 10));
        hikariConfig.setMinimumIdle(config.getIntProperty("db.pool.minimumIdle", 2));
        hikariConfig.setConnectionTimeout(config.getIntProperty("db.pool.connectionTimeout", 30000));
        hikariConfig.setIdleTimeout(config.getIntProperty("db.pool.idleTimeout", 600000));
        hikariConfig.setMaxLifetime(config.getIntProperty("db.pool.maxLifetime", 1800000));

        // Additional settings
        hikariConfig.setPoolName("SYOSPool");
        hikariConfig.setAutoCommit(true);
        hikariConfig.setConnectionTestQuery("SELECT 1");

        this.dataSource = new HikariDataSource(hikariConfig);
        logger.info("Database connection pool initialized successfully");
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }
}