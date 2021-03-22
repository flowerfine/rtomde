package cn.sliew.rtomde.platform.mybatis.builder.xml;

import cn.sliew.rtomde.platform.mybatis.builder.BaseBuilder;
import cn.sliew.rtomde.platform.mybatis.builder.BuilderException;
import cn.sliew.rtomde.platform.mybatis.cache.LettuceWrapper;
import cn.sliew.rtomde.platform.mybatis.datasource.DataSourceFactory;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.io.Resources;
import cn.sliew.rtomde.platform.mybatis.mapping.Environment;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
import cn.sliew.rtomde.platform.mybatis.parsing.XPathParser;
import cn.sliew.rtomde.platform.mybatis.reflection.DefaultReflectorFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.ReflectorFactory;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;
import cn.sliew.rtomde.platform.mybatis.session.ConfigurationRegistry;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

public class XMLConfigBuilder extends BaseBuilder {

    private boolean parsed;
    private final XPathParser parser;
    private String defaultEnvironment;
    private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

    public XMLConfigBuilder(Reader reader) {
        this(reader, null, null);
    }

    public XMLConfigBuilder(Reader reader, String environment) {
        this(reader, environment, null);
    }

    public XMLConfigBuilder(Reader reader, String environment, Properties props) {
        this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    public XMLConfigBuilder(InputStream inputStream) {
        this(inputStream, null, null);
    }

    public XMLConfigBuilder(InputStream inputStream, String environment) {
        this(inputStream, environment, null);
    }

    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        super(new Configuration());
        ErrorContext.instance().resource("SQL Mapper Configuration");
        this.configuration.setVariables(props);
        this.parsed = false;
        this.defaultEnvironment = environment;
        this.parser = parser;
    }

    public Configuration parse() {
        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;
        parseConfiguration(parser.evalNode("/configuration"));
        ConfigurationRegistry.registerConfiguration(configuration.getApplication(), configuration);
        return configuration;
    }

    private void parseConfiguration(XNode root) {
        try {
            String application = root.getStringAttribute("application");
            configuration.setApplication(application);
            propertiesElement(root.evalNode("properties"));
            Properties settings = settingsAsProperties(root.evalNode("settings"));
            // read it after objectFactory and objectWrapperFactory issue #631
            environmentsElement(root.evalNode("environments"));
            /**
             * fixme 远程读取一系列文件
             */
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
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

    /**
     * fixme 增加缓存节点的处理
     */
    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            if (defaultEnvironment == null) {
                defaultEnvironment = context.getStringAttribute("default");
            }
            for (XNode envNode : context.getChildren()) {
                String envId = envNode.getStringAttribute("id");
                Environment.Builder envBuilder = Environment.builder().id(envId);
                dataSourcesElement(envNode.evalNodes("dataSource"), envBuilder);
                lettuceElement(envNode.evalNode("lettuce"), envBuilder);

                Environment environment = envBuilder.build();
                configuration.addEnvironment(environment);
                if (environment.getId().equals(defaultEnvironment)) {
                    configuration.setDefaultEnv(environment);
                }
            }
        }
    }

    private void dataSourcesElement(List<XNode> contexts, Environment.Builder envBuilder) throws Exception {
        for (XNode context : contexts) {
            dataSourceElement(context, envBuilder);
        }
    }

    private void dataSourceElement(XNode context, Environment.Builder envBuilder) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String dataSourceId = context.getStringAttribute("id");
        String type = context.getStringAttribute("type");
        Properties props = context.getChildrenAsProperties();
        DataSourceFactory factory = (DataSourceFactory) resolveClass(type).getDeclaredConstructor().newInstance();
        factory.setProperties(props);
        // eager construct may influence bootstrap speed
        envBuilder.dataSource(dataSourceId, factory.getDataSource());
    }

    private void lettuceElement(XNode context, Environment.Builder envBuilder) {
        String id = context.getStringAttribute("id");
        String type = context.getStringAttribute("type", "standalone");
        Properties props = context.getChildrenAsProperties();
        props.putAll(configuration.getVariables());
        LettuceWrapper lettuceWrapper = LettuceWrapper.builder().id(id).type(type).props(props).build();
        envBuilder.lettuceWrapper(lettuceWrapper);
    }

    private void typeHandlerElement(XNode parent) {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                String javaTypeName = child.getStringAttribute("javaType");
                String jdbcTypeName = child.getStringAttribute("jdbcType");
                String handlerTypeName = child.getStringAttribute("handler");
                Class<?> javaTypeClass = resolveClass(javaTypeName);
                JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
                Class<?> typeHandlerClass = resolveClass(handlerTypeName);
                if (javaTypeClass != null) {
                    if (jdbcType == null) {
                        typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
                    } else {
                        typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
                    }
                } else {
                    typeHandlerRegistry.register(typeHandlerClass);
                }
            }
        }
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
