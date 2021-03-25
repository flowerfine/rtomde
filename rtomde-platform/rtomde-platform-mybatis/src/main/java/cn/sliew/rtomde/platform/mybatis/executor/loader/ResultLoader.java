package cn.sliew.rtomde.platform.mybatis.executor.loader;

import cn.sliew.rtomde.platform.mybatis.cache.CacheKey;
import cn.sliew.rtomde.platform.mybatis.executor.Executor;
import cn.sliew.rtomde.platform.mybatis.executor.ResultExtractor;
import cn.sliew.rtomde.platform.mybatis.mapping.BoundSql;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.reflection.factory.ObjectFactory;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;
import cn.sliew.rtomde.platform.mybatis.session.RowBounds;

import java.sql.SQLException;
import java.util.List;

public class ResultLoader {

    protected final Configuration configuration;
    protected final Executor executor;
    protected final MappedStatement mappedStatement;
    protected final Object parameterObject;
    protected final Class<?> targetType;
    protected final ObjectFactory objectFactory;
    protected final CacheKey cacheKey;
    protected final BoundSql boundSql;
    protected final ResultExtractor resultExtractor;
    protected final long creatorThreadId;

    protected boolean loaded;
    protected Object resultObject;

    public ResultLoader(Configuration config, Executor executor, MappedStatement mappedStatement, Object parameterObject, Class<?> targetType, CacheKey cacheKey, BoundSql boundSql) {
        this.configuration = config;
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
        this.targetType = targetType;
        this.objectFactory = configuration.getObjectFactory();
        this.cacheKey = cacheKey;
        this.boundSql = boundSql;
        this.resultExtractor = new ResultExtractor(configuration, objectFactory);
        this.creatorThreadId = Thread.currentThread().getId();
    }

    public Object loadResult() throws SQLException {
        List<Object> list = selectList();
        resultObject = resultExtractor.extractObjectFromList(list, targetType);
        return resultObject;
    }

    private <E> List<E> selectList() throws SQLException {
        Executor localExecutor = executor;
        if (Thread.currentThread().getId() != this.creatorThreadId || localExecutor.isClosed()) {
            localExecutor = newExecutor();
        }
        try {
            return localExecutor.query(mappedStatement, parameterObject, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, cacheKey, boundSql);
        } finally {
            if (localExecutor != executor) {
                localExecutor.close();
            }
        }
    }

    private Executor newExecutor() {
        return configuration.newExecutor();
    }

    public boolean wasNull() {
        return resultObject == null;
    }

}
