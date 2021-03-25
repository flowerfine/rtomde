package cn.sliew.rtomde.platform.mybatis.session;

/**
 * Creates an {@link SqlSession} out of a connection or a DataSource
 */
public interface SqlSessionFactory {

    SqlSession openSession();

    Configuration getConfiguration();

}
