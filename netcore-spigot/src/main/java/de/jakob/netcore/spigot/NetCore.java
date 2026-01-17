package de.jakob.netcore.spigot;

import de.jakob.netcore.api.NetCoreAPI;
import de.jakob.netcore.api.database.DatabaseSettings;
import de.jakob.netcore.api.database.DatabaseType;
import de.jakob.netcore.api.redis.RedisSettings;
import de.jakob.netcore.common.database.DefaultDatabaseManager;
import de.jakob.netcore.common.util.LuckPermsUtil;
import de.jakob.netcore.common.util.PlaceholderUtil;
import de.jakob.netcore.common.messages.MessageFactory;
import de.jakob.netcore.common.redis.SimpleRedisProvider;
import de.jakob.netcore.spigot.listeners.ChatListener;
import de.jakob.netcore.spigot.chat.ChatManager;
import de.jakob.netcore.spigot.command.NetCoreCommand;
import de.jakob.netcore.spigot.listeners.ConnectionListener;
import de.jakob.netcore.spigot.scoreboard.ScoreboardManager;
import de.jakob.netcore.spigot.tablist.TablistManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class NetCore extends JavaPlugin {

    private FileConfiguration config;
    private String serverBrand;

    private DefaultDatabaseManager databaseManager;
    private SimpleRedisProvider redisProvider;
    private NetCoreAPI netCoreAPI;

    private ScoreboardManager scoreboardManager;
    private ChatManager chatManager;
    private TablistManager tablistManager;

    @Override
    public void onEnable() {

        getLogger().info("Loading config...");
        this.saveDefaultConfig();
        config = this.getConfig();

        this.serverBrand = Bukkit.getName();


        getLogger().info("Loading database provider...");
        if (!setupDatabase()) {
            getLogger().severe("Database provider could not be initialized. See the error above for more information.");
            return;
        }

        getLogger().info("Loading redis provider...");
        if (!setupRedis()) {
            getLogger().severe("Redis provider could not be initialized. See the error above for more info.");
            return;
        }


        if (PlaceholderUtil.enabled) {
            MessageFactory.setPlaceholderParser((uuid, text) -> {
                if (uuid != null) {
                    return PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), text);
                }
                return PlaceholderAPI.setPlaceholders(null, text);
            });
            getLogger().info("PlaceholderAPI hook enabled!");
        } else {
            getLogger().warning("PlaceholderAPI not found! Scoreboards might not work correctly.");
        }

        if (LuckPermsUtil.isEnabled()) {
            getLogger().info("LuckPerms hook enabled!");
        }

        getLogger().info("Creating API instance...");
        netCoreAPI = new NetCoreAPI(databaseManager, redisProvider);
        NetCoreAPI.setInstance(netCoreAPI);

        scoreboardManager = new ScoreboardManager(this);
        chatManager = new ChatManager(this);
        tablistManager = new TablistManager(this);

        registerListeners();
        registerCommands();

        String pluginVersion = getDescription().getVersion();
        sendConsoleMessage(ChatColor.AQUA + "   _  __    __  " + ChatColor.GRAY + "_____            ");
        sendConsoleMessage(ChatColor.AQUA + "  / |/ /__ / /_" + ChatColor.GRAY + "/ ___/__  _______      " + ChatColor.GRAY + "Version " + ChatColor.AQUA + pluginVersion);
        sendConsoleMessage(ChatColor.AQUA + " /    / -_) __" + ChatColor.GRAY + "/ /__/ _ \\/ __/ -_)     " + ChatColor.GRAY + "Running on Backend - " + ChatColor.AQUA + serverBrand);
        sendConsoleMessage(ChatColor.AQUA + "/_/|_/\\__/\\__/" + ChatColor.GRAY + "\\___/\\___/_/  \\__/ ");
        sendConsoleMessage("                                 ");

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
        databaseManager = new DefaultDatabaseManager(databaseSettings, databaseType);

        try {
            databaseManager.getGlobalDatabaseProvider().connect();
            return true;
        } catch (Exception e) {
            getLogger().severe(e.getMessage());
            return false;
        }
    }

    public void reload() {

        this.saveDefaultConfig();
        this.reloadConfig();

        chatManager.reloadConfig();
        scoreboardManager.reloadConfig();
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

        redisProvider = new SimpleRedisProvider(redisSettings);
        try {
            redisProvider.connect();
            return true;
        } catch (Exception e) {
            getLogger().severe(e.getMessage());
            return false;
        }
    }


    public void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new ConnectionListener(this), this);
    }

    public void registerCommands() {

        getCommand("netcore").setExecutor(new NetCoreCommand(this));

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

    public void sendConsoleMessage(String text) {
        Bukkit.getConsoleSender().sendMessage(text);
    }

    public String getServerBrand() {
        return serverBrand;
    }

    public DefaultDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SimpleRedisProvider getRedisProvider() {
        return redisProvider;
    }


    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public de.jakob.netcore.spigot.tablist.TablistManager getTablistManager() {
        return tablistManager;
    }

    public NetCoreAPI getNetCoreAPI() {
        return netCoreAPI;
    }
}