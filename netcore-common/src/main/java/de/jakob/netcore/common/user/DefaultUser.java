package de.jakob.netcore.common.user;

import de.jakob.netcore.api.user.User;
import de.jakob.netcore.api.user.UserSettings;

import java.util.UUID;

public class DefaultUser implements User {

    private final UUID uuid;
    private final String name;
    private final String ipAddress;
    private final UserSettings settings;
    private final long firstJoin;
    private final long lastJoin;
    private final long playTime;
    private final boolean online;

    public DefaultUser(UUID uuid, String name, String ipAddress, UserSettings settings, long firstJoin, long lastJoin, long playTime, boolean online) {
        this.uuid = uuid;
        this.name = name;
        this.ipAddress = ipAddress;
        this.settings = settings;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
        this.playTime = playTime;
        this.online = online;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIPAddress() {
        return ipAddress;
    }

    @Override
    public UserSettings getSettings() {
        return settings;
    }

    @Override
    public long getFirstJoinTimestamp() {
        return firstJoin;
    }

    @Override
    public long getLastJoin() {
        return lastJoin;
    }

    @Override
    public long getPlayTime() {
        return online ? playTime + getSessionTime() : playTime;
    }

    @Override
    public long getSessionTime() {
        return System.currentTimeMillis() - lastJoin;
    }

    public boolean isOnline() {
        return online;
    }
}