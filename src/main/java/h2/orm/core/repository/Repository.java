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
     * Find all entities with sort
     */
    List<T> findAll(Sort sort);

    /**
     * Find all entities with pagination
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Find entities by field value
     */
    List<T> findByField(String fieldName, Object value);

    /**
     * Find entities by field value with sorting
     */
    List<T> findByField(String fieldName, Object value, Sort sort);

    /**
     * Find entities by field value with pagination
     */
    Page<T> findByField(String fieldName, Object value, Pageable pageable);

    /**
     * Find entities by multiple field values (AND condition)
     */
    List<T> findByFields(java.util.Map<String, Object> fieldValues);

    /**
     * Find entities by multiple field values with sorting
     */
    List<T> findByFields(java.util.Map<String, Object> fieldValues, Sort sort);

    /**
     * Find entities by multiple field values with pagination
     */
    Page<T> findByFields(java.util.Map<String, Object> fieldValues, Pageable pageable);

    /**
     * Find first entity by field value
     */
    Optional<T> findFirstByField(String fieldName, Object value);

    /**
     * Check if any entity exists with field value
     */
    boolean existsByField(String fieldName, Object value);

    /**
     * Count entities by field value
     */
    long countByField(String fieldName, Object value);

    /**
     * Delete entities by field value
     */
    void deleteByField(String fieldName, Object value);

    /**
     * Find entities where field is null
     */
    List<T> findByFieldIsNull(String fieldName);

    /**
     * Find entities where field is not null
     */
    List<T> findByFieldIsNotNull(String fieldName);

    /**
     * Find entities where field value is in given collection
     */
    List<T> findByFieldIn(String fieldName, java.util.Collection<?> values);

    /**
     * Find entities where field value is between two values (for numbers/dates)
     */
    List<T> findByFieldBetween(String fieldName, Object startValue, Object endValue);

    /**
     * Find entities where string field contains given value
     */
    List<T> findByFieldContaining(String fieldName, String value);

    /**
     * Find entities where string field starts with given value
     */
    List<T> findByFieldStartingWith(String fieldName, String value);

    /**
     * Find entities where string field ends with given value
     */
    List<T> findByFieldEndingWith(String fieldName, String value);

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
