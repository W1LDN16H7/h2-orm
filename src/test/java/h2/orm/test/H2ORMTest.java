package h2.orm.test;

import h2.orm.H2ORM;
import h2.orm.core.repository.*;
import h2.orm.core.service.TableManager;
import h2.orm.example.Product;
import h2.orm.example.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for H2-ORM functionality
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class H2ORMTest {

    private static JpaRepository<User, Long> userRepo;
    private static JpaRepository<Product, Long> productRepo;

    @BeforeAll
    static void setupH2ORM() {
        H2ORM.startInMemory();
        userRepo = H2ORM.repository(User.class);
        productRepo = H2ORM.repository(Product.class);
    }

    @AfterAll
    static void shutdownH2ORM() {
        H2ORM.stop();
    }

    @BeforeEach
    void setupTestData() {
        // Clear existing data
        userRepo.deleteAll();
        productRepo.deleteAll();

        // Create test users
        User user1 = new User("alice_j", "alice@example.com", "Alice Johnson");
        User user2 = new User("bob_s", "bob@example.com", "Bob Smith");
        User user3 = new User("charlie_b", "charlie@example.com", "Charlie Brown");

        userRepo.saveAll(List.of(user1, user2, user3));

        // Create test products (need to check Product constructor)
        Product product1 = new Product("Laptop", "High-performance laptop", new java.math.BigDecimal("999.99"));
        Product product2 = new Product("Smartphone", "Latest model smartphone", new java.math.BigDecimal("699.99"));

        productRepo.saveAll(List.of(product1, product2));
    }

    @Test
    @Order(1)
    @DisplayName("Test Basic Repository Operations")
    void testBasicRepositoryOperations() {
        // Test count
        assertEquals(3, userRepo.count());
        assertEquals(2, productRepo.count());

        // Test findAll
        List<User> allUsers = userRepo.findAll();
        assertEquals(3, allUsers.size());

        // Test findById
        User firstUser = allUsers.get(0);
        Optional<User> foundUser = userRepo.findById(firstUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(firstUser.getFullName(), foundUser.get().getFullName());

        // Test existsById
        assertTrue(userRepo.existsById(firstUser.getId()));
        assertFalse(userRepo.existsById(9999L));

        // Test save
        User newUser = new User("diana_p", "diana@example.com", "Diana Prince");
        User savedUser = userRepo.save(newUser);
        assertNotNull(savedUser.getId());
        assertEquals(4, userRepo.count());

        // Test delete
        userRepo.delete(savedUser);
        assertEquals(3, userRepo.count());
    }

    @Test
    @Order(2)
    @DisplayName("Test Sorting Functionality")
    void testSortingFunctionality() {
        // Test ascending sort by fullName
        List<User> sortedByFullName = userRepo.findAll(Sort.by("fullName"));
        assertEquals("Alice Johnson", sortedByFullName.get(0).getFullName());
        assertEquals("Bob Smith", sortedByFullName.get(1).getFullName());
        assertEquals("Charlie Brown", sortedByFullName.get(2).getFullName());

        // Test descending sort by username
        List<User> sortedByUsernameDesc = userRepo.findAll(Sort.by(Sort.Direction.DESC, "username"));
        assertEquals("charlie_b", sortedByUsernameDesc.get(0).getUsername());
        assertEquals("bob_s", sortedByUsernameDesc.get(1).getUsername());
        assertEquals("alice_j", sortedByUsernameDesc.get(2).getUsername());

        // Test multiple field sorting
        Sort multiSort = Sort.by("username").and(Sort.by(Sort.Direction.DESC, "fullName"));
        List<User> multiSorted = userRepo.findAll(multiSort);
        assertEquals(3, multiSorted.size());

        // Test Sort.Order fluent API
        Sort.Order usernameOrder = Sort.Order.asc("username");
        assertTrue(usernameOrder.isAscending());
        assertFalse(usernameOrder.isDescending());

        Sort.Order fullNameOrder = Sort.Order.desc("fullName");
        assertTrue(fullNameOrder.isDescending());
        assertFalse(fullNameOrder.isAscending());
    }

    @Test
    @Order(3)
    @DisplayName("Test Pagination Functionality")
    void testPaginationFunctionality() {
        // Test basic pagination
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<User> firstPage = userRepo.findAll(pageRequest);

        assertEquals(0, firstPage.getNumber());
        assertEquals(2, firstPage.getSize());
        assertEquals(3, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals(2, firstPage.getNumberOfElements());
        assertTrue(firstPage.hasNext());
        assertFalse(firstPage.hasPrevious());
        assertTrue(firstPage.isFirst());
        assertFalse(firstPage.isLast());

        // Test second page
        Page<User> secondPage = userRepo.findAll(firstPage.nextPageable());
        assertEquals(1, secondPage.getNumber());
        assertEquals(1, secondPage.getNumberOfElements());
        assertFalse(secondPage.hasNext());
        assertTrue(secondPage.hasPrevious());
        assertFalse(secondPage.isFirst());
        assertTrue(secondPage.isLast());

        // Test pagination with sorting
        PageRequest sortedPageRequest = PageRequest.of(0, 2, Sort.by("fullName"));
        Page<User> sortedPage = userRepo.findAll(sortedPageRequest);
        assertEquals("Alice Johnson", sortedPage.getContent().get(0).getFullName());
        assertEquals("Bob Smith", sortedPage.getContent().get(1).getFullName());

        // Test page mapping
        Page<String> mappedPage = firstPage.map(User::getFullName);
        assertEquals(2, mappedPage.getNumberOfElements());
        assertTrue(mappedPage.getContent().stream().allMatch(name -> name instanceof String));
    }

    @Test
    @Order(4)
    @DisplayName("Test Table Management")
    void testTableManagement() {
        // Test table existence - fix table names to match actual @Table annotations
        assertTrue(H2ORM.tableExists("users"));  // User entity uses @Table(name = "users")
        assertTrue(H2ORM.tableExists("products")); // Assuming Product entity uses @Table(name = "products")
        assertFalse(H2ORM.tableExists("nonexistent_table"));

        // Test table row counts
        assertEquals(3, H2ORM.getTableRowCount(User.class));
        assertEquals(2, H2ORM.getTableRowCount(Product.class));

        // Test table info
        TableManager.TableInfo userTableInfo = H2ORM.getTableInfo(User.class);
        assertNotNull(userTableInfo);
        assertEquals("users", userTableInfo.getTableName()); // Fix expected table name
        assertTrue(userTableInfo.isExists());
        assertEquals(3, userTableInfo.getRowCount());

        // Test all table info
        List<TableManager.TableInfo> allTables = H2ORM.getAllTableInfo();
        assertTrue(allTables.size() >= 2);
        assertTrue(allTables.stream().anyMatch(table -> "users".equals(table.getTableName())));
        assertTrue(allTables.stream().anyMatch(table -> "products".equals(table.getTableName())));
    }

    @Test
    @Order(5)
    @DisplayName("Test Advanced Repository Operations")
    void testAdvancedRepositoryOperations() {
        // Test findAllById
        List<User> allUsers = userRepo.findAll();
        List<Long> userIds = allUsers.stream().map(User::getId).limit(2).toList();
        List<User> foundUsers = userRepo.findAllById(userIds);
        assertEquals(2, foundUsers.size());

        // Test saveAndFlush
        User newUser = new User("test_user", "test@example.com", "Test User");
        User savedUser = userRepo.saveAndFlush(newUser);
        assertNotNull(savedUser.getId());
        assertEquals("Test User", savedUser.getFullName());

        // Test batch operations
        List<User> batchUsers = List.of(
                new User("batch1", "batch1@example.com", "Batch User 1"),
                new User("batch2", "batch2@example.com", "Batch User 2")
        );
        List<User> savedBatchUsers = userRepo.saveAllAndFlush(batchUsers);
        assertEquals(2, savedBatchUsers.size());
        assertEquals(6, userRepo.count()); // 3 original + 1 test + 2 batch

        // Test deleteAllById
        List<Long> batchIds = savedBatchUsers.stream().map(User::getId).toList();
        userRepo.deleteAllById(batchIds);
        assertEquals(4, userRepo.count()); // 3 original + 1 test

        // Cleanup
        userRepo.delete(savedUser);
        assertEquals(3, userRepo.count());
    }

    @Test
    @Order(6)
    @DisplayName("Test Export Functionality")
    void testExportFunctionality() {
        assertDoesNotThrow(() -> {
            // Test CSV export
            H2ORM.exportToCsv(User.class, "./test-exports/users.csv");

            // Test Excel export
            H2ORM.exportToExcel(User.class, "./test-exports/users.xlsx");

            // Test JSON export
            H2ORM.exportToJson(User.class, "./test-exports/users.json");

            // Test export with specific data - filter by active users
            List<User> activeUsers = userRepo.findAll().stream()
                    .filter(user -> user.getIsActive() != null && user.getIsActive())
                    .toList();
            H2ORM.exportToCsv(activeUsers, "./test-exports/active_users.csv");
        });
    }

    @Test
    @Order(7)
    @DisplayName("Test Sort Edge Cases")
    void testSortEdgeCases() {
        // Test unsorted
        Sort unsorted = Sort.unsorted();
        assertTrue(unsorted.isUnsorted());
        assertFalse(unsorted.isSorted());

        // Test empty sort
        List<User> unsortedUsers = userRepo.findAll(unsorted);
        assertEquals(3, unsortedUsers.size());

        // Test sort by non-existent field (should not crash)
        assertDoesNotThrow(() -> {
            userRepo.findAll(Sort.by("nonExistentField"));
        });

        // Test null handling
        Sort.Order orderWithNulls = Sort.Order.asc("fullName").nullsLast();
        assertEquals(Sort.NullHandling.NULLS_LAST, orderWithNulls.getNullHandling());
    }

    @Test
    @Order(8)
    @DisplayName("Test PageRequest Edge Cases")
    void testPageRequestEdgeCases() {
        // Test invalid page parameters
        assertThrows(IllegalArgumentException.class, () -> PageRequest.of(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> PageRequest.of(0, 0));

        // Test large page size
        Page<User> largePage = userRepo.findAll(PageRequest.of(0, 100));
        assertEquals(3, largePage.getNumberOfElements());
        assertEquals(1, largePage.getTotalPages());

        // Test page beyond data
        Page<User> emptyPage = userRepo.findAll(PageRequest.of(10, 10));
        assertEquals(0, emptyPage.getNumberOfElements());
        assertTrue(emptyPage.getContent().isEmpty());

        // Test withPage method
        PageRequest original = PageRequest.of(0, 2);
        Pageable modified = original.withPage(1);
        assertEquals(1, modified.getPageNumber());
        assertEquals(2, modified.getPageSize());
    }

    @Test
    @Order(9)
    @DisplayName("Test Transaction Management")
    void testTransactionManagement() {
        // Test that operations work correctly within transactions
        long initialCount = userRepo.count();

        // This should work (save operation in transaction)
        User newUser = new User("trans_test", "transaction@example.com", "Transaction Test");
        User savedUser = userRepo.save(newUser);
        assertNotNull(savedUser.getId());
        assertEquals(initialCount + 1, userRepo.count());

        // Cleanup
        userRepo.delete(savedUser);
        assertEquals(initialCount, userRepo.count());
    }

    @Test
    @Order(10)
    @DisplayName("Test H2ORM Lifecycle")
    void testH2ORMLifecycle() {
        // Test that H2ORM is running
        assertTrue(H2ORM.isRunning());

        // Test repository creation
        JpaRepository<User, Long> newUserRepo = H2ORM.repository(User.class);
        assertNotNull(newUserRepo);

        // Test that repositories work
        assertEquals(3, newUserRepo.count());
    }
}
