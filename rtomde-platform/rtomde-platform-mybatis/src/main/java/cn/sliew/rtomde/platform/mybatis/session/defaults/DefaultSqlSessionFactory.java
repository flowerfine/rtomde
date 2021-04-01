package cn.sliew.rtomde.platform.mybatis.session.defaults;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.exceptions.ExceptionFactory;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.executor.Executor;
import cn.sliew.rtomde.platform.mybatis.session.SqlSession;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final MybatisPlatformOptions platform;

    public DefaultSqlSessionFactory(MybatisPlatformOptions platform) {
        this.platform = platform;
    }

    @Override
    public SqlSession openSession(String application) {
        return openSessionFromDataSource(application);
    }

    @Override
    public MybatisPlatformOptions getPlatform() {
        return platform;
    }

    private SqlSession openSessionFromDataSource(String application) {
        try {
            MybatisApplicationOptions applicationOptions = platform.getApplicationOptions(application);
            Executor executor = applicationOptions.newExecutor();
            return new DefaultSqlSession(applicationOptions, executor);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }
}
