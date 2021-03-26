package cn.sliew.rtomde.platform.mybatis.driver;

import cn.sliew.milky.common.chain.AbstractPipelineProcess;

import java.util.concurrent.CompletableFuture;

public class MapperCommand extends AbstractMapperCommand {

    public static final String MAPPER_COMMAND = "MapperCommand";

    @Override
    public void onEvent(AbstractPipelineProcess<String, Object> process, MapperContext<String, Object> context, CompletableFuture<?> future) {
        //执行业务逻辑

    }

}
