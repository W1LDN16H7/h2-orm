package h2.orm.exception.types;

import h2.orm.exception.H2OrmException;

/**
 * Exception thrown when configuration is invalid
 */
public class ConfigurationException extends H2OrmException {

    public ConfigurationException(String message, String suggestion) {
        super(
            "Configuration error",
            message,
            suggestion
        );
    }

    public ConfigurationException(String message, String suggestion, Throwable cause) {
        super(
            "Configuration error",
            message,
            suggestion,
            cause
        );
    }
}
