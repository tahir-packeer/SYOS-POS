// File: src/main/java/org/example/config/ConfigManager.java
package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager for centralized application settings
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;

    private ConfigManager() {
        loadProperties();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application properties", e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDoubleProperty(String key, double defaultValue) {
        String value = properties.getProperty(key);
        try {
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Database configuration methods
    public String getDatabaseUrl() {
        return getProperty("db.url");
    }

    public String getDatabaseUsername() {
        return getProperty("db.username");
    }

    public String getDatabasePassword() {
        return getProperty("db.password");
    }

    public int getReorderThreshold() {
        return getIntProperty("business.reorder.threshold", 50);
    }

    public double getDiscountRate() {
        return getDoubleProperty("business.discount.rate", 0.0);
    }
}