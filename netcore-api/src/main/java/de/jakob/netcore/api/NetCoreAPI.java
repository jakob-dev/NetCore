package de.jakob.netcore.api;

import de.jakob.netcore.api.database.DatabaseManager;
import de.jakob.netcore.api.database.DatabaseProvider;
import de.jakob.netcore.api.redis.RedisProvider;

public class NetCoreAPI {

    private static NetCoreAPI instance;

    private final DatabaseManager databaseManager;
    private final RedisProvider redisProvider;

    public NetCoreAPI(DatabaseManager databaseManager, RedisProvider redisProvider) {
        this.databaseManager = databaseManager;
        this.redisProvider = redisProvider;
    }

    public static void setInstance(NetCoreAPI apiImplementation) {
        if (instance != null) {
            throw new UnsupportedOperationException("NetCoreAPI has already been initialized!");
        }
        instance = apiImplementation;
    }

    public static NetCoreAPI get() {
        if (instance == null) {
            throw new IllegalStateException("NetCoreAPI has not been initialized!");
        }
        return instance;

    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public RedisProvider getRedisProvider() {
        return redisProvider;
    }
}