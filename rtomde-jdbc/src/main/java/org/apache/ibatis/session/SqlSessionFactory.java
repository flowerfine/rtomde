package org.apache.ibatis.session;

import java.sql.Connection;

/**
 * Creates an {@link SqlSession} out of a connection or a DataSource
 */
public interface SqlSessionFactory {

    SqlSession openSession();

    SqlSession openSession(Connection connection);

    SqlSession openSession(ExecutorType execType);

    SqlSession openSession(ExecutorType execType, Connection connection);

    Configuration getConfiguration();

}
