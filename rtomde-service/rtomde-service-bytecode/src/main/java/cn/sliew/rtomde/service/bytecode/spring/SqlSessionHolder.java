package cn.sliew.rtomde.service.bytecode.spring;

import cn.sliew.rtomde.platform.mybatis.session.SqlSession;
import org.springframework.transaction.support.ResourceHolderSupport;

import static org.springframework.util.Assert.notNull;

/**
 * Used to keep current {@code SqlSession} in {@code TransactionSynchronizationManager}. The {@code SqlSessionFactory}
 * that created that {@code SqlSession} is used as a key. {@code ExecutorType} is also kept to be able to check if the
 * user is trying to change it during a TX (that is not allowed) and throw a Exception in that case.
 */
public final class SqlSessionHolder extends ResourceHolderSupport {

    private final SqlSession sqlSession;

    /**
     * Creates a new holder instance.
     *
     * @param sqlSession the {@code SqlSession} has to be hold.
     */
    public SqlSessionHolder(SqlSession sqlSession) {

        notNull(sqlSession, "SqlSession must not be null");

        this.sqlSession = sqlSession;
    }

    public SqlSession getSqlSession() {
        return sqlSession;
    }

}
