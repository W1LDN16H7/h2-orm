/**
 * H2-ORM Main API Package
 *
 * <p>This package contains the main entry point and primary API for the H2-ORM library.
 * The library provides a Spring Boot-style ORM experience with automatic repository generation,
 * transaction management, and backup/export capabilities.</p>
 *
 * <h2>Quick Start Example:</h2>
 * <pre>{@code
 * // 1. Start the database
 * H2ORM.start("./data/myapp");
 *
 * // 2. Get repository (like Spring Boot @Autowired)
 * JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
 *
 * // 3. Use repository methods
 * User user = userRepo.save(new User("john", "john@example.com"));
 * List<User> users = userRepo.findAll();
 *
 * // 4. Export and backup
 * H2ORM.exportToCsv(User.class, "./exports/users.csv");
 * H2ORM.backup("./backups/myapp_backup");
 *
 * // 5. Clean shutdown
 * H2ORM.stop();
 * }</pre>
 *
 * <h2>Main Classes:</h2>
 * <ul>
 *   <li>{@link h2.orm.H2ORM} - Main entry point and API facade</li>
 * </ul>
 *
 * @author H2-ORM Team
 * @version 1.0.0
 * @since 1.0.0
 */
package h2.orm;
