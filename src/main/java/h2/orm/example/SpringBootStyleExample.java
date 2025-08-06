package h2.orm.example;

import h2.orm.H2ORM;
import h2.orm.core.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Complete example showing how to use H2-ORM exactly like Spring Boot JPA
 * No handlers needed - just entities, repositories, and direct usage!
 * Now uses the new core architecture for better performance and reliability
 */
public class SpringBootStyleExample {

    public static void main(String[] args) {
        try {
            // 1. Initialize the database (like @SpringBootApplication does)
            initializeDatabase();

            // 2. Get repositories (like @Autowired in Spring Boot)
            JpaRepository<User, Long> userRepository = H2ORM.repository(User.class);
            JpaRepository<Product, Long> productRepository = H2ORM.repository(Product.class);

            // 3. Use repositories exactly like Spring Boot JPA!
            demonstrateBasicCrud(userRepository, productRepository);
            demonstrateAdvancedFeatures(userRepository, productRepository);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("💡 Cause: " + e.getCause().getMessage());
            }
        } finally {
            // Clean shutdown
            H2ORM.stop();
        }
    }

    /**
     * Initialize database using new H2ORM entry point
     */
    private static void initializeDatabase() {
        // Using the new simplified H2ORM entry point
        H2ORM.start("./data/myapp");

        System.out.println("✅ Database initialized successfully with new core architecture!");

        // Clean up any existing data to avoid unique constraint violations
        cleanupExistingData();
    }

    /**
     * Clean up existing data to avoid unique constraint violations
     */
    private static void cleanupExistingData() {
        try {
            JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
            JpaRepository<Product, Long> productRepo = H2ORM.repository(Product.class);

            // Delete all existing data
            productRepo.deleteAll();
            userRepo.deleteAll();

            System.out.println("🧹 Cleaned up existing data");
        } catch (Exception e) {
            // Ignore cleanup errors - tables might not exist yet
            System.out.println("ℹ️ No existing data to clean up (first run)");
        }
    }

    /**
     * Demonstrate basic CRUD operations - exactly like Spring Boot JPA
     */
    private static void demonstrateBasicCrud(JpaRepository<User, Long> userRepo,
                                           JpaRepository<Product, Long> productRepo) {

        System.out.println("\n🚀 Basic CRUD Operations:");

        // CREATE - Save entities
        User user1 = new User("john_doe", "john@example.com", "John Doe");
        User user2 = new User("jane_smith", "jane@example.com", "Jane Smith");

        userRepo.save(user1);
        userRepo.save(user2);
        System.out.println("Created users: " + user1.getId() + ", " + user2.getId());

        // Create products
        Product product1 = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"));
        Product product2 = new Product("Mouse", "Wireless optical mouse", new BigDecimal("29.99"));
        product1.setStockQuantity(10);
        product2.setStockQuantity(50);
        product1.setCreatedBy(user1);
        product2.setCreatedBy(user2);

        productRepo.saveAll(List.of(product1, product2));
        System.out.println("Created products: " + product1.getId() + ", " + product2.getId());

        // READ - Find operations
        System.out.println("\n📖 Reading data:");

        // Find by ID
        Optional<User> foundUser = userRepo.findById(user1.getId());
        foundUser.ifPresent(u -> System.out.println("Found user: " + u.getUsername()));

        // Find all
        List<User> allUsers = userRepo.findAll();
        System.out.println("Total users: " + allUsers.size());

        List<Product> allProducts = productRepo.findAll();
        System.out.println("Total products: " + allProducts.size());

        // Count
        long userCount = userRepo.count();
        long productCount = productRepo.count();
        System.out.println("Users count: " + userCount + ", Products count: " + productCount);

        // UPDATE
        System.out.println("\n✏️ Updating data:");
        user1.setFullName("John Updated Doe");
        userRepo.save(user1); // save() works for both create and update
        System.out.println("Updated user: " + user1.getFullName());

        // UPDATE with saveAndFlush
        product1.setPrice(new BigDecimal("899.99"));
        productRepo.saveAndFlush(product1);
        System.out.println("Updated product price: " + product1.getPrice());
    }

    /**
     * Demonstrate advanced features
     */
    private static void demonstrateAdvancedFeatures(JpaRepository<User, Long> userRepo,
                                                   JpaRepository<Product, Long> productRepo) {

        System.out.println("\n🔥 Advanced Features:");

        // Batch operations
        List<User> newUsers = List.of(
            new User("alice", "alice@example.com", "Alice Johnson"),
            new User("bob", "bob@example.com", "Bob Wilson"),
            new User("charlie", "charlie@example.com", "Charlie Brown")
        );

        userRepo.saveAllAndFlush(newUsers);
        System.out.println("Batch saved " + newUsers.size() + " users");

        // Exists check
        boolean exists = userRepo.existsById(1L);
        System.out.println("User with ID 1 exists: " + exists);

        // Find multiple by IDs
        List<User> someUsers = userRepo.findAllById(List.of(1L, 2L, 3L));
        System.out.println("Found users by IDs: " + someUsers.size());

        // Batch delete
        System.out.println("\n🗑️ Deletion operations:");

        // Delete by ID
        if (!newUsers.isEmpty()) {
            userRepo.deleteById(newUsers.get(0).getId());
            System.out.println("Deleted user by ID");
        }

        // Delete entities
        if (newUsers.size() > 1) {
            userRepo.deleteAll(newUsers.subList(1, 2));
            System.out.println("Deleted user entities");
        }

        // Final count
        System.out.println("Final user count: " + userRepo.count());
        System.out.println("Final product count: " + productRepo.count());

        // Show some data
        System.out.println("\n📋 Final data:");
        userRepo.findAll().forEach(System.out::println);
        productRepo.findAll().forEach(System.out::println);
    }
}

/**
 * Example of custom repository interface (like Spring Data JPA)
 */
interface UserRepository extends JpaRepository<User, Long> {
    // You can add custom methods here and implement them
    // For now, all basic methods are inherited from JpaRepository
}

/**
 * Example of custom repository interface for Product
 */
interface ProductRepository extends JpaRepository<Product, Long> {
    // Custom methods would go here
}
