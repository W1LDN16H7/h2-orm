/**
 * H2-ORM - Modern Spring Boot-style ORM Library for Java
 *
 * <p>H2-ORM is a lightweight, powerful Object-Relational Mapping library that brings
 * Spring Boot-style repository patterns to any Java application. Built with modern
 * Java features and designed for developer productivity.</p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li><strong>Spring Boot-like API</strong> - Familiar repository patterns and annotations</li>
 *   <li><strong>Multi-Database Support</strong> - H2, MySQL, PostgreSQL, SQLite</li>
 *   <li><strong>Zero Configuration</strong> - Works out of the box with sensible defaults</li>
 *   <li><strong>High Performance</strong> - Optimized query execution and connection pooling</li>
 *   <li><strong>Advanced Backup/Export</strong> - CSV, JSON, Excel export with compression</li>
 *   <li><strong>Powerful Querying</strong> - Dynamic queries, sorting, pagination</li>
 *   <li><strong>Transaction Management</strong> - Automatic rollback and error handling</li>
 * </ul>
 *
 * <h2>Quick Start:</h2>
 * <pre>{@code
 * // Start H2-ORM
 * H2ORM.startInMemory();
 *
 * // Get repository - just like Spring Boot!
 * JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
 *
 * // Use it like Spring Data JPA
 * User user = new User("john", "john@example.com");
 * userRepo.save(user);
 *
 * List<User> users = userRepo.findAll();
 * Page<User> userPage = userRepo.findAll(PageRequest.of(0, 10));
 * }</pre>
 *
 * <h2>Main Classes:</h2>
 * <ul>
 *   <li>{@link h2.orm.H2ORM} - Main entry point for starting/stopping and utilities</li>
 *   <li>{@link h2.orm.core.repository.JpaRepository} - Spring Boot-style repository interface</li>
 *   <li>{@link h2.orm.core.DatabaseInitializer} - Low-level database operations</li>
 *   <li>{@link h2.orm.core.service.BackupService} - Backup and export functionality</li>
 * </ul>
 *
 * @author W1LDN16H7
 * @version 1.0.0
 * @since 1.0.0
 * @see <a href="https://github.com/W1LDN16H7/h2-orm">GitHub Repository</a>
 */
package h2.orm;
