package de.jakob.netcore.api.redis;

public record RedisSettings(
        String host,
        int port,
        String password,
        int database,
        int timeout,
        int maxPoolSize,
        int maxPoolIdle,
        int minPoolIdle) {


    public RedisSettings(String host, int port, String password, int timeout, int maxPoolSize, int maxPoolIdle, int minPoolIdle) {
        this(host, port, password, 0, timeout, maxPoolSize, maxPoolIdle, minPoolIdle);
    }

}
