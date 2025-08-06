package h2.orm.core.repository;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * A page is a sublist of a list of objects - similar to Spring Data JPA Page
 */
public interface Page<T> extends Iterable<T> {
    
    /**
     * Get the number of the current page
     */
    int getNumber();
    
    /**
     * Get the size of the page
     */
    int getSize();
    
    /**
     * Get the number of elements currently on this page
     */
    int getNumberOfElements();
    
    /**
     * Get the content as List
     */
    List<T> getContent();
    
    /**
     * Check if the page has content
     */
    boolean hasContent();
    
    /**
     * Get the sorting parameters for the page
     */
    Sort getSort();
    
    /**
     * Check if this is the first page
     */
    boolean isFirst();
    
    /**
     * Check if this is the last page
     */
    boolean isLast();
    
    /**
     * Check if there is a next page
     */
    boolean hasNext();
    
    /**
     * Check if there is a previous page
     */
    boolean hasPrevious();
    
    /**
     * Get the pageable descriptor that was used to request this page
     */
    Pageable getPageable();
    
    /**
     * Get the pageable to request the next page
     */
    Pageable nextPageable();
    
    /**
     * Get the pageable to request the previous page
     */
    Pageable previousPageable();
    
    /**
     * Get the total number of elements
     */
    long getTotalElements();
    
    /**
     * Get the total number of pages
     */
    int getTotalPages();
    
    /**
     * Convert the content of the page to another type
     */
    <U> Page<U> map(Function<? super T, ? extends U> converter);
    
    @Override
    default Iterator<T> iterator() {
        return getContent().iterator();
    }
    
    /**
     * Create empty page
     */
    static <T> Page<T> empty() {
        return empty(Pageable.unpaged());
    }
    
    /**
     * Create empty page with pageable
     */
    static <T> Page<T> empty(Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 0);
    }
}
