package cn.sliew.rtomde.platform.mybatis.session.defaults;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.exceptions.ExceptionFactory;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.executor.Executor;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;
import cn.sliew.rtomde.platform.mybatis.session.SqlSession;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final MybatisApplicationOptions application;

    public DefaultSqlSessionFactory(MybatisApplicationOptions application) {
        this.application = application;
    }

    @Override
    public SqlSession openSession() {
        return openSessionFromDataSource();
    }

    @Override
    public MybatisApplicationOptions getApplication() {
        return application;
    }

    private SqlSession openSessionFromDataSource() {
        try {
            Executor executor = application.newExecutor();
            return new DefaultSqlSession(configuration, executor);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }
}
