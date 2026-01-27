package de.jakob.netcore.api.user;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserManager {

    boolean isUserCached(UUID uuid);

    boolean isUserCached(String name);

    public User getCachedUser(UUID uuid);

    public User getCachedUser(String name);

    public Set<User> getCachedUsers();

    CompletableFuture<UUID> lookupUUID(String username);

    CompletableFuture<String> lookupUsername(UUID uuid);

    CompletableFuture<User> loadUser(UUID uuid);

    CompletableFuture<User> loadUser(String name);

    CompletableFuture<User> handleProxyLogin(UUID uuid, String name, String ip);

    CompletableFuture<User> handleServerLogin(UUID uuid);

    void handleProxyQuit(UUID uuid);

    void handleServerQuit(UUID uuid);

}
