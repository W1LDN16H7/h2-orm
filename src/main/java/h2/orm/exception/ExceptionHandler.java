package h2.orm.exception;

import h2.orm.exception.types.*;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central exception handler for H2-ORM library
 * Converts technical exceptions to user-friendly H2OrmExceptions
 */
public class ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    /**
     * Handle any exception and convert to user-friendly H2OrmException
     */
    public static H2OrmException handleException(String operation, Throwable throwable) {
        logger.error("Exception during {}: {}", operation, throwable.getMessage(), throwable);

        // Handle specific exception types
        if (throwable instanceof jakarta.persistence.TransactionRequiredException) {
            return new TransactionRequiredException(operation);
        }

        if (throwable instanceof ConstraintViolationException) {
            return handleConstraintViolation((ConstraintViolationException) throwable);
        }

        if (throwable instanceof jakarta.persistence.EntityExistsException) {
            return new EntityOperationException("save entity (entity already exists)", throwable);
        }

        if (throwable instanceof jakarta.persistence.EntityNotFoundException) {
            return new EntityOperationException("find entity (entity not found)", throwable);
        }

        if (throwable instanceof IllegalStateException &&
            throwable.getMessage() != null &&
            throwable.getMessage().contains("EntityManagerFactory not initialized")) {
            return new DatabaseNotInitializedException();
        }

        // Handle SQL/database errors by message content
        if (throwable.getMessage() != null) {
            String message = throwable.getMessage().toLowerCase();

            if (message.contains("unique") || message.contains("duplicate")) {
                return handleUniqueConstraintViolation(throwable);
            }

            if (message.contains("table") && (message.contains("doesn't exist") || message.contains("not found"))) {
                return new ConfigurationException(
                    "Database table doesn't exist.",
                    "Make sure your entities are properly configured and database schema is created."
                );
            }

            if (message.contains("connection")) {
                return new ConfigurationException(
                    "Database connection failed.",
                    "Check your database configuration and ensure the database server is running."
                );
            }
        }

        // Generic fallback
        return new RepositoryException(operation, throwable);
    }

    private static H2OrmException handleConstraintViolation(ConstraintViolationException cve) {
        String message = cve.getMessage();
        if (message != null && message.toLowerCase().contains("unique")) {
            return handleUniqueConstraintViolation(cve);
        }

        return new EntityOperationException("save entity (constraint violation)", cve);
    }

    private static H2OrmException handleUniqueConstraintViolation(Throwable throwable) {
        String message = throwable.getMessage();

        // Try to extract field name and value from error message
        String field = "field";
        String value = "unknown";

        if (message != null) {
            // Parse common unique constraint error patterns
            if (message.contains("username")) {
                field = "username";
            } else if (message.contains("email")) {
                field = "email";
            }

            // Try to extract value from error message
            int start = message.indexOf("'");
            int end = message.lastIndexOf("'");
            if (start != -1 && end != -1 && start < end) {
                value = message.substring(start + 1, end);
            }
        }

        return new UniqueConstraintException(field, value, throwable);
    }

    /**
     * Safe execution wrapper that handles exceptions
     */
    public static <T> T safeExecute(String operation, ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (H2OrmException e) {
            // Re-throw our custom exceptions as-is
            throw e;
        } catch (Exception e) {
            throw handleException(operation, e);
        }
    }

    /**
     * Safe execution wrapper for void operations
     */
    public static void safeExecute(String operation, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (H2OrmException e) {
            // Re-throw our custom exceptions as-is
            throw e;
        } catch (Exception e) {
            throw handleException(operation, e);
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
