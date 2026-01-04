package de.jakob.netcore.common.database;

import de.jakob.netcore.api.database.DatabaseManager;
import de.jakob.netcore.api.database.DatabaseProvider;
import de.jakob.netcore.api.database.DatabaseSettings;
import de.jakob.netcore.api.database.DatabaseType;
import de.jakob.netcore.common.database.impl.MariaDBProvider;
import de.jakob.netcore.common.database.impl.MySQLProvider;
import de.jakob.netcore.common.database.impl.SQLiteProvider;

import java.io.File;

public class AbstractDatabaseManager implements DatabaseManager {

    private final DatabaseProvider globalProvider;
    private final File baseDataFolder;

    public AbstractDatabaseManager(DatabaseProvider globalProvider, File baseDataFolder) {
        this.globalProvider = globalProvider;
        this.baseDataFolder = baseDataFolder;
    }

    @Override
    public DatabaseProvider getGlobalDatabaseProvider() {
        return this.globalProvider;
    }

    @Override
    public DatabaseProvider createProvider(DatabaseSettings settings, DatabaseType type) {
        return switch (type) {
            case MYSQL -> new MariaDBProvider(settings);
            case MARIADB -> new MySQLProvider(settings);
            case SQLITE -> new SQLiteProvider(settings, baseDataFolder);
            default -> throw new UnsupportedOperationException("Database type not (yet) implemented: " + type);
        };
    }
}
