package org.mybatis.spring;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.support.ResourceHolderSupport;

import static org.springframework.util.Assert.notNull;

/**
 * Used to keep current {@code SqlSession} in {@code TransactionSynchronizationManager}. The {@code SqlSessionFactory}
 * that created that {@code SqlSession} is used as a key. {@code ExecutorType} is also kept to be able to check if the
 * user is trying to change it during a TX (that is not allowed) and throw a Exception in that case.
 */
public final class SqlSessionHolder extends ResourceHolderSupport {

    private final SqlSession sqlSession;

    private final ExecutorType executorType;

    private final PersistenceExceptionTranslator exceptionTranslator;

    /**
     * Creates a new holder instance.
     *
     * @param sqlSession          the {@code SqlSession} has to be hold.
     * @param executorType        the {@code ExecutorType} has to be hold.
     * @param exceptionTranslator the {@code PersistenceExceptionTranslator} has to be hold.
     */
    public SqlSessionHolder(SqlSession sqlSession, ExecutorType executorType,
                            PersistenceExceptionTranslator exceptionTranslator) {

        notNull(sqlSession, "SqlSession must not be null");
        notNull(executorType, "ExecutorType must not be null");

        this.sqlSession = sqlSession;
        this.executorType = executorType;
        this.exceptionTranslator = exceptionTranslator;
    }

    public SqlSession getSqlSession() {
        return sqlSession;
    }

    public ExecutorType getExecutorType() {
        return executorType;
    }

    public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {
        return exceptionTranslator;
    }

}
