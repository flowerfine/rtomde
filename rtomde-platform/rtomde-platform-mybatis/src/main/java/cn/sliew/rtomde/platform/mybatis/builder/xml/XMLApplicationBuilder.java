package cn.sliew.rtomde.platform.mybatis.builder.xml;

import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.parsing.XPathParser;

import java.io.InputStream;
import java.io.Reader;

public class XMLApplicationBuilder {

    private boolean parsed;
    private final XPathParser parser;

    public XMLApplicationBuilder(Reader reader, MybatisPlatformOptions platform) {
        this(new XPathParser(reader, true, platform.getVariables(), new XMLMapperEntityResolver()));
    }

    public XMLApplicationBuilder(InputStream inputStream, MybatisPlatformOptions platform) {
        this(new XPathParser(inputStream, true, platform.getVariables(), new XMLMapperEntityResolver()));
    }

    private XMLApplicationBuilder(XPathParser parser) {
        ErrorContext.instance().resource("SQL Mapper Configuration");
        this.parsed = false;
        this.parser = parser;
    }
}
