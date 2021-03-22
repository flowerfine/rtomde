package org.mybatis.spring;

import org.springframework.beans.factory.DisposableBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.lang.reflect.Proxy.newProxyInstance;
import static cn.sliew.rtomde.platform.mybatis.reflection.ExceptionUtil.unwrapThrowable;
import static org.mybatis.spring.SqlSessionUtils.closeSqlSession;
import static org.mybatis.spring.SqlSessionUtils.getSqlSession;
import static org.springframework.util.Assert.notNull;

/**
 * Thread safe, Spring managed, {@code SqlSession} that works with Spring transaction management to ensure that that the
 * actual SqlSession used is the one associated with the current Spring transaction. In addition, it manages the session
 * life-cycle, including closing, committing or rolling back the session as necessary based on the Spring transaction
 * configuration.
 * <p>
 * The template needs a SqlSessionFactory to create SqlSessions, passed as a constructor argument. It also can be
 * constructed indicating the executor type to be used, if not, the default executor type, defined in the session
 * factory will be used.
 * <p>
 * This template converts MyBatis PersistenceExceptions into unchecked DataAccessExceptions, using, by default, a
 * {@code MyBatisExceptionTranslator}.
 * <p>
 * Because SqlSessionTemplate is thread safe, a single instance can be shared by all DAOs; there should also be a small
 * memory savings by doing this. This pattern can be used in Spring configuration files as follows:
 *
 * <pre class="code">
 * {@code
 * <bean id="sqlSessionTemplate" class="org.mybatis.spring.SqlSessionTemplate">
 *   <constructor-arg ref="sqlSessionFactory" />
 * </bean>
 * }
 * </pre>
 *
 * @see SqlSessionFactory
 * @see MyBatisExceptionTranslator
 */
public class SqlSessionTemplate implements SqlSession, DisposableBean {

    private final SqlSessionFactory sqlSessionFactory;

    private final ExecutorType executorType;

    private final SqlSession sqlSessionProxy;

    /**
     * Constructs a Spring managed SqlSession with the {@code SqlSessionFactory} provided as an argument.
     *
     * @param sqlSessionFactory a factory of SqlSession
     */
    public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType());
    }

    /**
     * Constructs a Spring managed SqlSession with the {@code SqlSessionFactory} provided as an argument and the given
     * {@code ExecutorType} {@code ExecutorType} cannot be changed once the {@code SqlSessionTemplate} is constructed.
     *
     * @param sqlSessionFactory a factory of SqlSession
     * @param executorType      an executor type on session
     */
    public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) {
        notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
        notNull(executorType, "Property 'executorType' is required");
        this.sqlSessionFactory = sqlSessionFactory;
        this.executorType = executorType;

        this.sqlSessionProxy = (SqlSession) newProxyInstance(SqlSessionFactory.class.getClassLoader(),
                new Class[]{SqlSession.class}, new SqlSessionInterceptor());
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return this.sqlSessionFactory;
    }

    public ExecutorType getExecutorType() {
        return this.executorType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T selectOne(String statement) {
        return this.sqlSessionProxy.selectOne(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return this.sqlSessionProxy.selectOne(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, mapKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey, rowBounds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> List<E> selectList(String statement) {
        return this.sqlSessionProxy.selectList(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.sqlSessionProxy.selectList(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectList(statement, parameter, rowBounds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void select(String statement, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, rowBounds, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        throw new UnsupportedOperationException("Manual close is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCache() {
        this.sqlSessionProxy.clearCache();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        return this.sqlSessionFactory.getConfiguration();
    }

    /**
     * Allow gently dispose bean:
     *
     * <pre>
     * {@code
     *
     * <bean id="sqlSession" class="org.mybatis.spring.SqlSessionTemplate">
     *  <constructor-arg index="0" ref="sqlSessionFactory" />
     * </bean>
     * }
     * </pre>
     * <p>
     * The implementation of {@link DisposableBean} forces spring context to use {@link DisposableBean#destroy()} method
     * instead of {@link SqlSessionTemplate#close()} to shutdown gently.
     *
     * @see SqlSessionTemplate#close()
     * @see "org.springframework.beans.factory.support.DisposableBeanAdapter#inferDestroyMethodIfNecessary(Object, RootBeanDefinition)"
     * @see "org.springframework.beans.factory.support.DisposableBeanAdapter#CLOSE_METHOD_NAME"
     */
    @Override
    public void destroy() throws Exception {
        // This method forces spring disposer to avoid call of SqlSessionTemplate.close() which gives
        // UnsupportedOperationException
    }

    /**
     * Proxy needed to route MyBatis method calls to the proper SqlSession got from Spring's Transaction Manager It also
     * unwraps exceptions thrown by {@code Method#invoke(Object, Object...)} to pass a {@code PersistenceException} to the
     * {@code PersistenceExceptionTranslator}.
     */
    private class SqlSessionInterceptor implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            SqlSession sqlSession = getSqlSession(SqlSessionTemplate.this.sqlSessionFactory, SqlSessionTemplate.this.executorType);
            try {
                return method.invoke(sqlSession, args);
            } catch (Throwable t) {
                throw unwrapThrowable(t);
            } finally {
                if (sqlSession != null) {
                    closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
                }
            }
        }
    }

}
