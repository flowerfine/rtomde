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

import cn.sliew.milky.log.Logger;
import cn.sliew.mybatis.component.service.DataSourceService;
import cn.sliew.mybatis.component.service.impl.HikaricpDataSourceService;
import cn.sliew.mybatis.cursor.Cursor;
import cn.sliew.mybatis.executor.log.ConnectionLogger;
import cn.sliew.mybatis.executor.parameter.DefaultParameterHandler;
import cn.sliew.mybatis.executor.parameter.ParameterHandler;
import cn.sliew.mybatis.executor.resultset.DefaultResultSetHandler;
import cn.sliew.mybatis.executor.resultset.ResultSetHandler;
import cn.sliew.mybatis.executor.statement.RoutingStatementHandler;
import cn.sliew.mybatis.executor.statement.StatementHandler;
import cn.sliew.mybatis.mapping.BoundSql;
import cn.sliew.mybatis.mapping.MappedStatement;
import cn.sliew.mybatis.session.RowBounds;
import cn.sliew.mybatis.type.TypeHandlerRegistry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseExecutor implements Executor {

    private AtomicBoolean closed;

    protected BaseExecutor() {
        this.closed = new AtomicBoolean();
    }

    @Override
    public DataSourceService getDataSourceService() {
        return new HikaricpDataSourceService();
    }

    @Override
    public int update(ExecuteContext context) throws SQLException {
        MappedStatement ms = context.getMappedStatement();
        ErrorContext.instance().activity("executing an update").object(ms.getId());
        if (closed.get()) {
            throw new ExecutorException("Executor was closed.");
        }
        return doUpdate(ms, context.getBoundSql());
    }

    @Override
    public <E> List<E> query(ExecuteContext context) throws SQLException {
        MappedStatement ms = context.getMappedStatement();
        ErrorContext.instance().activity("executing a query").object(ms.getId());
        if (closed.get()) {
            throw new ExecutorException("Executor was closed.");
        }

        return doQuery(ms, context.getBoundSql(), context.getRowBounds());
    }

    @Override
    public <E> Cursor<E> queryCursor(ExecuteContext context) throws SQLException {
        MappedStatement ms = context.getMappedStatement();
        ErrorContext.instance().activity("executing a query cursor").object(ms.getId());
        if (closed.get()) {
            throw new ExecutorException("Executor was closed.");
        }
        return doQueryCursor(context.getMappedStatement(), context.getBoundSql(), context.getRowBounds());
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            throw new IllegalStateException(this + " is already closed, can't be closed twice!");
        }
    }

    protected abstract int doUpdate(MappedStatement ms, BoundSql boundSql) throws SQLException;

    protected abstract <E> List<E> doQuery(MappedStatement ms, BoundSql boundSql, RowBounds rowBounds) throws SQLException;

    protected abstract <E> Cursor<E> doQueryCursor(MappedStatement ms, BoundSql boundSql, RowBounds rowBounds) throws SQLException;

    protected void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    protected Connection getConnection(Logger statementLog) throws SQLException {
        DataSourceService dataSourceService = getDataSourceService();

        DataSource dataSource = dataSourceService.newInstance();
        Connection connection = dataSource.getConnection();
        if (statementLog.isDebugEnabled()) {
            return ConnectionLogger.newInstance(statementLog, connection);
        } else {
            return connection;
        }
    }

    protected StatementHandler newStatementHandler(MappedStatement ms) {
        ParameterHandler parameterHandler = new DefaultParameterHandler(ms);
        return new RoutingStatementHandler(ms.getStatementType(), parameterHandler);
    }

    protected ResultSetHandler newResultSetHandler(MappedStatement ms) {
        return new DefaultResultSetHandler(new TypeHandlerRegistry(), ms);
    }

}
