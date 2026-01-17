package de.jakob.netcore.common.database;

import de.jakob.netcore.api.database.DatabaseManager;
import de.jakob.netcore.api.database.DatabaseProvider;
import de.jakob.netcore.api.database.DatabaseSettings;
import de.jakob.netcore.api.database.DatabaseType;
import de.jakob.netcore.common.database.provider.H2Provider;
import de.jakob.netcore.common.database.provider.MariaDBProvider;
import de.jakob.netcore.common.database.provider.MySQLProvider;
import de.jakob.netcore.common.database.provider.SQLiteProvider;

public class DefaultDatabaseManager implements DatabaseManager {

    private final DatabaseType globalProviderType;
    private final DatabaseProvider globalProvider;

    public DefaultDatabaseManager(DatabaseSettings databaseSettings, DatabaseType globalProviderType) {
        this.globalProvider = createDatabaseProvider(databaseSettings, globalProviderType);
        this.globalProviderType = globalProviderType;
    }

    @Override
    public DatabaseProvider getGlobalDatabaseProvider() {
        return this.globalProvider;
    }

    @Override
    public DatabaseProvider createDatabaseProvider(DatabaseSettings settings, DatabaseType type) {
        return switch (type) {
            case MYSQL -> new MySQLProvider(settings);
            case MARIADB -> new MariaDBProvider(settings);
            case SQLITE -> new SQLiteProvider(settings, settings.databaseDirectory());
            case H2 -> new H2Provider(settings, settings.databaseDirectory());
        };
    }

    public DatabaseType getGlobalProviderType() {
        return globalProviderType;
    }
}

