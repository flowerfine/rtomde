package cn.sliew.rtomde.platform.mybatis.config;

import cn.sliew.milky.common.util.StringUtils;
import cn.sliew.rtomde.config.AbstractOptions;

public class LettuceOptions extends AbstractOptions {

    private static final long serialVersionUID = 9117158017575881779L;

    private String redisURI;

    private String clusterRedisURI;

    public boolean isCluster() {
        return StringUtils.isNotBlank(clusterRedisURI);
    }

    public String getRedisURI() {
        return redisURI;
    }

    public void setRedisURI(String redisURI) {
        this.redisURI = redisURI;
    }

    public String getClusterRedisURI() {
        return clusterRedisURI;
    }

    public void setClusterRedisURI(String clusterRedisURI) {
        this.clusterRedisURI = clusterRedisURI;
    }
}
