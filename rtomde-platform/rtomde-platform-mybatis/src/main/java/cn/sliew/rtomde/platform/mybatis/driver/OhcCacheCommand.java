package cn.sliew.rtomde.platform.mybatis.driver;

import cn.sliew.milky.cache.Cache;
import cn.sliew.milky.cache.ohc.OhcCacheFactory;
import cn.sliew.milky.common.chain.AbstractPipelineProcess;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class OhcCacheCommand extends AbstractMapperCommand {

    public static final String OHC_CACHE_COMMAND = "OhcCacheCommand";

    @Override
    public void onEvent(AbstractPipelineProcess<String, Object> process, MapperContext<String, Object> context, CompletableFuture<?> future) {
        OhcCacheFactory ohcCacheFactory = context.ohcCacheFactory();
        //todo OhcCacheOptions;
        Cache<Object, Object> cache = ohcCacheFactory.getCache(null);
        cache.computeIfAbsent(null, o -> {
            process.fireEvent(context, future);
            return future.get();
        }, Duration.ofSeconds(30L));
    }
}
