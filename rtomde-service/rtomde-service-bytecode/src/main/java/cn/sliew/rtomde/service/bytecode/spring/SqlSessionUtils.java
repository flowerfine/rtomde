package cn.sliew.rtomde.service.bytecode.spring;

import cn.sliew.rtomde.platform.mybatis.session.SqlSession;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import cn.sliew.rtomde.service.bytecode.logging.Logger;
import cn.sliew.rtomde.service.bytecode.logging.LoggerFactory;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.springframework.util.Assert.notNull;

/**
 * Handles MyBatis SqlSession life cycle. It can register and get SqlSessions from Spring
 * {@code TransactionSynchronizationManager}. Also works if no transaction is active.
 */
public final class SqlSessionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlSessionUtils.class);

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
     * @param application a MyBatis {@code application} to specify new sessions
     * @return a MyBatis {@code SqlSession}
     * @throws TransientDataAccessResourceException if a transaction is active and the {@code SqlSessionFactory} is not using a
     *                                              {@code SpringManagedTransactionFactory}
     */
    public static SqlSession getSqlSession(SqlSessionFactory sessionFactory, String application) {
        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);

        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

        SqlSession session = sessionHolder(holder);
        if (session != null) {
            return session;
        }

        LOGGER.debug(() -> "Creating a new SqlSession");
        return sessionFactory.openSession(application);
    }

    private static SqlSession sessionHolder(SqlSessionHolder holder) {
        SqlSession session = null;
        if (holder != null && holder.isSynchronizedWithTransaction()) {
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

}
