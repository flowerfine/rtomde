package cn.sliew.rtomde.platform.mybatis.executor.loader;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.executor.Executor;
import cn.sliew.rtomde.platform.mybatis.executor.ResultExtractor;
import cn.sliew.rtomde.platform.mybatis.mapping.BoundSql;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.reflection.factory.ObjectFactory;
import cn.sliew.rtomde.platform.mybatis.session.RowBounds;

import java.sql.SQLException;
import java.util.List;

public class ResultLoader {

    protected final MybatisApplicationOptions application;
    protected final Executor executor;
    protected final MappedStatement mappedStatement;
    protected final Object parameterObject;
    protected final Class<?> targetType;
    protected final ObjectFactory objectFactory;
    protected final BoundSql boundSql;
    protected final ResultExtractor resultExtractor;
    protected final long creatorThreadId;

    protected boolean loaded;
    protected Object resultObject;

    public ResultLoader(MybatisApplicationOptions application, Executor executor, MappedStatement mappedStatement, Object parameterObject, Class<?> targetType, BoundSql boundSql) {
        this.application = application;
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
        this.targetType = targetType;
        MybatisPlatformOptions platform = (MybatisPlatformOptions) application.getPlatform();
        this.objectFactory = platform.getObjectFactory();
        this.boundSql = boundSql;
        this.resultExtractor = new ResultExtractor(application, objectFactory);
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
            return localExecutor.query(mappedStatement, parameterObject, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, boundSql);
        } finally {
            if (localExecutor != executor) {
                localExecutor.close();
            }
        }
    }

    private Executor newExecutor() {
        return application.newExecutor();
    }

    public boolean wasNull() {
        return resultObject == null;
    }

}
