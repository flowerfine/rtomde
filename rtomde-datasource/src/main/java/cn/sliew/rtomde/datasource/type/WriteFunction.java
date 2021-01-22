package cn.sliew.rtomde.datasource.type;

import cn.sliew.rtomde.datasource.DataSourceException;
import cn.sliew.rtomde.datasource.statement.QueryStatement;

public interface WriteFunction {

    Class<?> getJavaType();

    default String getBindExpression() {
        return "?";
    }

    default void setNull(QueryStatement statement, int index) throws DataSourceException {
        statement.setParamter(index, null);
    }
}
