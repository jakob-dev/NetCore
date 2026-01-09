package de.jakob.netcore.api.database;

public interface DatabaseManager {

    DatabaseProvider getGlobalDatabaseProvider();

    DatabaseProvider createDatabaseProvider(DatabaseSettings settings, DatabaseType databaseType);

}
