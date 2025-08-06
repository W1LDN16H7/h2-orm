package h2.orm;

import h2.orm.config.DatabaseConfiguration;
import h2.orm.core.DatabaseInitializer;
import h2.orm.repository.JpaRepository;
import h2.orm.repository.Repositories;

/**
 * Main entry point for H2-ORM library - makes it super easy to use!
 *
 * Usage:
 * 1. H2ORM.start() - Initialize with defaults
 * 2. H2ORM.repository(EntityClass.class) - Get repository
 * 3. Use repository methods like Spring Boot JPA
 * 4. H2ORM.stop() - Clean shutdown
 */
public class H2ORM {

    /**
     * Start H2-ORM with default H2 in-memory database
     */
    public static void start() {
        DatabaseInitializer.initialize(DatabaseConfiguration.h2InMemory());
    }

    /**
     * Start H2-ORM with H2 file-based database
     */
    public static void start(String databasePath) {
        DatabaseInitializer.initialize(DatabaseConfiguration.h2File(databasePath));
    }

    /**
     * Start H2-ORM with custom configuration
     */
    public static void start(DatabaseConfiguration config) {
        DatabaseInitializer.initialize(config);
    }

    /**
     * Start H2-ORM from properties file
     */
    public static void startFromProperties(String propertiesFile) {
        DatabaseConfiguration config = DatabaseConfiguration.fromPropertiesFile(propertiesFile);
        DatabaseInitializer.initialize(config);
    }

    /**
     * Get repository for entity class - exactly like Spring Boot @Autowired
     */
    public static <T, ID extends java.io.Serializable> JpaRepository<T, ID> repository(Class<T> entityClass) {
        return Repositories.of(entityClass);
    }

    /**
     * Stop H2-ORM and cleanup resources
     */
    public static void stop() {
        DatabaseInitializer.shutdown();
    }

    /**
     * Check if H2-ORM is running
     */
    public static boolean isRunning() {
        return h2.orm.core.EntityManagerProvider.isInitialized();
    }

    // Quick setup methods for different databases

    /**
     * Start with H2 in-memory database (perfect for testing)
     */
    public static void startInMemory() {
        start(DatabaseConfiguration.h2InMemory());
    }

    /**
     * Start with SQLite database
     */
    public static void startSQLite(String dbPath) {
        start(DatabaseConfiguration.sqlite(dbPath));
    }

    /**
     * Start with MySQL database
     */
    public static void startMySQL(String host, int port, String database, String username, String password) {
        start(DatabaseConfiguration.mysql(host, port, database, username, password));
    }

    /**
     * Start with PostgreSQL database
     */
    public static void startPostgreSQL(String host, int port, String database, String username, String password) {
        start(DatabaseConfiguration.postgresql(host, port, database, username, password));
    }
}
