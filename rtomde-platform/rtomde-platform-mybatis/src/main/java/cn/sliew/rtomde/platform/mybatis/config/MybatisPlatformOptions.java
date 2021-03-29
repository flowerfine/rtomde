package cn.sliew.rtomde.platform.mybatis.config;

import cn.sliew.milky.common.log.Logger;
import cn.sliew.milky.common.log.Slf4JLogger;
import cn.sliew.rtomde.config.PlatformOptions;
import cn.sliew.rtomde.platform.mybatis.executor.loader.ProxyFactory;
import cn.sliew.rtomde.platform.mybatis.executor.loader.javassist.JavassistProxyFactory;
import cn.sliew.rtomde.platform.mybatis.io.VFS;
import cn.sliew.rtomde.platform.mybatis.plugin.Interceptor;
import cn.sliew.rtomde.platform.mybatis.plugin.InterceptorChain;
import cn.sliew.rtomde.platform.mybatis.reflection.DefaultReflectorFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.MetaObject;
import cn.sliew.rtomde.platform.mybatis.reflection.ReflectorFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.factory.DefaultObjectFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.factory.ObjectFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.wrapper.ObjectWrapperFactory;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriverRegistry;
import cn.sliew.rtomde.platform.mybatis.session.AutoMappingBehavior;
import cn.sliew.rtomde.platform.mybatis.session.AutoMappingUnknownColumnBehavior;
import cn.sliew.rtomde.platform.mybatis.type.JdbcType;
import cn.sliew.rtomde.platform.mybatis.type.TypeAliasRegistry;
import cn.sliew.rtomde.platform.mybatis.type.TypeHandlerRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * todo 更换log实现
 */
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

    protected String logPrefix;
    protected Class<? extends Logger> logImpl = Slf4JLogger.class;
    protected Class<? extends VFS> vfsImpl;
    protected Class<?> defaultSqlProviderType;
    protected JdbcType jdbcTypeForNull = JdbcType.OTHER;
    protected Set<String> lazyLoadTriggerMethods = new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString"));

    protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;
    protected AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior = AutoMappingUnknownColumnBehavior.NONE;

    protected Properties variables = new Properties();
    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected boolean lazyLoadingEnabled = false;
    protected ProxyFactory proxyFactory = new JavassistProxyFactory(); // #224 Using internal Javassist instead of OGNL

    /**
     * Configuration factory class.
     * Used to create Configuration for loading deserialized unread properties.
     *
     * @see <a href='https://github.com/mybatis/old-google-code-issues/issues/300'>Issue 300 (google code)</a>
     */
    protected Class<?> configurationFactory;

    protected final InterceptorChain interceptorChain = new InterceptorChain();
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry(this);
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();


    public MybatisPlatformOptions(Properties props) {
        this.environment = System.getenv("ENV");
        this.variables = props;
    }

    public Class<? extends VFS> getVfsImpl() {
        return this.vfsImpl;
    }

    public void setVfsImpl(Class<? extends VFS> vfsImpl) {
        if (vfsImpl != null) {
            this.vfsImpl = vfsImpl;
            VFS.addImplClass(this.vfsImpl);
        }
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }

    public String getEnvironment() {
        return environment;
    }

    public void setVariables(Properties variables) {
        this.variables = variables;
    }

    public Properties getVariables() {
        return variables;
    }

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    public ReflectorFactory getReflectorFactory() {
        return reflectorFactory;
    }

    public InterceptorChain getInterceptorChain() {
        return interceptorChain;
    }

    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }

    public boolean isUseColumnLabel() {
        return useColumnLabel;
    }

    public void setUseColumnLabel(boolean useColumnLabel) {
        this.useColumnLabel = useColumnLabel;
    }

    public Properties getSettings() {
        return settings;
    }

    public void setSettings(Properties settings) {
        this.settings = settings;
    }

    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    public boolean isShrinkWhitespacesInSql() {
        return shrinkWhitespacesInSql;
    }

    public void setShrinkWhitespacesInSql(boolean shrinkWhitespacesInSql) {
        this.shrinkWhitespacesInSql = shrinkWhitespacesInSql;
    }
}
