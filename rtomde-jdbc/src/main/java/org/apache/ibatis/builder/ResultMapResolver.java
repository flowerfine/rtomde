package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;

import java.util.List;

public class ResultMapResolver {
    private final MapperBuilderAssistant assistant;
    private final String id;
    private final Class<?> type;
    private final String extend;
    private final List<ResultMapping> resultMappings;
    private final Boolean autoMapping;

    public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, String extend, List<ResultMapping> resultMappings, Boolean autoMapping) {
        this.assistant = assistant;
        this.id = id;
        this.type = type;
        this.extend = extend;
        this.resultMappings = resultMappings;
        this.autoMapping = autoMapping;
    }

    public ResultMap resolve() {
        return assistant.addResultMap(this.id, this.type, this.extend, this.resultMappings, this.autoMapping);
    }

}
