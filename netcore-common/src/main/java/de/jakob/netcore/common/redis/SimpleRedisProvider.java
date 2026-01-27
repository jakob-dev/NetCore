package de.jakob.netcore.common.redis;

import de.jakob.netcore.api.redis.RedisMessageListener;
import de.jakob.netcore.api.redis.RedisProvider;
import de.jakob.netcore.api.redis.RedisSettings;
import redis.clients.jedis.*;

public class SimpleRedisProvider implements RedisProvider {

    private final RedisSettings settings;
    private RedisClient redisClient;

    public SimpleRedisProvider(RedisSettings settings) {
        this.settings = settings;
    }

    @Override
    public void connect() {

        JedisClientConfig config = DefaultJedisClientConfig.builder()
                .password(settings.password())
                .timeoutMillis(settings.timeout())
                .build();

        ConnectionPoolConfig connectionPoolConfig = new ConnectionPoolConfig();
        connectionPoolConfig.setMaxTotal(settings.poolSize());

        this.redisClient = RedisClient.builder()
                .hostAndPort(settings.host(), settings.port())
                .clientConfig(config)
                .poolConfig(connectionPoolConfig).build();

        redisClient.ping();
    }

    @Override
    public void disconnect() {
        if (redisClient.getPool() != null && !redisClient.getPool().isClosed()) {
            redisClient.close();
        }
    }

    @Override
    public boolean isConnected() {
        if (redisClient.getPool() == null || redisClient.getPool().isClosed()) return false;
        try (Connection connection = redisClient.getPool().getResource()) {
            return connection.ping();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void publish(String channel, String message) {
        if (redisClient != null)
            redisClient.publish(channel, message);
    }

    @Override
    public void subscribe(RedisMessageListener listener, String... channels) {

        new Thread(() -> {
            try {
                JedisPubSub jedisPubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        listener.onMessage(channel, message);
                    }
                };
                redisClient.subscribe(jedisPubSub, channels);

            } catch (Exception e) {
            }
        }, "NetCore-Redis-Subscriber").start();

    }

    @Override
    public void set(String key, String value) {
        redisClient.set(key, value);
    }

    @Override
    public void set(String key, String value, long seconds) {
        redisClient.setex(key, seconds, value);
    }

    @Override
    public String get(String key) {
        return redisClient.get(key);
    }


    public RedisClient getRedisClient() {
        return redisClient;
    }


}
