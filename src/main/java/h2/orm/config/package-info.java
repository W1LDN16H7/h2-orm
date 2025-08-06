/**
 * H2-ORM Configuration Package
 *
 * <p>This package contains database configuration classes that allow you to set up
 * connections to various databases including H2, SQLite, MySQL, and PostgreSQL.</p>
 *
 * <h2>Configuration Examples:</h2>
 * <pre>{@code
 * // H2 in-memory database
 * DatabaseConfiguration config = DatabaseConfiguration.h2InMemory();
 *
 * // H2 file-based database
 * DatabaseConfiguration config = DatabaseConfiguration.h2File("./data/myapp");
 *
 * // MySQL database
 * DatabaseConfiguration config = DatabaseConfiguration.mysql("localhost", 3306, "mydb", "user", "pass");
 *
 * // Custom configuration
 * DatabaseConfiguration config = DatabaseConfigBuilder.create()
 *     .h2()
 *     .fileBased()
 *     .path("./data/myapp")
 *     .showSql(true)
 *     .poolSize(20)
 *     .build();
 * }</pre>
 *
 * <h2>Main Classes:</h2>
 * <ul>
 *   <li>{@link h2.orm.config.DatabaseConfiguration} - Main configuration class</li>
 *   <li>{@link h2.orm.config.DatabaseConfigBuilder} - Fluent builder for configurations</li>
 * </ul>
 *
 * @author W1LDN16H7
 * @version 1.0.0
 * @since 1.0.0
 * @see <a href="https://github.com/W1LDN16H7/h2-orm">GitHub Repository</a>
 */
package h2.orm.config;
