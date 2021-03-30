package cn.sliew.rtomde.platform.mybatis.session.defaults;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.exceptions.ExceptionFactory;
import cn.sliew.rtomde.platform.mybatis.exceptions.TooManyResultsException;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.executor.Executor;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.reflection.ParamNameResolver;
import cn.sliew.rtomde.platform.mybatis.session.ResultHandler;
import cn.sliew.rtomde.platform.mybatis.session.RowBounds;
import cn.sliew.rtomde.platform.mybatis.session.SqlSession;

import java.util.List;

/**
 * The default implementation for {@link SqlSession}.
 * Note that this class is not Thread-Safe.
 */
public class DefaultSqlSession implements SqlSession {

    private final MybatisApplicationOptions application;
    private final Executor executor;

    public DefaultSqlSession(MybatisApplicationOptions application, Executor executor) {
        this.application = application;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        // Popular vote was to return null on 0 results and throw exception on too many.
        List<T> list = this.selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return this.selectList(statement, null);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.selectList(statement, parameter, RowBounds.DEFAULT);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        try {
            MappedStatement ms = application.getMappedStatement(statement);
            return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        select(statement, parameter, RowBounds.DEFAULT, handler);
    }

    @Override
    public void select(String statement, ResultHandler handler) {
        select(statement, null, RowBounds.DEFAULT, handler);
    }

    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        try {
            MappedStatement ms = application.getMappedStatement(statement);
            executor.query(ms, wrapCollection(parameter), rowBounds, handler);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void close() {
        try {
            executor.close();
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public MybatisApplicationOptions getApplication() {
        return application;
    }

    private Object wrapCollection(final Object object) {
        return ParamNameResolver.wrapToMapIfCollection(object, null);
    }

}
