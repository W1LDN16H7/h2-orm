package h2.orm.example;

import h2.orm.H2ORM;
import h2.orm.core.repository.JpaRepository;
import h2.orm.core.repository.PageRequest;
import h2.orm.core.repository.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Practical example demonstrating field-based queries for your use case
 * Shows how to find entities by status fields like "processed", "active", etc.
 */
public class FieldBasedQueryExample {

    private static final Logger logger = LoggerFactory.getLogger(FieldBasedQueryExample.class);

    public static void main(String[] args) {
        try {
            // Start H2-ORM
            logger.info("🚀 Starting Field-Based Query Examples...");
            H2ORM.startInMemory();

            // Create test data
            setupTestData();

            // Demonstrate field-based queries
            demonstrateProcessedStatusQueries();
            demonstrateAdvancedFieldQueries();
            demonstrateMultipleFieldQueries();
            demonstrateRangeAndTextQueries();

            logger.info("✅ All field-based query examples completed!");

        } catch (Exception e) {
            logger.error("❌ Example failed", e);
        } finally {
            H2ORM.stop();
        }
    }

    private static void setupTestData() {
        logger.info("\n📝 Setting up test data...");

        JpaRepository<Order, Long> orderRepo = H2ORM.repository(Order.class);
        JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);

        // Clear existing data
        orderRepo.deleteAll();
        userRepo.deleteAll();

        // Create users
        User user1 = new User("John Doe", "john@example.com", "");
        User user2 = new User("Jane Smith", "jane@example.com", "");
        User user3 = new User("Bob Wilson", "bob@example.com", "");
        userRepo.saveAll(List.of(user1, user2, user3));

        // Create orders with different statuses
        Order order1 = new Order("ORD-001", user1, Order.Status.PROCESSED, 150.00);
        Order order2 = new Order("ORD-002", user2, Order.Status.NEW, 200.00);
        Order order3 = new Order("ORD-003", user1, Order.Status.SHIPPED, 300.00);
        Order order4 = new Order("ORD-004", user3, Order.Status.PROCESSED, 75.00);
        Order order5 = new Order("ORD-005", user2, Order.Status.NEW, 400.00);
        Order order6 = new Order("ORD-006", user1, Order.Status.CANCELLED, 100.00);

        orderRepo.saveAll(List.of(order1, order2, order3, order4, order5, order6));
        logger.info("✅ Created {} orders and {} users", orderRepo.count(), userRepo.count());
    }

    /**
     * Demonstrate queries for processed status - your main use case
     */
    private static void demonstrateProcessedStatusQueries() {
        logger.info("\n🔍 Testing Processed Status Queries...");

        JpaRepository<Order, Long> orderRepo = H2ORM.repository(Order.class);

        // Find all processed orders
        List<Order> processedOrders = orderRepo.findByField("status", Order.Status.PROCESSED);
        logger.info("✅ Found {} processed orders:", processedOrders.size());
        processedOrders.forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));

        // Find all new orders
        List<Order> newOrders = orderRepo.findByField("status", Order.Status.NEW);
        logger.info("✅ Found {} new orders:", newOrders.size());
        newOrders.forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));

        // Find processed orders sorted by amount
        List<Order> sortedProcessed = orderRepo.findByField("status", Order.Status.PROCESSED,
                Sort.by(Sort.Direction.DESC, "amount"));
        logger.info("✅ Processed orders sorted by amount (DESC):");
        sortedProcessed.forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));

        // Count orders by status
        long processedCount = orderRepo.countByField("status", Order.Status.PROCESSED);
        long newCount = orderRepo.countByField("status", Order.Status.NEW);
        long shippedCount = orderRepo.countByField("status", Order.Status.SHIPPED);
        logger.info("✅ Order counts - Processed: {}, New: {}, Shipped: {}",
                processedCount, newCount, shippedCount);

        // Check if processed orders exist
        boolean hasProcessed = orderRepo.existsByField("status", Order.Status.PROCESSED);
        boolean hasPending = orderRepo.existsByField("status", Order.Status.PENDING);
        logger.info("✅ Status checks - Has Processed: {}, Has Pending: {}", hasProcessed, hasPending);
    }

    /**
     * Demonstrate advanced field queries
     */
    private static void demonstrateAdvancedFieldQueries() {
        logger.info("\n🔍 Testing Advanced Field Queries...");

        JpaRepository<Order, Long> orderRepo = H2ORM.repository(Order.class);

        // Find orders with amount greater than 200
        List<Order> expensiveOrders = orderRepo.findByFieldBetween("amount", 200.0, Double.MAX_VALUE);
        logger.info("✅ Found {} expensive orders (>= $200):", expensiveOrders.size());
        expensiveOrders.forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));

        // Find orders by multiple statuses
        List<Order> activeOrders = orderRepo.findByFieldIn("status",
                List.of(Order.Status.NEW, Order.Status.PROCESSED, Order.Status.SHIPPED));
        logger.info("✅ Found {} active orders (NEW/PROCESSED/SHIPPED):", activeOrders.size());
        activeOrders.forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));

        // Find orders with specific order number patterns
        List<Order> ordersStartingWith001 = orderRepo.findByFieldStartingWith("orderNumber", "ORD-00");
        logger.info("✅ Found {} orders starting with 'ORD-00':", ordersStartingWith001.size());
        ordersStartingWith001.forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));

        // Find first processed order
        var firstProcessed = orderRepo.findFirstByField("status", Order.Status.PROCESSED);
        if (firstProcessed.isPresent()) {
            Order order = firstProcessed.get();
            logger.info("✅ First processed order: {} (${})", order.getOrderNumber(), order.getAmount());
        }
    }

    /**
     * Demonstrate multiple field queries
     */
    private static void demonstrateMultipleFieldQueries() {
        logger.info("\n🔍 Testing Multiple Field Queries...");

        JpaRepository<Order, Long> orderRepo = H2ORM.repository(Order.class);

        // Find processed orders with amount > 100
        Map<String, Object> criteria = Map.of(
                "status", Order.Status.PROCESSED,
                "amount", 150.0  // This will find exact match, for range use other methods
        );
        List<Order> specificOrders = orderRepo.findByFields(criteria);
        logger.info("✅ Found {} orders matching multiple criteria:", specificOrders.size());
        specificOrders.forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));

        // Find orders by user and status
        JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
        User firstUser = userRepo.findAll().get(0);

        Map<String, Object> userAndStatus = Map.of(
                "customer", firstUser,
                "status", Order.Status.PROCESSED
        );
        List<Order> userProcessedOrders = orderRepo.findByFields(userAndStatus);
        logger.info("✅ Found {} processed orders for user {}:",
                userProcessedOrders.size(), firstUser.getFullName());
        userProcessedOrders.forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));
    }

    /**
     * Demonstrate range and text queries
     */
    private static void demonstrateRangeAndTextQueries() {
        logger.info("\n🔍 Testing Range and Text Queries...");

        JpaRepository<Order, Long> orderRepo = H2ORM.repository(Order.class);

        // Find orders in amount range
        List<Order> midRangeOrders = orderRepo.findByFieldBetween("amount", 100.0, 300.0);
        logger.info("✅ Found {} orders between $100-$300:", midRangeOrders.size());
        midRangeOrders.forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));

        // Find orders containing specific text in order number
        List<Order> ordersContaining002 = orderRepo.findByFieldContaining("orderNumber", "002");
        logger.info("✅ Found {} orders containing '002':", ordersContaining002.size());
        ordersContaining002.forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));

        // Pagination with field queries
        var pageRequest = PageRequest.of(0, 2, Sort.by("amount"));
        var processedPage = orderRepo.findByField("status", Order.Status.PROCESSED, pageRequest);
        logger.info("✅ Paginated processed orders (page 0, size 2):");
        logger.info("  - Total elements: {}", processedPage.getTotalElements());
        logger.info("  - Total pages: {}", processedPage.getTotalPages());
        processedPage.getContent().forEach(order ->
            logger.info("  - {} (${}) - {}", order.getOrderNumber(), order.getAmount(), order.getStatus()));
    }
}
