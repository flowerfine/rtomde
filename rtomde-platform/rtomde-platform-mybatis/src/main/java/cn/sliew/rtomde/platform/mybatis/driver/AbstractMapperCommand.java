package cn.sliew.rtomde.platform.mybatis.driver;

import cn.sliew.milky.common.chain.AbstractPipelineProcess;
import cn.sliew.milky.common.chain.Command;
import cn.sliew.milky.common.chain.Context;
import cn.sliew.milky.common.chain.PipelineException;

import java.util.concurrent.CompletableFuture;

/**
 * 异常的处理
 */
public abstract class AbstractMapperCommand implements Command<String, Object> {

    public abstract void onEvent(AbstractPipelineProcess<String, Object> process, MapperContext<String, Object> context, CompletableFuture<?> future);

    @Override
    public void onEvent(AbstractPipelineProcess<String, Object> process, Context<String, Object> context, CompletableFuture<?> completableFuture) {
        onEvent(process, (MapperContext)context, completableFuture);
    }

    @Override
    public void exceptionCaught(AbstractPipelineProcess<String, Object> process, Context<String, Object> context, CompletableFuture<?> completableFuture, Throwable throwable) throws PipelineException {
        exceptionCaught(process, (MapperContext)context, completableFuture, throwable);
    }

    public void exceptionCaught(AbstractPipelineProcess<String, Object> process, MapperContext<String, Object> context, CompletableFuture<?> future, Throwable throwable) throws PipelineException {
        if (throwable instanceof Exception) {
            future.completeExceptionally(throwable);
        } else {
            //todo 发送告警信息。
        }
    }
}
