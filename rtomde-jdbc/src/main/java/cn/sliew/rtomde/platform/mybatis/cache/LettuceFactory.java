package cn.sliew.rtomde.platform.mybatis.cache;

import cn.sliew.rtomde.common.utils.StringUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;

import java.time.Duration;
import java.util.Properties;

public class LettuceFactory {

    private final Properties props;

    private RedisClient client;
    private RedisClusterClient clusterClient;

    public LettuceFactory(Properties props) {
        this.props = props;
        String redisURI = props.getProperty("redisURI");
        String clusterRedisURI = props.getProperty("clusterRedisURI");
        if (StringUtils.isNotEmpty(redisURI) && StringUtils.isNotEmpty(clusterRedisURI)) {
            throw new IllegalStateException("redisURL and clusterRedisURI can't be coexisted");
        }
        if (StringUtils.isNotEmpty(redisURI)) {
            this.client = RedisClient.create(redisURI);
        }
        if (StringUtils.isNotEmpty(clusterRedisURI)) {
            ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                    .enablePeriodicRefresh(Duration.ofMinutes(10L))
                    .enableAllAdaptiveRefreshTriggers()
                    .build();
            this.clusterClient = RedisClusterClient.create(clusterRedisURI);
            this.clusterClient.setOptions(ClusterClientOptions.builder()
                    .topologyRefreshOptions(topologyRefreshOptions)
                    .build());
        }

    }

    public RedisClient getStandaloneInstance() {
        return client;
    }

    public RedisClusterClient getClusterInstance() {
        return clusterClient;
    }

}
