/**
 * H2-ORM Repository Package
 *
 * <p>This package provides Spring Boot-style repository interfaces and implementations.
 * You can use these repositories exactly like you would in Spring Data JPA.</p>
 *
 * <h2>Repository Examples:</h2>
 * <pre>{@code
 * // Get a repository (like @Autowired in Spring Boot)
 * JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);
 *
 * // Basic CRUD operations
 * User user = userRepo.save(new User("john", "john@example.com"));
 * Optional<User> found = userRepo.findById(1L);
 * List<User> allUsers = userRepo.findAll();
 * userRepo.delete(user);
 *
 * // Counting and existence checks
 * long count = userRepo.count();
 * boolean exists = userRepo.existsById(1L);
 *
 * // Batch operations
 * List<User> users = Arrays.asList(user1, user2, user3);
 * userRepo.saveAll(users);
 * userRepo.deleteAll();
 * }</pre>
 *
 * <h2>Main Interfaces:</h2>
 * <ul>
 *   <li>{@link h2.orm.core.repository.JpaRepository} - Main repository interface</li>
 *   <li>{@link h2.orm.core.repository.Repositories} - Repository factory</li>
 * </ul>
 *
 * @author H2-ORM Team
 * @version 1.0.0
 * @since 1.0.0
 */
package h2.orm.core.repository;
