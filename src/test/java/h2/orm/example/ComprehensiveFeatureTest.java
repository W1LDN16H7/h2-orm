package h2.orm.example;

import h2.orm.H2ORM;
import h2.orm.core.repository.JpaRepository;
import h2.orm.core.repository.Page;
import h2.orm.core.repository.PageRequest;
import h2.orm.core.repository.Sort;
import h2.orm.core.service.TableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comprehensive test example demonstrating all H2-ORM features:
 * - Sorting and Pagination
 * - Table Management
 * - Export functionality
 * - Repository operations
 */
public class ComprehensiveFeatureTest {

    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveFeatureTest.class);

    public static void main(String[] args) {
        try {
            // Start H2-ORM
            logger.info("🚀 Starting H2-ORM Comprehensive Feature Test...");
            H2ORM.start("./data/feature_test");

            // Run all tests
            testRepositoryBasicOperations();
            testSortingFunctionality();
            testPaginationFunctionality();
            testTableManagement();
            testExportFunctionality();
            testAdvancedQueries();

            logger.info("✅ All tests completed successfully!");

        } catch (Exception e) {
            logger.error("❌ Test failed", e);
        } finally {
            H2ORM.stop();
        }
    }

    /**
     * Test basic repository operations
     */
    private static void testRepositoryBasicOperations() {
        logger.info("\n📝 Testing Basic Repository Operations...");

        JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
        JpaRepository<Product, Long> productRepo = H2ORM.repository(Product.class);

        // Clear existing data
        userRepo.deleteAll();
        productRepo.deleteAll();

        // Create test users
        User user1 = new User("alice_j", "alice@example.com", "Alice Johnson");
        User user2 = new User("bob_s", "bob@example.com", "Bob Smith");
        User user3 = new User("charlie_b", "charlie@example.com", "Charlie Brown");
        User user4 = new User("diana_p", "diana@example.com", "Diana Prince");
        User user5 = new User("eve_d", "eve@example.com", "Eve Davis");

        // Save users
        userRepo.saveAll(List.of(user1, user2, user3, user4, user5));
        logger.info("✅ Created {} users", userRepo.count());

        // Create test products
        Product product1 = new Product("Laptop", "High-performance laptop", new BigDecimal("1500.00"));
        Product product2 = new Product("Smartphone", "Latest model smartphone", new BigDecimal("800.00"));
        Product product3 = new Product("Tablet", "Portable tablet device", new BigDecimal("600.00"));
        Product product4 = new Product("Smartwatch", "Feature-rich smartwatch", new BigDecimal("250.00"));
        Product product5 = new Product("Headphones", "Noise-cancelling headphones", new BigDecimal("150.00"));
        

        // Save products
        productRepo.saveAll(List.of(product1, product2, product3, product4, product5));
        logger.info("✅ Created {} products", productRepo.count());

        // Test findById
        User foundUser = userRepo.findById(user1.getId()).orElse(null);
        logger.info("✅ Found user by ID: {}", foundUser != null ? foundUser.getFullName() : "null");

        // Test existsById
        boolean exists = userRepo.existsById(user1.getId());
        logger.info("✅ User exists check: {}", exists);
    }

    /**
     * Test sorting functionality
     */
    private static void testSortingFunctionality() {
        logger.info("\n🔄 Testing Sorting Functionality...");

        JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);

        // Test ascending sort by username
        List<User> sortedByUsername = userRepo.findAll(Sort.by("username"));
        logger.info("✅ Users sorted by username (ASC):");
        sortedByUsername.forEach(user -> logger.info("  - {}", user.getFullName()));

        // Test descending sort by fullName
        List<User> sortedByFullName = userRepo.findAll(Sort.by(Sort.Direction.DESC, "fullName"));
        logger.info("✅ Users sorted by fullName (DESC):");
        sortedByFullName.forEach(user -> logger.info("  - {}", user.getFullName()));

        // Test multiple field sorting
        Sort multiSort = Sort.by("username").and(Sort.by(Sort.Direction.DESC, "fullName"));
        List<User> multiSorted = userRepo.findAll(multiSort);
        logger.info("✅ Users sorted by username ASC, then fullName DESC:");
        multiSorted.forEach(user -> logger.info("  - {} ({})", user.getFullName(), user.getUsername()));

        // Test fluent API
        Sort fluentSort = Sort.by(Sort.Order.desc("fullName").nullsLast())
                .and(Sort.by(Sort.Order.asc("username")));
        List<User> fluentSorted = userRepo.findAll(fluentSort);
        logger.info("✅ Users with fluent sort API:");
        fluentSorted.forEach(user -> logger.info("  - {} ({})", user.getFullName(), user.getUsername()));
    }

    /**
     * Test pagination functionality
     */
    private static void testPaginationFunctionality() {
        logger.info("\n📄 Testing Pagination Functionality...");

        JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);

        // Test basic pagination
        PageRequest pageRequest = PageRequest.of(0, 2); // First page, 2 items per page
        Page<User> firstPage = userRepo.findAll(pageRequest);

        logger.info("✅ First page (0, size=2):");
        logger.info("  - Total elements: {}", firstPage.getTotalElements());
        logger.info("  - Total pages: {}", firstPage.getTotalPages());
        logger.info("  - Current page: {}", firstPage.getNumber());
        logger.info("  - Has next: {}", firstPage.hasNext());
        logger.info("  - Has previous: {}", firstPage.hasPrevious());
        firstPage.getContent().forEach(user -> logger.info("  - {}", user.getFullName()));

        // Test pagination with sorting
        PageRequest sortedPageRequest = PageRequest.of(1, 2, Sort.by("username"));
        Page<User> secondPage = userRepo.findAll(sortedPageRequest);

        logger.info("✅ Second page (1, size=2) sorted by username:");
        secondPage.getContent().forEach(user -> logger.info("  - {}", user.getFullName()));

        // Test page navigation
        if (firstPage.hasNext()) {
            Page<User> nextPage = userRepo.findAll(firstPage.nextPageable());
            logger.info("✅ Next page content:");
            nextPage.getContent().forEach(user -> logger.info("  - {}", user.getFullName()));
        }

        // Test large page size
        Page<User> largePage = userRepo.findAll(PageRequest.of(0, 10));
        logger.info("✅ Large page (size=10): {} items", largePage.getNumberOfElements());

        // Test page mapping
        Page<String> mappedPage = firstPage.map(User::getFullName);
        logger.info("✅ Mapped page (names only):");
        mappedPage.getContent().forEach(name -> logger.info("  - {}", name));
    }

    /**
     * Test table management functionality
     */
    private static void testTableManagement() {
        logger.info("\n🗄️ Testing Table Management...");

        // Get table information
        TableManager.TableInfo userTableInfo = H2ORM.getTableInfo(User.class);
        logger.info("✅ User table info: {}", userTableInfo);

        TableManager.TableInfo productTableInfo = H2ORM.getTableInfo(Product.class);
        logger.info("✅ Product table info: {}", productTableInfo);

        // Get all table information
        List<TableManager.TableInfo> allTables = H2ORM.getAllTableInfo();
        logger.info("✅ All tables:");
        allTables.forEach(table -> logger.info("  - {}", table));

        // Test table row counts
        long userCount = H2ORM.getTableRowCount(User.class);
        long productCount = H2ORM.getTableRowCount(Product.class);
        logger.info("✅ Row counts - Users: {}, Products: {}", userCount, productCount);

        // Test table existence
        boolean userTableExists = H2ORM.tableExists("user");
        boolean productTableExists = H2ORM.tableExists("product");
        logger.info("✅ Table existence - Users: {}, Products: {}", userTableExists, productTableExists);

        // Test truncate (commented out to preserve test data)
        // H2ORM.truncateTable(Product.class);
        // logger.info("✅ Truncated Product table");

        // Test reset auto-increment (commented out to preserve test data)
        // H2ORM.resetAutoIncrement(User.class);
        // logger.info("✅ Reset auto-increment for User table");
    }

    /**
     * Test export functionality
     */
    private static void testExportFunctionality() {
        logger.info("\n📤 Testing Export Functionality...");

        try {
            // Export all users to different formats
            H2ORM.exportToCsv(User.class, "./exports/test_users.csv");
            logger.info("✅ Exported users to CSV");

            H2ORM.exportToExcel(User.class, "./exports/test_users.xlsx");
            logger.info("✅ Exported users to Excel");

            H2ORM.exportToJson(User.class, "./exports/test_users.json");
            logger.info("✅ Exported users to JSON");

            // Export products
            H2ORM.exportToCsv(Product.class, "./exports/test_products.csv");
            logger.info("✅ Exported products to CSV");

            H2ORM.exportToExcel(Product.class, "./exports/test_products.xlsx");
            logger.info("✅ Exported products to Excel");

            H2ORM.exportToJson(Product.class, "./exports/test_products.json");
            logger.info("✅ Exported products to JSON");

            // Export specific data with custom filtering
            JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
            List<User> activeUsers = userRepo.findAll(Sort.by("username"))
                    .stream()
                    .filter(user -> user.getIsActive() != null && user.getIsActive())
                    .toList();

            H2ORM.exportToCsv(activeUsers, "./exports/active_users.csv");
            logger.info("✅ Exported {} active users to CSV", activeUsers.size());

        } catch (Exception e) {
            logger.error("❌ Export test failed", e);
        }
    }

    /**
     * Test advanced query operations
     */
    private static void testAdvancedQueries() {
        logger.info("\n🔍 Testing Advanced Query Operations...");

        JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
        JpaRepository<Product, Long> productRepo = H2ORM.repository(Product.class);

        // Test batch operations
        List<Long> userIds = userRepo.findAll().stream()
                .map(User::getId)
                .limit(3)
                .toList();

        List<User> usersByIds = userRepo.findAllById(userIds);
        logger.info("✅ Found {} users by IDs", usersByIds.size());

        // Test save and flush
        User newUser = new User("Test User", "test@example.com", "kapil");
        User savedUser = userRepo.saveAndFlush(newUser);
        logger.info("✅ Saved and flushed user: {}", savedUser.getFullName());

        // Test batch save
        // write 5 users with different names
        List<User> batchUsers =  List.of(
            new User("Batch User 1", "hello",  "hi"),
            new User("Batch User 2", "hello2",  "hi2"),
            new User("Batch User 3", "hello3",  "hi3"),
            new User("Batch User 4", "hello4",  "hi4"),
            new User("Batch User 5", "hello5",  "hi5")
        );
        List<User> savedBatchUsers = userRepo.saveAllAndFlush(batchUsers);
        logger.info("✅ Batch saved {} users", savedBatchUsers.size());

        // Test count operations
        long totalUsers = userRepo.count();
        long totalProducts = productRepo.count();
        logger.info("✅ Total counts - Users: {}, Products: {}", totalUsers, totalProducts);

        // Cleanup test data
        userRepo.delete(savedUser);
        userRepo.deleteAll(savedBatchUsers);
        logger.info("✅ Cleaned up test data");
    }
}
