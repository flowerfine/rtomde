package cn.sliew.rtomde.platform.mybatis.builder.xml;

import cn.sliew.rtomde.platform.mybatis.builder.*;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisCacheOptions;
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

public class XMLMapperBuilder extends BaseBuilder {

    private final XPathParser parser;
    private final MapperBuilderAssistant builderAssistant;
    private final Map<String, XNode> sqlFragments;
    private final String resource;

    public XMLMapperBuilder(InputStream inputStream, MybatisApplicationOptions application, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(inputStream, true, application.getProps(), new XMLMapperEntityResolver()),
                application, resource, sqlFragments);
    }

    private XMLMapperBuilder(XPathParser parser, MybatisApplicationOptions application, String resource, Map<String, XNode> sqlFragments) {
        super(application);
        this.builderAssistant = new MapperBuilderAssistant(application, resource);
        this.parser = parser;
        this.sqlFragments = sqlFragments;
        this.resource = resource;
    }

    /**
     * 因为在application层配置lettuce连接信息，在mapper层配置缓存配置信息，
     * select在引用的时候会生成一个新的缓存对象，所以缓存配置也是可以支持跨域支持的。
     */
    public void parse() {
        if (!application.isResourceLoaded(resource)) {
            mapperElement(parser.evalNode("/mapper"));
            application.addLoadedResource(resource);
        }
        parsePendingResultMaps();
        parsePendingStatements();
    }

    public XNode getSqlFragment(String refid) {
        return sqlFragments.get(refid);
    }

    private void mapperElement(XNode context) {
        try {
            String application = context.getStringAttribute("application");
            if (!this.application.getId().equals(application)) {
                throw new BuilderException("Mapper's application '" + application + "' cannot match Config's application '" + this.application.getId() + "'");
            }
            String namespace = context.getStringAttribute("namespace");
            if (namespace == null || namespace.isEmpty()) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }

            builderAssistant.setCurrentNamespace(namespace);
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
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(application, builderAssistant, context);
            try {
                statementParser.parseStatementNode();
            } catch (IncompleteElementException e) {
                application.addIncompleteStatement(statementParser);
            }
        }
    }

    private void parsePendingResultMaps() {
        Collection<ResultMapResolver> incompleteResultMaps = application.getIncompleteResultMaps();
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
        Collection<XMLStatementBuilder> incompleteStatements = application.getIncompleteStatements();
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

    private void cacheElement(XNode context) {
        if (context != null) {
            String id = context.getStringAttribute("id");
            String type = context.getStringAttribute("type");
            String refId = context.getStringAttribute("refId");
            Long expire = context.getLongAttribute("expire", 30000L);
            Long size = context.getLongAttribute("size", 30000L);
            Properties props = context.getChildrenAsProperties();
            builderAssistant.addCache(id, type, refId, expire, size, props);
        }
    }

    private void parameterMapElement(List<XNode> list) {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            String type = parameterMapNode.getStringAttribute("type");
            List<XNode> parameterNodes = parameterMapNode.evalNodes("parameter");
            List<ParameterMapping> parameterMappings = new ArrayList<>();
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
        // 解析失败后记录解析上下文，稍后继续解析。
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, type, extend, resultMappings, autoMapping);
        try {
            return resultMapResolver.resolve();
        } catch (IncompleteElementException e) {
            application.addIncompleteResultMap(resultMapResolver);
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

}
