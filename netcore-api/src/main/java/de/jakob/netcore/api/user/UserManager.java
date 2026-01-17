package de.jakob.netcore.api.user;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserManager {

    public User getCachedUser(UUID uuid);

    public User getCachedUser(String name);

    public Set<User> getCachedUsers();

    CompletableFuture<UUID> lookupUniqueId(String username);

    CompletableFuture<String> lookupUsername(UUID uuid);

    CompletableFuture<User> loadUser(UUID uuid);

    CompletableFuture<User> loadUser(String name);

    boolean isCached(UUID uuid);

    boolean isCached(String name);

}
