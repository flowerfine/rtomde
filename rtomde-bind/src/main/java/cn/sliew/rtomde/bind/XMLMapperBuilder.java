package cn.sliew.rtomde.bind;

import cn.sliew.rtomde.common.xml.ParseException;
import cn.sliew.rtomde.common.xml.XNode;
import cn.sliew.rtomde.common.xml.XPathParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class XMLMapperBuilder {

    private final XPathParser parser;
    private final Map<String, XNode> sqlFragments;
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
        this.sqlFragments = fragments;
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
                String jdbcType = parameterNode.getStringAttribute("jdbcType");
                String resultMap = parameterNode.getStringAttribute("resultMap");
                String mode = parameterNode.getStringAttribute("mode");
                String typeHandler = parameterNode.getStringAttribute("typeHandler");
                Integer numericScale = parameterNode.getIntAttribute("numericScale");
                ParameterMode modeEnum = resolveParameterMode(mode);
                Class<?> javaTypeClass = resolveClass(javaType);
                JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
                Class<? extends TypeHandler<?>> typeHandlerClass = resolveClass(typeHandler);
                ParameterMapping parameterMapping = builderAssistant.buildParameterMapping(parameterClass, property, javaTypeClass, jdbcTypeEnum, resultMap, modeEnum, typeHandlerClass, numericScale);
                parameterMappings.add(parameterMapping);
            }
            builderAssistant.addParameterMap(id, parameterClass, parameterMappings);
        }
    }
}
