package cn.sliew.rtomde.platform.mybatis.executor;

import cn.sliew.rtomde.platform.mybatis.executor.statement.StatementHandler;
import cn.sliew.rtomde.platform.mybatis.logging.Log;
import cn.sliew.rtomde.platform.mybatis.mapping.BoundSql;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;
import cn.sliew.rtomde.platform.mybatis.session.ResultHandler;
import cn.sliew.rtomde.platform.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SimpleExecutor extends BaseExecutor {

    public SimpleExecutor(Configuration configuration) {
        super(configuration);
    }


    @Override
    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
            stmt = prepareStatement(handler, ms.getStatementLog());
            return handler.query(stmt, resultHandler);
        } finally {
            closeStatement(stmt);
        }
    }

    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
        Connection connection = getConnection(handler.getDataSourceId(), statementLog);
        Statement stmt = handler.prepare(connection);
        handler.parameterize(stmt);
        return stmt;
    }

}
