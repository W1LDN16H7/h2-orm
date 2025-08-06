package h2.orm.core.repository;

import h2.orm.core.EntityManagerProvider;
import h2.orm.core.MetadataManager;
import h2.orm.core.QueryExecutor;
import h2.orm.core.TransactionManager;
import h2.orm.exception.ExceptionHandler;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract JPA Repository implementation similar to Spring Data JPA
 * Now uses QueryExecutor for optimized query performance
 */
public abstract class AbstractRepository<T, ID extends Serializable> implements JpaRepository<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    // SECURITY ADDITION: Input validation constants
    private static final int MAX_PAGE_SIZE = 1000;
    private static final int MAX_BATCH_SIZE = 1000;

    protected final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public AbstractRepository() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    protected AbstractRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    @Override
    public T save(T entity) {
        return ExceptionHandler.safeExecute("save entity", () -> {
            return TransactionManager.executeInTransaction(em -> {
                if (MetadataManager.isEntityNew(entity)) {
                    em.persist(entity);
                    return entity;
                } else {
                    return em.merge(entity);
                }
            });
        });
    }

    @Override
    public List<T> saveAll(Iterable<T> entities) {
        return ExceptionHandler.safeExecute("save all entities", () -> {
            return TransactionManager.executeInTransaction(em -> {
                List<T> savedEntities = new ArrayList<>();
                int batchSize = 20;
                int count = 0;

                for (T entity : entities) {
                    T savedEntity;
                    if (MetadataManager.isEntityNew(entity)) {
                        em.persist(entity);
                        savedEntity = entity;
                    } else {
                        savedEntity = em.merge(entity);
                    }
                    savedEntities.add(savedEntity);

                    if (++count % batchSize == 0) {
                        em.flush();
                        em.clear();
                    }
                }

                em.flush();
                return savedEntities;
            });
        });
    }

    @Override
    public Optional<T> findById(ID id) {
        return ExceptionHandler.safeExecute("find entity by ID", () -> {
            EntityManager em = getEntityManager();
            try {
                T entity = em.find(entityClass, id);
                return Optional.ofNullable(entity);
            } finally {
                EntityManagerProvider.closeEntityManager();
            }
        });
    }

    @Override
    public boolean existsById(ID id) {
        return ExceptionHandler.safeExecute("check entity exists", () -> {
            // Use QueryExecutor for optimized exists check
            String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e WHERE e.id = :id";
            Long count = QueryExecutor.executeSingleResultQuery(jpql, Long.class, Map.of("id", id));
            return count > 0;
        });
    }

    @Override
    public List<T> findAll() {
        return ExceptionHandler.safeExecute("find all entities", () -> {
            // Use QueryExecutor for optimized query execution
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            return QueryExecutor.executeQuery(jpql, entityClass);
        });
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        return ExceptionHandler.safeExecute("find entities by IDs", () -> {
            List<ID> idList = new ArrayList<>();
            ids.forEach(idList::add);

            if (idList.isEmpty()) {
                return new ArrayList<>();
            }

            // Use QueryExecutor for batch ID lookup
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.id IN :ids";
            return QueryExecutor.executeQuery(jpql, entityClass, Map.of("ids", idList));
        });
    }

    @Override
    public long count() {
        return ExceptionHandler.safeExecute("count entities", () -> {
            // Use QueryExecutor for optimized count query
            String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            return QueryExecutor.executeSingleResultQuery(jpql, Long.class);
        });
    }

    @Override
    public void deleteById(ID id) {
        ExceptionHandler.safeExecute("delete entity by ID", () -> {
            // Use QueryExecutor for optimized delete
            String jpql = "DELETE FROM " + entityClass.getSimpleName() + " e WHERE e.id = :id";
            QueryExecutor.executeUpdate(jpql, Map.of("id", id));
        });
    }

    @Override
    public void delete(T entity) {
        ExceptionHandler.safeExecute("delete entity", () -> {
            TransactionManager.executeInTransaction(em -> {
                T managedEntity = em.contains(entity) ? entity : em.merge(entity);
                em.remove(managedEntity);
                return null;
            });
        });
    }

    @Override
    public void deleteAllById(Iterable<ID> ids) {
        ExceptionHandler.safeExecute("delete entities by IDs", () -> {
            List<ID> idList = new ArrayList<>();
            ids.forEach(idList::add);

            if (!idList.isEmpty()) {
                // Use QueryExecutor for batch delete
                String jpql = "DELETE FROM " + entityClass.getSimpleName() + " e WHERE e.id IN :ids";
                QueryExecutor.executeUpdate(jpql, Map.of("ids", idList));
            }
        });
    }

    @Override
    public void deleteAll(Iterable<T> entities) {
        ExceptionHandler.safeExecute("delete entities", () -> {
            TransactionManager.executeInTransaction(em -> {
                for (T entity : entities) {
                    T managedEntity = em.contains(entity) ? entity : em.merge(entity);
                    em.remove(managedEntity);
                }
                return null;
            });
        });
    }

    @Override
    public void deleteAll() {
        ExceptionHandler.safeExecute("delete all entities", () -> {
            // Use QueryExecutor for optimized bulk delete
            String jpql = "DELETE FROM " + entityClass.getSimpleName();
            QueryExecutor.executeUpdate(jpql);
        });
    }

    @Override
    public void flush() {
        ExceptionHandler.safeExecute("flush changes", () -> {
            TransactionManager.executeInTransaction(em -> {
                em.flush();
                return null;
            });
        });
    }

    @Override
    public T saveAndFlush(T entity) {
        return ExceptionHandler.safeExecute("save and flush entity", () -> {
            return TransactionManager.executeInTransaction(em -> {
                T savedEntity;
                if (MetadataManager.isEntityNew(entity)) {
                    em.persist(entity);
                    savedEntity = entity;
                } else {
                    savedEntity = em.merge(entity);
                }
                em.flush();
                return savedEntity;
            });
        });
    }

    @Override
    public List<T> saveAllAndFlush(Iterable<T> entities) {
        return ExceptionHandler.safeExecute("save all and flush entities", () -> {
            return TransactionManager.executeInTransaction(em -> {
                List<T> savedEntities = new ArrayList<>();
                for (T entity : entities) {
                    T savedEntity;
                    if (MetadataManager.isEntityNew(entity)) {
                        em.persist(entity);
                        savedEntity = entity;
                    } else {
                        savedEntity = em.merge(entity);
                    }
                    savedEntities.add(savedEntity);
                }
                em.flush();
                return savedEntities;
            });
        });
    }

    @Override
    public void deleteInBatch(Iterable<T> entities) {
        ExceptionHandler.safeExecute("delete entities in batch", () -> {
            // Extract IDs and use QueryExecutor for batch delete
            List<ID> ids = new ArrayList<>();
            for (T entity : entities) {
                ID id = MetadataManager.getEntityId(entity);
                if (id != null) {
                    ids.add(id);
                }
            }
            if (!ids.isEmpty()) {
                String jpql = "DELETE FROM " + entityClass.getSimpleName() + " e WHERE e.id IN :ids";
                QueryExecutor.executeUpdate(jpql, Map.of("ids", ids));
            }
        });
    }

    @Override
    public void deleteAllInBatch() {
        ExceptionHandler.safeExecute("delete all entities in batch", () -> {
            String jpql = "DELETE FROM " + entityClass.getSimpleName();
            QueryExecutor.executeUpdate(jpql);
        });
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<ID> ids) {
        ExceptionHandler.safeExecute("delete entities by IDs in batch", () -> {
            List<ID> idList = new ArrayList<>();
            ids.forEach(idList::add);
            if (!idList.isEmpty()) {
                String jpql = "DELETE FROM " + entityClass.getSimpleName() + " e WHERE e.id IN :ids";
                QueryExecutor.executeUpdate(jpql, Map.of("ids", idList));
            }
        });
    }

    @Override
    public T getOne(ID id) {
        return ExceptionHandler.safeExecute("get entity reference", () -> {
            EntityManager em = getEntityManager();
            try {
                return em.getReference(entityClass, id);
            } finally {
                EntityManagerProvider.closeEntityManager();
            }
        });
    }

    @Override
    public T getReferenceById(ID id) {
        return getOne(id);
    }

    // Sorting and Paging implementations

    @Override
    public List<T> findAll(Sort sort) {
        return ExceptionHandler.safeExecute("find all entities with sort", () -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            if (sort.isSorted()) {
                // Validate sort fields before executing query
                Sort validatedSort = validateAndFilterSort(sort);
                if (validatedSort.isSorted()) {
                    jpql += validatedSort.toOrderByClause("e");
                }
            }
            return QueryExecutor.executeQuery(jpql, entityClass);
        });
    }

    /**
     * Validates sort fields and filters out invalid ones to prevent query failures
     */
    private Sort validateAndFilterSort(Sort sort) {
        if (sort.isUnsorted()) {
            return sort;
        }

        List<Sort.Order> validOrders = new ArrayList<>();
        for (Sort.Order order : sort) {
            if (isValidSortField(order.getProperty())) {
                validOrders.add(order);
            } else {
                // Log warning but don't fail - just skip invalid fields
                logger.warn("Ignoring invalid sort field: {} for entity: {}",
                           order.getProperty(), entityClass.getSimpleName());
            }
        }

        return validOrders.isEmpty() ? Sort.unsorted() : Sort.by(validOrders.toArray(new Sort.Order[0]));
    }

    /**
     * SECURITY ENHANCED: Improved sort field validation
     */
    private boolean isValidSortField(String fieldName) {
        // SECURITY CHECK: Basic validation
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return false;
        }

        // SECURITY CHECK: Length validation
        if (fieldName.length() > 50) {
            logger.warn("Sort field name too long: {}", fieldName);
            return false;
        }

        // SECURITY CHECK: Pattern validation (only allow alphanumeric and underscore)
        if (!fieldName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            logger.warn("Invalid sort field name pattern: {}", fieldName);
            return false;
        }

        try {
            // Use reflection to check if the field exists on the entity class
            entityClass.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            // Also check for JPA attribute mappings using EntityManager metamodel
            try {
                EntityManager em = getEntityManager();
                var metamodel = em.getMetamodel();
                var entityType = metamodel.entity(entityClass);
                entityType.getAttribute(fieldName);
                return true;
            } catch (Exception ex) {
                logger.debug("Field not found in entity or metamodel: {}", fieldName);
                return false;
            }
        } catch (Exception e) {
            logger.warn("Error validating sort field: {}", fieldName, e);
            return false;
        }
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return ExceptionHandler.safeExecute("find all entities with pagination", () -> {
            // SECURITY FIX: Validate pagination parameters
            validatePaginationSecurity(pageable);

            // First, get the total count
            String countJpql = "SELECT COUNT(e) FROM " + getSecureEntityName() + " e";
            Long total = QueryExecutor.executeSingleResultQuery(countJpql, Long.class);

            if (total == 0) {
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            // Then get the actual data
            String jpql = "SELECT e FROM " + getSecureEntityName() + " e";
            if (pageable.getSort().isSorted()) {
                // SECURITY FIX: Validate sort fields before adding to query
                Sort validatedSort = validateAndFilterSort(pageable.getSort());
                if (validatedSort.isSorted()) {
                    jpql += validatedSort.toOrderByClause("e");
                }
            }

            EntityManager em = getEntityManager();
            try {
                var query = em.createQuery(jpql, entityClass);

                if (pageable.isPaged()) {
                    query.setFirstResult((int) pageable.getOffset());
                    query.setMaxResults(pageable.getPageSize());
                }

                List<T> content = query.getResultList();
                return new PageImpl<>(content, pageable, total);
            } finally {
                EntityManagerProvider.closeEntityManager();
            }
        });
    }

    /**
     * SECURITY ADDITION: Validate pagination parameters to prevent DoS
     */
    private void validatePaginationSecurity(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new SecurityException("Page size too large: " + pageable.getPageSize() +
                                      ". Maximum allowed: " + MAX_PAGE_SIZE);
        }

        if (pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page size must be positive: " + pageable.getPageSize());
        }

        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("Page number cannot be negative: " + pageable.getPageNumber());
        }

        // Prevent extremely large offset calculations that could cause overflow
        long offset = pageable.getOffset();
        if (offset > Integer.MAX_VALUE) {
            throw new SecurityException("Page offset too large: " + offset);
        }
    }

    /**
     * SECURITY ADDITION: Get validated entity name to prevent injection
     * Updated to be flexible while maintaining security
     */
    private String getSecureEntityName() {
        String entityName = entityClass.getSimpleName();

        // FLEXIBLE SECURITY: Validate the name pattern instead of using rigid whitelist
        // This allows any valid Java class name while preventing injection
        if (!entityName.matches("^[A-Z][a-zA-Z0-9]*$")) {
            throw new SecurityException("Invalid entity name pattern: " + entityName +
                                      ". Entity names must start with uppercase letter and contain only alphanumeric characters.");
        }

        // Additional length check for security
        if (entityName.length() > 100) {
            throw new SecurityException("Entity name too long: " + entityName.length() + " characters");
        }

        return entityName;
    }

    @Override
    public Page<T> findByField(String fieldName, Object value, Pageable pageable) {
        return ExceptionHandler.safeExecute("find entities by field " + fieldName + " with pagination", () -> {
            // Get count first
            String countJpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :value";
            Long total = QueryExecutor.executeSingleResultQuery(countJpql, Long.class, Map.of("value", value));

            if (total == 0) {
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            // Get data
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :value";
            if (pageable.getSort().isSorted()) {
                jpql += pageable.getSort().toOrderByClause("e");
            }

            EntityManager em = getEntityManager();
            try {
                var query = em.createQuery(jpql, entityClass);
                query.setParameter("value", value);

                if (pageable.isPaged()) {
                    query.setFirstResult((int) pageable.getOffset());
                    query.setMaxResults(pageable.getPageSize());
                }

                List<T> content = query.getResultList();
                return new PageImpl<>(content, pageable, total);
            } finally {
                EntityManagerProvider.closeEntityManager();
            }
        });
    }


    @Override
    public List<T> findByField(String fieldName, Object value) {
        return ExceptionHandler.safeExecute("find entities by field " + fieldName, () -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :value";
            return QueryExecutor.executeQuery(jpql, entityClass, Map.of("value", value));
        });
    }

    @Override
    public List<T> findByField(String fieldName, Object value, Sort sort) {
        return ExceptionHandler.safeExecute("find entities by field " + fieldName + " with sort", () -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :value";
            if (sort.isSorted()) {
                jpql += sort.toOrderByClause("e");
            }
            return QueryExecutor.executeQuery(jpql, entityClass, Map.of("value", value));
        });
    }

    @Override
    public List<T> findByFields(Map<String, Object> fieldValues) {
        return ExceptionHandler.safeExecute("find entities by multiple fields", () -> {
            if (fieldValues.isEmpty()) {
                return findAll();
            }

            StringBuilder jpql = new StringBuilder("SELECT e FROM " + entityClass.getSimpleName() + " e WHERE ");
            Map<String, Object> parameters = new HashMap<>();

            int index = 0;
            for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
                if (index > 0) {
                    jpql.append(" AND ");
                }
                String paramName = "param" + index;
                jpql.append("e.").append(entry.getKey()).append(" = :").append(paramName);
                parameters.put(paramName, entry.getValue());
                index++;
            }

            return QueryExecutor.executeQuery(jpql.toString(), entityClass, parameters);
        });
    }

    @Override
    public List<T> findByFields(Map<String, Object> fieldValues, Sort sort) {
        return ExceptionHandler.safeExecute("find entities by multiple fields with sort", () -> {
            if (fieldValues.isEmpty()) {
                return findAll(sort);
            }

            StringBuilder jpql = new StringBuilder("SELECT e FROM " + entityClass.getSimpleName() + " e WHERE ");
            Map<String, Object> parameters = new HashMap<>();

            int index = 0;
            for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
                if (index > 0) {
                    jpql.append(" AND ");
                }
                String paramName = "param" + index;
                jpql.append("e.").append(entry.getKey()).append(" = :").append(paramName);
                parameters.put(paramName, entry.getValue());
                index++;
            }

            if (sort.isSorted()) {
                jpql.append(sort.toOrderByClause("e"));
            }

            return QueryExecutor.executeQuery(jpql.toString(), entityClass, parameters);
        });
    }

    @Override
    public Page<T> findByFields(Map<String, Object> fieldValues, Pageable pageable) {
        return ExceptionHandler.safeExecute("find entities by multiple fields with pagination", () -> {
            if (fieldValues.isEmpty()) {
                return findAll(pageable);
            }

            StringBuilder whereClause = new StringBuilder(" WHERE ");
            Map<String, Object> parameters = new HashMap<>();

            int index = 0;
            for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
                if (index > 0) {
                    whereClause.append(" AND ");
                }
                String paramName = "param" + index;
                whereClause.append("e.").append(entry.getKey()).append(" = :").append(paramName);
                parameters.put(paramName, entry.getValue());
                index++;
            }

            // Get count
            String countJpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e" + whereClause;
            Long total = QueryExecutor.executeSingleResultQuery(countJpql, Long.class, parameters);

            if (total == 0) {
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            // Get data
            String jpql = MessageFormat.format("SELECT e FROM {0} e{1}", entityClass.getSimpleName(), whereClause);
            if (pageable.getSort().isSorted()) {
                jpql += pageable.getSort().toOrderByClause("e");
            }

            EntityManager em = getEntityManager();
            try {
                var query = em.createQuery(jpql, entityClass);
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }

                if (pageable.isPaged()) {
                    query.setFirstResult((int) pageable.getOffset());
                    query.setMaxResults(pageable.getPageSize());
                }

                List<T> content = query.getResultList();
                return new PageImpl<>(content, pageable, total);
            } finally {
                EntityManagerProvider.closeEntityManager();
            }
        });
    }

    @Override
    public Optional<T> findFirstByField(String fieldName, Object value) {
        return ExceptionHandler.safeExecute("find first entity by field " + fieldName, () -> {
            EntityManager em = getEntityManager();
            try {
                String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :value";
                var query = em.createQuery(jpql, entityClass);
                query.setParameter("value", value);
                query.setMaxResults(1);

                List<T> results = query.getResultList();
                return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            } finally {
                EntityManagerProvider.closeEntityManager();
            }
        });
    }

    @Override
    public boolean existsByField(String fieldName, Object value) {
        return ExceptionHandler.safeExecute("check if entity exists by field " + fieldName, () -> {
            String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :value";
            Long count = QueryExecutor.executeSingleResultQuery(jpql, Long.class, Map.of("value", value));
            return count > 0;
        });
    }

    @Override
    public long countByField(String fieldName, Object value) {
        return ExceptionHandler.safeExecute("count entities by field " + fieldName, () -> {
            String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :value";
            return QueryExecutor.executeSingleResultQuery(jpql, Long.class, Map.of("value", value));
        });
    }

    @Override
    public void deleteByField(String fieldName, Object value) {
        ExceptionHandler.safeExecute("delete entities by field " + fieldName, () -> {
            String jpql = "DELETE FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :value";
            QueryExecutor.executeUpdate(jpql, Map.of("value", value));
        });
    }

    @Override
    public List<T> findByFieldIsNull(String fieldName) {
        return ExceptionHandler.safeExecute("find entities where field " + fieldName + " is null", () -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " IS NULL";
            return QueryExecutor.executeQuery(jpql, entityClass);
        });
    }

    @Override
    public List<T> findByFieldIsNotNull(String fieldName) {
        return ExceptionHandler.safeExecute("find entities where field " + fieldName + " is not null", () -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " IS NOT NULL";
            return QueryExecutor.executeQuery(jpql, entityClass);
        });
    }

    @Override
    public List<T> findByFieldIn(String fieldName, java.util.Collection<?> values) {
        return ExceptionHandler.safeExecute("find entities where field " + fieldName + " is in collection", () -> {
            if (values.isEmpty()) {
                return new ArrayList<>();
            }
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " IN :values";
            return QueryExecutor.executeQuery(jpql, entityClass, Map.of("values", values));
        });
    }

    @Override
    public List<T> findByFieldBetween(String fieldName, Object startValue, Object endValue) {
        return ExceptionHandler.safeExecute("find entities where field " + fieldName + " is between values", () -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " BETWEEN :start AND :end";
            return QueryExecutor.executeQuery(jpql, entityClass, Map.of("start", startValue, "end", endValue));
        });
    }

    @Override
    public List<T> findByFieldContaining(String fieldName, String value) {
        return ExceptionHandler.safeExecute("find entities where field " + fieldName + " contains value", () -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " LIKE :value";
            return QueryExecutor.executeQuery(jpql, entityClass, Map.of("value", "%" + value + "%"));
        });
    }

    @Override
    public List<T> findByFieldStartingWith(String fieldName, String value) {
        return ExceptionHandler.safeExecute("find entities where field " + fieldName + " starts with value", () -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " LIKE :value";
            return QueryExecutor.executeQuery(jpql, entityClass, Map.of("value", value + "%"));
        });
    }

    @Override
    public List<T> findByFieldEndingWith(String fieldName, String value) {
        return ExceptionHandler.safeExecute("find entities where field " + fieldName + " ends with value", () -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " LIKE :value";
            return QueryExecutor.executeQuery(jpql, entityClass, Map.of("value", "%" + value));
        });
    }
}
