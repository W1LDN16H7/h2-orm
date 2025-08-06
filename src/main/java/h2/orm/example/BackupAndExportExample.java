package h2.orm.example;

import h2.orm.H2ORM;

import h2.orm.core.repository.JpaRepository;
import h2.orm.core.service.BackupService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Complete example showing H2-ORM's powerful backup and export features
 * Demonstrates how easy it is to backup, restore, and export data
 */
public class BackupAndExportExample {

    public static void main(String[] args) {
        try {
            // 1. Initialize H2-ORM
            H2ORM.start("./data/backup_export_demo");
            System.out.println("🚀 H2-ORM started successfully!\n");

            // 2. Create some sample data
            createSampleData();

            // 3. Demonstrate backup features
            demonstrateBackupFeatures();

            // 4. Demonstrate export features
            demonstrateExportFeatures();

            // 5. Demonstrate restore features
            demonstrateRestoreFeatures();

            System.out.println("\n🎉 All backup and export features demonstrated successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            H2ORM.stop();
        }
    }

    private static void createSampleData() {
        System.out.println("📝 Creating sample data...");

        JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
        JpaRepository<Product, Long> productRepo = H2ORM.repository(Product.class);

        // Clean start - DELETE IN CORRECT ORDER to handle foreign keys
        // First delete products (child table), then users (parent table)
        productRepo.deleteAll();
        userRepo.deleteAll();

        // Create users
        User john = userRepo.save(new User("john_doe", "john@example.com", "John Doe"));
        User jane = userRepo.save(new User("jane_smith", "jane@example.com", "Jane Smith"));
        User alice = userRepo.save(new User("alice_johnson", "alice@example.com", "Alice Johnson"));

        // Create products
        Product laptop = new Product("MacBook Pro", "High-performance laptop", new BigDecimal("2499.99"));
        laptop.setStockQuantity(5);
        laptop.setCreatedBy(john);

        Product mouse = new Product("Magic Mouse", "Wireless mouse", new BigDecimal("99.99"));
        mouse.setStockQuantity(20);
        mouse.setCreatedBy(jane);

        Product keyboard = new Product("Magic Keyboard", "Wireless keyboard", new BigDecimal("199.99"));
        keyboard.setStockQuantity(15);
        keyboard.setCreatedBy(alice);

        productRepo.saveAll(List.of(laptop, mouse, keyboard));

        System.out.println("✅ Created " + userRepo.count() + " users and " + productRepo.count() + " products\n");
    }

    private static void demonstrateBackupFeatures() {
        System.out.println("💾 Demonstrating Backup Features:");

        // 1. Create a regular backup
        System.out.println("Creating regular backup...");
        H2ORM.backup("./backups/demo_backup");
        System.out.println("✅ Regular backup created");

        // 2. Create a compressed backup (saves space)
        System.out.println("Creating compressed backup...");
        H2ORM.backupCompressed("./backups/demo_backup_compressed");
        System.out.println("✅ Compressed backup created");

        // 3. Get backup information
        BackupService.BackupInfo backupInfo = H2ORM.getBackupInfo("./backups/demo_backup_compressed.gz");
        if (backupInfo != null) {
            System.out.println("📊 Backup Info:");
            System.out.println("   File: " + backupInfo.getFilePath());
            System.out.println("   Size: " + backupInfo.getFileSize() + " bytes");
            System.out.println("   Compressed: " + backupInfo.isCompressed());
            System.out.println("   Created: " + backupInfo.getCreationTime());
        }

        System.out.println();
    }

    private static void demonstrateExportFeatures() {
        System.out.println("📤 Demonstrating Export Features:");

        // 1. Export all users to different formats
        System.out.println("Exporting all users...");
        H2ORM.exportToCsv(User.class, "./exports/all_users.csv");
        H2ORM.exportToExcel(User.class, "./exports/all_users.xlsx");
        H2ORM.exportToJson(User.class, "./exports/all_users.json");
        System.out.println("✅ Users exported to CSV, Excel, and JSON");

        // 2. Export all products
        System.out.println("Exporting all products...");
        H2ORM.exportToCsv(Product.class, "./exports/all_products.csv");
        H2ORM.exportToExcel(Product.class, "./exports/all_products.xlsx");
        H2ORM.exportToJson(Product.class, "./exports/all_products.json");
        System.out.println("✅ Products exported to CSV, Excel, and JSON");

        // 3. Export specific data (filtered)
        JpaRepository<Product, Long> productRepo = H2ORM.repository(Product.class);
        List<Product> expensiveProducts = productRepo.findAll().stream()
            .filter(p -> p.getPrice().compareTo(new BigDecimal("150")) > 0)
            .toList();

        H2ORM.exportToCsv(expensiveProducts, "./exports/expensive_products.csv");
        H2ORM.exportToJson(expensiveProducts, "./exports/expensive_products.json");
        System.out.println("✅ Expensive products (>$150) exported");

        System.out.println();
    }

    private static void demonstrateRestoreFeatures() {
        System.out.println("🔄 Demonstrating Restore Features:");

        // First, let's modify some data
        JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
        JpaRepository<Product, Long> productRepo = H2ORM.repository(Product.class);

        System.out.println("Current data count - Users: " + userRepo.count() + ", Products: " + productRepo.count());

        // Delete some data to simulate data loss - CORRECT ORDER for foreign keys
        System.out.println("Simulating data loss (deleting in correct order)...");

        // First delete products (child table), then users (parent table)
        productRepo.deleteAll();
        userRepo.deleteAll();

        System.out.println("❌ Deleted all data (simulating data loss)");
        System.out.println("After deletion - Users: " + userRepo.count() + ", Products: " + productRepo.count());

        // Restore from backup
        System.out.println("Restoring from backup...");

        // Find the backup file (it will have a timestamp)
        try {
            // For demo purposes, we'll restore from compressed backup
            H2ORM.restoreCompressed("./backups/demo_backup_compressed.gz");
            System.out.println("✅ Database restored from compressed backup");

            // Verify restoration
            System.out.println("After restore - Users: " + userRepo.count() + ", Products: " + productRepo.count());

        } catch (Exception e) {
            System.out.println("⚠️ Note: Restore might not work perfectly in this demo due to active connections");
            System.out.println("   In a real application, you would typically restore when the database is not in use");
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * Advanced example showing programmatic access to services
     */
    private static void demonstrateAdvancedFeatures() {
        System.out.println("🔧 Advanced Features:");

        // Access services directly for advanced operations
        var backupService = H2ORM.getBackupService();
        var exportService = H2ORM.getExportService();

        // You can use these services for custom operations
        System.out.println("📚 Advanced services available for custom operations");
        System.out.println("   - BackupService: " + backupService.getClass().getSimpleName());
        System.out.println("   - ExportService: " + exportService.getClass().getSimpleName());
    }
}

/**
 * Extension showing how users can create scheduled backups
 */
class ScheduledBackupExample {

    public static void setupDailyBackup() {
        // Example of how users could set up scheduled backups
        System.out.println("💡 Example: Setting up daily backups");
        System.out.println("   You can use Java's ScheduledExecutorService:");
        System.out.println("   scheduler.scheduleAtFixedRate(() -> {");
        System.out.println("       H2ORM.backupCompressed(\"./backups/daily_\" + LocalDate.now());");
        System.out.println("   }, 0, 24, TimeUnit.HOURS);");
    }
}
