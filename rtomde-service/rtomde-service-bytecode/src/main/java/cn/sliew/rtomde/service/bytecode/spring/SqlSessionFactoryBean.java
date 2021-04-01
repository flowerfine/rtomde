package cn.sliew.rtomde.service.bytecode.spring;

import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLApplicationBuilder;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLMetadataBuilder;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;
import cn.sliew.rtomde.platform.mybatis.io.Resources;
import cn.sliew.rtomde.platform.mybatis.io.VFS;
import cn.sliew.rtomde.platform.mybatis.reflection.factory.ObjectFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.wrapper.ObjectWrapperFactory;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriver;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactoryBuilder;
import cn.sliew.rtomde.platform.mybatis.type.TypeHandler;
import cn.sliew.rtomde.service.bytecode.logging.Logger;
import cn.sliew.rtomde.service.bytecode.logging.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasLength;
import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * todo 后续提供一个 {@code DataEngine}，现在先这样玩吧。
 */
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean, ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlSessionFactoryBean.class);

    private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory METADATA_READER_FACTORY = new CachingMetadataReaderFactory();

    private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();

    private SqlSessionFactory sqlSessionFactory;

    private Resource metadataLocation;

    private String environment;

    private Properties platformProperties;

    private MybatisPlatformOptions platform;

    private Resource[] applicationLocations;

    private boolean failFast;

    private TypeHandler<?>[] typeHandlers;

    private String typeHandlersPackage;

    @SuppressWarnings("rawtypes")
    private Class<? extends TypeHandler> defaultEnumTypeHandler;

    private Class<?>[] typeAliases;

    private String typeAliasesPackage;

    private LanguageDriver[] scriptingLanguageDrivers;

    private Class<? extends LanguageDriver> defaultScriptingLanguageDriver;

    private Class<? extends VFS> vfs;

    private ObjectFactory objectFactory;

    private ObjectWrapperFactory objectWrapperFactory;

    /**
     * Set the location of the {@code MybatisPlatformOptions} xml config file. A typical value is
     * "WEB-INF/mybatis-metadata.xml".
     *
     * @param metadataLocation a location the MyBatis Platform metadata xml config file
     */
    public void setMetadataLocation(Resource metadataLocation) {
        this.metadataLocation = metadataLocation;
    }

    /**
     * <b>NOTE:</b> This class <em>overrides</em> any {@code Environment} you have set in the MyBatis config file. This is
     * used only as a placeholder name. The default value is {@code SqlSessionFactoryBean.class.getSimpleName()}.
     *
     * @param environment the environment name
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Set optional properties to be passed into the Mybatis Platform metadata config, as alternative to a
     * {@code &lt;properties&gt;} tag in the metadata xml file. This will be used to resolve placeholders in the
     * config file.
     *
     * @param platformProperties optional properties for the Mybatis Platform
     */
    public void setPlatformProperties(Properties platformProperties) {
        this.platformProperties = platformProperties;
    }

    /**
     * Set a customized MyBatis Platform config.
     *
     * @param platform MyBatis Platform config.
     */
    public void setPlatform(MybatisPlatformOptions platform) {
        this.platform = platform;
    }

    /**
     * Set locations of MyBatis Platform application files that are going to be merged into the {@code MybatisApplicationOptions} config
     * at runtime.
     * <p>
     * This property being based on Spring's resource abstraction also allows for specifying resource patterns here: e.g.
     * "classpath*:application/*-application.xml".
     *
     * @param applicationLocations location of MyBatis Platform application files
     */
    public void setApplicationLocations(Resource... applicationLocations) {
        this.applicationLocations = applicationLocations;
    }

    /**
     * If true, a final check is done on Configuration to assure that all mapped statements are fully loaded and there is
     * no one still pending to resolve includes. Defaults to false.
     *
     * @param failFast enable failFast
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    /**
     * Set type handlers. They must be annotated with {@code MappedTypes} and optionally with {@code MappedJdbcTypes}
     *
     * @param typeHandlers Type handler list
     */
    public void setTypeHandlers(TypeHandler<?>... typeHandlers) {
        this.typeHandlers = typeHandlers;
    }

    /**
     * Packages to search for type handlers.
     *
     * <p>
     * Allow to specify a wildcard such as {@code com.example.*.typehandler}.
     *
     * @param typeHandlersPackage package to scan for type handlers
     */
    public void setTypeHandlersPackage(String typeHandlersPackage) {
        this.typeHandlersPackage = typeHandlersPackage;
    }

    /**
     * Set the default type handler class for enum.
     *
     * @param defaultEnumTypeHandler The default type handler class for enum
     * @since 2.0.5
     */
    public void setDefaultEnumTypeHandler(
            @SuppressWarnings("rawtypes") Class<? extends TypeHandler> defaultEnumTypeHandler) {
        this.defaultEnumTypeHandler = defaultEnumTypeHandler;
    }

    /**
     * List of type aliases to register. They can be annotated with {@code Alias}
     *
     * @param typeAliases Type aliases list
     */
    public void setTypeAliases(Class<?>... typeAliases) {
        this.typeAliases = typeAliases;
    }

    /**
     * Packages to search for type aliases.
     *
     * <p>
     * Allow to specify a wildcard such as {@code com.example.*.model}.
     *
     * @param typeAliasesPackage package to scan for domain objects
     */
    public void setTypeAliasesPackage(String typeAliasesPackage) {
        this.typeAliasesPackage = typeAliasesPackage;
    }

    /**
     * Set scripting language drivers.
     *
     * @param scriptingLanguageDrivers scripting language drivers
     */
    public void setScriptingLanguageDrivers(LanguageDriver... scriptingLanguageDrivers) {
        this.scriptingLanguageDrivers = scriptingLanguageDrivers;
    }

    /**
     * Set a default scripting language driver class.
     *
     * @param defaultScriptingLanguageDriver A default scripting language driver class
     */
    public void setDefaultScriptingLanguageDriver(Class<? extends LanguageDriver> defaultScriptingLanguageDriver) {
        this.defaultScriptingLanguageDriver = defaultScriptingLanguageDriver;
    }

    /**
     * Sets the VFS.
     *
     * @param vfs a VFS
     */
    public void setVfs(Class<? extends VFS> vfs) {
        this.vfs = vfs;
    }

    /**
     * Gets the VFS.
     *
     * @return a specified VFS
     */
    public Class<? extends VFS> getVfs() {
        return this.vfs;
    }

    /**
     * Sets the ObjectFactory.
     *
     * @param objectFactory a custom ObjectFactory
     */
    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    /**
     * Sets the ObjectWrapperFactory.
     *
     * @param objectWrapperFactory a specified ObjectWrapperFactory
     */
    public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory) {
        this.objectWrapperFactory = objectWrapperFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(sqlSessionFactoryBuilder, "Property 'sqlSessionFactoryBuilder' is required");
        state((platform == null && metadataLocation == null) || !(platform != null && metadataLocation != null),
                "Property 'platform' and 'metadataLocation' can not specified with together");

        this.sqlSessionFactory = buildSqlSessionFactory();
    }

    protected SqlSessionFactory buildSqlSessionFactory() throws Exception {
        final MybatisPlatformOptions targetPlatform;
        XMLMetadataBuilder xmlMetadataBuilder = null;
        if (this.platform != null) {
            targetPlatform = this.platform;
            if (targetPlatform.getVariables() == null || targetPlatform.getVariables().isEmpty()) {
                targetPlatform.setVariables(this.platformProperties);
            } else if (this.platformProperties != null) {
                targetPlatform.getVariables().putAll(this.platformProperties);
            }
        } else if (this.metadataLocation != null) {
            xmlMetadataBuilder = new XMLMetadataBuilder(this.metadataLocation.getInputStream(), environment, this.platformProperties);
            targetPlatform = xmlMetadataBuilder.parse();
        } else {
            LOGGER.debug(
                    () -> "Property 'platform' or 'metadataLocation' not specified, using default MyBatis Platform metadata config");
            targetPlatform = new MybatisPlatformOptions();
            Optional.ofNullable(this.platformProperties).ifPresent(targetPlatform::setVariables);
        }

        if (environment != null) {
            targetPlatform.setEnvironment(environment);
        }

        if (!isEmpty(this.typeHandlers)) {
            Stream.of(this.typeHandlers).forEach(typeHandler -> {
                targetPlatform.getTypeHandlerRegistry().register(typeHandler);
                LOGGER.debug(() -> "Registered type handler: '" + typeHandler + "'");
            });
        }

        if (hasLength(this.typeHandlersPackage)) {
            scanClasses(this.typeHandlersPackage, TypeHandler.class).stream().filter(clazz -> !clazz.isAnonymousClass())
                    .filter(clazz -> !clazz.isInterface()).filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                    .forEach(targetPlatform.getTypeHandlerRegistry()::register);
        }

        if (defaultEnumTypeHandler != null) {
            targetPlatform.setDefaultEnumTypeHandler(defaultEnumTypeHandler);
        }

        if (!isEmpty(this.typeAliases)) {
            Stream.of(this.typeAliases).forEach(typeAlias -> {
                targetPlatform.getTypeAliasRegistry().registerAlias(typeAlias);
                LOGGER.debug(() -> "Registered type alias: '" + typeAlias + "'");
            });
        }

        if (hasLength(this.typeAliasesPackage)) {
            scanClasses(this.typeAliasesPackage, null).stream().filter(clazz -> !clazz.isAnonymousClass())
                    .filter(clazz -> !clazz.isInterface()).filter(clazz -> !clazz.isMemberClass())
                    .forEach(targetPlatform.getTypeAliasRegistry()::registerAlias);
        }

        if (!isEmpty(this.scriptingLanguageDrivers)) {
            Stream.of(this.scriptingLanguageDrivers).forEach(languageDriver -> {
                targetPlatform.getLanguageRegistry().register(languageDriver);
                LOGGER.debug(() -> "Registered scripting language driver: '" + languageDriver + "'");
            });
        }

        Optional.ofNullable(this.defaultScriptingLanguageDriver).ifPresent(targetPlatform::setDefaultScriptingLanguage);
        Optional.ofNullable(this.vfs).ifPresent(targetPlatform::setVfsImpl);
        Optional.ofNullable(this.objectFactory).ifPresent(targetPlatform::setObjectFactory);
        Optional.ofNullable(this.objectWrapperFactory).ifPresent(targetPlatform::setObjectWrapperFactory);

        if (this.applicationLocations != null) {
            if (this.applicationLocations.length == 0) {
                LOGGER.warn(() -> "Property 'applicationLocations' was specified but matching resources are not found.");
            } else {
                for (Resource applicationLocation : this.applicationLocations) {
                    if (applicationLocation == null) {
                        continue;
                    }
                    try {
                        XMLApplicationBuilder xmlApplicationBuilder = new XMLApplicationBuilder(applicationLocation.getInputStream(), targetPlatform);
                        xmlApplicationBuilder.parse();
                    } catch (Exception e) {
                        throw new NestedIOException("Failed to parse Mybatis Platform application resource: '" + applicationLocation + "'", e);
                    } finally {
                        ErrorContext.instance().reset();
                    }
                    LOGGER.debug(() -> "Parsed Mybatis Platform application file: '" + applicationLocation + "'");
                }
            }
        } else {
            LOGGER.debug(() -> "Property 'applicationLocation' was not specified.");
        }

        return this.sqlSessionFactoryBuilder.build(targetPlatform);
    }

    @Override
    public SqlSessionFactory getObject() throws Exception {
        if (this.sqlSessionFactory == null) {
            afterPropertiesSet();
        }
        return this.sqlSessionFactory;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Class<? extends SqlSessionFactory> getObjectType() {
        return this.sqlSessionFactory == null ? SqlSessionFactory.class : this.sqlSessionFactory.getClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (failFast && event instanceof ContextRefreshedEvent) {
            // fail-fast -> check all statements are completed
            this.sqlSessionFactory.getPlatform().getAllApplicationOptions().forEach(MybatisApplicationOptions::getMappedStatementNames);
        }
    }

    private Set<Class<?>> scanClasses(String packagePatterns, Class<?> assignableType) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        String[] packagePatternArray = tokenizeToStringArray(packagePatterns,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        for (String packagePattern : packagePatternArray) {
            Resource[] resources = RESOURCE_PATTERN_RESOLVER.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(packagePattern) + "/**/*.class");
            for (Resource resource : resources) {
                try {
                    ClassMetadata classMetadata = METADATA_READER_FACTORY.getMetadataReader(resource).getClassMetadata();
                    Class<?> clazz = Resources.classForName(classMetadata.getClassName());
                    if (assignableType == null || assignableType.isAssignableFrom(clazz)) {
                        classes.add(clazz);
                    }
                } catch (Throwable e) {
                    LOGGER.warn(() -> "Cannot load the '" + resource + "'. Cause by " + e.toString());
                }
            }
        }
        return classes;
    }
}
