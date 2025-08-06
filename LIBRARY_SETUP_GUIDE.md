# H2-ORM Standalone Library Setup Guide

## 🚀 Quick Start

H2-ORM is now a standalone JPA library that works exactly like Spring Boot JPA - no handlers needed!

### 1. Add to Your Project

**Maven:**
```xml
<dependency>
    <groupId>com.h2.orm</groupId>
    <artifactId>H2-ORM</artifactId>
    <version>v0.0.1</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'com.h2.orm:H2-ORM:v0.0.1'
```

### 2. Create Entities (Just Like Spring Boot)

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String email;
    
    // constructors, getters, setters...
}
```

### 3. Initialize Database & Use Repositories

```java
public class MyApp {
    public static void main(String[] args) {
        // Initialize database (like @SpringBootApplication)
        EntityManagerProvider.initialize();
        // or: EntityManagerProvider.initialize(DatabaseConfiguration.h2File("./data/myapp"));
        
        // Get repository (like @Autowired)
        JpaRepository<User, Long> userRepo = Repositories.of(User.class);
        
        // Use it exactly like Spring Boot JPA!
        User user = new User("john", "john@example.com");
        userRepo.save(user);
        
        Optional<User> found = userRepo.findById(1L);
        List<User> all = userRepo.findAll();
        long count = userRepo.count();
        
        // Cleanup
        EntityManagerProvider.shutdown();
    }
}
```

## 🛠️ Configuration Options

### Database Types

```java
// H2 In-Memory (Default)
EntityManagerProvider.initialize();

// H2 File-Based
EntityManagerProvider.initialize(DatabaseConfiguration.h2File("./data/mydb"));

// SQLite
EntityManagerProvider.initialize(DatabaseConfiguration.sqlite("./data/mydb.db"));

// MySQL
EntityManagerProvider.initialize(
    DatabaseConfiguration.mysql("localhost", 3306, "mydb", "user", "pass")
);

// PostgreSQL
EntityManagerProvider.initialize(
    DatabaseConfiguration.postgresql("localhost", 5432, "mydb", "user", "pass")
);
```

### Properties File Configuration

Create `h2-orm.properties`:
```properties
h2orm.database.type=h2
h2orm.database.mode=file
h2orm.database.path=./data/myapp
h2orm.jpa.ddl.auto=update
h2orm.jpa.show.sql=true
```

Then:
```java
EntityManagerProvider.initialize("h2-orm.properties");
```

### XML Configuration

Create `h2-orm-config.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <entry key="h2orm.database.type">h2</entry>
    <entry key="h2orm.database.mode">file</entry>
    <entry key="h2orm.database.path">./data/myapp</entry>
</properties>
```

### Fluent Configuration

```java
DatabaseConfiguration config = DatabaseConfigBuilder.create()
    .h2()
    .fileBased()
    .path("./data/myapp")
    .showSql(true)
    .enableCache()
    .poolSize(20)
    .build();

EntityManagerProvider.initialize(config);
```

## 📚 Repository Methods (All Spring Data JPA Methods)

```java
JpaRepository<User, Long> repo = Repositories.of(User.class);

// Basic CRUD
User saved = repo.save(user);
List<User> saved = repo.saveAll(userList);
Optional<User> found = repo.findById(1L);
List<User> all = repo.findAll();
List<User> some = repo.findAllById(Arrays.asList(1L, 2L, 3L));
boolean exists = repo.existsById(1L);
long count = repo.count();

// Deletion
repo.deleteById(1L);
repo.delete(user);
repo.deleteAll(userList);
repo.deleteAllById(Arrays.asList(1L, 2L));
repo.deleteAll();

// Advanced operations
repo.flush();
User saved = repo.saveAndFlush(user);
List<User> saved = repo.saveAllAndFlush(userList);
repo.deleteInBatch(userList);
repo.deleteAllInBatch();
User reference = repo.getReferenceById(1L);
```

## 🔧 Advanced Features

### Custom Repositories

```java
// Define custom repository interface
interface UserRepository extends JpaRepository<User, Long> {
    // Inherit all standard methods
    // Add custom methods if needed
}

// Use it
UserRepository userRepo = (UserRepository) Repositories.of(User.class);
```

### Transaction Management

```java
// Automatic transactions (recommended)
User result = EntityManagerProvider.executeInTransaction(em -> {
    User user = new User("john", "john@example.com");
    em.persist(user);
    return user;
});

// Manual transaction control
EntityManager em = EntityManagerProvider.getEntityManager();
try {
    em.getTransaction().begin();
    // your operations
    em.getTransaction().commit();
} finally {
    EntityManagerProvider.closeEntityManager();
}
```

### Export Features

```java
ExportService exportService = new ExportService();

// Export to CSV
List<User> users = userRepo.findAll();
exportService.exportToCsv(users, "users.csv");

// Export to Excel
exportService.exportToExcel(users, "users.xlsx");

// Export to JSON
exportService.exportToJson(users, "users.json");
```

### Backup Features

```java
BackupService backupService = new BackupService();

// Create backup
backupService.createBackup("./backups/myapp_backup");

// Restore backup
backupService.restoreBackup("./backups/myapp_backup_20240101_120000.sql");

// Compressed backup
backupService.createCompressedBackup("./backups/myapp_backup");
```

## 🎯 Key Differences from Handlers

### ❌ Old Way (with handlers):
```java
ModernJpaDatabaseHandler handler = new ModernJpaDatabaseHandler();
handler.save(user);
List<User> users = handler.findAll(User.class);
```

### ✅ New Way (Spring Boot style):
```java
JpaRepository<User, Long> userRepo = Repositories.of(User.class);
userRepo.save(user);
List<User> users = userRepo.findAll();
```

## 📋 Configuration Properties Reference

| Property | Default | Description |
|----------|---------|-------------|
| `h2orm.database.type` | `h2` | Database type (h2, sqlite, mysql, postgresql) |
| `h2orm.database.mode` | `file` | H2 mode (file, memory) |
| `h2orm.database.path` | `./data/h2orm` | Database file path |
| `h2orm.jpa.ddl.auto` | `update` | Schema generation (create, update, validate, none) |
| `h2orm.jpa.show.sql` | `false` | Show SQL statements |
| `h2orm.pool.size` | `10` | Connection pool size |

## 🚀 Migration from Old Version

1. Replace handler initialization:
   ```java
   // Old
   ModernJpaDatabaseHandler handler = new ModernJpaDatabaseHandler();
   
   // New
   EntityManagerProvider.initialize();
   JpaRepository<User, Long> userRepo = Repositories.of(User.class);
   ```

2. Replace method calls:
   ```java
   // Old
   handler.save(user);
   handler.findAll(User.class);
   
   // New
   userRepo.save(user);
   userRepo.findAll();
   ```

3. Keep your entities unchanged - they work as-is!

## 🎉 You're Ready!

Your H2-ORM library now works exactly like Spring Boot JPA. No more handlers needed - just pure, clean JPA repository pattern!
