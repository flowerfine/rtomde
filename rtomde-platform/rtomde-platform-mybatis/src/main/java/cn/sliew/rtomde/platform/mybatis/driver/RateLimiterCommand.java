package cn.sliew.rtomde.platform.mybatis.driver;

import cn.sliew.milky.common.chain.AbstractPipelineProcess;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

import java.util.concurrent.CompletableFuture;

public class RateLimiterCommand extends AbstractMapperCommand {

    public static final String RATE_LIMITER_COMMAND = "RateLimiterCommand";

    /**
     * todo 获取 {@code RateLimiter} 的时候加上 {@code RateLimiterConfig} 以防止初始化 {@code RateLimiter}
     */
    @Override
    public void onEvent(AbstractPipelineProcess<String, Object> process, MapperContext<String, Object> context, CompletableFuture<?> future) {
        RateLimiterRegistry rateLimiterRegistry = context.rateLimiterRegistry();
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(context.mappedStatement().getId());
        rateLimiter.executeRunnable(() -> process.fireEvent(context, future));
    }
}
