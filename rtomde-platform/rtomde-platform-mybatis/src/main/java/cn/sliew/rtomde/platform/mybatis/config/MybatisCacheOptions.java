package cn.sliew.rtomde.platform.mybatis.config;

import cn.sliew.rtomde.config.CacheOptions;

import java.time.Duration;
import java.util.Properties;

public class MybatisCacheOptions extends CacheOptions {

    private static final long serialVersionUID = 3940228062170145601L;

    /**
     * lettuce options
     */
    protected LettuceOptions lettuce;

    /**
     * cache entry expire time
     */
    protected Duration expire;

    /**
     * cache entry size
     */
    protected Long size;

    /**
     * cache additional properties
     */
    protected Properties properties;

    public LettuceOptions getLettuce() {
        return lettuce;
    }

    public void setLettuce(LettuceOptions lettuce) {
        this.lettuce = lettuce;
    }

    public Duration getExpire() {
        return expire;
    }

    public void setExpire(Duration expire) {
        this.expire = expire;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
