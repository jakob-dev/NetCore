package de.jakob.netcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jakob.netcore.api.NetCoreAPI;
import de.jakob.netcore.api.database.DatabaseSettings;
import de.jakob.netcore.api.database.DatabaseType;
import de.jakob.netcore.api.redis.RedisSettings;
import de.jakob.netcore.common.database.DefaultDatabaseManager;
import de.jakob.netcore.common.redis.SimpleRedisProvider;
import de.jakob.netcore.velocity.config.ConfigManager;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = "netcore", name = "NetCore", version = "1.0.0", authors = {"EinfachJaakob"})
public class NetCore {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;

    private ConfigManager configManager;
    private ConfigurationNode config;

    private DefaultDatabaseManager databaseManager;
    private SimpleRedisProvider redisProvider;
    private NetCoreAPI netCoreAPI;

    @Inject
    public NetCore(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory, PluginContainer pluginContainer) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.pluginContainer = pluginContainer;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {

        logger.info("Loading config...");
        configManager = new ConfigManager(dataDirectory, logger);
        config = configManager.getConfig("config.yml");

        logger.info("Loading database provider...");

        if (!setupDatabase()) {
            logger.error("Database provider could not be initialized. See the error above for more info.");
            return;
        }

        logger.info("Loading redis provider...");
        if (!setupRedis()) {
            logger.error("Redis provider could not be initialized. See the error above for more info.");
            return;
        }

        logger.info("Creating API instance...");
        netCoreAPI = new NetCoreAPI(databaseManager, redisProvider);
        NetCoreAPI.setInstance(netCoreAPI);


        Optional<String> pluginVersion = pluginContainer.getDescription().getVersion();
        sendConsoleMessage("<aqua>   _  __    __  <gray>_____            ");
        sendConsoleMessage("<aqua>  / |/ /__ / /_<gray>/ ___/__  _______      <gray>Version <aqua>" + pluginVersion.orElse("unknown"));
        sendConsoleMessage("<aqua> /    / -_) __<gray>/ /__/ _ \\/ __/ -_)     <gray>Running on Proxy - <aqua>Velocity");
        sendConsoleMessage("<aqua>/_/|_/\\__/\\__/<gray>\\___/\\___/_/  \\__/ ");
        sendConsoleMessage("                                 ");

    }

    public boolean setupDatabase() {

        DatabaseSettings databaseSettings = new DatabaseSettings(
                config.node("Database", "host").getString(),
                config.node("Database", "port").getInt(),
                config.node("Database", "database").getString(),
                config.node("Database", "username").getString(),
                config.node("Database", "password").getString(),
                config.node("Database", "max-pool-size").getInt(),
                config.node("Database", "connection-timeout").getInt(),
                config.node("Database", "database-directory").getString()
        );

        DatabaseType databaseType = DatabaseType.valueOf(config.node("Database", "provider").getString("MariaDB").toUpperCase());
        databaseManager = new DefaultDatabaseManager(databaseSettings, databaseType);

        try {
            databaseManager.getGlobalDatabaseProvider().connect();
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return false;
        }
    }

    public boolean setupRedis() {
        RedisSettings redisSettings = new RedisSettings(
                config.node("Redis", "host").getString(),
                config.node("Redis", "port").getInt(),
                config.node("Redis", "password").getString(),
                config.node("Redis", "database").getInt(),
                config.node("Redis", "max-pool-size").getInt(),
                config.node("Redis", "connection-timeout").getInt()
        );

        redisProvider = new SimpleRedisProvider(redisSettings);
        try {
            redisProvider.connect();
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return false;
        }

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (databaseManager != null && databaseManager.getGlobalDatabaseProvider() != null) {
            logger.info("Closing database provider...");
            databaseManager.getGlobalDatabaseProvider().disconnect();
        }

        if (redisProvider != null) {
            logger.info("Closing redis provider...");
            redisProvider.disconnect();
        }
    }

    public void sendConsoleMessage(String text) {
        proxyServer.getConsoleCommandSource().sendRichMessage(text);
    }


    public ConfigManager getConfigManager() {
        return configManager;
    }

    public NetCoreAPI getNetCoreAPI() {
        return netCoreAPI;
    }

    public DefaultDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SimpleRedisProvider getRedisProvider() {
        return redisProvider;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }
}
