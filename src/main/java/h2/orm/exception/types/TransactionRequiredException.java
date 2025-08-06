package h2.orm.exception.types;

import h2.orm.exception.H2OrmException;

/**
 * Exception thrown when transaction is required but not active
 */
public class TransactionRequiredException extends H2OrmException {

    public TransactionRequiredException(String operation) {
        super(
            "Transaction required for operation: " + operation,
            "The operation '" + operation + "' requires an active transaction.",
            "This operation will be automatically wrapped in a transaction."
        );
    }
}
