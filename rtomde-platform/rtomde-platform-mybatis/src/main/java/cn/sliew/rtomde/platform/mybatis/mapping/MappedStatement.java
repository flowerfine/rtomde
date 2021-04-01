package cn.sliew.rtomde.platform.mybatis.mapping;

import cn.sliew.milky.log.Logger;
import cn.sliew.milky.log.LoggerFactory;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriver;

import java.util.List;

public final class MappedStatement {

    private final String resource;
    private final MybatisApplicationOptions application;
    private final String id;
    private final String dataSourceId;
    private final ParameterMap parameterMap;
    private final ResultMap resultMap;
    private final SqlSource sqlSource;
    private final Integer timeout;

    private final String cacheRefId;
    private final Logger statementLog;
    private final LanguageDriver lang;

    private MappedStatement(String resource, MybatisApplicationOptions application, String id, String dataSourceId, ParameterMap parameterMap, ResultMap resultMap, SqlSource sqlSource, Integer timeout, String cacheRefId, Logger statementLog, LanguageDriver lang) {
        this.resource = resource;
        this.application = application;
        this.id = id;
        this.dataSourceId = dataSourceId;
        this.parameterMap = parameterMap;
        this.resultMap = resultMap;
        this.sqlSource = sqlSource;
        this.timeout = timeout;
        this.cacheRefId = cacheRefId;
        this.statementLog = statementLog;
        this.lang = lang;
    }

    public static Builder builder(MybatisApplicationOptions application) {
        return new Builder(application);
    }

    public static class Builder {
        private String resource;
        private MybatisApplicationOptions application;
        private String id;
        private String dataSourceId;
        private ParameterMap parameterMap;
        private ResultMap resultMap;
        private SqlSource sqlSource;
        private Integer timeout;

        private String cacheRef;
        private Logger statementLog;
        private LanguageDriver lang;

        private Builder(MybatisApplicationOptions application) {
            this.application = application;
        }

        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder dataSourceId(String dataSourceId) {
            this.dataSourceId = dataSourceId;
            return this;
        }

        public Builder parameterMap(ParameterMap parameterMap) {
            this.parameterMap = parameterMap;
            return this;
        }

        public Builder resultMap(ResultMap resultMap) {
            this.resultMap = resultMap;
            return this;
        }

        public Builder sqlSource(SqlSource sqlSource) {
            this.sqlSource = sqlSource;
            return this;
        }

        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder cacheRef(String cacheRef) {
            this.cacheRef = cacheRef;
            return this;
        }

        public Builder lang(LanguageDriver driver) {
            this.lang = driver;
            return this;
        }

        public MappedStatement build() {
            String logId = id;
            if (application.getLogPrefix() != null) {
                logId = application.getLogPrefix() + id;
            }
            this.statementLog = LoggerFactory.getLogger(logId);
            if (this.lang == null) {
                MybatisPlatformOptions platform = (MybatisPlatformOptions) application.getPlatform();
                this.lang = platform.getDefaultScriptingLanguageInstance();
            }
            return new MappedStatement(resource, application, id, dataSourceId, parameterMap, resultMap, sqlSource, timeout, cacheRef, statementLog, lang);
        }
    }

    public String getResource() {
        return resource;
    }

    public MybatisApplicationOptions getApplication() {
        return application;
    }

    public String getId() {
        return id;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public ParameterMap getParameterMap() {
        return parameterMap;
    }

    public ResultMap getResultMap() {
        return resultMap;
    }

    public SqlSource getSqlSource() {
        return sqlSource;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public String getCacheRefId() {
        return cacheRefId;
    }

    public Logger getStatementLog() {
        return statementLog;
    }

    public LanguageDriver getLang() {
        return lang;
    }

    public BoundSql getBoundSql(Object parameterObject) {
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            boundSql = new BoundSql(application, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
        }
        return boundSql;
    }

}
