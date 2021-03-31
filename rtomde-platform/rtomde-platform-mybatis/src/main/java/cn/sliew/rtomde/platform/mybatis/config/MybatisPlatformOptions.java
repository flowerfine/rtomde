package cn.sliew.rtomde.platform.mybatis.config;

import cn.sliew.milky.log.Log4J2LoggerFactory;
import cn.sliew.milky.log.LoggerFactory;
import cn.sliew.rtomde.common.utils.StringUtils;
import cn.sliew.rtomde.config.PlatformOptions;
import cn.sliew.rtomde.platform.mybatis.builder.BuilderException;
import cn.sliew.rtomde.platform.mybatis.executor.Executor;
import cn.sliew.rtomde.platform.mybatis.executor.parameter.ParameterHandler;
import cn.sliew.rtomde.platform.mybatis.executor.resultset.DefaultResultSetHandler;
import cn.sliew.rtomde.platform.mybatis.executor.resultset.ResultSetHandler;
import cn.sliew.rtomde.platform.mybatis.executor.statement.PreparedStatementHandler;
import cn.sliew.rtomde.platform.mybatis.executor.statement.StatementHandler;
import cn.sliew.rtomde.platform.mybatis.io.VFS;
import cn.sliew.rtomde.platform.mybatis.mapping.BoundSql;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.reflection.DefaultReflectorFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.MetaObject;
import cn.sliew.rtomde.platform.mybatis.reflection.ReflectorFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.factory.DefaultObjectFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.factory.ObjectFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.wrapper.ObjectWrapperFactory;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriver;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriverRegistry;
import cn.sliew.rtomde.platform.mybatis.scripting.defaults.RawLanguageDriver;
import cn.sliew.rtomde.platform.mybatis.scripting.xmltags.XMLLanguageDriver;
import cn.sliew.rtomde.platform.mybatis.session.AutoMappingBehavior;
import cn.sliew.rtomde.platform.mybatis.session.AutoMappingUnknownColumnBehavior;
import cn.sliew.rtomde.platform.mybatis.session.ResultHandler;
import cn.sliew.rtomde.platform.mybatis.session.RowBounds;
import cn.sliew.rtomde.platform.mybatis.type.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class MybatisPlatformOptions extends PlatformOptions {

    private static final long serialVersionUID = -2816717900936922789L;

    private final String environment;
    private Properties settings;

    protected boolean safeRowBoundsEnabled;
    protected boolean safeResultHandlerEnabled = true;
    protected boolean aggressiveLazyLoading;
    protected boolean multipleResultSetsEnabled = true;
    protected boolean useColumnLabel = true;
    protected boolean callSettersOnNulls;
    protected boolean useActualParamName = true;
    protected boolean returnInstanceForEmptyRow;
    protected boolean shrinkWhitespacesInSql;

    protected Class<? extends VFS> vfsImpl;
    protected JdbcType jdbcTypeForNull = JdbcType.OTHER;
    protected Set<String> lazyLoadTriggerMethods = new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString"));

    protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;
    protected AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior = AutoMappingUnknownColumnBehavior.NONE;

    protected Properties variables = new Properties();
    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry(this);
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    public MybatisPlatformOptions() {
        this(null, new Properties());
    }

    public MybatisPlatformOptions(String environment) {
        this(environment, new Properties());
    }

    public MybatisPlatformOptions(Properties props) {
        this(null, props);
    }

    public MybatisPlatformOptions(String environment, Properties props) {
        LoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);

        if (StringUtils.isEmpty(environment)) {
            environment = System.getenv("ENV");
            if (StringUtils.isEmpty(environment)) {
                environment = "dev";
            }
        }
        this.environment = environment;
        this.variables = props;

        typeAliasRegistry.registerAlias("XML", XMLLanguageDriver.class);
        typeAliasRegistry.registerAlias("RAW", RawLanguageDriver.class);

        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
        languageRegistry.register(RawLanguageDriver.class);
    }

    public void settings(Properties props) {
        this.settings = props;
        this.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
        this.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
        this.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
        this.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
        this.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
        this.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
        this.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
        this.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
        this.setShrinkWhitespacesInSql(booleanValueOf(props.getProperty("shrinkWhitespacesInSql"), false));

        this.setVfsImpl(resolveClass(props.getProperty("vfsImpl")));
        this.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
        this.setLazyLoadTriggerMethods(stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));

        this.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
        this.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));

//        this.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
        this.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));
        this.setDefaultEnumTypeHandler(resolveClass(props.getProperty("defaultEnumTypeHandler")));
    }

    public String getEnvironment() {
        return environment;
    }

    public boolean isSafeRowBoundsEnabled() {
        return safeRowBoundsEnabled;
    }

    public void setSafeRowBoundsEnabled(boolean safeRowBoundsEnabled) {
        this.safeRowBoundsEnabled = safeRowBoundsEnabled;
    }

    public boolean isSafeResultHandlerEnabled() {
        return safeResultHandlerEnabled;
    }

    public void setSafeResultHandlerEnabled(boolean safeResultHandlerEnabled) {
        this.safeResultHandlerEnabled = safeResultHandlerEnabled;
    }

    public boolean isAggressiveLazyLoading() {
        return aggressiveLazyLoading;
    }

    public void setAggressiveLazyLoading(boolean aggressiveLazyLoading) {
        this.aggressiveLazyLoading = aggressiveLazyLoading;
    }

    public boolean isMultipleResultSetsEnabled() {
        return multipleResultSetsEnabled;
    }

    public void setMultipleResultSetsEnabled(boolean multipleResultSetsEnabled) {
        this.multipleResultSetsEnabled = multipleResultSetsEnabled;
    }

    public boolean isUseColumnLabel() {
        return useColumnLabel;
    }

    public void setUseColumnLabel(boolean useColumnLabel) {
        this.useColumnLabel = useColumnLabel;
    }

    public boolean isCallSettersOnNulls() {
        return callSettersOnNulls;
    }

    public void setCallSettersOnNulls(boolean callSettersOnNulls) {
        this.callSettersOnNulls = callSettersOnNulls;
    }

    public boolean isUseActualParamName() {
        return useActualParamName;
    }

    public void setUseActualParamName(boolean useActualParamName) {
        this.useActualParamName = useActualParamName;
    }

    public boolean isReturnInstanceForEmptyRow() {
        return returnInstanceForEmptyRow;
    }

    public void setReturnInstanceForEmptyRow(boolean returnEmptyInstance) {
        this.returnInstanceForEmptyRow = returnEmptyInstance;
    }

    public boolean isShrinkWhitespacesInSql() {
        return shrinkWhitespacesInSql;
    }

    public void setShrinkWhitespacesInSql(boolean shrinkWhitespacesInSql) {
        this.shrinkWhitespacesInSql = shrinkWhitespacesInSql;
    }

    public void setVfsImpl(Class<? extends VFS> vfsImpl) {
        if (vfsImpl != null) {
            this.vfsImpl = vfsImpl;
            VFS.addImplClass(this.vfsImpl);
        }
    }

    public Class<? extends VFS> getVfsImpl() {
        return this.vfsImpl;
    }

    public void setJdbcTypeForNull(JdbcType jdbcTypeForNull) {
        this.jdbcTypeForNull = jdbcTypeForNull;
    }

    public JdbcType getJdbcTypeForNull() {
        return jdbcTypeForNull;
    }

    public void setLazyLoadTriggerMethods(Set<String> lazyLoadTriggerMethods) {
        this.lazyLoadTriggerMethods = lazyLoadTriggerMethods;
    }

    public Set<String> getLazyLoadTriggerMethods() {
        return lazyLoadTriggerMethods;
    }

    public void setAutoMappingBehavior(AutoMappingBehavior autoMappingBehavior) {
        this.autoMappingBehavior = autoMappingBehavior;
    }

    public AutoMappingBehavior getAutoMappingBehavior() {
        return autoMappingBehavior;
    }

    /**
     * Sets the auto mapping unknown column behavior.
     *
     * @param autoMappingUnknownColumnBehavior the new auto mapping unknown column behavior
     * @since 3.4.0
     */
    public void setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior) {
        this.autoMappingUnknownColumnBehavior = autoMappingUnknownColumnBehavior;
    }

    /**
     * Gets the auto mapping unknown column behavior.
     *
     * @return the auto mapping unknown column behavior
     * @since 3.4.0
     */
    public AutoMappingUnknownColumnBehavior getAutoMappingUnknownColumnBehavior() {
        return autoMappingUnknownColumnBehavior;
    }

    /**
     * Set a default {@link TypeHandler} class for {@link Enum}.
     * A default {@link TypeHandler} is {@link EnumTypeHandler}.
     *
     * @param typeHandler a type handler class for {@link Enum}
     * @since 3.4.5
     */
    public void setDefaultEnumTypeHandler(Class<? extends TypeHandler> typeHandler) {
        if (typeHandler != null) {
            getTypeHandlerRegistry().setDefaultEnumTypeHandler(typeHandler);
        }
    }

    public void setVariables(Properties variables) {
        this.variables = variables;
    }

    public Properties getVariables() {
        return variables;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public ReflectorFactory getReflectorFactory() {
        return reflectorFactory;
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }

    public void setDefaultScriptingLanguage(Class<? extends LanguageDriver> driver) {
        if (driver == null) {
            driver = XMLLanguageDriver.class;
        }
        getLanguageRegistry().setDefaultDriverClass(driver);
    }

    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }

    /**
     * Gets the language driver.
     *
     * @return the language driver
     */
    public LanguageDriver getLanguageDriver() {
        return languageRegistry.getDefaultDriver();
    }

    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    public ResultSetHandler newResultSetHandler(MappedStatement mappedStatement, RowBounds rowBounds, ResultHandler resultHandler) {
        return new DefaultResultSetHandler(mappedStatement, resultHandler, rowBounds);
    }

    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        return new PreparedStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    }

    private Boolean booleanValueOf(String value, Boolean defaultValue) {
        return value == null ? defaultValue : Boolean.valueOf(value);
    }

    private Integer integerValueOf(String value, Integer defaultValue) {
        return value == null ? defaultValue : Integer.valueOf(value);
    }

    private Set<String> stringSetValueOf(String value, String defaultValue) {
        value = value == null ? defaultValue : value;
        return new HashSet<>(Arrays.asList(value.split(",")));
    }

    private <T> Class<? extends T> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new BuilderException("Error resolving class. Cause: " + e, e);
        }
    }

    private <T> Class<? extends T> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }

}
