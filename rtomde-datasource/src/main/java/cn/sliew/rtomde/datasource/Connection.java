package cn.sliew.rtomde.datasource;

import cn.sliew.rtomde.datasource.statement.QueryStatement;
import cn.sliew.rtomde.datasource.type.Type;

import java.util.List;

public interface Connection {

    List<Type> getTypes();

    QueryStatement createStatement() throws DataSourceException;
}
