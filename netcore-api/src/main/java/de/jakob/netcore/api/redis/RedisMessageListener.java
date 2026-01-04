package de.jakob.netcore.api.redis;

public interface RedisMessageListener {

    void onMessage(String channel, String message);

}
