package de.jakob.netcore.api.user;

import java.util.UUID;

public interface User {

    UUID getUUID();

    String getName();

    String getIPAddress();

    UserSettings getSettings();

    long getFirstJoinTimestamp();

    long getLastJoin();

    long getPlayTime();

    long getSessionTime();


}
