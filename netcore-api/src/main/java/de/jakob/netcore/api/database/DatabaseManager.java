package de.jakob.netcore.api.database;

public interface DatabaseManager {

    DatabaseProvider getGlobalDatabaseProvider();

    DatabaseProvider createProvider(DatabaseSettings settings, DatabaseType type);

}
