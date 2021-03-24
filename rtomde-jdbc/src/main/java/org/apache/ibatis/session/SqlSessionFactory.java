package org.apache.ibatis.session;

/**
 * Creates an {@link SqlSession} out of a connection or a DataSource
 */
public interface SqlSessionFactory {

    SqlSession openSession();

    SqlSession openSession(ExecutorType execType);

    Configuration getConfiguration();

}
