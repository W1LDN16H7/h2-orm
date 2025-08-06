// H2-ORM Security Fixes Implementation

// 1. CRITICAL: Fix SQL Injection in BackupService
public class SecureBackupService {

    private static final Pattern SAFE_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9._/-]+$");
    private static final String BACKUP_BASE_DIR = "./backups/";

    public void backup(String backupPath) {
        // Input validation and sanitization
        String sanitizedPath = sanitizeAndValidatePath(backupPath);
        if (sanitizedPath == null) {
            throw new SecurityException("Invalid backup path: " + backupPath);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFile = BACKUP_BASE_DIR + sanitizedPath + "_" + timestamp + ".sql";

        // Ensure path is within allowed directory
        Path normalizedPath = Paths.get(backupFile).normalize();
        if (!normalizedPath.startsWith(Paths.get(BACKUP_BASE_DIR).normalize())) {
            throw new SecurityException("Path traversal attempt detected");
        }

        // Use parameterized approach for H2 SCRIPT command
        TransactionManager.executeInTransaction(em -> {
            var session = em.unwrap(org.hibernate.Session.class);
            session.doWork(connection -> {
                // Use prepared statement approach
                String sql = "SCRIPT TO ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, normalizedPath.toString());
                    stmt.execute();
                    logger.info("Database backup created securely");
                }
            });
            return null;
        });
    }

    private String sanitizeAndValidatePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        // Remove dangerous characters
        String sanitized = path.replaceAll("[^a-zA-Z0-9._/-]", "");

        // Check against whitelist pattern
        if (!SAFE_PATH_PATTERN.matcher(sanitized).matches()) {
            return null;
        }

        return sanitized;
    }
}

// 2. CRITICAL: Secure Query Builder
public class SecureQueryBuilder {

    private static final Set<String> ALLOWED_ENTITY_NAMES = Set.of(
        "User", "Product", "Order" // Whitelist known entities
    );

    public static String buildSecureQuery(Class<?> entityClass, String operation) {
        String entityName = entityClass.getSimpleName();

        // Validate entity name against whitelist
        if (!ALLOWED_ENTITY_NAMES.contains(entityName)) {
            throw new SecurityException("Unauthorized entity access: " + entityName);
        }

        // Use safe string formatting
        return String.format("SELECT e FROM %s e", entityName);
    }
}

// 3. HIGH: Input Validation Framework
public class SecurityValidator {

    public static void validatePagination(Pageable pageable) {
        if (pageable.getPageSize() > 1000) {
            throw new IllegalArgumentException("Page size too large: " + pageable.getPageSize());
        }

        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("Invalid page number: " + pageable.getPageNumber());
        }
    }

    public static void validateBatchSize(int batchSize) {
        if (batchSize > 1000) {
            throw new IllegalArgumentException("Batch size too large: " + batchSize);
        }
    }

    public static void validateSortField(String fieldName, Class<?> entityClass) {
        // Use reflection safely
        try {
            Field field = entityClass.getDeclaredField(fieldName);
            if (!field.isAnnotationPresent(Column.class)) {
                throw new SecurityException("Field not allowed for sorting: " + fieldName);
            }
        } catch (NoSuchFieldException e) {
            throw new SecurityException("Invalid sort field: " + fieldName);
        }
    }
}

// 4. MEDIUM: Secure Logging Configuration
public class SecureLogging {

    public static void logSecurely(String operation, Object... params) {
        // Sanitize parameters before logging
        Object[] sanitized = Arrays.stream(params)
            .map(SecurityValidator::sanitizeForLogging)
            .toArray();

        logger.info("Operation: {} with sanitized params: {}", operation, sanitized);
    }

    private static Object sanitizeForLogging(Object param) {
        if (param instanceof String) {
            String str = (String) param;
            // Mask sensitive patterns
            return str.replaceAll("password=\\w+", "password=***")
                     .replaceAll("jdbc:[^\\s]+", "jdbc:***");
        }
        return param;
    }
}

// 5. HIGH: Rate Limiting Implementation
@Component
public class RateLimiter {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientId, int maxRequests, Duration window) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId,
            k -> new TokenBucket(maxRequests, window));
        return bucket.tryConsume();
    }

    private static class TokenBucket {
        private final int capacity;
        private final Duration refillPeriod;
        private int tokens;
        private Instant lastRefill;

        TokenBucket(int capacity, Duration refillPeriod) {
            this.capacity = capacity;
            this.refillPeriod = refillPeriod;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            if (Duration.between(lastRefill, now).compareTo(refillPeriod) >= 0) {
                tokens = capacity;
                lastRefill = now;
            }
        }
    }
}
