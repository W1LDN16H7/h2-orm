package h2.orm.config;

/**
 * Enhanced database configuration class with comprehensive settings
 * for H2-ORM standalone library
 */
public class DatabaseConfiguration {
    private String driverClass;
    private String url;
    private String username;
    private String password;
    private String dialect;
    private String ddlAuto = "update";
    private boolean showSql = false;
    private boolean formatSql = false;
    private int poolSize = 10;
    private int batchSize = 20;
    private boolean useSecondLevelCache = true;
    private boolean useQueryCache = true;
    private long connectionTimeout = 30000;
    private long idleTimeout = 600000;
    private long maxLifetime = 1800000;
    private int minimumIdle = 5;
    private String poolName = "H2-ORM-Pool";

    // Default constructors
    public DatabaseConfiguration() {}

    // Static factory methods for quick setup
    public static DatabaseConfiguration h2InMemory() {
        return DatabaseConfigBuilder.create()
                .h2()
                .inMemory()
                .build();
    }

    public static DatabaseConfiguration h2File(String path) {
        return DatabaseConfigBuilder.create()
                .h2()
                .fileBased()
                .path(path)
                .build();
    }

    public static DatabaseConfiguration sqlite(String path) {
        return DatabaseConfigBuilder.create()
                .sqlite()
                .path(path)
                .build();
    }

    public static DatabaseConfiguration mysql(String host, int port, String database, String username, String password) {
        return DatabaseConfigBuilder.create()
                .mysql()
                .host(host)
                .port(port)
                .database(database)
                .credentials(username, password)
                .build();
    }

    public static DatabaseConfiguration postgresql(String host, int port, String database, String username, String password) {
        return DatabaseConfigBuilder.create()
                .postgresql()
                .host(host)
                .port(port)
                .database(database)
                .credentials(username, password)
                .build();
    }

    public static DatabaseConfiguration fromProperties() {
        return DatabaseConfigBuilder.fromProperties().build();
    }

    public static DatabaseConfiguration fromPropertiesFile(String filename) {
        return DatabaseConfigBuilder.fromPropertiesFile(filename).build();
    }

    // Getters and Setters
    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getDdlAuto() {
        return ddlAuto;
    }

    public void setDdlAuto(String ddlAuto) {
        this.ddlAuto = ddlAuto;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }

    public boolean isFormatSql() {
        return formatSql;
    }

    public void setFormatSql(boolean formatSql) {
        this.formatSql = formatSql;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isUseSecondLevelCache() {
        return useSecondLevelCache;
    }

    public void setUseSecondLevelCache(boolean useSecondLevelCache) {
        this.useSecondLevelCache = useSecondLevelCache;
    }

    public boolean isUseQueryCache() {
        return useQueryCache;
    }

    public void setUseQueryCache(boolean useQueryCache) {
        this.useQueryCache = useQueryCache;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public String toString() {
        return "DatabaseConfiguration{" +
                "driverClass='" + driverClass + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", dialect='" + dialect + '\'' +
                ", ddlAuto='" + ddlAuto + '\'' +
                ", showSql=" + showSql +
                ", formatSql=" + formatSql +
                ", poolSize=" + poolSize +
                ", batchSize=" + batchSize +
                ", useSecondLevelCache=" + useSecondLevelCache +
                ", useQueryCache=" + useQueryCache +
                '}';
    }
}
