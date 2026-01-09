package de.jakob.netcore.api.redis;

public record RedisSettings(
        String host,
        int port,
        String password,
        int database,
        int timeout,
        int poolSize) {


    public RedisSettings(String host, int port, String password, int timeout, int poolSize) {
        this(host, port, password, 0, timeout, poolSize);
    }

}
