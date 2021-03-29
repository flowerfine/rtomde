package cn.sliew.rtomde.platform.mybatis.builder;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.BoundSql;
import cn.sliew.rtomde.platform.mybatis.mapping.ParameterMapping;
import cn.sliew.rtomde.platform.mybatis.mapping.SqlSource;

import java.util.List;

public class StaticSqlSource implements SqlSource {

    private final String sql;
    private final List<ParameterMapping> parameterMappings;
    private final MybatisApplicationOptions application;

    public StaticSqlSource(MybatisApplicationOptions application, String sql) {
        this(application, sql, null);
    }

    public StaticSqlSource(MybatisApplicationOptions application, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.application = application;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(application, sql, parameterMappings, parameterObject);
    }

}
