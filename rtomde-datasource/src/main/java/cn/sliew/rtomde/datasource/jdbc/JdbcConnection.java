package cn.sliew.rtomde.datasource.jdbc;

import cn.sliew.rtomde.datasource.Connection;
import cn.sliew.rtomde.datasource.DataSourceException;
import cn.sliew.rtomde.datasource.statement.QueryStatement;
import cn.sliew.rtomde.datasource.type.Type;

import java.util.List;

public class JdbcConnection implements Connection {

    @Override
    public List<Type> getTypes() {
        return null;
    }

    @Override
    public QueryStatement createStatement() throws DataSourceException {
        return null;
    }
}
