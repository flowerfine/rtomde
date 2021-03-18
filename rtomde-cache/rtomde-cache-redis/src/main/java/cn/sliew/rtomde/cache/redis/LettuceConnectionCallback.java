package cn.sliew.rtomde.cache.redis;

import io.lettuce.core.api.StatefulRedisConnection;

public interface LettuceConnectionCallback {

    Object doWithRedis(StatefulRedisConnection connection);
}
