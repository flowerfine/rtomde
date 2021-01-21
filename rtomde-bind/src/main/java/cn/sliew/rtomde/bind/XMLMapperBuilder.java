package cn.sliew.rtomde.bind;

import cn.sliew.rtomde.common.xml.XNode;
import cn.sliew.rtomde.common.xml.XPathParser;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class XMLMapperBuilder {

    private final XPathParser parser;
    private final Map<String, XNode> sqlFragments;
    private final String resource;

    public XMLMapperBuilder(InputStream inputStream, String resource, Map<String, XNode> fragments) {
        this(new XPathParser(inputStream, true, new Properties(), new XMLMapperEntityResolver()),
                resource,
                fragments);
    }

    public XMLMapperBuilder(XPathParser parser, String resource, Map<String, XNode> fragments) {
        this.parser = parser;
        this.sqlFragments = fragments;
        this.resource = resource;
    }




}
