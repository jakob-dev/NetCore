package de.jakob.netcore.common.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.jakob.netcore.api.database.DatabaseProvider;
import de.jakob.netcore.api.database.DatabaseSettings;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class NetCoreDatabaseProvider implements DatabaseProvider {

    protected final DatabaseSettings settings;
    protected HikariDataSource hikariDataSource;

    protected NetCoreDatabaseProvider(DatabaseSettings settings) {
        this.settings = settings;
    }

    @Override
    public void connect() {
        HikariConfig config = new HikariConfig();

        config.setPoolName("netcore-" + settings.database());
        config.setJdbcUrl(getJdbcUrl());
        config.setDriverClassName(getDriverClass());

        config.setMaximumPoolSize(settings.maxPoolSize());
        config.setConnectionTimeout(settings.connectionTimeout());

        applyConfigSettings(config);

        if (settings.hikariSettings() != null)
            for (String property : settings.hikariSettings().keySet()) {
                config.addDataSourceProperty(property, settings.hikariSettings().get(property));
            }

        this.hikariDataSource = new HikariDataSource(config);
    }

    @Override
    public void disconnect() {
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
        }
    }

    @Override
    public boolean isConnected() {
        return hikariDataSource != null && !hikariDataSource.isClosed();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (hikariDataSource == null) throw new SQLException("Database is not connected!");
        return hikariDataSource.getConnection();
    }

    protected abstract String getJdbcUrl();

    protected abstract String getDriverClass();

    protected abstract void applyConfigSettings(HikariConfig config);
}
