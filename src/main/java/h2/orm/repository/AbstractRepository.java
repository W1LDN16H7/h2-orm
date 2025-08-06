package h2.orm.repository;

import h2.orm.core.EntityManagerProvider;
import h2.orm.core.MetadataManager;
import h2.orm.core.QueryExecutor;
import h2.orm.core.TransactionManager;
import h2.orm.exception.ExceptionHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract JPA Repository implementation similar to Spring Data JPA
 * Now uses QueryExecutor for optimized query performance
 */
public abstract class AbstractRepository<T, ID extends Serializable> implements JpaRepository<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

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
                @SuppressWarnings("unchecked")
                ID id = (ID) MetadataManager.getIdValue(entity);
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
            // Use QueryExecutor for optimized bulk delete
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
                // Use QueryExecutor for batch delete
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

    // Additional utility methods using QueryExecutor

    /**
     * Find entities with pagination using QueryExecutor
     */
    public List<T> findAll(int offset, int limit) {
        return ExceptionHandler.safeExecute("find entities with pagination", () -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            return QueryExecutor.executePaginatedQuery(jpql, entityClass, null, offset, limit);
        });
    }

    /**
     * Execute custom JPQL query
     */
    public List<T> findByJpql(String jpql, Map<String, Object> parameters) {
        return ExceptionHandler.safeExecute("execute custom JPQL query", () -> {
            return QueryExecutor.executeQuery(jpql, entityClass, parameters);
        });
    }

    /**
     * Execute custom native SQL query
     */
    public List<T> findByNativeQuery(String sql, Map<String, Object> parameters) {
        return ExceptionHandler.safeExecute("execute custom native query", () -> {
            return QueryExecutor.executeNativeQuery(sql, entityClass, parameters);
        });
    }
}
