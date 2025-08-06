package h2.orm.core;

import jakarta.persistence.*;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Core metadata manager for H2-ORM
 * Handles entity metadata, reflection, and schema information
 */
public class MetadataManager {

    private static final Logger logger = LoggerFactory.getLogger(MetadataManager.class);
    private static final Map<Class<?>, EntityMetadata> metadataCache = new HashMap<>();

    /**
     * Get entity metadata with caching
     */
    public static EntityMetadata getEntityMetadata(Class<?> entityClass) {
        return metadataCache.computeIfAbsent(entityClass, MetadataManager::buildEntityMetadata);
    }

    /**
     * Get all managed entity types
     */
    public static Set<EntityType<?>> getAllEntityTypes() {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            Metamodel metamodel = em.getMetamodel();
            return metamodel.getEntities();
        } finally {
            EntityManagerProvider.closeEntityManager();
        }
    }

    /**
     * Get table name for entity
     */
    public static String getTableName(Class<?> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return entityClass.getSimpleName().toLowerCase();
    }

    /**
     * Get ID field for entity
     */
    public static Field getIdField(Class<?> entityClass) {
        return getEntityMetadata(entityClass).getIdField();
    }

    /**
     * Get ID value from entity instance
     */
    public static Object getIdValue(Object entity) {
        try {
            Field idField = getIdField(entity.getClass());
            if (idField != null) {
                idField.setAccessible(true);
                return idField.get(entity);
            }
        } catch (Exception e) {
            logger.warn("Failed to get ID value from entity", e);
        }
        return null;
    }

    /**
     * Check if entity is new (ID is null or zero)
     */
    public static boolean isEntityNew(Object entity) {
        Object idValue = getIdValue(entity);
        if (idValue == null) {
            return true;
        }
        if (idValue instanceof Number) {
            return ((Number) idValue).longValue() == 0;
        }
        return false;
    }

    /**
     * Get all fields with JPA annotations
     */
    public static List<Field> getJpaFields(Class<?> entityClass) {
        return getEntityMetadata(entityClass).getJpaFields();
    }

    /**
     * Get column name for field
     */
    public static String getColumnName(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        }
        return field.getName();
    }

    /**
     * Build entity metadata
     */
    private static EntityMetadata buildEntityMetadata(Class<?> entityClass) {
        logger.debug("Building metadata for entity: {}", entityClass.getSimpleName());

        EntityMetadata metadata = new EntityMetadata();
        metadata.setEntityClass(entityClass);
        metadata.setTableName(getTableName(entityClass));

        List<Field> jpaFields = new ArrayList<>();
        Field idField = null;

        // Process all fields including inherited ones
        Class<?> currentClass = entityClass;
        while (currentClass != null && !currentClass.equals(Object.class)) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (hasJpaAnnotation(field)) {
                    jpaFields.add(field);

                    if (field.isAnnotationPresent(Id.class)) {
                        idField = field;
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        metadata.setJpaFields(jpaFields);
        metadata.setIdField(idField);

        return metadata;
    }

    /**
     * Check if field has JPA annotations
     */
    private static boolean hasJpaAnnotation(Field field) {
        return field.isAnnotationPresent(Id.class) ||
               field.isAnnotationPresent(Column.class) ||
               field.isAnnotationPresent(JoinColumn.class) ||
               field.isAnnotationPresent(OneToOne.class) ||
               field.isAnnotationPresent(OneToMany.class) ||
               field.isAnnotationPresent(ManyToOne.class) ||
               field.isAnnotationPresent(ManyToMany.class) ||
               field.isAnnotationPresent(Embedded.class) ||
               field.isAnnotationPresent(GeneratedValue.class);
    }

    /**
     * Clear metadata cache
     */
    public static void clearCache() {
        metadataCache.clear();
        logger.debug("Metadata cache cleared");
    }

    /**
     * Entity metadata holder class
     */
    public static class EntityMetadata {
        private Class<?> entityClass;
        private String tableName;
        private Field idField;
        private List<Field> jpaFields;

        // Getters and setters
        public Class<?> getEntityClass() { return entityClass; }
        public void setEntityClass(Class<?> entityClass) { this.entityClass = entityClass; }

        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }

        public Field getIdField() { return idField; }
        public void setIdField(Field idField) { this.idField = idField; }

        public List<Field> getJpaFields() { return jpaFields; }
        public void setJpaFields(List<Field> jpaFields) { this.jpaFields = jpaFields; }

        @Override
        public String toString() {
            return "EntityMetadata{" +
                    "entityClass=" + entityClass.getSimpleName() +
                    ", tableName='" + tableName + '\'' +
                    ", idField=" + (idField != null ? idField.getName() : "null") +
                    ", jpaFieldsCount=" + (jpaFields != null ? jpaFields.size() : 0) +
                    '}';
        }
    }
}
