package cn.sliew.rtomde.cache.redis;

import cn.sliew.milky.serialize.protostuff.ProtostuffDataInputView;
import cn.sliew.milky.serialize.protostuff.ProtostuffDataOutputView;
import io.lettuce.core.codec.RedisCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ProtostuffCodec<K, V> implements RedisCodec<K, V> {

    private Logger log = LoggerFactory.getLogger(ProtostuffCodec.class);

    public static final ProtostuffCodec INSTANCE = new ProtostuffCodec();

    @Override
    public K decodeKey(ByteBuffer byteBuffer) {
        return (K) decode(byteBuffer);
    }

    @Override
    public V decodeValue(ByteBuffer byteBuffer) {
        return (V) decode(byteBuffer);
    }

    @Override
    public ByteBuffer encodeKey(K k) {
        return encode(k);
    }

    @Override
    public ByteBuffer encodeValue(V v) {
        return encode(v);
    }

    private ByteBuffer encode(Object obj) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ProtostuffDataOutputView outputView = new ProtostuffDataOutputView(outputStream);
            outputView.writeObject(obj);
            outputView.flushBuffer();
            byte[] bytes = outputStream.toByteArray();
            ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length + 4);
            byteBuffer.putInt(bytes.length);
            byteBuffer.put(bytes);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private Object decode(ByteBuffer byteBuffer) {
        try {
            byte[] bytes = new byte[byteBuffer.getInt()];
            byteBuffer.get(bytes);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            ProtostuffDataInputView inputView = new ProtostuffDataInputView(inputStream);
            return inputView.readObject();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
