package cn.sliew.rtomde.datasource.jdbc;

import cn.sliew.rtomde.datasource.DataSourceException;
import cn.sliew.rtomde.datasource.record.RecordSet;
import cn.sliew.rtomde.datasource.statement.QueryStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class JdbcQueryStatement implements QueryStatement {

    private PreparedStatement statement;

    @Override
    public RecordSet executeQuery() {
        return null;
    }

    /**
     * todo 根据 parameter的类型选择不同的TypeHandler
     */
    @Override
    public void setParamter(int paramterIndex, Object paramter) throws DataSourceException {
        try {
            if (paramter == null) {
                statement.setNull(paramterIndex, Types.JAVA_OBJECT);
            } else {
                statement.setObject(paramterIndex, paramter);
            }
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public int getQueryTimeout() {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) {

    }

    @Override
    public void cancel() {

    }
}
