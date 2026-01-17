package de.jakob.netcore.api.user;

public record UserSettings(
        String chatColor, boolean scoreboard, boolean chatPing
) {
}
