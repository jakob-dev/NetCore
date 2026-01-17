package de.jakob.netcore.common.database.provider;

import com.zaxxer.hikari.HikariConfig;
import de.jakob.netcore.api.database.DatabaseSettings;
import de.jakob.netcore.common.database.HikariDatabaseProvider;

public class MySQLProvider extends HikariDatabaseProvider {

    public MySQLProvider(DatabaseSettings settings) {
        super(settings);
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:mysql://" + settings.host() + ":" + settings.port() + "/" + settings.database() + "?useSSL=false";
    }

    @Override
    protected String getDriverClass() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    protected void applyConfigSettings(HikariConfig config) {
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
    }
}
