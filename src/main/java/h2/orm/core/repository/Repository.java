package h2.orm.core.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Base repository interface similar to Spring Data JPA
 * Provides standard CRUD operations for entities
 */
public interface Repository<T, ID extends Serializable> {

    /**
     * Save an entity
     */
    T save(T entity);

    /**
     * Save all entities
     */
    List<T> saveAll(Iterable<T> entities);

    /**
     * Find entity by ID
     */
    Optional<T> findById(ID id);

    /**
     * Check if entity exists by ID
     */
    boolean existsById(ID id);

    /**
     * Find all entities
     */
    List<T> findAll();

    /**
     * Find all entities by IDs
     */
    List<T> findAllById(Iterable<ID> ids);

    /**
     * Count all entities
     */
    long count();

    /**
     * Delete entity by ID
     */
    void deleteById(ID id);

    /**
     * Delete entity
     */
    void delete(T entity);

    /**
     * Delete all entities by IDs
     */
    void deleteAllById(Iterable<ID> ids);

    /**
     * Delete all entities
     */
    void deleteAll(Iterable<T> entities);

    /**
     * Delete all entities
     */
    void deleteAll();
}
