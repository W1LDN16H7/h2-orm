package h2.orm.core.service;

import h2.orm.core.EntityManagerProvider;
import h2.orm.core.MetadataManager;
import h2.orm.core.TransactionManager;
import h2.orm.exception.ExceptionHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.metamodel.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Professional table management utility for H2-ORM
 * Provides comprehensive database schema operations
 */
public class TableManager {

    private static final Logger logger = LoggerFactory.getLogger(TableManager.class);

    /**
     * Create tables for all registered entities
     */
    public static void createAll() {
        ExceptionHandler.safeExecute("create all tables", () -> {
            Set<EntityType<?>> entityTypes = MetadataManager.getAllEntityTypes();
            logger.info("Creating tables for {} entities", entityTypes.size());

            for (EntityType<?> entityType : entityTypes) {
                create(entityType.getJavaType());
            }

            logger.info("Successfully created all tables");
        });
    }

    /**
     * Create table for specific entity
     */
    public static void create(Class<?> entityClass) {
        ExceptionHandler.safeExecute("create table for " + entityClass.getSimpleName(), () -> {
            String tableName = MetadataManager.getTableName(entityClass);

            if (exists(tableName)) {
                logger.info("Table {} already exists", tableName);
                return;
            }

            String createTableSql = generateCreateTableSql(entityClass);
            executeDDL(createTableSql);

            logger.info("Created table: {}", tableName);
        });
    }

    /**
     * Drop table for specific entity
     */
    public static void drop(Class<?> entityClass) {
        ExceptionHandler.safeExecute("drop table for " + entityClass.getSimpleName(), () -> {
            String tableName = MetadataManager.getTableName(entityClass);

            if (!exists(tableName)) {
                logger.warn("Table {} does not exist", tableName);
                return;
            }

            String dropTableSql = "DROP TABLE IF EXISTS " + tableName;
            executeDDL(dropTableSql);

            logger.info("Dropped table: {}", tableName);
        });
    }

    /**
     * Drop all tables
     */
    public static void dropAll() {
        ExceptionHandler.safeExecute("drop all tables", () -> {
            Set<EntityType<?>> entityTypes = MetadataManager.getAllEntityTypes();
            logger.info("Dropping tables for {} entities", entityTypes.size());

            // Drop in reverse order to handle foreign key constraints
            List<EntityType<?>> entityList = new ArrayList<>(entityTypes);
            Collections.reverse(entityList);

            for (EntityType<?> entityType : entityList) {
                drop(entityType.getJavaType());
            }

            logger.info("Successfully dropped all tables");
        });
    }

    /**
     * Truncate table (remove all data but keep structure)
     */
    public static void truncate(Class<?> entityClass) {
        ExceptionHandler.safeExecute("truncate table for " + entityClass.getSimpleName(), () -> {
            String tableName = MetadataManager.getTableName(entityClass);

            if (!exists(tableName)) {
                logger.warn("Table {} does not exist", tableName);
                return;
            }

            TransactionManager.executeInTransaction(em -> {
                // Use DELETE instead of TRUNCATE for better compatibility
                String deleteSql = "DELETE FROM " + tableName;
                Query query = em.createNativeQuery(deleteSql);
                int deletedRows = query.executeUpdate();

                logger.info("Truncated table {}: {} rows deleted", tableName, deletedRows);
                return null;
            });
        });
    }

    /**
     * Truncate all tables
     */
    public static void truncateAll() {
        ExceptionHandler.safeExecute("truncate all tables", () -> {
            Set<EntityType<?>> entityTypes = MetadataManager.getAllEntityTypes();
            logger.info("Truncating tables for {} entities", entityTypes.size());

            for (EntityType<?> entityType : entityTypes) {
                truncate(entityType.getJavaType());
            }

            logger.info("Successfully truncated all tables");
        });
    }

    /**
     * Check if table exists
     */
    public static boolean exists(String tableName) {
        return ExceptionHandler.safeExecute("check table exists: " + tableName, () -> {
            EntityManager em = EntityManagerProvider.getEntityManager();
            try {
                String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = UPPER(?)";
                Query query = em.createNativeQuery(sql);
                query.setParameter(1, tableName);

                Number count = (Number) query.getSingleResult();
                return count.intValue() > 0;
            } finally {
                EntityManagerProvider.closeEntityManager();
            }
        });
    }

    /**
     * Get all table names
     */
    public static List<String> getAllTableNames() {
        return ExceptionHandler.safeExecute("get all table names", () -> {
            EntityManager em = EntityManagerProvider.getEntityManager();
            try {
                String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
                Query query = em.createNativeQuery(sql);

                @SuppressWarnings("unchecked")
                List<String> tableNames = query.getResultList();
                return tableNames;
            } finally {
                EntityManagerProvider.closeEntityManager();
            }
        });
    }

    /**
     * Get table row count
     */
    public static long getByRowCount(Class<?> entityClass) {
        return ExceptionHandler.safeExecute("get table row count for " + entityClass.getSimpleName(), () -> {
            String tableName = MetadataManager.getTableName(entityClass);

            EntityManager em = EntityManagerProvider.getEntityManager();
            try {
                String sql = "SELECT COUNT(*) FROM " + tableName;
                Query query = em.createNativeQuery(sql);

                Number count = (Number) query.getSingleResult();
                return count.longValue();
            } finally {
                EntityManagerProvider.closeEntityManager();
            }
        });
    }

    /**
     * Get table information
     */
    public static TableInfo getInfo(Class<?> entityClass) {
        return ExceptionHandler.safeExecute("get table info for " + entityClass.getSimpleName(), () -> {
            String tableName = MetadataManager.getTableName(entityClass);

            TableInfo info = new TableInfo();
            info.setTableName(tableName);
            info.setEntityClass(entityClass);
            info.setExists(exists(tableName));

            if (info.isExists()) {
                info.setRowCount(getByRowCount(entityClass));
                info.setColumns(getTableColumns(tableName));
            }

            return info;
        });
    }

    /**
     * Get all table information
     */
    public static List<TableInfo> getAllTableInfo() {
        return ExceptionHandler.safeExecute("get all table info", () -> {
            Set<EntityType<?>> entityTypes = MetadataManager.getAllEntityTypes();

            return entityTypes.stream()
                    .map(entityType -> getInfo(entityType.getJavaType()))
                    .collect(Collectors.toList());
        });
    }

    /**
     * Reset auto-increment counter for table
     */
    public static void resetAutoIncrement(Class<?> entityClass) {
        ExceptionHandler.safeExecute("reset auto increment for " + entityClass.getSimpleName(), () -> {
            String tableName = MetadataManager.getTableName(entityClass);

            TransactionManager.executeInTransaction(em -> {
                // H2 specific syntax for resetting auto increment
                String sql = "ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1";
                try {
                    Query query = em.createNativeQuery(sql);
                    query.executeUpdate();
                    logger.info("Reset auto increment for table: {}", tableName);
                } catch (Exception e) {
                    logger.warn("Could not reset auto increment for table {}: {}", tableName, e.getMessage());
                }
                return null;
            });
        });
    }

    // Private helper methods

    private static void executeDDL(String sql) {
        TransactionManager.executeInTransaction(em -> {
            Query query = em.createNativeQuery(sql);
            query.executeUpdate();
            return null;
        });
    }

    private static String generateCreateTableSql(Class<?> entityClass) {
        // This is a simplified version - in a real implementation,
        // you would analyze the entity annotations to generate proper DDL
        String tableName = MetadataManager.getTableName(entityClass);
        return "CREATE TABLE IF NOT EXISTS " + tableName + " (id BIGINT PRIMARY KEY AUTO_INCREMENT)";
    }

    private static List<String> getTableColumns(String tableName) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = UPPER(?) ORDER BY ORDINAL_POSITION";
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, tableName);

            @SuppressWarnings("unchecked")
            List<String> columns = query.getResultList();
            return columns;
        } finally {
            EntityManagerProvider.closeEntityManager();
        }
    }

    /**
     * Table information holder class
     */
    public static class TableInfo {
        private String tableName;
        private Class<?> entityClass;
        private boolean exists;
        private long rowCount;
        private List<String> columns;

        // Getters and setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }

        public Class<?> getEntityClass() { return entityClass; }
        public void setEntityClass(Class<?> entityClass) { this.entityClass = entityClass; }

        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }

        public long getRowCount() { return rowCount; }
        public void setRowCount(long rowCount) { this.rowCount = rowCount; }

        public List<String> getColumns() { return columns; }
        public void setColumns(List<String> columns) { this.columns = columns; }

        @Override
        public String toString() {
            return "TableInfo{" +
                    "tableName='" + tableName + '\'' +
                    ", entityClass=" + (entityClass != null ? entityClass.getSimpleName() : "null") +
                    ", exists=" + exists +
                    ", rowCount=" + rowCount +
                    ", columns=" + (columns != null ? columns.size() : 0) +
                    '}';
        }
    }
}
