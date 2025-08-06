package h2.orm.exception;

/**
 * Base exception for all H2-ORM related errors
 * Provides user-friendly error messages and suggestions
 */
public class H2OrmException extends RuntimeException {

    private final String userFriendlyMessage;
    private final String suggestion;

    public H2OrmException(String message) {
        super(message);
        this.userFriendlyMessage = message;
        this.suggestion = null;
    }

    public H2OrmException(String message, String userFriendlyMessage) {
        super(message);
        this.userFriendlyMessage = userFriendlyMessage;
        this.suggestion = null;
    }

    public H2OrmException(String message, String userFriendlyMessage, String suggestion) {
        super(message);
        this.userFriendlyMessage = userFriendlyMessage;
        this.suggestion = suggestion;
    }

    public H2OrmException(String message, Throwable cause) {
        super(message, cause);
        this.userFriendlyMessage = message;
        this.suggestion = null;
    }

    public H2OrmException(String message, String userFriendlyMessage, String suggestion, Throwable cause) {
        super(message, cause);
        this.userFriendlyMessage = userFriendlyMessage;
        this.suggestion = suggestion;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public String getSuggestion() {
        return suggestion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("H2-ORM Error: ").append(userFriendlyMessage);

        if (suggestion != null && !suggestion.isEmpty()) {
            sb.append("\n💡 Suggestion: ").append(suggestion);
        }

        if (getCause() != null) {
            sb.append("\n🔍 Technical details: ").append(getCause().getMessage());
        }

        return sb.toString();
    }
}
