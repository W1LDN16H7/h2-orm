package h2.orm.core;

import h2.orm.config.DatabaseConfiguration;
import h2.orm.exception.types.ConfigurationException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Core database initializer for H2-ORM
 * Handles database setup, schema validation, and initialization tasks
 */
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    /**
     * Initialize database with given configuration
     */
    public static void initialize(DatabaseConfiguration config) {
        try {
            logger.info("Initializing database with configuration: {}", config.getUrl());

            // Initialize EntityManagerProvider
            EntityManagerProvider.initialize(config);

            // Validate database connection
            validateConnection();

            // Try to log database information (non-critical)
            try {
                logDatabaseInfo();
            } catch (Exception e) {
                logger.debug("Could not retrieve database metadata (non-critical): {}", e.getMessage());
            }

            logger.info("Database initialization completed successfully");

        } catch (Exception e) {
            logger.error("Database initialization failed", e);
            throw new ConfigurationException(
                "Failed to initialize database: " + e.getMessage(),
                "Check your database configuration and ensure the database is accessible."
            );
        }
    }

    /**
     * Validate database connection
     */
    public static void validateConnection() {
        EntityManagerProvider.executeInTransaction(em -> {
            // Simple validation query
            em.createNativeQuery("SELECT 1").getSingleResult();
            logger.debug("Database connection validated successfully");
            return null;
        });
    }

    /**
     * Get existing table names in the database
     */
    public static Set<String> getExistingTables() {
        return EntityManagerProvider.executeInTransaction(em -> {
            Set<String> tables = new HashSet<>();
            try {
                Connection connection = em.unwrap(Connection.class);
                DatabaseMetaData metaData = connection.getMetaData();

                try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        tables.add(rs.getString("TABLE_NAME").toLowerCase());
                    }
                }
            } catch (SQLException e) {
                logger.warn("Could not retrieve table information", e);
            }
            return tables;
        });
    }

    /**
     * Check if specific table exists
     */
    public static boolean tableExists(String tableName) {
        return getExistingTables().contains(tableName.toLowerCase());
    }

    /**
     * Log database information
     */
    private static void logDatabaseInfo() {
        EntityManagerProvider.executeInTransaction(em -> {
            try {
                Connection connection = em.unwrap(Connection.class);
                DatabaseMetaData metaData = connection.getMetaData();

                logger.info("Database Info - Product: {}, Version: {}",
                           metaData.getDatabaseProductName(),
                           metaData.getDatabaseProductVersion());
                logger.info("Driver Info - Name: {}, Version: {}",
                           metaData.getDriverName(),
                           metaData.getDriverVersion());

            } catch (SQLException e) {
                logger.debug("Could not retrieve database metadata", e);
            }
            return null;
        });
    }

    /**
     * Create database schema if not exists (for databases that support it)
     */
    public static void createSchemaIfNotExists(String schemaName) {
        EntityManagerProvider.executeInTransaction(em -> {
            try {
                em.createNativeQuery("CREATE SCHEMA IF NOT EXISTS " + schemaName).executeUpdate();
                logger.info("Schema '{}' created or already exists", schemaName);
            } catch (Exception e) {
                logger.debug("Could not create schema '{}': {}", schemaName, e.getMessage());
            }
            return null;
        });
    }

    /**
     * Execute custom initialization SQL scripts
     */
    public static void executeInitScript(String sql) {
        EntityManagerProvider.executeInTransaction(em -> {
            try {
                String[] statements = sql.split(";");
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        em.createNativeQuery(trimmed).executeUpdate();
                    }
                }
                logger.info("Initialization script executed successfully");
            } catch (Exception e) {
                logger.error("Failed to execute initialization script", e);
                throw new ConfigurationException(
                    "Failed to execute database initialization script",
                    "Check your SQL syntax and database permissions."
                );
            }
            return null;
        });
    }

    /**
     * Cleanup and shutdown database connections
     */
    public static void shutdown() {
        try {
            EntityManagerProvider.shutdown();
            logger.info("Database shutdown completed");
        } catch (Exception e) {
            logger.warn("Error during database shutdown", e);
        }
    }
}
