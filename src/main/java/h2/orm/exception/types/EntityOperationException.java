package h2.orm.exception.types;

import h2.orm.exception.H2OrmException;

/**
 * Exception thrown when entity operation fails
 */
public class EntityOperationException extends H2OrmException {

    public EntityOperationException(String operation, Throwable cause) {
        super(
            "Entity operation failed: " + operation,
            "Failed to " + operation.toLowerCase() + " entity.",
            "Check if the entity is valid and database connection is working.",
            cause
        );
    }
}
