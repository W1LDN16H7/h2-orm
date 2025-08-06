package h2.orm.exception.types;

import h2.orm.exception.H2OrmException;

/**
 * Exception thrown when repository operation fails
 */
public class RepositoryException extends H2OrmException {

    public RepositoryException(String operation, Throwable cause) {
        super(
            "Repository operation failed: " + operation,
            "Failed to execute repository operation: " + operation,
            "Check your entity configuration and database connection.",
            cause
        );
    }
}
