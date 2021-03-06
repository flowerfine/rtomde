package cn.sliew.rtomde.platform.mybatis.scripting.xmltags;

import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLMapperEntityResolver;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.executor.parameter.ParameterHandler;
import cn.sliew.rtomde.platform.mybatis.mapping.BoundSql;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.mapping.SqlSource;
import cn.sliew.rtomde.platform.mybatis.parsing.PropertyParser;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
import cn.sliew.rtomde.platform.mybatis.parsing.XPathParser;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriver;
import cn.sliew.rtomde.platform.mybatis.scripting.defaults.DefaultParameterHandler;
import cn.sliew.rtomde.platform.mybatis.scripting.defaults.RawSqlSource;

public class XMLLanguageDriver implements LanguageDriver {

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    @Override
    public SqlSource createSqlSource(MybatisApplicationOptions application, XNode script, Class<?> parameterType) {
        XMLScriptBuilder builder = new XMLScriptBuilder(application, script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public SqlSource createSqlSource(MybatisApplicationOptions application, String script, Class<?> parameterType) {
        // issue #3
        if (script.startsWith("<script>")) {
            XPathParser parser = new XPathParser(script, false, application.getProps(), new XMLMapperEntityResolver());
            return createSqlSource(application, parser.evalNode("/script"), parameterType);
        } else {
            // issue #127
            script = PropertyParser.parse(script, application.getProps());
            TextSqlNode textSqlNode = new TextSqlNode(script);
            if (textSqlNode.isDynamic()) {
                return new DynamicSqlSource(application, textSqlNode);
            } else {
                return new RawSqlSource(application, script, parameterType);
            }
        }
    }

}
