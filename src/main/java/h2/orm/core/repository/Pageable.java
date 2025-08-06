package h2.orm.core.repository;

import java.util.Optional;

/**
 * Abstract interface for pagination information - similar to Spring Data JPA Pageable
 */
public interface Pageable {

    /**
     * Get the page number (zero-based)
     */
    int getPageNumber();

    /**
     * Get the page size
     */
    int getPageSize();

    /**
     * Get the offset (calculated from page number and size)
     */
    long getOffset();

    /**
     * Get the sort information
     */
    Sort getSort();

    /**
     * Get the sort information or default sort if unsorted
     */
    default Sort getSortOr(Sort sort) {
        return getSort().isSorted() ? getSort() : sort;
    }

    /**
     * Get next pageable
     */
    Pageable next();

    /**
     * Get previous pageable or first if already at first page
     */
    Pageable previousOrFirst();

    /**
     * Get first pageable
     */
    Pageable first();

    /**
     * Get pageable with new page number
     */
    Pageable withPage(int pageNumber);

    /**
     * Check if there is a previous page
     */
    boolean hasPrevious();

    /**
     * Check if this is the first page
     */
    default boolean isFirst() {
        return !hasPrevious();
    }

    /**
     * Get unpaged instance
     */
    static Pageable unpaged() {
        return Unpaged.INSTANCE;
    }

    /**
     * Get unpaged instance with sort
     */
    static Pageable unpaged(Sort sort) {
        return Unpaged.sorted(sort);
    }

    /**
     * Create pageable with page and size
     */
    static Pageable ofSize(int pageSize) {
        return PageRequest.of(0, pageSize);
    }

    /**
     * Check if this pageable is paged
     */
    default boolean isPaged() {
        return true;
    }

    /**
     * Check if this pageable is unpaged
     */
    default boolean isUnpaged() {
        return !isPaged();
    }

    /**
     * Get optional previous pageable
     */
    default Optional<Pageable> toOptional() {
        return isUnpaged() ? Optional.empty() : Optional.of(this);
    }
}
