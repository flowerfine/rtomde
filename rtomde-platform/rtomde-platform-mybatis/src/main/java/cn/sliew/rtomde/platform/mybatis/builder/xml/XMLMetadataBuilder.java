package cn.sliew.rtomde.platform.mybatis.builder.xml;

import cn.sliew.rtomde.platform.mybatis.builder.BuilderException;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.io.Resources;
import cn.sliew.rtomde.platform.mybatis.io.VFS;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
import cn.sliew.rtomde.platform.mybatis.parsing.XPathParser;
import cn.sliew.rtomde.platform.mybatis.plugin.Interceptor;
import cn.sliew.rtomde.platform.mybatis.reflection.MetaClass;
import cn.sliew.rtomde.platform.mybatis.reflection.ReflectorFactory;
import cn.sliew.rtomde.platform.mybatis.type.JdbcType;
import cn.sliew.rtomde.platform.mybatis.type.TypeAliasRegistry;
import cn.sliew.rtomde.platform.mybatis.type.TypeHandlerRegistry;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

public class XMLMetadataBuilder {

    private boolean parsed;
    private final XPathParser parser;

    protected final TypeAliasRegistry typeAliasRegistry;
    protected final TypeHandlerRegistry typeHandlerRegistry;
    private final ReflectorFactory reflectorFactory;
    private final MybatisPlatformOptions platform;

    public XMLMetadataBuilder(Reader reader) {
        this(reader, null);
    }

    public XMLMetadataBuilder(Reader reader, Properties props) {
        this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), props);
    }

    public XMLMetadataBuilder(InputStream inputStream) {
        this(inputStream, null);
    }

    public XMLMetadataBuilder(InputStream inputStream, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), props);
    }

    private XMLMetadataBuilder(XPathParser parser, Properties props) {
        ErrorContext.instance().resource("Mybatis Platform Options");
        this.parsed = false;
        this.parser = parser;
        this.platform = new MybatisPlatformOptions(props);
        this.typeAliasRegistry = platform.getTypeAliasRegistry();
        this.typeHandlerRegistry = platform.getTypeHandlerRegistry();
        this.reflectorFactory = platform.getReflectorFactory();
    }

    public MybatisPlatformOptions parse() {
        if (parsed) {
            throw new BuilderException("Each XMLMetadataBuilder can only be used once.");
        }
        parsed = true;
        parseMetadata(parser.evalNode("/metadata"));
        return platform;
    }

    private void parseMetadata(XNode root) {
        try {
            String name = root.getStringAttribute("name");
            platform.setName(name);
            propertiesElement(root.evalNode("properties"));
            Properties settings = settingsAsProperties(root.evalNode("settings"));
            loadCustomVfs(settings);
            typeAliasesElement(root.evalNode("typeAliases"));
            // fixme page。如何处理分页问题，交由业务来处理？？？
            pluginElement(root.evalNode("plugins"));
            typeHandlerElement(root.evalNode("typeHandlers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    private Properties settingsAsProperties(XNode context) {
        if (context == null) {
            return new Properties();
        }
        Properties props = context.getChildrenAsProperties();
        // Check that all settings are known to the configuration class
        MetaClass metaConfig = MetaClass.forClass(MybatisPlatformOptions.class, reflectorFactory);
        for (Object key : props.keySet()) {
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new BuilderException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
            }
        }
        return props;
    }

    private void loadCustomVfs(Properties props) {
        Class<? extends VFS> vfsImpl = resolveClass(props.getProperty("vfsImpl"));
        platform.setVfsImpl(vfsImpl);
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

    private void pluginElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                String interceptor = child.getStringAttribute("interceptor");
                Properties properties = child.getChildrenAsProperties();
                Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).getDeclaredConstructor().newInstance();
                interceptorInstance.setProperties(properties);
                platform.addInterceptor(interceptorInstance);
            }
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
            Properties vars = platform.getVariables();
            if (vars != null) {
                defaults.putAll(vars);
            }
            parser.setVariables(defaults);
            platform.setVariables(defaults);
        }
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

    protected <T> Class<? extends T> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new BuilderException("Error resolving class. Cause: " + e, e);
        }
    }

    protected <T> Class<? extends T> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }

    protected JdbcType resolveJdbcType(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return JdbcType.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new BuilderException("Error resolving JdbcType. Cause: " + e, e);
        }
    }

}
