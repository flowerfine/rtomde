package cn.sliew.rtomde.datasource.jdbc;

import cn.sliew.rtomde.datasource.DataSourceException;
import cn.sliew.rtomde.datasource.record.RecordSet;
import cn.sliew.rtomde.datasource.statement.QueryStatement;
import cn.sliew.rtomde.datasource.type.Type;
import cn.sliew.rtomde.datasource.type.TypeHandler;
import cn.sliew.rtomde.datasource.type.TypeManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class JdbcQueryStatement implements QueryStatement {

    private final PreparedStatement statement;
    private final TypeManager typeManager;

    public JdbcQueryStatement(PreparedStatement statement, TypeManager typeManager) {
        this.statement = statement;
        this.typeManager = typeManager;
    }

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
                Type type = typeManager.getType(paramter.getClass());
                TypeHandler typeHandler = typeManager.getTypeHandler(type);
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
