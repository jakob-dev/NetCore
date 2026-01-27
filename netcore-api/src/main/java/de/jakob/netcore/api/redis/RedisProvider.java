package de.jakob.netcore.api.redis;

public interface RedisProvider {

    void connect();

    void disconnect();

    boolean isConnected();

    void publish(String channel, String message);

    void subscribe(RedisMessageListener listener, String... channels);

    void set(String key, String value);

    void set(String key, String value, long seconds);

    String get(String key);

}
