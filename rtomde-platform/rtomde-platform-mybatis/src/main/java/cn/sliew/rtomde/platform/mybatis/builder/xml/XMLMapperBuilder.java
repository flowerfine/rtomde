package cn.sliew.rtomde.platform.mybatis.builder.xml;

import cn.sliew.rtomde.platform.mybatis.builder.*;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisArgumentOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.mapping.ParameterMapping;
import cn.sliew.rtomde.platform.mybatis.mapping.ResultMap;
import cn.sliew.rtomde.platform.mybatis.mapping.ResultMapping;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
import cn.sliew.rtomde.platform.mybatis.parsing.XPathParser;
import cn.sliew.rtomde.platform.mybatis.type.JdbcType;
import cn.sliew.rtomde.platform.mybatis.type.TypeHandler;

import java.io.InputStream;
import java.util.*;

public class XMLMapperBuilder {

    private final XPathParser parser;
    private final MapperBuilderAssistant builderAssistant;
    private final Map<String, XNode> sqlFragments;
    private final String resource;
    private final MybatisApplicationOptions application;

    public XMLMapperBuilder(InputStream inputStream, Properties properties, MybatisApplicationOptions application, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(inputStream, true, properties, new XMLMapperEntityResolver()),
                application, resource, sqlFragments);
    }

    private XMLMapperBuilder(XPathParser parser, MybatisApplicationOptions application, String resource, Map<String, XNode> sqlFragments) {
        this.parser = parser;
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
        this.application = application;
        this.resource = resource;
        this.sqlFragments = sqlFragments;
    }

    public void parse() {
        if (!application.isResourceLoaded(resource)) {
            mapperElement(parser.evalNode("/mapper"));
            application.addLoadedResource(resource);
            bindMapperForNamespace();
        }
        parsePendingResultMaps();
        parsePendingStatements();
    }

    public XNode getSqlFragment(String refid) {
        return sqlFragments.get(refid);
    }

    private void mapperElement(XNode context) {
        try {
            String namespace = context.getStringAttribute("namespace");
            if (namespace == null || namespace.isEmpty()) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }
            String application = context.getStringAttribute("application");
            if (!this.application.getId().equals(application)) {
                throw new BuilderException("Mapper's application '" + application + "' cannot match Config's application '" + this.application.getId() + "'");
            }
            builderAssistant.setCurrentNamespace(namespace);
            cacheRefElement(context.evalNode("cache-ref"));
            cacheElement(context.evalNode("cache"));
            parameterMapElement(context.evalNodes("/mapper/parameterMap"));
            resultMapElements(context.evalNodes("/mapper/resultMap"));
            sqlElement(context.evalNodes("/mapper/sql"));
            buildStatementFromContext(context.evalNodes("select"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
        }
    }

    private void buildStatementFromContext(List<XNode> list) {
        for (XNode context : list) {
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context);
            try {
                statementParser.parseStatementNode();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteStatement(statementParser);
            }
        }
    }

    private void parsePendingResultMaps() {
        Collection<ResultMapResolver> incompleteResultMaps = configuration.getIncompleteResultMaps();
        synchronized (incompleteResultMaps) {
            Iterator<ResultMapResolver> iter = incompleteResultMaps.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolve();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // ResultMap is still missing a resource...
                }
            }
        }
    }

    private void parsePendingStatements() {
        Collection<XMLStatementBuilder> incompleteStatements = configuration.getIncompleteStatements();
        synchronized (incompleteStatements) {
            Iterator<XMLStatementBuilder> iter = incompleteStatements.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().parseStatementNode();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Statement is still missing a resource...
                }
            }
        }
    }

    private void cacheRefElement(XNode context) {
        if (context != null) {
            configuration.addCacheRef(builderAssistant.getCurrentNamespace(), context.getStringAttribute("id"));
            CacheRefResolver cacheRefResolver = new CacheRefResolver(builderAssistant, context.getStringAttribute("id"));
            try {
                cacheRefResolver.resolveCacheRef();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteCacheRef(cacheRefResolver);
            }
        }
    }

    private void cacheElement(XNode context) {
        if (context != null) {
            String id = context.getStringAttribute("id");
            String type = context.getStringAttribute("type");
            String refId = context.getStringAttribute("refId");
            Long expire = context.getLongAttribute("expire", 30000L);
            Long size = context.getLongAttribute("size", 30000L);
            Properties props = context.getChildrenAsProperties();
            builderAssistant.useNewCache(id, type, refId, expire, size, props);
        }
    }

    private void parameterMapElement(List<XNode> list) {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            String type = parameterMapNode.getStringAttribute("type");
            List<XNode> parameterNodes = parameterMapNode.evalNodes("parameter");
            List<MybatisArgumentOptions> parameterMappings = new ArrayList<>();
            for (XNode parameterNode : parameterNodes) {
                String property = parameterNode.getStringAttribute("property");
                String javaType = parameterNode.getStringAttribute("javaType");
                String jdbcType = parameterNode.getStringAttribute("jdbcType");
                String typeHandler = parameterNode.getStringAttribute("typeHandler");
                Class<?> javaTypeClass = resolveClass(javaType);
                JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
                Class<? extends TypeHandler<?>> typeHandlerClass = resolveClass(typeHandler);
                ParameterMapping parameterMapping = builderAssistant.buildParameterMapping(property, javaTypeClass, jdbcTypeEnum, typeHandlerClass);
                parameterMappings.add(parameterMapping);
            }
            builderAssistant.addParameterMap(id, type, parameterMappings);
        }
    }

    private void resultMapElements(List<XNode> list) {
        for (XNode resultMapNode : list) {
            try {
                resultMapElement(resultMapNode, Collections.emptyList());
            } catch (IncompleteElementException e) {
                // ignore, it will be retried
            }
        }
    }

    private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings) {
        ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
        String id = resultMapNode.getStringAttribute("id");
        String type = resultMapNode.getStringAttribute("type");
        String extend = resultMapNode.getStringAttribute("extends");
        Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
        List<ResultMapping> resultMappings = new ArrayList<>(additionalResultMappings);
        for (XNode resultNode : resultMapNode.getChildren()) {
            String property = resultNode.getStringAttribute("property");
            String javaType = resultNode.getStringAttribute("javaType");
            String column = resultNode.getStringAttribute("column");
            String jdbcType = resultNode.getStringAttribute("jdbcType");
            String typeHandler = resultNode.getStringAttribute("typeHandler");
            Class<?> javaTypeClass = resolveClass(javaType);
            Class<? extends TypeHandler<?>> typeHandlerClass = resolveClass(typeHandler);
            JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
            ResultMapping resultMapping = builderAssistant.buildResultMapping(property, javaTypeClass, column, jdbcTypeEnum, typeHandlerClass);
            resultMappings.add(resultMapping);
        }
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, type, extend, resultMappings, autoMapping);
        try {
            return resultMapResolver.resolve();
        } catch (IncompleteElementException e) {
            configuration.addIncompleteResultMap(resultMapResolver);
            throw e;
        }
    }

    private void sqlElement(List<XNode> list) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            id = builderAssistant.applyCurrentNamespace(id, false);
            sqlFragments.put(id, context);
        }
    }

    private void bindMapperForNamespace() {
//        String namespace = builderAssistant.getCurrentNamespace();
//        if (namespace != null) {
//            Class<?> boundType = null;
//            try {
//                boundType = Resources.classForName(namespace);
//            } catch (ClassNotFoundException e) {
//                // ignore, bound type is not required
//            }
//            if (boundType != null && !configuration.hasMapper(boundType)) {
//                // Spring may not know the real resource name so we set a flag
//                // to prevent loading again this resource from the mapper interface
//                // look at MapperAnnotationBuilder#loadXmlResource
//                configuration.addLoadedResource("namespace:" + namespace);
//                configuration.addMapper(boundType);
//            }
//        }
    }

    protected <T> Class<? extends T> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new BuilderException("Error resolving class. Cause: " + e, e);
        }
    }

    protected <T> Class<? extends T> resolveAlias(String alias) {
        MybatisPlatformOptions platform = (MybatisPlatformOptions) this.application.getPlatform();
        return platform.getTypeAliasRegistry().resolveAlias(alias);
    }

    protected JdbcType resolveJdbcType(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return JdbcType.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new BuilderException("Error resolving JdbcType. Cause: " + e, e);
        }
    }
}