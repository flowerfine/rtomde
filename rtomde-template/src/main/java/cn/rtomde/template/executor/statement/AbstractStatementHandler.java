package cn.rtomde.template.executor.statement;

import cn.rtomde.template.executor.ExecutorException;
import cn.rtomde.template.executor.parameter.ParameterHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractStatementHandler<T extends Statement> implements StatementHandler<T> {

    protected ParameterHandler parameterHandler;

    public AbstractStatementHandler(ParameterHandler parameterHandler) {
        this.parameterHandler = parameterHandler;
    }

    @Override
    public T prepare(Connection connection, StatementContext context) throws SQLException {
        T statement = null;
        try {
            statement = instantiateStatement(connection, context);
            setStatementTimeout(statement, context);
            setFetchSize(statement, context);
            return statement;
        } catch (SQLException e) {
            closeStatement(statement);
            throw e;
        } catch (Exception e) {
            closeStatement(statement);
            throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
        }
    }

    protected abstract T instantiateStatement(Connection connection, StatementContext context) throws SQLException;

    protected void setStatementTimeout(Statement stmt, StatementContext context) throws SQLException {
        Integer queryTimeout = context.getQueryTimeout();
        if (queryTimeout != null) {
            stmt.setQueryTimeout(queryTimeout);
        }
    }

    protected void setFetchSize(Statement stmt, StatementContext context) throws SQLException {
        Integer fetchSize = context.getFetchSize();
        if (fetchSize != null) {
            stmt.setFetchSize(fetchSize);
        }
    }

    protected void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            //ignore
        }
    }
}
