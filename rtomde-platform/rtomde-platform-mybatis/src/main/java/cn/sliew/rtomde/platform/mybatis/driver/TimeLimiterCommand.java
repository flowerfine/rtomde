package cn.sliew.rtomde.platform.mybatis.driver;

import cn.sliew.milky.common.chain.AbstractPipelineProcess;
import cn.sliew.milky.common.chain.PipelineException;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import java.util.concurrent.CompletableFuture;

public class TimeLimiterCommand extends AbstractMapperCommand {

    public static final String TIME_LIMITER_COMMAND = "TimeLimiterCommand";

    @Override
    public void onEvent(AbstractPipelineProcess<String, Object> process, MapperContext<String, Object> context, CompletableFuture<?> future) {
        TimeLimiterRegistry timeLimiterRegistry = context.timeLimiterRegistry();
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(context.mappedStatement().getId());
        try {
            timeLimiter.executeFutureSupplier(() -> {
                process.fireEvent(context, future);
                return future;
            });
        } catch (Exception e) {
            throw new PipelineException(e.getMessage(), e);
        }
    }
}
