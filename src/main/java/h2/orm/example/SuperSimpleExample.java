package h2.orm.example;

import h2.orm.H2ORM;
import h2.orm.core.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Super Simple Example - shows how easy H2-ORM is to use!
 * Just like Spring Boot but even simpler!
 */
public class SuperSimpleExample {

    public static void main(String[] args) {
        try {
            // 1. Start H2-ORM (one line!)
            H2ORM.start("./data/simple_example");

            // 2. Get repositories (like @Autowired in Spring Boot)
            JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
            JpaRepository<Product, Long> productRepo = H2ORM.repository(Product.class);

            // 3. Use it exactly like Spring Boot JPA!
            System.out.println("🚀 H2-ORM Super Simple Example");

            // Clean start
            userRepo.deleteAll();
            productRepo.deleteAll();

            // Create and save users
            User john = userRepo.save(new User("john_doe", "john@example.com", "John Doe"));
            User jane = userRepo.save(new User("jane_smith", "jane@example.com", "Jane Smith"));

            System.out.println("✅ Created users: " + john.getUsername() + ", " + jane.getUsername());

            // Create and save products
            Product laptop = new Product("MacBook Pro", "High-performance laptop", new BigDecimal("2499.99"));
            laptop.setStockQuantity(5);
            laptop.setCreatedBy(john);

            Product mouse = new Product("Magic Mouse", "Wireless mouse", new BigDecimal("99.99"));
            mouse.setStockQuantity(20);
            mouse.setCreatedBy(jane);

            productRepo.saveAll(List.of(laptop, mouse));
            System.out.println("✅ Created products: " + laptop.getName() + ", " + mouse.getName());

            // Query data
            List<User> allUsers = userRepo.findAll();
            List<Product> allProducts = productRepo.findAll();

            System.out.println("\n📊 Current Data:");
            System.out.println("Users (" + userRepo.count() + "):");
            allUsers.forEach(u -> System.out.println("  - " + u.getUsername() + " (" + u.getEmail() + ")"));

            System.out.println("Products (" + productRepo.count() + "):");
            allProducts.forEach(p -> System.out.println("  - " + p.getName() + " - $" + p.getPrice()));

            // Update example
            laptop.setPrice(new BigDecimal("2299.99"));
            productRepo.saveAndFlush(laptop);
            System.out.println("✅ Updated laptop price to: $" + laptop.getPrice());

            System.out.println("\n🎉 Success! H2-ORM is working perfectly!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("💡 Cause: " + e.getCause().getMessage());
            }
        } finally {
            // 4. Clean shutdown
            H2ORM.stop();
        }
    }
}
