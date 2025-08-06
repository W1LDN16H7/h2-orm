package h2.orm.core.repository;

import java.io.Serializable;
import java.util.List;

/**
 * JPA Repository interface similar to Spring Data JPA
 * Extends base Repository with additional JPA-specific methods
 */
public interface JpaRepository<T, ID extends Serializable> extends Repository<T, ID> {

    /**
     * Flush changes to database
     */
    void flush();

    /**
     * Save and flush entity
     */
    T saveAndFlush(T entity);

    /**
     * Save all and flush entities
     */
    List<T> saveAllAndFlush(Iterable<T> entities);

    /**
     * Delete entities in batch
     */
    void deleteInBatch(Iterable<T> entities);

    /**
     * Delete all entities in batch
     */
    void deleteAllInBatch();

    /**
     * Delete all entities in batch by IDs
     */
    void deleteAllByIdInBatch(Iterable<ID> ids);

    /**
     * Get reference to entity (lazy loading)
     */
    T getOne(ID id);

    /**
     * Get reference to entity (lazy loading) - JPA 3.0 style
     */
    T getReferenceById(ID id);
}
