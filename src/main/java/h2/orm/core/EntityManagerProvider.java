package h2.orm.core;

import h2.orm.config.DatabaseConfiguration;
import h2.orm.exception.types.DatabaseNotInitializedException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core EntityManager Provider for H2-ORM library
 * Centralized management of JPA EntityManagerFactory and EntityManager instances
 */
public class EntityManagerProvider {
    private static final Logger logger = LoggerFactory.getLogger(EntityManagerProvider.class);
    private static final Map<String, EntityManagerFactory> factories = new ConcurrentHashMap<>();
    private static final ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<>();
    private static String defaultConfigName = "default";
    private static DatabaseConfiguration currentConfig;

    /**
     * Initialize with default configuration from properties
     */
    public static void initialize() {
        DatabaseConfiguration config = DatabaseConfiguration.fromProperties();
        initialize(config);
    }

    /**
     * Initialize with custom properties file
     */
    public static void initialize(String propertiesFile) {
        DatabaseConfiguration config = DatabaseConfiguration.fromPropertiesFile(propertiesFile);
        initialize(config);
    }

    /**
     * Initialize with specific configuration
     */
    public static void initialize(DatabaseConfiguration config) {
        initialize(defaultConfigName, config);
        currentConfig = config;
    }

    /**
     * Initialize with named configuration for multi-database support
     */
    public static void initialize(String configName, DatabaseConfiguration config) {
        EntityManagerFactory existingFactory = factories.get(configName);
        if (existingFactory != null && existingFactory.isOpen()) {
            existingFactory.close();
        }

        try {
            Map<String, Object> properties = buildJpaProperties(config);

            logger.info("Initializing EntityManagerFactory for config: {} with database: {}",
                    configName, config.getUrl());

            EntityManagerFactory factory = Persistence.createEntityManagerFactory("h2-orm-pu", properties);
            factories.put(configName, factory);

            logger.info("EntityManagerFactory initialized successfully for config: {}", configName);
        } catch (Exception e) {
            logger.error("Failed to initialize EntityManagerFactory for config: {}", configName, e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    /**
     * Get EntityManager for default configuration
     */
    public static EntityManager getEntityManager() {
        return getEntityManager(defaultConfigName);
    }

    /**
     * Get EntityManager for named configuration
     */
    public static EntityManager getEntityManager(String configName) {
        EntityManager em = entityManagerThreadLocal.get();
        if (em == null || !em.isOpen()) {
            EntityManagerFactory factory = factories.get(configName);
            if (factory == null) {
                throw new DatabaseNotInitializedException();
            }
            em = factory.createEntityManager();
            entityManagerThreadLocal.set(em);
        }
        return em;
    }

    /**
     * Close current thread's EntityManager
     */
    public static void closeEntityManager() {
        EntityManager em = entityManagerThreadLocal.get();
        if (em != null && em.isOpen()) {
            try {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            } catch (Exception e) {
                logger.warn("Error closing EntityManager", e);
            } finally {
                entityManagerThreadLocal.remove();
            }
        }
    }

    /**
     * Execute operation in transaction
     */
    public static <T> T executeInTransaction(TransactionCallback<T> callback) {
        return executeInTransaction(defaultConfigName, callback);
    }

    /**
     * Execute operation in transaction with named configuration
     */
    public static <T> T executeInTransaction(String configName, TransactionCallback<T> callback) {
        EntityManager em = getEntityManager(configName);
        try {
            em.getTransaction().begin();
            T result = callback.execute(em);
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            closeEntityManager();
        }
    }

    /**
     * Shutdown all EntityManagerFactories
     */
    public static void shutdown() {
        closeEntityManager();
        factories.values().forEach(factory -> {
            if (factory.isOpen()) {
                try {
                    factory.close();
                } catch (Exception e) {
                    logger.warn("Error closing EntityManagerFactory", e);
                }
            }
        });
        factories.clear();
        logger.info("All EntityManagerFactories shut down");
    }

    /**
     * Check if provider is initialized
     */
    public static boolean isInitialized() {
        return isInitialized(defaultConfigName);
    }

    /**
     * Check if named configuration is initialized
     */
    public static boolean isInitialized(String configName) {
        EntityManagerFactory factory = factories.get(configName);
        return factory != null && factory.isOpen();
    }

    /**
     * Get current configuration
     */
    public static DatabaseConfiguration getCurrentConfiguration() {
        return currentConfig;
    }

    private static Map<String, Object> buildJpaProperties(DatabaseConfiguration config) {
        Map<String, Object> properties = new HashMap<>();

        // Basic connection properties
        properties.put("jakarta.persistence.jdbc.driver", config.getDriverClass());
        properties.put("jakarta.persistence.jdbc.url", config.getUrl());
        properties.put("jakarta.persistence.jdbc.user", config.getUsername());
        properties.put("jakarta.persistence.jdbc.password", config.getPassword());

        // Hibernate specific properties
        properties.put("hibernate.dialect", config.getDialect());
        properties.put("hibernate.hbm2ddl.auto", config.getDdlAuto());
        properties.put("hibernate.show_sql", String.valueOf(config.isShowSql()));
        properties.put("hibernate.format_sql", String.valueOf(config.isFormatSql()));
        properties.put("hibernate.use_sql_comments", "true");

        // Connection pool settings (HikariCP)
        properties.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        properties.put("hibernate.hikari.maximumPoolSize", String.valueOf(config.getPoolSize()));
        properties.put("hibernate.hikari.minimumIdle", String.valueOf(config.getMinimumIdle()));
        properties.put("hibernate.hikari.connectionTimeout", String.valueOf(config.getConnectionTimeout()));
        properties.put("hibernate.hikari.idleTimeout", String.valueOf(config.getIdleTimeout()));
        properties.put("hibernate.hikari.maxLifetime", String.valueOf(config.getMaxLifetime()));
        properties.put("hibernate.hikari.poolName", config.getPoolName());

        // Performance settings
        properties.put("hibernate.jdbc.batch_size", String.valueOf(config.getBatchSize()));
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.jdbc.batch_versioned_data", "true");

        // Cache settings - Disable caching by default for simplicity
        properties.put("hibernate.cache.use_second_level_cache", "false");
        properties.put("hibernate.cache.use_query_cache", "false");

        if (config.isUseSecondLevelCache()) {
            // Use Hibernate's built-in cache instead of JCache
            properties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.internal.NoCacheRegionFactory");
        }

        // Additional optimizations
        properties.put("hibernate.connection.autocommit", "false");
        properties.put("hibernate.jdbc.lob.non_contextual_creation", "true");
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");

        return properties;
    }

    /**
     * Functional interface for transaction callbacks
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(EntityManager entityManager) throws Exception;
    }
}
