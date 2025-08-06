package h2.orm.exception.types;

import h2.orm.exception.H2OrmException;

/**
 * Exception thrown when database is not initialized
 */
public class DatabaseNotInitializedException extends H2OrmException {

    public DatabaseNotInitializedException() {
        super(
            "Database not initialized",
            "The H2-ORM database connection has not been initialized yet.",
            "Call H2ORM.start() or EntityManagerProvider.initialize() before using repositories."
        );
    }
}
