package h2.orm.core.repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sort option for repository queries - similar to Spring Data JPA Sort
 * Provides fluent API for building sort criteria
 */
public class Sort implements Iterable<Sort.Order> {

    private static final Sort UNSORTED = new Sort(new ArrayList<>());

    private final List<Order> orders;

    private Sort(List<Order> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
    }

    /**
     * Create Sort instance with multiple orders
     */
    public static Sort by(Order... orders) {
        return new Sort(Arrays.asList(orders));
    }

    /**
     * Create Sort instance with property names (ascending by default)
     */
    public static Sort by(String... properties) {
        if (properties.length == 0) {
            return UNSORTED;
        }
        return by(Arrays.stream(properties)
                .map(Order::asc)
                .toArray(Order[]::new));
    }

    /**
     * Create Sort instance with direction and properties
     */
    public static Sort by(Direction direction, String... properties) {
        return by(Arrays.stream(properties)
                .map(property -> new Order(direction, property))
                .toArray(Order[]::new));
    }

    /**
     * Create Sort instance with direction and property list
     */
    public static Sort by(Direction direction, List<String> properties) {
        return by(direction, properties.toArray(new String[0]));
    }

    /**
     * Get unsorted instance
     */
    public static Sort unsorted() {
        return UNSORTED;
    }

    /**
     * Add ascending order for property
     */
    public Sort and(Sort sort) {
        List<Order> combinedOrders = new ArrayList<>(this.orders);
        combinedOrders.addAll(sort.orders);
        return new Sort(combinedOrders);
    }

    /**
     * Get order for specific property
     */
    public Optional<Order> getOrderFor(String property) {
        return orders.stream()
                .filter(order -> order.getProperty().equals(property))
                .findFirst();
    }

    /**
     * Check if sort is sorted
     */
    public boolean isSorted() {
        return !orders.isEmpty();
    }

    /**
     * Check if sort is unsorted
     */
    public boolean isUnsorted() {
        return orders.isEmpty();
    }

    /**
     * Create descending sort for properties
     */
    public Sort descending() {
        return by(orders.stream()
                .map(order -> new Order(Direction.DESC, order.getProperty()))
                .toArray(Order[]::new));
    }

    /**
     * Create ascending sort for properties
     */
    public Sort ascending() {
        return by(orders.stream()
                .map(order -> new Order(Direction.ASC, order.getProperty()))
                .toArray(Order[]::new));
    }

    @Override
    public Iterator<Order> iterator() {
        return orders.iterator();
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    /**
     * Convert to ORDER BY clause
     */
    public String toOrderByClause(String alias) {
        if (isUnsorted()) {
            return "";
        }

        return " ORDER BY " + orders.stream()
                .map(order -> (alias != null ? alias + "." : "") + order.getProperty() + " " + order.getDirection().name())
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sort sort = (Sort) obj;
        return Objects.equals(orders, sort.orders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orders);
    }

    @Override
    public String toString() {
        return orders.isEmpty() ? "UNSORTED" : orders.toString();
    }

    /**
     * Sort direction enum
     */
    public enum Direction {
        ASC, DESC;

        public boolean isAscending() {
            return this == ASC;
        }

        public boolean isDescending() {
            return this == DESC;
        }

        public static Direction fromString(String value) {
            try {
                return Direction.valueOf(value.toUpperCase());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid direction: " + value);
            }
        }
    }

    /**
     * Property order specification
     */
    public static class Order {
        private final Direction direction;
        private final String property;
        private final NullHandling nullHandling;

        public Order(Direction direction, String property) {
            this(direction, property, NullHandling.NATIVE);
        }

        public Order(Direction direction, String property, NullHandling nullHandling) {
            this.direction = direction;
            this.property = property;
            this.nullHandling = nullHandling;
        }

        /**
         * Create ascending order
         */
        public static Order asc(String property) {
            return new Order(Direction.ASC, property);
        }

        /**
         * Create descending order
         */
        public static Order desc(String property) {
            return new Order(Direction.DESC, property);
        }

        /**
         * Create order by direction and property
         */
        public static Order by(String property) {
            return new Order(Direction.ASC, property);
        }

        public Direction getDirection() {
            return direction;
        }

        public String getProperty() {
            return property;
        }

        public NullHandling getNullHandling() {
            return nullHandling;
        }

        public boolean isAscending() {
            return direction.isAscending();
        }

        public boolean isDescending() {
            return direction.isDescending();
        }

        public Order with(Direction direction) {
            return new Order(direction, property, nullHandling);
        }

        public Order with(NullHandling nullHandling) {
            return new Order(direction, property, nullHandling);
        }

        public Order nullsFirst() {
            return with(NullHandling.NULLS_FIRST);
        }

        public Order nullsLast() {
            return with(NullHandling.NULLS_LAST);
        }

        public Order nullsNative() {
            return with(NullHandling.NATIVE);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Order order = (Order) obj;
            return direction == order.direction &&
                    Objects.equals(property, order.property) &&
                    nullHandling == order.nullHandling;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, property, nullHandling);
        }

        @Override
        public String toString() {
            return property + ": " + direction;
        }
    }

    /**
     * Null handling options
     */
    public enum NullHandling {
        NATIVE, NULLS_FIRST, NULLS_LAST
    }
}
