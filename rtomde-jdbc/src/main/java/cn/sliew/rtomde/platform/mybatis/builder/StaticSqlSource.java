package cn.sliew.rtomde.platform.mybatis.builder;

import cn.sliew.rtomde.platform.mybatis.mapping.BoundSql;
import cn.sliew.rtomde.platform.mybatis.mapping.ParameterMapping;
import cn.sliew.rtomde.platform.mybatis.mapping.SqlSource;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;

import java.util.List;

public class StaticSqlSource implements SqlSource {

    private final String sql;
    private final List<ParameterMapping> parameterMappings;
    private final Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }

}
