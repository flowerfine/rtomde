package cn.rtomde.template.executor.statement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SimpleStatementHandler extends AbstractStatementHandler<Statement> {

    public SimpleStatementHandler() {
        super(null);
    }

    @Override
    protected Statement instantiateStatement(Connection connection, StatementContext context) throws SQLException {
        if (context.getResultSetType() == null) {
            return connection.createStatement();
        } else {
            return connection.createStatement(context.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
        }
    }

    @Override
    public void parameterize(Statement statement, StatementContext context) throws SQLException {
        // N/A
    }

    @Override
    public void batch(Statement statement, StatementContext context) throws SQLException {
        statement.addBatch(context.getSql());
    }

    @Override
    public int update(Statement statement, StatementContext context) throws SQLException {
        statement.execute(context.getSql());
        return statement.getUpdateCount();
    }

    @Override
    public void query(Statement statement, StatementContext context) throws SQLException {
        statement.executeQuery(context.getSql());
    }

    @Override
    public void queryCursor(Statement statement, StatementContext context) throws SQLException {
        statement.executeQuery(context.getSql());
    }
}
