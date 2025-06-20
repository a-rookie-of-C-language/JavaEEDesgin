package site.arookieofc.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import site.arookieofc.annotation.config.Config;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtil {

    @Config("jdbc.url")
    private static String URL;

    @Config("jdbc.username")
    private static String USERNAME;

    @Config("jdbc.password")
    private static String PASSWORD;

    private static HikariDataSource dataSource;
    private static volatile boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initializeDataSource();
        }
        return dataSource.getConnection();
    }

    private static synchronized void initializeDataSource() {
        if (initialized) {
            return; // 双重检查锁定
        }
        try {
            if (URL == null) {
                throw new RuntimeException("Database URL not configured. Check config.yml and @Config annotations.");
            }
            HikariConfig config = getConfig();
            dataSource = new HikariDataSource(config);
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database connection pool: " + e.getMessage(), e);
        }
    }

    private static HikariConfig getConfig() {
        HikariConfig config = getHikariConfig();
        // MySQL特定配置
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "false"); // 修改为false
        config.addDataSourceProperty("maintainTimeStats", "false");
        return config;
    }

    private static HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(20);          // 最大连接数
        config.setAutoCommit(false);
        config.setMinimumIdle(5);               // 最小空闲连接数
        config.setConnectionTimeout(30000);     // 连接超时时间(30秒)
        config.setIdleTimeout(600000);          // 空闲连接超时时间(10分钟)
        config.setMaxLifetime(1800000);         // 连接最大生存时间(30分钟)
        config.setLeakDetectionThreshold(60000); // 连接泄漏检测阈值(1分钟)
        return config;
    }
}