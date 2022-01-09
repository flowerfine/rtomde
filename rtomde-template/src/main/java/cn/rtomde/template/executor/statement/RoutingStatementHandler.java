package cn.rtomde.template.executor.statement;

import cn.rtomde.template.executor.ExecutorException;
import cn.rtomde.template.executor.parameter.ParameterHandler;
import cn.rtomde.template.mapping.StatementType;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class RoutingStatementHandler implements StatementHandler {

    private final StatementHandler delegate;

    public RoutingStatementHandler(StatementType statementType, ParameterHandler parameterHandler) {
        switch (statementType) {
            case STATEMENT:
                delegate = new SimpleStatementHandler();
                break;
            case PREPARED:
                delegate = new PreparedStatementHandler(parameterHandler);
                break;
            default:
                throw new ExecutorException("Unknown statement type: " + statementType);
        }

    }

    @Override
    public Statement prepare(Connection connection, StatementContext context) throws SQLException {
        return delegate.prepare(connection, context);
    }

    @Override
    public void parameterize(Statement statement, StatementContext context) throws SQLException {
        delegate.parameterize(statement, context);
    }

    @Override
    public void batch(Statement statement, StatementContext context) throws SQLException {
        delegate.batch(statement, context);
    }

    @Override
    public int update(Statement statement, StatementContext context) throws SQLException {
        return delegate.update(statement, context);
    }

    @Override
    public void query(Statement statement, StatementContext context) throws SQLException {
        delegate.query(statement, context);
    }

    @Override
    public void queryCursor(Statement statement, StatementContext context) throws SQLException {
        delegate.queryCursor(statement, context);
    }
}
