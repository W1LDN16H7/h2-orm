package h2.orm.config;

/**
 * Database configuration class for different database types
 */
public class DatabaseConfig {
    private String driverClass;
    private String url;
    private String username;
    private String password;
    private String dialect;
    private String ddlAuto = "update"; // create, create-drop, update, validate, none
    private boolean showSql = false;
    private boolean formatSql = false;
    private int poolSize = 10;
    private int batchSize = 20;
    private boolean useSecondLevelCache = true;
    private boolean useQueryCache = true;

    // H2 Database Configuration
    public static DatabaseConfig h2() {
        DatabaseConfig config = new DatabaseConfig();
        config.driverClass = "org.h2.Driver";
        config.url = "jdbc:h2:./app;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;AUTO_SERVER=TRUE";
        config.username = "sa";
        config.password = "";
        config.dialect = "org.hibernate.dialect.H2Dialect";
        return config;
    }

    // MySQL Database Configuration
    public static DatabaseConfig mysql(String host, int port, String database, String username, String password) {
        DatabaseConfig config = new DatabaseConfig();
        config.driverClass = "com.mysql.cj.jdbc.Driver";
        config.url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                                  host, port, database);
        config.username = username;
        config.password = password;
        config.dialect = "org.hibernate.dialect.MySQL8Dialect";
        return config;
    }

    // PostgreSQL Database Configuration
    public static DatabaseConfig postgresql(String host, int port, String database, String username, String password) {
        DatabaseConfig config = new DatabaseConfig();
        config.driverClass = "org.postgresql.Driver";
        config.url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        config.username = username;
        config.password = password;
        config.dialect = "org.hibernate.dialect.PostgreSQL10Dialect";
        return config;
    }

    // SQLite Database Configuration
    public static DatabaseConfig sqlite(String filePath) {
        DatabaseConfig config = new DatabaseConfig();
        config.driverClass = "org.sqlite.JDBC";
        config.url = "jdbc:sqlite:" + filePath;
        config.username = "";
        config.password = "";
        config.dialect = "org.hibernate.dialect.SQLiteDialect";
        return config;
    }

    // Getters and Setters
    public String getDriverClass() { return driverClass; }
    public void setDriverClass(String driverClass) { this.driverClass = driverClass; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDialect() { return dialect; }
    public void setDialect(String dialect) { this.dialect = dialect; }

    public String getDdlAuto() { return ddlAuto; }
    public void setDdlAuto(String ddlAuto) { this.ddlAuto = ddlAuto; }

    public boolean isShowSql() { return showSql; }
    public void setShowSql(boolean showSql) { this.showSql = showSql; }

    public boolean isFormatSql() { return formatSql; }
    public void setFormatSql(boolean formatSql) { this.formatSql = formatSql; }

    public int getPoolSize() { return poolSize; }
    public void setPoolSize(int poolSize) { this.poolSize = poolSize; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public boolean isUseSecondLevelCache() { return useSecondLevelCache; }
    public void setUseSecondLevelCache(boolean useSecondLevelCache) { this.useSecondLevelCache = useSecondLevelCache; }

    public boolean isUseQueryCache() { return useQueryCache; }
    public void setUseQueryCache(boolean useQueryCache) { this.useQueryCache = useQueryCache; }
}
