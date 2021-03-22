package cn.sliew.rtomde.platform.mybatis.builder.xml;

import cn.sliew.rtomde.config.ApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.builder.BuilderException;
import cn.sliew.rtomde.platform.mybatis.config.DatasourceOptions;
import cn.sliew.rtomde.platform.mybatis.config.LettuceOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.io.Resources;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
import cn.sliew.rtomde.platform.mybatis.parsing.XPathParser;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XMLApplicationBuilder {

    private boolean parsed;
    private final XPathParser parser;
    private final MybatisPlatformOptions platform;
    private final MybatisApplicationOptions application;

    public XMLApplicationBuilder(Reader reader, MybatisPlatformOptions platform) {
        this(new XPathParser(reader, true, platform.getVariables(), new XMLMapperEntityResolver()), platform);
    }

    public XMLApplicationBuilder(InputStream inputStream, MybatisPlatformOptions platform) {
        this(new XPathParser(inputStream, true, platform.getVariables(), new XMLMapperEntityResolver()), platform);
    }

    private XMLApplicationBuilder(XPathParser parser, MybatisPlatformOptions platform) {
        ErrorContext.instance().resource("Mybatis Platform Application Options");
        this.parsed = false;
        this.parser = parser;
        this.platform = platform;
        this.application = new MybatisApplicationOptions();
        this.application.setPlatform(platform);
    }

    public ApplicationOptions parse() {
        if (parsed) {
            throw new BuilderException("Each XMLApplicationBuilder can only be used once.");
        }
        parsed = true;
        parseApplication(parser.evalNode("/application"));
        return application;
    }

    private void parseApplication(XNode root) {
        try {
            String name = root.getStringAttribute("name");
            application.setName(name);
            propertiesElement(root.evalNode("properties"));
            environmentElement(root.evalNode("environments"));
            /**
             * fixme 远程读取一系列文件
             */
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing Mybatis Platform Application Options. Cause: " + e, e);
        }
    }

    private void propertiesElement(XNode context) {
        if (context != null) {
            Properties defaults = context.getChildrenAsProperties();
            Properties vars = platform.getVariables();
            if (vars != null) {
                defaults.putAll(vars);
            }
            parser.setVariables(defaults);
            application.setParameters(new HashMap<String, String>((Map) defaults));
        }
    }

    private void environmentElement(XNode context) {
        if (context != null) {
            for (XNode envNode : context.getChildren()) {
                String envId = envNode.getStringAttribute("id");
                if (!platform.getEnvironment().equals(envId)) {
                    continue;
                }
                dataSourceElement(envNode.evalNode("dataSource"));
                lettuceElement(envNode.evalNode("lettuce"));
            }
        }
    }

    private void dataSourceElement(XNode context) {
        String id = context.getStringAttribute("id");
        XNode jdbcUrl = context.evalNode("jdbcUrl");
        XNode username = context.evalNode("username");
        XNode password = context.evalNode("password");
        XNode driverClassName = context.evalNode("driverClassName");
        XNode profileSQL = context.evalNode("profileSQL");
        DatasourceOptions dataSource = new DatasourceOptions();
        dataSource.setId(id);
        dataSource.setJdbcUrl(jdbcUrl.getStringBody());
        dataSource.setUsername(username.getStringBody());
        dataSource.setPassword(password.getStringBody());
        dataSource.setDriverClassName(driverClassName.getStringBody());
        dataSource.setProfileSQL(profileSQL.getBooleanBody());
        application.setDatasource(dataSource);
    }

    private void lettuceElement(XNode context) {
        String id = context.getStringAttribute("id");
        XNode redisURI = context.evalNode("redisURI");
        XNode clusterRedisURI = context.evalNode("clusterRedisURI");
        LettuceOptions lettuce = new LettuceOptions();
        lettuce.setId(id);
        if (redisURI != null) {
            lettuce.setRedisURI(redisURI.getStringBody());
        }
        if (clusterRedisURI != null) {
            lettuce.setClusterRedisURI(clusterRedisURI.getStringBody());
        }
        application.setLettuce(lettuce);
    }

    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                String url = child.getStringAttribute("url");
                if (url != null) {
                    ErrorContext.instance().resource(url);
                    InputStream inputStream = Resources.getUrlAsStream(url);
                    Map<String, String> parameters = application.getParameters();
                    Properties properties = new Properties();
                    properties.putAll(parameters);
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream,properties, application, url, application.getSqlFragments());
                    mapperParser.parse();
                } else {
                    throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                }
            }
        }
    }


}
