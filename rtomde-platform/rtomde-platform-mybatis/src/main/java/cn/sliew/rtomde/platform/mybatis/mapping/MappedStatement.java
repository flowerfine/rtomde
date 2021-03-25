package cn.sliew.rtomde.platform.mybatis.mapping;

import cn.sliew.milky.cache.lettuce.LettuceCacheFactory;
import cn.sliew.milky.cache.lettuce.LettuceCacheOptions;
import cn.sliew.milky.common.log.Logger;
import cn.sliew.milky.common.log.LoggerFactory;
import cn.sliew.rtomde.platform.mybatis.cache.Cache;
import cn.sliew.rtomde.platform.mybatis.config.MybatisCacheOptions;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriver;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;

import java.util.List;

public final class MappedStatement {

    private final String resource;
    private final Configuration configuration;
    private final String id;
    private final String dataSourceId;
    private final ParameterMap parameterMap;
    private final ResultMap resultMap;
    private final SqlSource sqlSource;
    private final Integer timeout;

    private final Cache cache;
    private final Logger statementLog;
    private final LanguageDriver lang;

    private MappedStatement(String resource, Configuration configuration, String id, String dataSourceId, ParameterMap parameterMap, ResultMap resultMap, SqlSource sqlSource, Integer timeout, Cache cache, Logger statementLog, LanguageDriver lang) {
        this.resource = resource;
        this.configuration = configuration;
        this.id = id;
        this.dataSourceId = dataSourceId;
        this.parameterMap = parameterMap;
        this.resultMap = resultMap;
        this.sqlSource = sqlSource;
        this.timeout = timeout;
        this.cache = cache;
        this.statementLog = statementLog;
        this.lang = lang;
    }

    public static Builder builder(Configuration configuration) {
        return new Builder(configuration);
    }

    public static class Builder {
        private String resource;
        private Configuration configuration;
        private String id;
        private String dataSourceId;
        private ParameterMap parameterMap;
        private ResultMap resultMap;
        private SqlSource sqlSource;
        private Integer timeout;

        private String cacheRef;
        private Logger statementLog;
        private LanguageDriver lang;

        private Builder(Configuration configuration) {
            this.configuration = configuration;
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
            if (configuration.getLogPrefix() != null) {
                logId = configuration.getLogPrefix() + id;
            }
            this.statementLog = LoggerFactory.getLogger(logId);
            if (this.lang == null) {
                this.lang = configuration.getDefaultScriptingLanguageInstance();
            }
            Environment environment = configuration.getEnvironment();
            MybatisCacheOptions cacheOptions = environment.getCacheOptionsById(cacheRef);
            // fixme 创建Cache对象
            LettuceCacheFactory factory = new LettuceCacheFactory();
            LettuceCacheOptions lettuceCacheOptions = new LettuceCacheOptions();

            return new MappedStatement(resource, configuration, id, dataSourceId, parameterMap, resultMap, sqlSource, timeout, cacheRef, statementLog, lang);
        }
    }

    public String getResource() {
        return resource;
    }

    public Configuration getConfiguration() {
        return configuration;
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

    public Cache getCache() {
        return cache;
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
            boundSql = new BoundSql(configuration, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
        }
        return boundSql;
    }

}
