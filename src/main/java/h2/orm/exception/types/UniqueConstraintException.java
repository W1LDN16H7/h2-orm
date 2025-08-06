package h2.orm.exception.types;

import h2.orm.exception.H2OrmException;

/**
 * Exception thrown when unique constraint is violated
 */
public class UniqueConstraintException extends H2OrmException {

    public UniqueConstraintException(String field, Object value, Throwable cause) {
        super(
            "Unique constraint violation",
            "A record with " + field + " = '" + value + "' already exists.",
            "Use a different value for " + field + " or update the existing record instead.",
            cause
        );
    }
}
