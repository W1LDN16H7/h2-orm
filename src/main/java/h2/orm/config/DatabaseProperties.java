package h2.orm.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Centralized configuration manager for H2-ORM library
 * Supports both .properties files and XML configuration with override capabilities
 */
public class DatabaseProperties {
    private static final String DEFAULT_PROPERTIES_FILE = "h2-orm.properties";
    private static final String DEFAULT_XML_FILE = "h2-orm-config.xml";

    private final Properties properties;
    private final Map<String, String> overrides;

    private static DatabaseProperties instance;

    private DatabaseProperties() {
        this.properties = new Properties();
        this.overrides = new ConcurrentHashMap<>();
        loadDefaultProperties();
    }

    public static synchronized DatabaseProperties getInstance() {
        if (instance == null) {
            instance = new DatabaseProperties();
        }
        return instance;
    }

    /**
     * Load properties from custom file
     */
    public DatabaseProperties loadFromFile(String filename) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is != null) {
                if (filename.endsWith(".xml")) {
                    properties.loadFromXML(is);
                } else {
                    properties.load(is);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from: " + filename, e);
        }
        return this;
    }

    /**
     * Override specific property programmatically
     */
    public DatabaseProperties override(String key, String value) {
        overrides.put(key, value);
        return this;
    }

    /**
     * Get property value with override support
     */
    public String getProperty(String key) {
        return overrides.getOrDefault(key, properties.getProperty(key));
    }

    /**
     * Get property value with default fallback
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Get boolean property
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Get integer property
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void loadDefaultProperties() {
        // Set default values
        properties.setProperty("h2orm.database.type", "h2");
        properties.setProperty("h2orm.database.mode", "file"); // file or memory
        properties.setProperty("h2orm.database.path", "./data/h2orm");
        properties.setProperty("h2orm.database.username", "sa");
        properties.setProperty("h2orm.database.password", "");

        // Connection pool settings
        properties.setProperty("h2orm.pool.size", "10");
        properties.setProperty("h2orm.pool.timeout", "30000");
        properties.setProperty("h2orm.pool.idle.timeout", "600000");
        properties.setProperty("h2orm.pool.max.lifetime", "1800000");

        // JPA/Hibernate settings
        properties.setProperty("h2orm.jpa.ddl.auto", "update");
        properties.setProperty("h2orm.jpa.show.sql", "false");
        properties.setProperty("h2orm.jpa.format.sql", "false");
        properties.setProperty("h2orm.jpa.batch.size", "20");
        properties.setProperty("h2orm.jpa.cache.use.second.level", "true");
        properties.setProperty("h2orm.jpa.cache.use.query", "true");

        // Export settings
        properties.setProperty("h2orm.export.csv.delimiter", ",");
        properties.setProperty("h2orm.export.excel.sheet.name", "Data");
        properties.setProperty("h2orm.export.json.pretty.print", "true");

        // Backup settings
        properties.setProperty("h2orm.backup.auto.enabled", "false");
        properties.setProperty("h2orm.backup.interval.hours", "24");
        properties.setProperty("h2orm.backup.retention.days", "7");
        properties.setProperty("h2orm.backup.compression", "true");

        // Monitoring settings
        properties.setProperty("h2orm.monitoring.enabled", "true");
        properties.setProperty("h2orm.monitoring.query.log.enabled", "false");
        properties.setProperty("h2orm.monitoring.slow.query.threshold.ms", "1000");

        // Try to load from default files
        loadFromFile(DEFAULT_PROPERTIES_FILE);
        loadFromFile(DEFAULT_XML_FILE);
    }

    /**
     * Database configuration keys
     */
    public static class Keys {
        public static final String DATABASE_TYPE = "h2orm.database.type";
        public static final String DATABASE_MODE = "h2orm.database.mode";
        public static final String DATABASE_PATH = "h2orm.database.path";
        public static final String DATABASE_USERNAME = "h2orm.database.username";
        public static final String DATABASE_PASSWORD = "h2orm.database.password";
        public static final String DATABASE_HOST = "h2orm.database.host";
        public static final String DATABASE_PORT = "h2orm.database.port";
        public static final String DATABASE_NAME = "h2orm.database.name";

        public static final String POOL_SIZE = "h2orm.pool.size";
        public static final String POOL_TIMEOUT = "h2orm.pool.timeout";
        public static final String POOL_IDLE_TIMEOUT = "h2orm.pool.idle.timeout";
        public static final String POOL_MAX_LIFETIME = "h2orm.pool.max.lifetime";

        public static final String JPA_DDL_AUTO = "h2orm.jpa.ddl.auto";
        public static final String JPA_SHOW_SQL = "h2orm.jpa.show.sql";
        public static final String JPA_FORMAT_SQL = "h2orm.jpa.format.sql";
        public static final String JPA_BATCH_SIZE = "h2orm.jpa.batch.size";
        public static final String JPA_CACHE_USE_SECOND_LEVEL = "h2orm.jpa.cache.use.second.level";
        public static final String JPA_CACHE_USE_QUERY = "h2orm.jpa.cache.use.query";
    }
}
