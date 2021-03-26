package cn.sliew.rtomde.platform.mybatis.driver;

import cn.sliew.milky.common.chain.AbstractPipelineProcess;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

import java.util.concurrent.CompletableFuture;

public class RetryCommand extends AbstractMapperCommand {

    public static final String RETRY_COMMAND = "RetryCommand";

    @Override
    public void onEvent(AbstractPipelineProcess<String, Object> process, MapperContext<String, Object> context, CompletableFuture<?> future) {
        RetryRegistry retryRegistry = context.retryRegistry();
        Retry retry = retryRegistry.retry(context.mappedStatement().getId());
        retry.executeRunnable(() -> process.fireEvent(context, future));
    }
}
