package h2.orm.core.repository;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Basic Page implementation - similar to Spring Data JPA PageImpl
 */
public class PageImpl<T> implements Page<T> {
    
    private final List<T> content;
    private final Pageable pageable;
    private final long total;
    
    /**
     * Constructor with content and pageable
     */
    public PageImpl(List<T> content, Pageable pageable, long total) {
        this.content = content;
        this.pageable = pageable;
        this.total = total;
    }
    
    /**
     * Constructor with content only (unpaged)
     */
    public PageImpl(List<T> content) {
        this(content, Pageable.unpaged(), content.size());
    }
    
    @Override
    public int getNumber() {
        return pageable.isPaged() ? pageable.getPageNumber() : 0;
    }
    
    @Override
    public int getSize() {
        return pageable.isPaged() ? pageable.getPageSize() : content.size();
    }
    
    @Override
    public int getNumberOfElements() {
        return content.size();
    }
    
    @Override
    public List<T> getContent() {
        return content;
    }
    
    @Override
    public boolean hasContent() {
        return !content.isEmpty();
    }
    
    @Override
    public Sort getSort() {
        return pageable.getSort();
    }
    
    @Override
    public boolean isFirst() {
        return !hasPrevious();
    }
    
    @Override
    public boolean isLast() {
        return !hasNext();
    }
    
    @Override
    public boolean hasNext() {
        return getNumber() + 1 < getTotalPages();
    }
    
    @Override
    public boolean hasPrevious() {
        return getNumber() > 0;
    }
    
    @Override
    public Pageable getPageable() {
        return pageable;
    }
    
    @Override
    public Pageable nextPageable() {
        return hasNext() ? pageable.next() : Pageable.unpaged();
    }
    
    @Override
    public Pageable previousPageable() {
        return hasPrevious() ? pageable.previousOrFirst() : Pageable.unpaged();
    }
    
    @Override
    public long getTotalElements() {
        return total;
    }
    
    @Override
    public int getTotalPages() {
        return pageable.isPaged() ? 
            (int) Math.ceil((double) total / (double) pageable.getPageSize()) : 1;
    }
    
    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        List<U> converted = content.stream().map(converter).collect(Collectors.toList());
        return new PageImpl<>(converted, pageable, total);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof PageImpl)) {
            return false;
        }
        
        PageImpl<?> that = (PageImpl<?>) obj;
        return Objects.equals(this.content, that.content) &&
                Objects.equals(this.pageable, that.pageable) &&
                this.total == that.total;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(content, pageable, total);
    }
    
    @Override
    public String toString() {
        return String.format("Page %d of %d containing %s instances", 
                getNumber() + 1, getTotalPages(), content.getClass().getSimpleName());
    }
}
