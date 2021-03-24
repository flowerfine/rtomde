package cn.sliew.rtomde.platform.mybatis.builder;

import cn.sliew.rtomde.platform.mybatis.cache.Cache;

public class CacheRefResolver {
    private final MapperBuilderAssistant assistant;
    private final String refid;

    public CacheRefResolver(MapperBuilderAssistant assistant, String refid) {
        this.assistant = assistant;
        this.refid = refid;
    }

    public Cache resolveCacheRef() {
        return assistant.useCacheRef(refid);
    }
}