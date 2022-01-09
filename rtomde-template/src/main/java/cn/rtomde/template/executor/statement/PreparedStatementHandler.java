package cn.rtomde.template.executor.statement;

import cn.rtomde.template.executor.parameter.ParameterHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PreparedStatementHandler extends AbstractStatementHandler<PreparedStatement> {

    public PreparedStatementHandler(ParameterHandler parameterHandler) {
        super(parameterHandler);
    }

    @Override
    protected PreparedStatement instantiateStatement(Connection connection, StatementContext context) throws SQLException {
        if (context.getResultSetType() == null) {
            return connection.prepareStatement(context.getSql());
        } else {
            return connection.prepareStatement(context.getSql(), context.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
        }
    }

    @Override
    public void parameterize(PreparedStatement statement, StatementContext context) throws SQLException {
        parameterHandler.setParameters(statement, context.getParameterMappings(), context.getEvent());
    }

    @Override
    public void batch(PreparedStatement statement, StatementContext context) throws SQLException {
        statement.addBatch();
    }

    @Override
    public int update(PreparedStatement statement, StatementContext context) throws SQLException {
        statement.execute();
        return statement.getUpdateCount();
    }

    @Override
    public void query(PreparedStatement statement, StatementContext context) throws SQLException {
        statement.execute();
    }

    @Override
    public void queryCursor(PreparedStatement statement, StatementContext context) throws SQLException {
        statement.execute();
    }
}
