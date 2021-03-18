package cn.sliew.rtomde.cache.redis;

import cn.sliew.rtomde.cache.DummyReadWriteLock;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import org.apache.ibatis.cache.Cache;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class LettuceCache implements Cache {

    private final ReadWriteLock readWriteLock = new DummyReadWriteLock();

    private String id;

    private static RedisClient client;

    private static StatefulRedisConnection connection;

    private Integer timeout;

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
        connection = client.connect(ByteArrayCodec.INSTANCE);
    }

    private Object execute(LettuceConnectionCallback callback) {
        return callback.doWithRedis(connection);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getSize() {
        return (Integer) execute(new LettuceConnectionCallback() {
            @Override
            public Object doWithRedis(StatefulRedisConnection connection) {
                RedisCommands commands = connection.sync();
                Map<byte[], byte[]> result = commands.hgetall(id.getBytes());
                return result.size();
            }
        });
    }

    @Override
    public void putObject(final Object key, final Object value) {
        execute(new LettuceConnectionCallback() {
            @Override
            public Object doWithRedis(StatefulRedisConnection connection) {
                RedisCommands commands = connection.sync();
                final byte[] idBytes = id.getBytes();
                commands.hset(idBytes, key.toString().getBytes(), value.toString().getBytes());

                if (timeout != null && commands.ttl(idBytes) == -1) {
                    commands.expire(idBytes, timeout);
                }
                return null;
            }
        });
    }

    @Override
    public Object getObject(final Object key) {
        return execute(new LettuceConnectionCallback() {
            @Override
            public Object doWithRedis(StatefulRedisConnection connection) {
                RedisCommands commands = connection.sync();
                return commands.hget(id.getBytes(), key.toString().getBytes());
            }
        });
    }

    @Override
    public Object removeObject(final Object key) {
        return execute(new LettuceConnectionCallback() {
            @Override
            public Object doWithRedis(StatefulRedisConnection connection) {
                RedisCommands commands = connection.sync();
                return commands.hdel(id, key.toString());
            }
        });
    }

    @Override
    public void clear() {
        execute(new LettuceConnectionCallback() {
            @Override
            public Object doWithRedis(StatefulRedisConnection connection) {
                RedisCommands commands = connection.sync();
                commands.del(id);
                return null;
            }
        });

    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        return "Redis {" + id + "}";
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
