package h2.orm.core.repository;

/**
 * Unpaged implementation of Pageable - similar to Spring Data JPA Unpaged
 */
enum Unpaged implements Pageable {

    INSTANCE;

    @Override
    public boolean isPaged() {
        return false;
    }

    @Override
    public Pageable previousOrFirst() {
        return this;
    }

    @Override
    public Pageable next() {
        return this;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

    @Override
    public int getPageSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPageNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getOffset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pageable first() {
        return this;
    }

    @Override
    public Pageable withPage(int pageNumber) {
        if (pageNumber == 0) {
            return this;
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Create unpaged instance with sort
     */
    public static Pageable sorted(Sort sort) {
        return sort.isUnsorted() ? INSTANCE : new UnpagedSorted(sort);
    }

    /**
     * Unpaged implementation with sorting
     */
    static class UnpagedSorted implements Pageable {

        private final Sort sort;

        UnpagedSorted(Sort sort) {
            this.sort = sort;
        }

        @Override
        public boolean isPaged() {
            return false;
        }

        @Override
        public Pageable previousOrFirst() {
            return this;
        }

        @Override
        public Pageable next() {
            return this;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public Sort getSort() {
            return sort;
        }

        @Override
        public int getPageSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getPageNumber() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getOffset() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Pageable first() {
            return this;
        }

        @Override
        public Pageable withPage(int pageNumber) {
            if (pageNumber == 0) {
                return this;
            }
            throw new UnsupportedOperationException();
        }
    }
}
