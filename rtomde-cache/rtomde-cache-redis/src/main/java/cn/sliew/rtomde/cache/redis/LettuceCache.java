package cn.sliew.rtomde.cache.redis;

import cn.sliew.rtomde.cache.DummyReadWriteLock;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import cn.sliew.rtomde.platform.mybatis.cache.Cache;

import java.time.Duration;
import java.util.concurrent.locks.ReadWriteLock;

public class LettuceCache implements Cache {

    private final ReadWriteLock readWriteLock = new DummyReadWriteLock();

    private String id;

    private static RedisClient client;

    private static StatefulRedisConnection connection;

    /**
     * cache expire time with millseconds.
     */
    private Integer expire;

    public LettuceCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
        RedisURI redisURI = RedisURI.builder()
                .withHost("localhost")
                .withPort(6379)
                .withPassword("123")
                .withDatabase(0)
                .withTimeout(Duration.ofSeconds(1L))
                .build();
        client = RedisClient.create(redisURI);
        connection = client.connect(ProtostuffCodec.INSTANCE);
        connection.setTimeout(Duration.ofSeconds(1L));
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getSize() {
        RedisCommands commands = connection.sync();
        return commands.hlen(id).intValue();
    }

    @Override
    public void putObject(final Object key, final Object value) {
        RedisCommands commands = connection.sync();
        commands.hset(id, key, value);
        if (expire != null && commands.ttl(id) == -1) {
            commands.expire(id, expire);
        }
    }

    @Override
    public Object getObject(final Object key) {
        RedisCommands commands = connection.sync();
        return commands.hget(id, key);
    }

    @Override
    public Object removeObject(final Object key) {
        RedisCommands commands = connection.sync();
        return commands.hdel(id, key);
    }

    @Override
    public void clear() {
        RedisCommands commands = connection.sync();
        commands.del(id);
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        return "Redis {" + id + "}";
    }

    public void setExpire(Integer expire) {
        this.expire = expire;
    }
}
