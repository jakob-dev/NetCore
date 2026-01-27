package de.jakob.netcore.common.user;

import com.google.gson.Gson;
import de.jakob.netcore.api.database.DatabaseProvider;
import de.jakob.netcore.api.database.queries.CreateTableQuery;
import de.jakob.netcore.api.database.queries.InsertQuery;
import de.jakob.netcore.api.database.queries.Query;
import de.jakob.netcore.api.database.queries.SelectQuery;
import de.jakob.netcore.api.database.queries.UpdateQuery;
import de.jakob.netcore.api.redis.RedisProvider;
import de.jakob.netcore.api.user.User;
import de.jakob.netcore.api.user.UserManager;
import de.jakob.netcore.api.user.UserSettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultUserManager implements UserManager {

    private final DatabaseProvider databaseProvider;
    private final RedisProvider redisProvider;
    private final Map<UUID, User> localCache = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();
    private final long cacheDuration;


    public DefaultUserManager(DatabaseProvider databaseProvider, RedisProvider redisProvider, int cacheDuration) {
        this.databaseProvider = databaseProvider;
        this.redisProvider = redisProvider;
        this.cacheDuration = cacheDuration * 60L;

        CreateTableQuery createTableSQL = new CreateTableQuery("netcore_users")
                .ifNotExists()
                .column("uuid", "VARCHAR(36) NOT NULL")
                .column("username", "VARCHAR(20) NOT NULL")
                .column("ip_address", "VARCHAR(39) NOT NULL")
                .column("first_join", "BIGINT NOT NULL")
                .column("last_join", "BIGINT NOT NULL")
                .column("playtime", "BIGINT NOT NULL")
                .column("settings", "VARCHAR(4096) NOT NULL")
                .primaryKey("uuid");

        try {
            Query createTable = new Query(databaseProvider, createTableSQL);
            createTable.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getCachedUser(UUID uuid) {
        return localCache.get(uuid);
    }

    @Override
    public User getCachedUser(String name) {
        return localCache.values().stream()
                .filter(user -> user.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<User> getCachedUsers() {
        return new HashSet<>(localCache.values());
    }

    @Override
    public CompletableFuture<UUID> lookupUUID(String username) {
        return CompletableFuture.supplyAsync(() -> {

            String redisKey = "usernames:" + username.toLowerCase();
            String cachedUUID = redisProvider.get(redisKey);
            if (cachedUUID != null) {
                try {
                    return UUID.fromString(cachedUUID);
                } catch (IllegalArgumentException ignored) {
                }
            }


            SelectQuery selectQuery = new SelectQuery("netcore_users")
                    .where("username = '" + username + "'")
                    .column("uuid");
            try {
                Query query = new Query(databaseProvider, selectQuery);
                ResultSet resultSet = query.executeQuery();
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("uuid"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<String> lookupUsername(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // Check Redis L2
            String redisKey = "users:" + uuid.toString();
            String cachedJson = redisProvider.get(redisKey);
            if (cachedJson != null) {
                DefaultUser user = gson.fromJson(cachedJson, DefaultUser.class);
                if (user != null) {
                    return user.getName();
                }
            }

            SelectQuery selectQuery = new SelectQuery("netcore_users")
                    .where("uuid = '" + uuid + "'")
                    .column("username");
            try {
                Query query = new Query(databaseProvider, selectQuery);
                ResultSet resultSet = query.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("username");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<User> loadUser(UUID uuid) {
        // Fallback method for generic API usage
        return handleServerLogin(uuid);
    }

    @Override
    public CompletableFuture<User> loadUser(String name) {
        User cached = getCachedUser(name);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return lookupUUID(name).thenCompose(uuid -> {
            if (uuid == null) return CompletableFuture.completedFuture(null);
            return loadUser(uuid);
        });
    }

    // --- Lazy Load Logic ---

    @Override
    public CompletableFuture<User> handleProxyLogin(UUID uuid, String name, String ip) {
        return CompletableFuture.supplyAsync(() -> {
            String redisKey = "users:" + uuid.toString();
            String redisData = redisProvider.get(redisKey);
            DefaultUser user;
            long now = System.currentTimeMillis();

            if (redisData != null) {

                user = gson.fromJson(redisData, DefaultUser.class);

                // Update Session Data
                user = new DefaultUser(
                        user.getUUID(),
                        name, // Update Name
                        ip,   // Update IP
                        user.getSettings(),
                        user.getFirstJoinTimestamp(),
                        now,  // New Session Start
                        user.getPlayTime(),
                        true // Online
                );

            } else {

                user = loadFromDatabase(uuid);

                if (user == null) {

                    UserSettings defaultSettings = new UserSettings("GRAY");
                    user = new DefaultUser(uuid, name, ip, defaultSettings, now, now, 0, true);
                    insertUserToDatabaseSync(user);
                } else {

                    user = new DefaultUser(
                            user.getUUID(),
                            name,
                            ip,
                            user.getSettings(),
                            user.getFirstJoinTimestamp(),
                            now,
                            user.getPlayTime(),
                            true // Online
                    );

                    updateUserMetadata(user);
                }
            }

            saveToRedis(user);

            localCache.put(uuid, user);

            return user;
        });
    }

    @Override
    public CompletableFuture<User> handleServerLogin(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String redisKey = "users:" + uuid.toString();
            String redisData = redisProvider.get(redisKey);

            if (redisData != null) {
                DefaultUser user = gson.fromJson(redisData, DefaultUser.class);

                long playTime = user.getPlayTime();
                if (user.isOnline()) {
                    playTime -= user.getSessionTime();
                }

                user = new DefaultUser(
                        user.getUUID(),
                        user.getName(),
                        user.getIPAddress(),
                        user.getSettings(),
                        user.getFirstJoinTimestamp(),
                        user.getLastJoin(),
                        playTime,
                        true
                );
                
                localCache.put(uuid, user);
                return user;
            }

            DefaultUser user = loadFromDatabase(uuid);
            if (user != null) {
                // Force online for local cache
                 user = new DefaultUser(
                        user.getUUID(),
                        user.getName(),
                        user.getIPAddress(),
                        user.getSettings(),
                        user.getFirstJoinTimestamp(),
                        user.getLastJoin(),
                        user.getPlayTime(), 
                        true // Force Online
                );
                
                localCache.put(uuid, user);

                saveToRedis(user);
                return user;
            }

            return null;
        });
    }

    @Override
    public void handleProxyQuit(UUID uuid) {
        User user = localCache.remove(uuid);
        if (user != null) {

            long totalPlayTime = user.getPlayTime();


            DefaultUser finalUser = new DefaultUser(
                    user.getUUID(),
                    user.getName(),
                    user.getIPAddress(),
                    user.getSettings(),
                    user.getFirstJoinTimestamp(),
                    user.getLastJoin(),
                    totalPlayTime,
                    false
            );


            saveUserToDatabase(finalUser);


            saveToRedis(finalUser);
        }
    }

    @Override
    public void handleServerQuit(UUID uuid) {
        localCache.remove(uuid);
    }


    private void saveToRedis(User user) {
        String redisKey = "users:" + user.getUUID().toString();
        redisProvider.set(redisKey, gson.toJson(user), cacheDuration);
        // Also cache username mapping
        redisProvider.set("usernames:" + user.getName().toLowerCase(), user.getUUID().toString(), cacheDuration);
    }

    private DefaultUser loadFromDatabase(UUID uuid) {
        SelectQuery selectQuery = new SelectQuery("netcore_users").where("uuid = '" + uuid + "'");
        try {
            Query query = new Query(databaseProvider, selectQuery);
            ResultSet resultSet = query.executeQuery();
            if (resultSet.next()) {
                String username = resultSet.getString("username");
                String ip = resultSet.getString("ip_address");
                long firstJoin = resultSet.getLong("first_join");
                long lastJoin = resultSet.getLong("last_join");
                long playtime = resultSet.getLong("playtime");
                String settingsJson = resultSet.getString("settings");
                UserSettings settings = gson.fromJson(settingsJson, UserSettings.class);

                return new DefaultUser(uuid, username, ip, settings, firstJoin, lastJoin, playtime, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void insertUserToDatabaseSync(User user) {
        try {
            InsertQuery insertQuery = new InsertQuery("netcore_users")
                    .value("uuid", "'" + user.getUUID().toString() + "'")
                    .value("username", "'" + user.getName() + "'")
                    .value("ip_address", "'" + user.getIPAddress() + "'")
                    .value("first_join", String.valueOf(user.getFirstJoinTimestamp()))
                    .value("last_join", String.valueOf(user.getLastJoin()))
                    .value("playtime", String.valueOf(user.getPlayTime()))
                    .value("settings", "'" + gson.toJson(user.getSettings()) + "'");

            new Query(databaseProvider, insertQuery).executeUpdate();
            System.out.println("Executing insert");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateUserMetadata(User user) {
        CompletableFuture.runAsync(() -> {
            try {
                UpdateQuery updateQuery = new UpdateQuery("netcore_users")
                        .where("uuid = '" + user.getUUID().toString() + "'")
                        .set("username", "'" + user.getName() + "'")
                        .set("ip_address", "'" + user.getIPAddress() + "'")
                        .set("last_join", String.valueOf(user.getLastJoin()));
                new Query(databaseProvider, updateQuery).executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void saveUserToDatabase(User user) {
        CompletableFuture.runAsync(() -> {
            try {
                UpdateQuery updateQuery = new UpdateQuery("netcore_users")
                        .where("uuid = '" + user.getUUID().toString() + "'")
                        .set("username", "'" + user.getName() + "'")
                        .set("ip_address", "'" + user.getIPAddress() + "'")
                        .set("last_join", String.valueOf(user.getLastJoin()))
                        .set("playtime", String.valueOf(user.getPlayTime()))
                        .set("settings", "'" + gson.toJson(user.getSettings()) + "'");

                new Query(databaseProvider, updateQuery).executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean isUserCached(UUID uuid) {
        return localCache.containsKey(uuid);
    }

    @Override
    public boolean isUserCached(String name) {
        return getCachedUser(name) != null;
    }

}
