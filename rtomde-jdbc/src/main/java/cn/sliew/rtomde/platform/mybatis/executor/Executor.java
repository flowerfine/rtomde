package cn.sliew.rtomde.platform.mybatis.executor;

import cn.sliew.rtomde.platform.mybatis.cache.CacheKey;
import cn.sliew.rtomde.platform.mybatis.mapping.BoundSql;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.reflection.MetaObject;
import cn.sliew.rtomde.platform.mybatis.session.ResultHandler;
import cn.sliew.rtomde.platform.mybatis.session.RowBounds;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    DataSource getDataSource(String dataSourceId);

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

    boolean isCached(MappedStatement ms, CacheKey key);

    void clearLocalCache();

    void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

    void close();

    boolean isClosed();

    void setExecutorWrapper(Executor executor);

}
