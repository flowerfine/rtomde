package org.apache.ibatis.builder.xml;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

public class XMLStatementBuilder extends BaseBuilder {

    private final MapperBuilderAssistant builderAssistant;
    private final XNode context;

    public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context) {
        super(configuration);
        this.builderAssistant = builderAssistant;
        this.context = context;
    }

    public void parseStatementNode() {
        String id = context.getStringAttribute("id");
        String dataSourceId = context.getStringAttribute("dataSourceId");
        // Include Fragments before parsing
        XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
        includeParser.applyIncludes(context.getNode());
        // Parse the SQL (pre: <include> were parsed and removed)
        id = builderAssistant.applyCurrentNamespace(id, true);

        LanguageDriver langDriver = configuration.getLanguageDriver();
        SqlSource sqlSource = langDriver.createSqlSource(configuration, context, null);
        String parameterMap = context.getStringAttribute("parameterMap");
        String resultMap = context.getStringAttribute("resultMap");
        Integer timeout = context.getIntAttribute("timeout");
        builderAssistant.addMappedStatement(id, dataSourceId, parameterMap, resultMap, sqlSource, timeout, langDriver);
    }
}
