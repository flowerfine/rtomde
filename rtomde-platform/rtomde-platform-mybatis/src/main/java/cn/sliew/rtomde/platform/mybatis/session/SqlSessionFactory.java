package cn.sliew.rtomde.platform.mybatis.session;

import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;

/**
 * Creates an {@link SqlSession} out of a connection or a DataSource
 */
public interface SqlSessionFactory {

    SqlSession openSession(String application);

    MybatisPlatformOptions getPlatform();

}
