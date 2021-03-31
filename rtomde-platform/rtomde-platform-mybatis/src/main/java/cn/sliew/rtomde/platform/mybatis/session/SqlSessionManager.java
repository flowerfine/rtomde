package cn.sliew.rtomde.platform.mybatis.session;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.reflection.ExceptionUtil;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Properties;

/**
 * 这里的api针对的都是老得mybatis的 config.xml 和 mapper.xml，在 config.xml 中会
 * 指出 mapper.xml 的地址
 * 换到了新的平台中，配置文件分为了三级：metadata.xml，config.xml和mapper.xml，三者
 * 不会同时创建，后面的api需要重新设计。
 */
public class SqlSessionManager implements SqlSessionFactory, SqlSession {

    private final SqlSessionFactory sqlSessionFactory;
    private final SqlSession sqlSessionProxy;

    private final ThreadLocal<SqlSession> localSqlSession = new ThreadLocal<>();

    private SqlSessionManager(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.sqlSessionProxy = (SqlSession) Proxy.newProxyInstance(
                SqlSessionFactory.class.getClassLoader(),
                new Class[]{SqlSession.class},
                new SqlSessionInterceptor());
    }

    public static SqlSessionManager newInstance(Reader reader) {
        return new SqlSessionManager(new SqlSessionFactoryBuilder().build(reader, null, null));
    }

    public static SqlSessionManager newInstance(Reader reader, String environment) {
        return new SqlSessionManager(new SqlSessionFactoryBuilder().build(reader, environment, null));
    }

    public static SqlSessionManager newInstance(Reader reader, Properties properties) {
        return new SqlSessionManager(new SqlSessionFactoryBuilder().build(reader, null, properties));
    }

    public static SqlSessionManager newInstance(InputStream inputStream) {
        return new SqlSessionManager(new SqlSessionFactoryBuilder().build(inputStream, null, null));
    }

    public static SqlSessionManager newInstance(InputStream inputStream, String environment) {
        return new SqlSessionManager(new SqlSessionFactoryBuilder().build(inputStream, environment, null));
    }

    public static SqlSessionManager newInstance(InputStream inputStream, Properties properties) {
        return new SqlSessionManager(new SqlSessionFactoryBuilder().build(inputStream, null, properties));
    }

    public static SqlSessionManager newInstance(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionManager(sqlSessionFactory);
    }

    public void startManagedSession() {
        this.localSqlSession.set(openSession());
    }

    public boolean isManagedSessionStarted() {
        return this.localSqlSession.get() != null;
    }

    @Override
    public SqlSession openSession() {
        return sqlSessionFactory.openSession();
    }

    @Override
    public MybatisApplicationOptions getApplication() {
        return sqlSessionFactory.getApplication();
    }

    @Override
    public <T> T selectOne(String statement) {
        return sqlSessionProxy.selectOne(statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return sqlSessionProxy.selectOne(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return sqlSessionProxy.selectList(statement);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return sqlSessionProxy.selectList(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return sqlSessionProxy.selectList(statement, parameter, rowBounds);
    }

    @Override
    public void select(String statement, ResultHandler handler) {
        sqlSessionProxy.select(statement, handler);
    }

    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        sqlSessionProxy.select(statement, parameter, handler);
    }

    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        sqlSessionProxy.select(statement, parameter, rowBounds, handler);
    }

    @Override
    public void close() {
        final SqlSession sqlSession = localSqlSession.get();
        if (sqlSession == null) {
            throw new SqlSessionException("Error:  Cannot close.  No managed session is started.");
        }
        try {
            sqlSession.close();
        } finally {
            localSqlSession.set(null);
        }
    }

    private class SqlSessionInterceptor implements InvocationHandler {
        public SqlSessionInterceptor() {
            // Prevent Synthetic Access
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final SqlSession sqlSession = SqlSessionManager.this.localSqlSession.get();
            if (sqlSession != null) {
                try {
                    return method.invoke(sqlSession, args);
                } catch (Throwable t) {
                    throw ExceptionUtil.unwrapThrowable(t);
                }
            } else {
                try (SqlSession autoSqlSession = openSession()) {
                    try {
                        return method.invoke(autoSqlSession, args);
                    } catch (Throwable t) {
                        throw ExceptionUtil.unwrapThrowable(t);
                    }
                }
            }
        }
    }

}
