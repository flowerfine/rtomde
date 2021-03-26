package cn.sliew.rtomde.platform.mybatis.driver;

import cn.sliew.milky.common.chain.AbstractPipelineProcess;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;

import java.util.concurrent.CompletableFuture;

public class BulkheadCommand extends AbstractMapperCommand {

    public static final String BULK_HEAD_COMMAND = "BulkheadCommand";

    @Override
    public void onEvent(AbstractPipelineProcess<String, Object> process, MapperContext<String, Object> context, CompletableFuture<?> future) {
        BulkheadRegistry bulkheadRegistry = context.bulkheadRegistry();
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(context.mappedStatement().getId());
        bulkhead.executeRunnable(() -> process.fireEvent(context, future));
    }
}
