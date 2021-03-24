package cn.sliew.rtomde.platform.mybatis.builder;

import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MapperBuilderAssistant extends BaseBuilder {

    private String currentNamespace;
    private final String resource;
    private Cache currentCache;
    private boolean unresolvedCacheRef;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        ErrorContext.instance().resource(resource);
        this.resource = resource;
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        if (currentNamespace == null) {
            throw new BuilderException("The mapper element requires a namespace attribute to be specified.");
        }
        if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
            throw new BuilderException("Wrong namespace. Expected '"
                    + this.currentNamespace + "' but found '" + currentNamespace + "'.");
        }
        this.currentNamespace = currentNamespace;
    }

    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
            // is it qualified with any namespace yet?
            if (base.contains(".")) {
                return base;
            }
        } else {
            // is it qualified with this namespace yet?
            if (base.startsWith(currentNamespace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new BuilderException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentNamespace + "." + base;
    }

    public Cache useCacheRef(String refid) {
        if (refid == null) {
            throw new BuilderException("cache-ref element requires a refid attribute.");
        }
        try {
            unresolvedCacheRef = true;
            Cache cache = configuration.getCache(refid);
            if (cache == null) {
                throw new IncompleteElementException("No cache for refid '" + refid + "' could be found.");
            }
            currentCache = cache;
            unresolvedCacheRef = false;
            return cache;
        } catch (IllegalArgumentException e) {
            throw new IncompleteElementException("No cache for refid '" + refid + "' could be found.", e);
        }
    }

    /**
     * fixme 处理缓存的处理
     */
    public Cache useNewCache(String id, String type, String cacheRefId, Long expire, Long size, Properties props) {
        id = applyCurrentNamespace(id, false);
        Cache cache = CacheBuilder.builder(configuration)
                .id(id)
                .type(type)
                .refId(cacheRefId)
                .expire(expire)
                .size(size)
                .properties(props)
                .build();
        configuration.addCache(cache);
        currentCache = cache;
        return cache;
    }

    public ParameterMap addParameterMap(String id, String type, List<ParameterMapping> parameterMappings) {
        id = applyCurrentNamespace(id, false);
        ParameterMap parameterMap = ParameterMap.builder(configuration)
                .id(id)
                .type(type)
                .parameterMappings(parameterMappings)
                .build();
        configuration.addParameterMap(parameterMap);
        return parameterMap;
    }

    public ParameterMapping buildParameterMapping(String property, Class<?> javaType, JdbcType jdbcType, Class<? extends TypeHandler<?>> typeHandler) {
        Class<?> javaTypeClass = resolveParameterJavaType(javaType);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, typeHandler);
        return ParameterMapping.builder(configuration)
                .property(property)
                .javaType(javaTypeClass)
                .jdbcType(jdbcType)
                .typeHandler(typeHandlerInstance)
                .build();
    }

    public ResultMap addResultMap(String id, String type, String extend, List<ResultMapping> resultMappings, Boolean autoMapping) {
        id = applyCurrentNamespace(id, false);
        extend = applyCurrentNamespace(extend, true);

        if (extend != null) {
            if (!configuration.hasResultMap(extend)) {
                throw new IncompleteElementException("Could not find a parent resultmap with id '" + extend + "'");
            }
            ResultMap resultMap = configuration.getResultMap(extend);
            List<ResultMapping> extendedResultMappings = new ArrayList<>(resultMap.getResultMappings());
            extendedResultMappings.removeAll(resultMappings);
            resultMappings.addAll(extendedResultMappings);
        }
        ResultMap resultMap = ResultMap.builder(configuration).id(id).type(type).resultMappings(resultMappings).autoMapping(autoMapping).build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }

    public ResultMapping buildResultMapping(String property, Class<?> javaType, String column, JdbcType jdbcType, Class<? extends TypeHandler<?>> typeHandler) {
        Class<?> javaTypeClass = resolveResultJavaType(javaType);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, typeHandler);
        return ResultMapping.builder(configuration)
                .property(property)
                .javaType(javaTypeClass)
                .column(column)
                .jdbcType(jdbcType)
                .typeHandler(typeHandlerInstance)
                .build();
    }

    public MappedStatement addMappedStatement(String id, String dataSourceId, String parameterMap, String resultMap, SqlSource sqlSource, Integer timeout, LanguageDriver lang) {
        if (unresolvedCacheRef) {
            throw new IncompleteElementException("Cache-ref not yet resolved");
        }
        id = applyCurrentNamespace(id, false);
        ParameterMap statementParameterMap = getStatementParameterMap(parameterMap);
        ResultMap statementResultMap = getStatementResultMap(resultMap);
        MappedStatement statement = MappedStatement.builder(configuration)
                .resource(resource)
                .id(id)
                .dataSourceId(dataSourceId)
                .parameterMap(statementParameterMap)
                .resultMap(statementResultMap)
                .sqlSource(sqlSource)
                .timeout(timeout)
                .cache(currentCache)
                .lang(lang)
                .build();
        configuration.addMappedStatement(statement);
        return statement;
    }

    private <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private ParameterMap getStatementParameterMap(String parameterMapId) {
        parameterMapId = applyCurrentNamespace(parameterMapId, true);
        ParameterMap parameterMap = null;
        if (parameterMapId != null) {
            try {
                parameterMap = configuration.getParameterMap(parameterMapId);
            } catch (IllegalArgumentException e) {
                throw new IncompleteElementException("Could not find parameter map " + parameterMapId, e);
            }
        }
        return parameterMap;
    }

    private ResultMap getStatementResultMap(String resultMapId) {
        resultMapId = applyCurrentNamespace(resultMapId, true);
        ResultMap resultMap = null;
        if (resultMapId != null) {
            try {
                resultMap = configuration.getResultMap(resultMapId);
            } catch (IllegalArgumentException e) {
                throw new IncompleteElementException("Could not find result map " + resultMapId, e);
            }
        }
        return resultMap;
    }

    private Class<?> resolveResultJavaType(Class<?> javaType) {
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    private Class<?> resolveParameterJavaType(Class<?> javaType) {
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

}
