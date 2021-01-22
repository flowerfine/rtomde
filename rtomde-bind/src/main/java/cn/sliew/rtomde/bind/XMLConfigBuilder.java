package cn.sliew.rtomde.bind;

import cn.sliew.rtomde.common.reflect.DefaultReflectorFactory;
import cn.sliew.rtomde.common.reflect.MetaClass;
import cn.sliew.rtomde.common.reflect.ReflectorFactory;
import cn.sliew.rtomde.common.resource.Resources;
import cn.sliew.rtomde.common.xml.ParseException;
import cn.sliew.rtomde.common.xml.XNode;
import cn.sliew.rtomde.common.xml.XPathParser;
import cn.sliew.rtomde.datasource.DataSourceFactory;
import cn.sliew.rtomde.datasource.DataSource;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class XMLConfigBuilder {

    private String application;
    private final Configuration configuration;
    private AtomicBoolean parsed;
    private final XPathParser parser;
    private String environment;

    private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        this.configuration = new Configuration();
        this.configuration.setVariables(props);
        this.parsed = new AtomicBoolean(false);
        this.environment = environment;
        this.parser = parser;
    }

    public Configuration parse() {
        if (parsed.get()) {
            throw new ParseException("Each XMLConfigBuilder can only be used once.");
        }
        if (parsed.compareAndSet(false, true)) {
            parseConfiguration(parser.evalNode("/configuration"));
        }
        return configuration;
    }

    private void parseConfiguration(XNode root) {
        try {
            String application = root.getStringAttribute("application");
            if (application == null || application.isEmpty()) {
                throw new ParseException("Config's application cannot be empty");
            }
            this.application = application;
            // issue #117 read properties first
            propertiesElement(root.evalNode("properties"));
            Properties settings = settingsAsProperties(root.evalNode("settings"));
            settingsElement(settings);
            environmentsElement(root.evalNode("environments"));
            databaseIdProviderElement(root.evalNode("databaseIdProvider"));
            typeHandlerElement(root.evalNode("typeHandlers"));
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    /**
     * todo 属性的覆盖问题
     */
    private void propertiesElement(XNode context) throws Exception {
        if (context != null) {
            Properties defaults = context.getChildrenAsProperties();
            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url");
            if (resource != null && url != null) {
                throw new ParseException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
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

    private Properties settingsAsProperties(XNode context) {
        if (context == null) {
            return new Properties();
        }
        Properties props = context.getChildrenAsProperties();
        // Check that all settings are known to the configuration class
        MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
        for (Object key : props.keySet()) {
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new ParseException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
            }
        }
        return props;
    }

    private void settingsElement(Properties props) {
//        configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
//        configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));
//        configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
//        configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
//        configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
//        configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
//        configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
//        configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
    }

    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            if (environment == null) {
                environment = context.getStringAttribute("default");
            }
            for (XNode child : context.getChildren()) {
                String id = child.getStringAttribute("id");
                if (isSpecifiedEnvironment(id)) {
                    DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                    DataSource dataSource = dsFactory.getDataSource(null);
                    Environment environment = Environment.builder().application(application).id(id).dataSource(dataSource).build();
                    configuration.setEnvironment(environment);
                }
            }
        }
    }

    private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            cn.sliew.rtomde.datasource.DataSourceFactory
            return null;
        }
        throw new ParseException("Environment declaration requires a DataSourceFactory.");
    }

    private boolean isSpecifiedEnvironment(String id) {
        if (environment == null) {
            throw new ParseException("No environment specified.");
        } else if (id == null) {
            throw new ParseException("Environment requires an id attribute.");
        } else if (environment.equals(id)) {
            return true;
        }
        return false;
    }

}