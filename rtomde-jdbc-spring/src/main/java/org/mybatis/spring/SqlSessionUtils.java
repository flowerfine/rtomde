package org.mybatis.spring;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.springframework.util.Assert.notNull;

/**
 * Handles MyBatis SqlSession life cycle. It can register and get SqlSessions from Spring
 * {@code TransactionSynchronizationManager}. Also works if no transaction is active.
 */
public final class SqlSessionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlSessionUtils.class);

    private static final String NO_EXECUTOR_TYPE_SPECIFIED = "No ExecutorType specified";
    private static final String NO_SQL_SESSION_FACTORY_SPECIFIED = "No SqlSessionFactory specified";
    private static final String NO_SQL_SESSION_SPECIFIED = "No SqlSession specified";

    /**
     * This class can't be instantiated, exposes static utility methods only.
     */
    private SqlSessionUtils() {
        // do nothing
    }

    /**
     * Creates a new MyBatis {@code SqlSession} from the {@code SqlSessionFactory} provided as a parameter and using its
     * {@code DataSource} and {@code ExecutorType}
     *
     * @param sessionFactory a MyBatis {@code SqlSessionFactory} to create new sessions
     * @return a MyBatis {@code SqlSession}
     * @throws TransientDataAccessResourceException if a transaction is active and the {@code SqlSessionFactory} is not using a
     *                                              {@code SpringManagedTransactionFactory}
     */
    public static SqlSession getSqlSession(SqlSessionFactory sessionFactory) {
        ExecutorType executorType = sessionFactory.getConfiguration().getDefaultExecutorType();
        return getSqlSession(sessionFactory, executorType);
    }

    /**
     * Gets an SqlSession from Spring Transaction Manager or creates a new one if needed. Tries to get a SqlSession out of
     * current transaction. If there is not any, it creates a new one. Then, it synchronizes the SqlSession with the
     * transaction if Spring TX is active and <code>SpringManagedTransactionFactory</code> is configured as a transaction
     * manager.
     *
     * @param sessionFactory a MyBatis {@code SqlSessionFactory} to create new sessions
     * @param executorType   The executor type of the SqlSession to create
     * @return an SqlSession managed by Spring Transaction Manager
     * @throws TransientDataAccessResourceException if a transaction is active and the {@code SqlSessionFactory} is not using a
     *                                              {@code SpringManagedTransactionFactory}
     */
    public static SqlSession getSqlSession(SqlSessionFactory sessionFactory, ExecutorType executorType) {

        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);
        notNull(executorType, NO_EXECUTOR_TYPE_SPECIFIED);

        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

        SqlSession session = sessionHolder(executorType, holder);
        if (session != null) {
            return session;
        }

        LOGGER.debug(() -> "Creating a new SqlSession");
        return sessionFactory.openSession(executorType);
    }

    private static SqlSession sessionHolder(ExecutorType executorType, SqlSessionHolder holder) {
        SqlSession session = null;
        if (holder != null && holder.isSynchronizedWithTransaction()) {
            if (holder.getExecutorType() != executorType) {
                throw new TransientDataAccessResourceException(
                        "Cannot change the ExecutorType when there is an existing transaction");
            }

            holder.requested();

            LOGGER.debug(() -> "Fetched SqlSession [" + holder.getSqlSession() + "] from current transaction");
            session = holder.getSqlSession();
        }
        return session;
    }

    /**
     * Checks if {@code SqlSession} passed as an argument is managed by Spring {@code TransactionSynchronizationManager}
     * If it is not, it closes it, otherwise it just updates the reference counter and lets Spring call the close callback
     * when the managed transaction ends
     *
     * @param session        a target SqlSession
     * @param sessionFactory a factory of SqlSession
     */
    public static void closeSqlSession(SqlSession session, SqlSessionFactory sessionFactory) {
        notNull(session, NO_SQL_SESSION_SPECIFIED);
        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);

        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        if ((holder != null) && (holder.getSqlSession() == session)) {
            LOGGER.debug(() -> "Releasing transactional SqlSession [" + session + "]");
            holder.released();
        } else {
            LOGGER.debug(() -> "Closing non transactional SqlSession [" + session + "]");
            session.close();
        }
    }

    /**
     * Returns if the {@code SqlSession} passed as an argument is being managed by Spring
     *
     * @param session        a MyBatis SqlSession to check
     * @param sessionFactory the SqlSessionFactory which the SqlSession was built with
     * @return true if session is transactional, otherwise false
     */
    public static boolean isSqlSessionTransactional(SqlSession session, SqlSessionFactory sessionFactory) {
        notNull(session, NO_SQL_SESSION_SPECIFIED);
        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);

        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

        return (holder != null) && (holder.getSqlSession() == session);
    }

    /**
     * Callback for cleaning up resources. It cleans TransactionSynchronizationManager and also commits and closes the
     * {@code SqlSession}. It assumes that {@code Connection} life cycle will be managed by
     * {@code DataSourceTransactionManager} or {@code JtaTransactionManager}
     */
    private static final class SqlSessionSynchronization extends TransactionSynchronizationAdapter {

        private final SqlSessionHolder holder;

        private final SqlSessionFactory sessionFactory;

        private boolean holderActive = true;

        public SqlSessionSynchronization(SqlSessionHolder holder, SqlSessionFactory sessionFactory) {
            notNull(holder, "Parameter 'holder' must be not null");
            notNull(sessionFactory, "Parameter 'sessionFactory' must be not null");

            this.holder = holder;
            this.sessionFactory = sessionFactory;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getOrder() {
            // order right before any Connection synchronization
            return DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void suspend() {
            if (this.holderActive) {
                LOGGER.debug(() -> "Transaction synchronization suspending SqlSession [" + this.holder.getSqlSession() + "]");
                TransactionSynchronizationManager.unbindResource(this.sessionFactory);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void resume() {
            if (this.holderActive) {
                LOGGER.debug(() -> "Transaction synchronization resuming SqlSession [" + this.holder.getSqlSession() + "]");
                TransactionSynchronizationManager.bindResource(this.sessionFactory, this.holder);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void beforeCommit(boolean readOnly) {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void beforeCompletion() {
            // Issue #18 Close SqlSession and deregister it now
            // because afterCompletion may be called from a different thread
            if (!this.holder.isOpen()) {
                LOGGER
                        .debug(() -> "Transaction synchronization deregistering SqlSession [" + this.holder.getSqlSession() + "]");
                TransactionSynchronizationManager.unbindResource(sessionFactory);
                this.holderActive = false;
                LOGGER.debug(() -> "Transaction synchronization closing SqlSession [" + this.holder.getSqlSession() + "]");
                this.holder.getSqlSession().close();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void afterCompletion(int status) {
            if (this.holderActive) {
                // afterCompletion may have been called from a different thread
                // so avoid failing if there is nothing in this one
                LOGGER.debug(() -> "Transaction synchronization deregistering SqlSession [" + this.holder.getSqlSession() + "]");
                TransactionSynchronizationManager.unbindResourceIfPossible(sessionFactory);
                this.holderActive = false;
                LOGGER.debug(() -> "Transaction synchronization closing SqlSession [" + this.holder.getSqlSession() + "]");
                this.holder.getSqlSession().close();
            }
            this.holder.reset();
        }
    }

}
