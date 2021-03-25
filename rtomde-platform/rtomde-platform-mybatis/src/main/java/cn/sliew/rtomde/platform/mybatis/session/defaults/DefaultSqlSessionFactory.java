package cn.sliew.rtomde.platform.mybatis.session.defaults;

import cn.sliew.rtomde.platform.mybatis.exceptions.ExceptionFactory;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.executor.Executor;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;
import cn.sliew.rtomde.platform.mybatis.session.SqlSession;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return openSessionFromDataSource();
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private SqlSession openSessionFromDataSource() {
        try {
            Executor executor = configuration.newExecutor();
            return new DefaultSqlSession(configuration, executor);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }
}
