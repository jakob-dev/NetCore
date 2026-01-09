package de.jakob.netcore.spigot;

import de.jakob.netcore.api.NetCoreAPI;
import de.jakob.netcore.api.database.DatabaseSettings;
import de.jakob.netcore.api.database.DatabaseType;
import de.jakob.netcore.api.redis.RedisSettings;
import de.jakob.netcore.common.database.NetCoreDatabaseManager;
import de.jakob.netcore.common.redis.NetCoreRedisProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class NetCore extends JavaPlugin {

    private FileConfiguration config;

    private NetCoreDatabaseManager databaseManager;
    private NetCoreRedisProvider redisProvider;
    private NetCoreAPI netCoreAPI;

    @Override
    public void onEnable() {

        getLogger().info("Loading config...");
        this.saveDefaultConfig();
        config = this.getConfig();

        getLogger().info("Loading database provider...");
        if (!setupDatabase()) {
            getLogger().severe("Database provider could not be initialized. See the error above for more info.");
            return;
        }

        getLogger().info("Loading redis provider...");
        if (!setupRedis()) {
            getLogger().severe("Redis provider could not be initialized. See the error above for more info.");
            return;
        }

        getLogger().info("Creating API instance...");
        netCoreAPI = new NetCoreAPI(databaseManager, redisProvider);
        NetCoreAPI.setInstance(netCoreAPI);
    }

    public boolean setupDatabase() {
        DatabaseSettings databaseSettings = new DatabaseSettings(
                config.getString("Database.host"),
                config.getInt("Database.port"),
                config.getString("Database.database"),
                config.getString("Database.username"),
                config.getString("Database.password"),
                config.getInt("Database.max-pool-size"),
                config.getInt("Database.connection-timeout"),
                config.getString("Database.database-directory")
        );

        DatabaseType databaseType = DatabaseType.valueOf(config.getString("Database.provider", "MariaDB").toUpperCase());
        databaseManager = new NetCoreDatabaseManager(databaseSettings, databaseType);

        try {
            databaseManager.getGlobalDatabaseProvider().connect();
            return true;
        } catch (Exception e) {
            getLogger().severe(e.getMessage());
            return false;
        }
    }

    public boolean setupRedis() {
        RedisSettings redisSettings = new RedisSettings(
                config.getString("Redis.host"),
                config.getInt("Redis.port"),
                config.getString("Redis.password"),
                config.getInt("Redis.database"),
                config.getInt("Redis.max-pool-size"),
                config.getInt("Redis.connection-timeout")
        );

        redisProvider = new NetCoreRedisProvider(redisSettings);
        try {
            redisProvider.connect();
            return true;
        } catch (Exception e) {
            getLogger().severe(e.getMessage());
            return false;
        }

    }

    @Override
    public void onDisable() {

        if (databaseManager != null && databaseManager.getGlobalDatabaseProvider() != null) {
            getLogger().info("Closing database provider...");
            databaseManager.getGlobalDatabaseProvider().disconnect();
        }

        if (redisProvider != null) {
            getLogger().info("Closing redis provider...");
            redisProvider.disconnect();
        }

    }

    public NetCoreAPI getNetCoreAPI() {
        return netCoreAPI;
    }
}
