package cn.sliew.rtomde.platform.mybatis.driver;

import cn.sliew.milky.cache.lettuce.LettuceCacheFactory;
import cn.sliew.milky.cache.ohc.OhcCacheFactory;
import cn.sliew.milky.common.chain.ContextMap;
import cn.sliew.milky.common.log.LoggerFactory;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.internal.InMemoryBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.internal.InMemoryCircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.InMemoryRateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.internal.InMemoryRetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.internal.InMemoryTimeLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;

public class MapperContext<K, V> extends ContextMap<K, V> {

    private final MappedStatement ms;

    private final MeterRegistry meterRegistry;

    private final CircuitBreakerRegistry circuitBreakerRegistry = new InMemoryCircuitBreakerRegistry();
    private final TimeLimiterRegistry timeLimiterRegistry = new InMemoryTimeLimiterRegistry();
    private final RateLimiterRegistry rateLimiterRegistry = new InMemoryRateLimiterRegistry();
    private final BulkheadRegistry bulkheadRegistry = new InMemoryBulkheadRegistry();
    private final RetryRegistry retryRegistry = new InMemoryRetryRegistry();
    private final LettuceCacheFactory lettuceCacheFactory = new LettuceCacheFactory();
    private final OhcCacheFactory ohcCacheFactory = new OhcCacheFactory();

    public MapperContext(MappedStatement ms, MeterRegistry meterRegistry) {
        this.ms = ms;
        this.meterRegistry = meterRegistry;
        this.logger = LoggerFactory.getLogger(ms.getId());

        // enable explanation
    }

    //提供日志

    //提供mapper的id

    //提供便利方法以访问限速，并发，熔断，重试，缓存，超时控制等。
    // 第一个为熔断
    // 第二个为超时
    // 第三个为限速 + 并发
    // 第四个为重试
    // 第五个为缓存。


    public MappedStatement mappedStatement() {
        return ms;
    }

    public MeterRegistry meterRegistry() {
        return meterRegistry;
    }

    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return circuitBreakerRegistry;
    }

    public TimeLimiterRegistry timeLimiterRegistry() {
        return timeLimiterRegistry;
    }

    public RateLimiterRegistry rateLimiterRegistry() {
        return rateLimiterRegistry;
    }

    public BulkheadRegistry bulkheadRegistry() {
        return bulkheadRegistry;
    }

    public RetryRegistry retryRegistry() {
        return retryRegistry;
    }

    public LettuceCacheFactory lettuceCacheFactory() {
        return lettuceCacheFactory;
    }

    public OhcCacheFactory ohcCacheFactory() {
        return ohcCacheFactory;
    }
}
