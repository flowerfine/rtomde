package cn.sliew.rtomde.platform.mybatis.scripting.defaults;

import cn.sliew.rtomde.platform.mybatis.builder.SqlSourceBuilder;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.BoundSql;
import cn.sliew.rtomde.platform.mybatis.mapping.SqlSource;
import cn.sliew.rtomde.platform.mybatis.scripting.xmltags.DynamicContext;
import cn.sliew.rtomde.platform.mybatis.scripting.xmltags.DynamicSqlSource;
import cn.sliew.rtomde.platform.mybatis.scripting.xmltags.SqlNode;

import java.util.HashMap;

/**
 * Static SqlSource. It is faster than {@link DynamicSqlSource} because mappings are
 * calculated during startup.
 */
public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    public RawSqlSource(MybatisApplicationOptions application, SqlNode rootSqlNode, Class<?> parameterType) {
        this(application, getSql(application, rootSqlNode), parameterType);
    }

    public RawSqlSource(MybatisApplicationOptions application, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(application);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
    }

    private static String getSql(MybatisApplicationOptions application, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(application, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

}
