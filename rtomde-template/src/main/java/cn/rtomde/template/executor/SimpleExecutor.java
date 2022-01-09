/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package cn.rtomde.template.executor;

import cn.sliew.mybatis.cursor.Cursor;
import cn.sliew.mybatis.executor.resultset.ResultSetHandler;
import cn.sliew.mybatis.executor.statement.StatementContext;
import cn.sliew.mybatis.executor.statement.StatementHandler;
import cn.sliew.mybatis.mapping.BoundSql;
import cn.sliew.mybatis.mapping.MappedStatement;
import cn.sliew.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SimpleExecutor extends BaseExecutor {

    @Override
    protected int doUpdate(MappedStatement ms, BoundSql boundSql) throws SQLException {
        Statement stmt = null;
        try {
            StatementHandler handler = newStatementHandler(ms);
            StatementContext statementContext = new StatementContext(ms, boundSql);
            stmt = prepareStatement(handler, statementContext);
            return handler.update(stmt, statementContext);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement ms, BoundSql boundSql, RowBounds rowBounds) throws SQLException {
        Statement stmt = null;
        try {
            StatementHandler statementHandler = newStatementHandler(ms);
            StatementContext statementContext = new StatementContext(ms, boundSql);
            stmt = prepareStatement(statementHandler, statementContext);
            statementHandler.query(stmt, statementContext);
            ResultSetHandler resultSetHandler = newResultSetHandler(ms);
            return resultSetHandler.handleResultSets(stmt, rowBounds);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    protected <E> Cursor<E> doQueryCursor(MappedStatement ms, BoundSql boundSql, RowBounds rowBounds) throws SQLException {
        StatementHandler statementHandler = newStatementHandler(ms);
        StatementContext statementContext = new StatementContext(ms, boundSql);
        Statement stmt = prepareStatement(statementHandler, statementContext);
        statementHandler.queryCursor(stmt, statementContext);
        stmt.closeOnCompletion();
        ResultSetHandler resultSetHandler = newResultSetHandler(ms);
        return resultSetHandler.handleCursorResultSets(stmt, rowBounds);
    }

    private Statement prepareStatement(StatementHandler handler, StatementContext statementContext) throws SQLException {
        Connection connection = getConnection(statementContext.getStatementLog());
        Statement stmt = handler.prepare(connection, statementContext);
        handler.parameterize(stmt, statementContext);
        return stmt;
    }

}
