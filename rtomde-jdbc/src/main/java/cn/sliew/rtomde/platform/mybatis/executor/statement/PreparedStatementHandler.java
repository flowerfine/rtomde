package cn.sliew.rtomde.platform.mybatis.executor.statement;

import cn.sliew.rtomde.platform.mybatis.executor.Executor;
import cn.sliew.rtomde.platform.mybatis.mapping.BoundSql;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.session.ResultHandler;
import cn.sliew.rtomde.platform.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class PreparedStatementHandler extends BaseStatementHandler {

    public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return resultSetHandler.handleResultSets(ps);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(boundSql.getSql());
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        parameterHandler.setParameters((PreparedStatement) statement);
    }

}
