package cn.sliew.rtomde.platform.mybatis.driver;

import cn.sliew.milky.cache.Cache;
import cn.sliew.milky.cache.lettuce.LettuceCacheFactory;
import cn.sliew.milky.common.chain.AbstractPipelineProcess;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class LettuceCacheCommand extends AbstractMapperCommand {

    public static final String LETTUCE_CACHE_COMMAND = "LettuceCacheCommand";

    @Override
    public void onEvent(AbstractPipelineProcess<String, Object> process, MapperContext<String, Object> context, CompletableFuture<?> future) {
        LettuceCacheFactory lettuceCacheFactory = context.lettuceCacheFactory();
        //todo LettuceCacheOptions;
        Cache<Object, Object> cache = lettuceCacheFactory.getCache(null);
        cache.computeIfAbsent(null, o -> {
            process.fireEvent(context, future);
            return future.get();
        }, Duration.ofSeconds(30L));
    }

}
