package cn.sliew.rtomde.platform.mybatis.builder.xml;

import cn.sliew.rtomde.platform.mybatis.builder.BaseBuilder;
import cn.sliew.rtomde.platform.mybatis.builder.BuilderException;
import cn.sliew.rtomde.platform.mybatis.config.DatasourceOptions;
import cn.sliew.rtomde.platform.mybatis.config.LettuceOptions;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.io.Resources;
import cn.sliew.rtomde.platform.mybatis.mapping.Environment;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
import cn.sliew.rtomde.platform.mybatis.parsing.XPathParser;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

public class XMLConfigBuilder extends BaseBuilder {

    private boolean parsed;
    private final XPathParser parser;

    public XMLConfigBuilder(Reader reader, Configuration configuration) {
        this(new XPathParser(reader, true, configuration.getVariables(), new XMLMapperEntityResolver()), configuration);
    }

    public XMLConfigBuilder(InputStream inputStream, Configuration configuration) {
        this(new XPathParser(inputStream, true, configuration.getVariables(), new XMLMapperEntityResolver()), configuration);
    }

    private XMLConfigBuilder(XPathParser parser, Configuration configuration) {
        super(configuration);
        ErrorContext.instance().resource("SQL Mapper Application Configuration");
        this.parsed = false;
        this.parser = parser;
    }

    public Configuration parse() {
        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;
        parseConfiguration(parser.evalNode("/application"));
        return configuration;
    }

    /**
     * fixme 远程读取mapper文件
     */
    private void parseConfiguration(XNode root) {
        try {
            String application = root.getStringAttribute("name");
            if (application == null || application.trim().length() == 0) {
                throw new BuilderException("Error parsing SQL Mapper Application Configuration. Must provide application name!");
            }
            configuration.setApplication(application);

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
            Properties vars = configuration.getVariables();
            if (vars != null) {
                defaults.putAll(vars);
            }
            parser.setVariables(defaults);
            configuration.setVariables(defaults);
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

    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            for (XNode envNode : context.getChildren()) {
                String envId = envNode.getStringAttribute("id");
                if (!configuration.getPlatform().getEnvironment().equals(envId)) {
                    continue;
                }

                Environment.Builder envBuilder = Environment.builder().id(envId);
                dataSourcesElement(envNode.evalNodes("hikaricp"), envBuilder);
                lettucesElement(envNode.evalNodes("lettuce"), envBuilder);
                configuration.setEnvironment(envBuilder.build());
            }
        }
    }

    private void dataSourcesElement(List<XNode> contexts, Environment.Builder envBuilder) throws Exception {
        for (XNode context : contexts) {
            dataSourceElement(context, envBuilder);
        }
    }

    private void dataSourceElement(XNode context, Environment.Builder envBuilder) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
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
        envBuilder.datasource(id, datasource);
    }

    private void lettucesElement(List<XNode> contexts, Environment.Builder envBuilder) {
        for (XNode context : contexts) {
            lettuceElement(context, envBuilder);
        }
    }

    private void lettuceElement(XNode context, Environment.Builder envBuilder) {
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
        envBuilder.lettuce(id, lettuce);
    }

    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                String url = child.getStringAttribute("url");
                if (url != null) {
                    ErrorContext.instance().resource(url);
                    InputStream inputStream = Resources.getUrlAsStream(url);
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
                    mapperParser.parse();
                } else {
                    throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                }
            }
        }
    }

}
