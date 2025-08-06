package h2.orm;

import h2.orm.config.DatabaseConfiguration;
import h2.orm.core.DatabaseInitializer;
import h2.orm.core.TransactionManager;
import h2.orm.core.repository.JpaRepository;
import h2.orm.core.repository.Repositories;
import h2.orm.core.service.BackupService;
import h2.orm.core.service.ExportService;

import java.util.List;

/**
 * Main entry point for H2-ORM library - makes it super easy to use!
 *
 * Usage:
 * 1. H2ORM.start() - Initialize with defaults
 * 2. H2ORM.repository(EntityClass.class) - Get repository
 * 3. Use repository methods like Spring Boot JPA
 * 4. H2ORM.backup() / H2ORM.export() - Easy backup and export
 * 5. H2ORM.stop() - Clean shutdown
 */
public class H2ORM {

    private static final BackupService backupService = new BackupService();
    private static final ExportService exportService = new ExportService();

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

    // ===== BACKUP SERVICES =====
    
    /**
     * Create a database backup
     * Usage: H2ORM.backup("./backups/myapp_backup");
     */
    public static void backup(String backupPath) {
        backupService.backup(backupPath);
    }
    
    /**
     * Create a compressed database backup (saves space)
     * Usage: H2ORM.backupCompressed("./backups/myapp_backup");
     */
    public static void backupCompressed(String backupPath) {
        backupService.compressed(backupPath);
    }
    
    /**
     * Restore database from backup
     * Usage: H2ORM.restore("./backups/myapp_backup_20250806_120000.sql");
     */
    public static void restore(String backupPath) {
        backupService.restore(backupPath);
    }
    
    /**
     * Restore database from compressed backup
     * Usage: H2ORM.restoreCompressed("./backups/myapp_backup.gz");
     */
    public static void restoreCompressed(String backupPath) {
        backupService.restoreCompressed(backupPath);
    }
    
    /**
     * Get backup file information
     * Usage: BackupInfo info = H2ORM.getBackupInfo("./backups/myapp_backup.sql");
     */
    public static BackupService.BackupInfo getBackupInfo(String backupPath) {
        return backupService.getBackupInfo(backupPath);
    }

    // ===== EXPORT SERVICES =====
    
    /**
     * Export all data from an entity to CSV
     * Usage: H2ORM.exportToCsv(User.class, "./exports/users.csv");
     */
    public static <T> void exportToCsv(Class<T> entityClass, String filePath) {
        TransactionManager.executeInTransaction(em -> {
            JpaRepository<T, ?> repo = repository(entityClass);
            List<T> data = repo.findAll();
            // Process export within the same transaction to avoid LazyInitializationException
            exportService.toCsv(data, filePath);
            return null;
        });
    }
    
    /**
     * Export specific data to CSV
     * Usage: H2ORM.exportToCsv(userList, "./exports/users.csv");
     */
    public static <T> void exportToCsv(List<T> data, String filePath) {
        // Ensure export happens within a transaction to handle any lazy-loaded data
        TransactionManager.executeInTransaction(em -> {
            exportService.toCsv(data, filePath);
            return null;
        });
    }

    /**
     * Export all data from an entity to Excel
     * Usage: H2ORM.exportToExcel(User.class, "./exports/users.xlsx");
     */
    public static <T> void exportToExcel(Class<T> entityClass, String filePath) {
        TransactionManager.executeInTransaction(em -> {
            JpaRepository<T, ?> repo = repository(entityClass);
            List<T> data = repo.findAll();
            exportService.toExcel(data, filePath);
            return null;
        });
    }

    /**
     * Export specific data to Excel
     * Usage: H2ORM.exportToExcel(userList, "./exports/users.xlsx");
     */
    public static <T> void exportToExcel(List<T> data, String filePath) {
        TransactionManager.executeInTransaction(em -> {
            exportService.toExcel(data, filePath);
            return null;
        });
    }

    /**
     * Export all data from an entity to JSON
     * Usage: H2ORM.exportToJson(User.class, "./exports/users.json");
     */
    public static <T> void exportToJson(Class<T> entityClass, String filePath) {
        TransactionManager.executeInTransaction(em -> {
            JpaRepository<T, ?> repo = repository(entityClass);
            List<T> data = repo.findAll();
            exportService.toJson(data, filePath);
            return null;
        });
    }

    /**
     * Export specific data to JSON
     * Usage: H2ORM.exportToJson(userList, "./exports/users.json");
     */
    public static <T> void exportToJson(List<T> data, String filePath) {
        TransactionManager.executeInTransaction(em -> {
            exportService.toJson(data, filePath);
            return null;
        });
    }

    // ===== UTILITY SERVICES =====
    
    /**
     * Get backup service for advanced operations
     */
    public static BackupService getBackupService() {
        return backupService;
    }
    
    /**
     * Get export service for advanced operations
     */
    public static ExportService getExportService() {
        return exportService;
    }

    // ===== TABLE MANAGEMENT SERVICES =====

    /**
     * Create tables for all registered entities
     * Usage: H2ORM.createAll();
     */
    public static void createAllTables() {
        h2.orm.core.service.TableManager.createAll();
    }

    /**
     * Create table for specific entity
     * Usage: H2ORM.createTable(User.class);
     */
    public static void createTable(Class<?> entityClass) {
        h2.orm.core.service.TableManager.create(entityClass);
    }

    /**
     * Drop table for specific entity
     * Usage: H2ORM.dropTable(User.class);
     */
    public static void dropTable(Class<?> entityClass) {
        h2.orm.core.service.TableManager.drop(entityClass);
    }

    /**
     * Drop all tables
     * Usage: H2ORM.dropAllTables();
     */
    public static void dropAllTables() {
        h2.orm.core.service.TableManager.dropAll();
    }

    /**
     * Truncate table (remove all data but keep structure)
     * Usage: H2ORM.truncateTable(User.class);
     */
    public static void truncateTable(Class<?> entityClass) {
        h2.orm.core.service.TableManager.truncate(entityClass);
    }

    /**
     * Truncate all tables
     * Usage: H2ORM.truncateAllTables();
     */
    public static void truncateAllTables() {
        h2.orm.core.service.TableManager.truncateAll();
    }

    /**
     * Check if table exists
     * Usage: boolean exists = H2ORM.tableExists("users");
     */
    public static boolean tableExists(String tableName) {
        return h2.orm.core.service.TableManager.exists(tableName);
    }

    /**
     * Get table row count
     * Usage: long count = H2ORM.getTableRowCount(User.class);
     */
    public static long getTableRowCount(Class<?> entityClass) {
        return h2.orm.core.service.TableManager.getByRowCount(entityClass);
    }

    /**
     * Get table information
     * Usage: TableInfo info = H2ORM.getTableInfo(User.class);
     */
    public static h2.orm.core.service.TableManager.TableInfo getTableInfo(Class<?> entityClass) {
        return h2.orm.core.service.TableManager.getInfo(entityClass);
    }

    /**
     * Get all table information
     * Usage: List<TableInfo> allTables = H2ORM.getAllTableInfo();
     */
    public static java.util.List<h2.orm.core.service.TableManager.TableInfo> getAllTableInfo() {
        return h2.orm.core.service.TableManager.getAllTableInfo();
    }

    /**
     * Reset auto-increment counter for table
     * Usage: H2ORM.resetAutoIncrement(User.class);
     */
    public static void resetAutoIncrement(Class<?> entityClass) {
        h2.orm.core.service.TableManager.resetAutoIncrement(entityClass);
    }
}
