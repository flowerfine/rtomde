package org.apache.ibatis.executor;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReuseExecutor extends BaseExecutor {

    private final Map<String, Statement> statementMap = new HashMap<>();

    public ReuseExecutor(Configuration configuration) {
        super(configuration);
    }

    @Override
    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.query(stmt, resultHandler);
    }

    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
        String sql = handler.getBoundSql().getSql();
        Statement stmt;
        if (hasStatementFor(sql)) {
            stmt = getStatement(sql);
        } else {
            Connection connection = getConnection(handler.getDataSourceId(), statementLog);
            stmt = handler.prepare(connection);
            putStatement(sql, stmt);
        }
        handler.parameterize(stmt);
        return stmt;
    }

    private boolean hasStatementFor(String sql) {
        try {
            Statement statement = statementMap.get(sql);
            return statement != null && !statement.getConnection().isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private Statement getStatement(String s) {
        return statementMap.get(s);
    }

    private void putStatement(String sql, Statement stmt) {
        statementMap.put(sql, stmt);
    }

}
