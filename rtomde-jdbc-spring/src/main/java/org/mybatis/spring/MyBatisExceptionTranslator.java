package org.mybatis.spring;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.transaction.TransactionException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default exception translator.
 * <p>
 * Translates MyBatis SqlSession returned exception into a Spring {@code DataAccessException} using Spring's
 * {@code SQLExceptionTranslator} Can load {@code SQLExceptionTranslator} eagerly or when the first exception is
 * translated.
 */
public class MyBatisExceptionTranslator implements PersistenceExceptionTranslator {

    private final ConcurrentMap<String, SQLExceptionTranslator> exceptionTranslators;

    /**
     * Creates a new {@code PersistenceExceptionTranslator} instance with {@code SQLErrorCodeSQLExceptionTranslator}.
     *
     * @param environment Environment to use to find metadata and establish which error codes are usable.
     */
    public MyBatisExceptionTranslator(Environment environment) {
        exceptionTranslators = new ConcurrentHashMap<>(4);
        Map<String, DataSource> dataSources = environment.getDataSources();
        dataSources.forEach((id, dataSource) -> exceptionTranslators.putIfAbsent(id, new SQLErrorCodeSQLExceptionTranslator(dataSource)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException e) {
        if (e instanceof PersistenceException) {
            PersistenceException persistenceException = (PersistenceException) e;
            SQLExceptionTranslator sqlExceptionTranslator = null;
            if (persistenceException.getMappedStatement().isPresent()) {
                String dataSourceId = persistenceException.getMappedStatement().get().getDataSourceId();
                sqlExceptionTranslator = this.exceptionTranslators.get(dataSourceId);
            }
            // Batch exceptions come inside another PersistenceException
            // recursion has a risk of infinite loop so better make another if
            if (e.getCause() instanceof PersistenceException) {
                e = (PersistenceException) e.getCause();
            }
            if (e.getCause() instanceof SQLException) {
                String task = e.getMessage() + "\n";
                SQLException se = (SQLException) e.getCause();
                if (sqlExceptionTranslator == null) {
                    return new UncategorizedSQLException(task, null, se);
                }
                DataAccessException dae = sqlExceptionTranslator.translate(task, null, se);
                return dae != null ? dae : new UncategorizedSQLException(task, null, se);
            } else if (e.getCause() instanceof TransactionException) {
                throw (TransactionException) e.getCause();
            }
            return new MyBatisSystemException(e);
        }
        return null;
    }

}
