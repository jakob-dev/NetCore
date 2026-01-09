package de.jakob.netcore.api.database;

import java.io.File;
import java.util.HashMap;

public record DatabaseSettings(
        String host,
        int port,
        String database,
        String username,
        String password,
        int maxPoolSize,
        int connectionTimeout,
        String databaseDirectory,
        HashMap<String, String> hikariSettings
) {

    public DatabaseSettings(String host, int port, String database, String username, String password, int maxPoolSize, int connectionTimeout, String databaseDirectory) {
        this(host, port, database, username, password, maxPoolSize, connectionTimeout, databaseDirectory, null);
    }

    public DatabaseSettings(String databaseDirectory, String fileName) {
        this(null, 0, fileName, null, null, 1, 5000, databaseDirectory, null);
    }

}
