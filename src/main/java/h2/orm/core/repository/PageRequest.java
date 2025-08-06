package h2.orm.core.repository;

import java.util.Objects;

/**
 * Basic Java Bean implementation of Pageable - similar to Spring Data JPA PageRequest
 */
public class PageRequest implements Pageable {

    private final int page;
    private final int size;
    private final Sort sort;

    /**
     * Creates a new PageRequest with sort parameters applied
     */
    protected PageRequest(int page, int size, Sort sort) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero!");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one!");
        }

        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    /**
     * Creates a new unsorted PageRequest
     */
    public static PageRequest of(int page, int size) {
        return of(page, size, Sort.unsorted());
    }

    /**
     * Creates a new PageRequest with sort parameters applied
     */
    public static PageRequest of(int page, int size, Sort sort) {
        return new PageRequest(page, size, sort);
    }

    /**
     * Creates a new PageRequest with sort direction and properties applied
     */
    public static PageRequest of(int page, int size, Sort.Direction direction, String... properties) {
        return of(page, size, Sort.by(direction, properties));
    }

    /**
     * Creates a new PageRequest for the first page
     */
    public static PageRequest ofSize(int pageSize) {
        return PageRequest.of(0, pageSize);
    }

    @Override
    public int getPageNumber() {
        return page;
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return (long) page * (long) size;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new PageRequest(getPageNumber() + 1, getPageSize(), getSort());
    }

    public Pageable previous() {
        return getPageNumber() == 0 ? this : new PageRequest(getPageNumber() - 1, getPageSize(), getSort());
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public Pageable first() {
        return new PageRequest(0, getPageSize(), getSort());
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new PageRequest(pageNumber, getPageSize(), getSort());
    }

    @Override
    public boolean hasPrevious() {
        return page > 0;
    }

    /**
     * Create new PageRequest with different sort
     */
    public PageRequest withSort(Sort sort) {
        return new PageRequest(this.page, this.size, sort);
    }

    /**
     * Create new PageRequest with sort direction and properties
     */
    public PageRequest withSort(Sort.Direction direction, String... properties) {
        return new PageRequest(this.page, this.size, Sort.by(direction, properties));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PageRequest)) {
            return false;
        }

        PageRequest that = (PageRequest) obj;
        return this.page == that.page && this.size == that.size && Objects.equals(this.sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, sort);
    }

    @Override
    public String toString() {
        return String.format("Page request [number: %d, size %d, sort: %s]",
                getPageNumber(), getPageSize(), sort);
    }
}
