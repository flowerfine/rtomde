package cn.sliew.rtomde.bind;

import cn.sliew.rtomde.common.xml.ParseException;
import cn.sliew.rtomde.common.xml.XNode;
import cn.sliew.rtomde.common.xml.XPathParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * todo 数据源配置相关的功能
 */
public class XMLMapperBuilder {

    private final XPathParser parser;
    private final Map<String, XNode> fragments;
    private final String resource;

    private String namespace;

    public XMLMapperBuilder(InputStream inputStream, String resource, Map<String, XNode> fragments) {
        this(new XPathParser(inputStream, true, new Properties(), new XMLMapperEntityResolver()),
                resource,
                fragments);
    }

    private XMLMapperBuilder(XPathParser parser, String resource, Map<String, XNode> fragments) {
        this.parser = parser;
        this.resource = resource;
        this.fragments = fragments;
    }

    public void parse() {
        mapperElement(parser.evalNode("/mapper"));
    }

    private void mapperElement(XNode context) {
        try {
            String namespace = context.getStringAttribute("namespace");
            if (namespace == null || namespace.isEmpty()) {
                throw new ParseException("Mapper's namespace cannot be empty");
            }
            this.namespace = namespace;
            //解析resultMap，paramterMap，解析query
            parameterMapElement(context.evalNodes("/mapper/parameterMap"));
            resultMapElements(context.evalNodes("/mapper/resultMap"));
            fragmentElement(context.evalNodes("/mapper/fragment"));
            queryStatementFromContext(context.evalNodes("query"));
        } catch (Exception e) {
            throw new ParseException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
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
                String columnType = parameterNode.getStringAttribute("columnType");
                String typeHandler = parameterNode.getStringAttribute("typeHandler");
                ParameterMapping parameterMapping = ParameterMapping.builder().property(property).javaType(javaType).columnType(columnType).typeHandler(typeHandler).build();
                parameterMappings.add(parameterMapping);
            }
            ParameterMap parameterMap = ParameterMap.builder().namespace(namespace).id(id).type(type).parameterMappings(parameterMappings).build();
        }
    }

    private void resultMapElements(List<XNode> list) {
        for (XNode resultMapNode : list) {
            try {
                resultMapElement(resultMapNode);
            } catch (IncompleteElementException e) {
                // ignore, it will be retried
            }
        }
    }

    private ResultMap resultMapElement(XNode resultMapNode) {
        String id = resultMapNode.getStringAttribute("id");
        String type = resultMapNode.getStringAttribute("type");
        Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
        List<ResultMapping> resultMappings = new ArrayList<>();
        List<XNode> resultNodes = resultMapNode.getChildren();
        for (XNode resultNode : resultNodes) {
            String property = resultNode.getStringAttribute("property");
            String javaType = resultNode.getStringAttribute("javaType");
            String column = resultNode.getStringAttribute("column");
            String columnType = resultNode.getStringAttribute("columnType");
            String typeHandler = resultNode.getStringAttribute("typeHandler");
            ResultMapping resultMapping = ResultMapping.builder().property(property).javaType(javaType).column(column).columnType(columnType).typeHandler(typeHandler).build();
            resultMappings.add(resultMapping);
        }
        return ResultMap.builder().namespace(namespace).id(id).type(type).autoMapping(autoMapping).resultMappings(resultMappings).build();
    }

    /**
     * fragment不应该有databaseId
     */
    private void fragmentElement(List<XNode> list) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            String qualified = namespace + "." + id;
            fragments.put(qualified, context);
        }
    }
    private void queryStatementFromContext(List<XNode> list) {
//        for (XNode context : list) {
//            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
//            try {
//                statementParser.parseStatementNode();
//            } catch (IncompleteElementException e) {
//                configuration.addIncompleteStatement(statementParser);
//            }
//        }
    }
}
