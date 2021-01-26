package org.apache.ibatis.builder;

import org.apache.ibatis.cache.Cache;

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