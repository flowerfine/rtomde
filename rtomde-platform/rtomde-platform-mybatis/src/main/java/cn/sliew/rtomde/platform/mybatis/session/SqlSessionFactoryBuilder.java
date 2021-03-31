package cn.sliew.rtomde.platform.mybatis.session;

import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLApplicationBuilder;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.exceptions.ExceptionFactory;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.session.defaults.DefaultSqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * Builds {@link SqlSession} instances.
 */
public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        return build(reader, new MybatisPlatformOptions(environment, properties));
    }

    public SqlSessionFactory build(Reader reader, MybatisPlatformOptions platform) {
        try {
            XMLApplicationBuilder parser = new XMLApplicationBuilder(reader, platform);
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                reader.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        return build(inputStream, new MybatisPlatformOptions(environment, properties));
    }

    public SqlSessionFactory build(InputStream inputStream, MybatisPlatformOptions platform) {
        try {
            XMLApplicationBuilder parser = new XMLApplicationBuilder(inputStream, platform);
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public SqlSessionFactory build(MybatisApplicationOptions application) {
        return new DefaultSqlSessionFactory(application);
    }

}
