package h2.orm.config;

/**
 * Fluent configuration builder for H2-ORM database setup
 * Integrates with DatabaseProperties for flexible configuration management
 */
public class DatabaseConfigBuilder {
    private final DatabaseProperties properties;
    private String databaseType;
    private String mode;
    private String path;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String password;
    private String ddlAuto;
    private Boolean showSql;
    private Boolean formatSql;
    private Integer poolSize;
    private Integer batchSize;
    private Boolean useSecondLevelCache;
    private Boolean useQueryCache;

    public DatabaseConfigBuilder() {
        this.properties = DatabaseProperties.getInstance();
    }

    public static DatabaseConfigBuilder create() {
        return new DatabaseConfigBuilder();
    }

    public static DatabaseConfigBuilder fromProperties() {
        return new DatabaseConfigBuilder().loadFromProperties();
    }

    public static DatabaseConfigBuilder fromPropertiesFile(String filename) {
        return new DatabaseConfigBuilder()
                .loadFromPropertiesFile(filename)
                .loadFromProperties();
    }

    public DatabaseConfigBuilder loadFromPropertiesFile(String filename) {
        properties.loadFromFile(filename);
        return this;
    }

    public DatabaseConfigBuilder loadFromProperties() {
        this.databaseType = properties.getProperty(DatabaseProperties.Keys.DATABASE_TYPE);
        this.mode = properties.getProperty(DatabaseProperties.Keys.DATABASE_MODE);
        this.path = properties.getProperty(DatabaseProperties.Keys.DATABASE_PATH);
        this.host = properties.getProperty(DatabaseProperties.Keys.DATABASE_HOST);
        this.databaseName = properties.getProperty(DatabaseProperties.Keys.DATABASE_NAME);
        this.username = properties.getProperty(DatabaseProperties.Keys.DATABASE_USERNAME);
        this.password = properties.getProperty(DatabaseProperties.Keys.DATABASE_PASSWORD);
        this.ddlAuto = properties.getProperty(DatabaseProperties.Keys.JPA_DDL_AUTO);

        String portStr = properties.getProperty(DatabaseProperties.Keys.DATABASE_PORT);
        if (portStr != null) {
            this.port = Integer.parseInt(portStr);
        }

        this.showSql = properties.getBooleanProperty(DatabaseProperties.Keys.JPA_SHOW_SQL, false);
        this.formatSql = properties.getBooleanProperty(DatabaseProperties.Keys.JPA_FORMAT_SQL, false);
        this.poolSize = properties.getIntProperty(DatabaseProperties.Keys.POOL_SIZE, 10);
        this.batchSize = properties.getIntProperty(DatabaseProperties.Keys.JPA_BATCH_SIZE, 20);
        this.useSecondLevelCache = properties.getBooleanProperty(DatabaseProperties.Keys.JPA_CACHE_USE_SECOND_LEVEL, true);
        this.useQueryCache = properties.getBooleanProperty(DatabaseProperties.Keys.JPA_CACHE_USE_QUERY, true);

        return this;
    }

    // Fluent API methods
    public DatabaseConfigBuilder h2() {
        this.databaseType = "h2";
        return this;
    }

    public DatabaseConfigBuilder sqlite() {
        this.databaseType = "sqlite";
        return this;
    }

    public DatabaseConfigBuilder mysql() {
        this.databaseType = "mysql";
        return this;
    }

    public DatabaseConfigBuilder postgresql() {
        this.databaseType = "postgresql";
        return this;
    }

    public DatabaseConfigBuilder inMemory() {
        this.mode = "memory";
        return this;
    }

    public DatabaseConfigBuilder fileBased() {
        this.mode = "file";
        return this;
    }

    public DatabaseConfigBuilder path(String path) {
        this.path = path;
        return this;
    }

    public DatabaseConfigBuilder host(String host) {
        this.host = host;
        return this;
    }

    public DatabaseConfigBuilder port(int port) {
        this.port = port;
        return this;
    }

    public DatabaseConfigBuilder database(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public DatabaseConfigBuilder credentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public DatabaseConfigBuilder ddlAuto(String ddlAuto) {
        this.ddlAuto = ddlAuto;
        return this;
    }

    public DatabaseConfigBuilder showSql(boolean showSql) {
        this.showSql = showSql;
        return this;
    }

    public DatabaseConfigBuilder formatSql(boolean formatSql) {
        this.formatSql = formatSql;
        return this;
    }

    public DatabaseConfigBuilder poolSize(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public DatabaseConfigBuilder batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public DatabaseConfigBuilder enableCache() {
        this.useSecondLevelCache = true;
        this.useQueryCache = true;
        return this;
    }

    public DatabaseConfigBuilder disableCache() {
        this.useSecondLevelCache = false;
        this.useQueryCache = false;
        return this;
    }

    public DatabaseConfiguration build() {
        DatabaseConfiguration config = new DatabaseConfiguration();

        // Set database type and connection details
        String dbType = this.databaseType != null ? this.databaseType : "h2";
        String dbMode = this.mode != null ? this.mode : "file";

        switch (dbType.toLowerCase()) {
            case "h2":
                buildH2Config(config, dbMode);
                break;
            case "sqlite":
                buildSqliteConfig(config);
                break;
            case "mysql":
                buildMysqlConfig(config);
                break;
            case "postgresql":
                buildPostgresqlConfig(config);
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }

        // Set common configuration
        if (ddlAuto != null) config.setDdlAuto(ddlAuto);
        if (showSql != null) config.setShowSql(showSql);
        if (formatSql != null) config.setFormatSql(formatSql);
        if (poolSize != null) config.setPoolSize(poolSize);
        if (batchSize != null) config.setBatchSize(batchSize);
        if (useSecondLevelCache != null) config.setUseSecondLevelCache(useSecondLevelCache);
        if (useQueryCache != null) config.setUseQueryCache(useQueryCache);

        return config;
    }

    private void buildH2Config(DatabaseConfiguration config, String mode) {
        config.setDriverClass("org.h2.Driver");
        config.setDialect("org.hibernate.dialect.H2Dialect");

        String dbPath = this.path != null ? this.path : "./data/h2orm";
        String url;

        if ("memory".equals(mode)) {
            url = "jdbc:h2:mem:h2orm;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false";
        } else {
            url = String.format("jdbc:h2:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;AUTO_SERVER=TRUE", dbPath);
        }

        config.setUrl(url);
        config.setUsername(this.username != null ? this.username : "sa");
        config.setPassword(this.password != null ? this.password : "");
    }

    private void buildSqliteConfig(DatabaseConfiguration config) {
        config.setDriverClass("org.sqlite.JDBC");
        config.setDialect("org.hibernate.dialect.SQLiteDialect");

        String dbPath = this.path != null ? this.path : "./data/h2orm.db";
        config.setUrl("jdbc:sqlite:" + dbPath);
        config.setUsername("");
        config.setPassword("");
    }

    private void buildMysqlConfig(DatabaseConfiguration config) {
        config.setDriverClass("com.mysql.cj.jdbc.Driver");
        config.setDialect("org.hibernate.dialect.MySQL8Dialect");

        String host = this.host != null ? this.host : "localhost";
        int port = this.port != null ? this.port : 3306;
        String database = this.databaseName != null ? this.databaseName : "h2orm";

        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                host, port, database);

        config.setUrl(url);
        config.setUsername(this.username != null ? this.username : "root");
        config.setPassword(this.password != null ? this.password : "");
    }

    private void buildPostgresqlConfig(DatabaseConfiguration config) {
        config.setDriverClass("org.postgresql.Driver");
        config.setDialect("org.hibernate.dialect.PostgreSQL10Dialect");

        String host = this.host != null ? this.host : "localhost";
        int port = this.port != null ? this.port : 5432;
        String database = this.databaseName != null ? this.databaseName : "h2orm";

        String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);

        config.setUrl(url);
        config.setUsername(this.username != null ? this.username : "postgres");
        config.setPassword(this.password != null ? this.password : "");
    }
}
