package cn.sliew.rtomde.platform.mybatis.builder.xml;

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
import java.util.List;
import java.util.Properties;

public class XMLApplicationBuilder extends BaseBuilder {

    private boolean parsed;
    private final XPathParser parser;

    public XMLApplicationBuilder(Reader reader, MybatisPlatformOptions platform) {
        this(new XPathParser(reader, true, platform.getVariables(), new XMLMapperEntityResolver()), platform);
    }

    public XMLApplicationBuilder(InputStream inputStream, MybatisPlatformOptions platform) {
        this(new XPathParser(inputStream, true, platform.getVariables(), new XMLMapperEntityResolver()), platform);
    }

    private XMLApplicationBuilder(XPathParser parser, MybatisPlatformOptions platform) {
        super(new MybatisApplicationOptions(platform));
        application.setPlatform(platform);
        ErrorContext.instance().resource("SQL Mapper Application Configuration");
        this.parsed = false;
        this.parser = parser;
    }

    public MybatisApplicationOptions parse() {
        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;
        parseApplication(parser.evalNode("/application"));
        return application;
    }

    /**
     * fixme 远程读取mapper文件
     */
    private void parseApplication(XNode root) {
        try {
            String application = root.getStringAttribute("name");
            if (application == null || application.trim().length() == 0) {
                throw new BuilderException("Error parsing SQL Mapper Application Configuration. Must provide application name!");
            }
            this.application.setId(application);
            this.application.setName(application);

            propertiesElement(root.evalNode("properties"));
            typeAliasesElement(root.evalNode("typeAliases"));
            environmentsElement(root.evalNode("environments"));
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Application Configuration. Cause: " + e, e);
        }
    }

    private void propertiesElement(XNode context) throws Exception {
        if (context != null) {
            Properties defaults = context.getChildrenAsProperties();
            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url");
            if (resource != null && url != null) {
                throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
            }
            if (resource != null) {
                defaults.putAll(Resources.getResourceAsProperties(resource));
            } else if (url != null) {
                defaults.putAll(Resources.getUrlAsProperties(url));
            }
            Properties props = application.getProps();
            if (props != null) {
                defaults.putAll(props);
            }
            parser.setVariables(defaults);
            application.setProps(defaults);
        }
    }

    private void typeAliasesElement(XNode parent) {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                String alias = child.getStringAttribute("alias");
                String type = child.getStringAttribute("type");
                try {
                    Class<?> clazz = Resources.classForName(type);
                    if (alias == null) {
                        typeAliasRegistry.registerAlias(clazz);
                    } else {
                        typeAliasRegistry.registerAlias(alias, clazz);
                    }
                } catch (ClassNotFoundException e) {
                    throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
                }
            }
        }
    }

    private void environmentsElement(XNode context) {
        if (context != null) {
            for (XNode envNode : context.getChildren()) {
                String envId = envNode.getStringAttribute("id");
                MybatisPlatformOptions platform = (MybatisPlatformOptions) application.getPlatform();
                if (!platform.getEnvironment().equals(envId)) {
                    continue;
                }

                dataSourcesElement(envNode.evalNodes("hikaricp"));
                lettucesElement(envNode.evalNodes("lettuce"));
            }
        }
    }

    private void dataSourcesElement(List<XNode> contexts) {
        for (XNode context : contexts) {
            dataSourceElement(context);
        }
    }

    private void dataSourceElement(XNode context) {
        String id = context.getStringAttribute("id");
        XNode jdbcUrl = context.evalNode("jdbcUrl");
        XNode username = context.evalNode("username");
        XNode password = context.evalNode("password");
        XNode driverClassName = context.evalNode("driverClassName");
        XNode profileSQL = context.evalNode("profileSQL");

        DatasourceOptions datasource = new DatasourceOptions();
        datasource.setId(id);
        datasource.setJdbcUrl(jdbcUrl.getStringBody());
        datasource.setUsername(username.getStringBody());
        datasource.setPassword(password.getStringBody());
        datasource.setDriverClassName(driverClassName.getStringBody());
        datasource.setProfileSQL(profileSQL.getBooleanBody());
        application.addDatasourceOptions(datasource);
    }

    private void lettucesElement(List<XNode> contexts) {
        for (XNode context : contexts) {
            lettuceElement(context);
        }
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
        application.addLettuceOptions(lettuce);
    }

    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                String url = child.getStringAttribute("url");
                if (url != null) {
                    ErrorContext.instance().resource(url);
                    InputStream inputStream = Resources.getUrlAsStream(url);
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, application, url, application.getSqlFragments());
                    mapperParser.parse();
                } else {
                    throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                }
            }
        }
    }

}
