package cn.sliew.rtomde.bind;

import cn.sliew.rtomde.datasource.statement.QueryStatement;

public interface ParameterHandler {

    /**
     * json object
     */
    Object getParameterObject();

    void setParameters(QueryStatement statement);
}
