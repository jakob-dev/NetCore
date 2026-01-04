package de.jakob.netcore.api.database;

public record DatabaseSettings(
        String host,
        int port,
        String database,
        String username,
        String password,
        int maxPoolSize,
        int connectionTimeout

) {

    public DatabaseSettings(String database, int maxPoolSize, int connectionTimeout) {
        this(null, 0, database, null, null, maxPoolSize, connectionTimeout);
    }

}
