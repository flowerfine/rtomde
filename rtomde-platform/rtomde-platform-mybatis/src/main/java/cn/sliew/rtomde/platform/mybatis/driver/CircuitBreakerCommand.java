package cn.sliew.rtomde.platform.mybatis.driver;

import cn.sliew.milky.common.chain.AbstractPipelineProcess;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.util.concurrent.CompletableFuture;

public class CircuitBreakerCommand extends AbstractMapperCommand {

    public static final String CIRCUIT_BREAKER_COMMAND = "CircuitBreakerCommand";

    @Override
    public void onEvent(AbstractPipelineProcess<String, Object> process, MapperContext<String, Object> context, CompletableFuture<?> future) {
        CircuitBreakerRegistry circuitBreakerRegistry = context.circuitBreakerRegistry();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(context.mappedStatement().getId());
        circuitBreaker.executeRunnable(() -> process.fireEvent(context, future));
    }
}
