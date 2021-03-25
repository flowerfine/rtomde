package cn.sliew.rtomde.platform.mybatis.builder.xml;

import cn.sliew.rtomde.platform.mybatis.builder.BaseBuilder;
import cn.sliew.rtomde.platform.mybatis.builder.MapperBuilderAssistant;
import cn.sliew.rtomde.platform.mybatis.mapping.SqlSource;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriver;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;

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
        String cacheRef = context.getStringAttribute("cacheRef");
        builderAssistant.addMappedStatement(id, dataSourceId, parameterMap, resultMap, sqlSource, timeout, langDriver, cacheRef);
    }
}
