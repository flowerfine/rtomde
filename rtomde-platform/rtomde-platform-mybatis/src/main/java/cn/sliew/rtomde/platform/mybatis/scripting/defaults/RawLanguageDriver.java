package cn.sliew.rtomde.platform.mybatis.scripting.defaults;

import cn.sliew.rtomde.platform.mybatis.builder.BuilderException;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.SqlSource;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
import cn.sliew.rtomde.platform.mybatis.scripting.xmltags.XMLLanguageDriver;

/**
 * As of 3.2.4 the default XML language is able to identify static statements
 * and create a {@link RawSqlSource}. So there is no need to use RAW unless you
 * want to make sure that there is not any dynamic tag for any reason.
 */
public class RawLanguageDriver extends XMLLanguageDriver {

    @Override
    public SqlSource createSqlSource(MybatisApplicationOptions application, XNode script, Class<?> parameterType) {
        SqlSource source = super.createSqlSource(application, script, parameterType);
        checkIsNotDynamic(source);
        return source;
    }

    @Override
    public SqlSource createSqlSource(MybatisApplicationOptions application, String script, Class<?> parameterType) {
        SqlSource source = super.createSqlSource(application, script, parameterType);
        checkIsNotDynamic(source);
        return source;
    }

    private void checkIsNotDynamic(SqlSource source) {
        if (!RawSqlSource.class.equals(source.getClass())) {
            throw new BuilderException("Dynamic content is not allowed when using RAW language");
        }
    }

}
