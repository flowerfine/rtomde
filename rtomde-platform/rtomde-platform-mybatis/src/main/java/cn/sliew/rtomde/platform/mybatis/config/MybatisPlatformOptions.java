package cn.sliew.rtomde.platform.mybatis.config;

import cn.sliew.milky.cache.Cache;
import cn.sliew.milky.common.log.Logger;
import cn.sliew.milky.common.log.Slf4JLogger;
import cn.sliew.rtomde.config.PlatformOptions;
import cn.sliew.rtomde.platform.mybatis.builder.ResultMapResolver;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLStatementBuilder;
import cn.sliew.rtomde.platform.mybatis.executor.loader.ProxyFactory;
import cn.sliew.rtomde.platform.mybatis.executor.loader.javassist.JavassistProxyFactory;
import cn.sliew.rtomde.platform.mybatis.io.VFS;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.mapping.ParameterMap;
import cn.sliew.rtomde.platform.mybatis.mapping.ResultMap;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
import cn.sliew.rtomde.platform.mybatis.plugin.Interceptor;
import cn.sliew.rtomde.platform.mybatis.plugin.InterceptorChain;
import cn.sliew.rtomde.platform.mybatis.reflection.DefaultReflectorFactory;
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

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * todo 更换log实现
 */
public class MybatisPlatformOptions extends PlatformOptions {

    private static final long serialVersionUID = -2816717900936922789L;

    private final String environment;

    protected Properties variables = new Properties();

    protected Class<? extends VFS> vfsImpl;

    private Map<String, String> settings;

    private final TypeHandlerRegistry typeHandlerRegistry;

    private final TypeAliasRegistry typeAliasRegistry;

    private final ObjectFactory objectFactory = new DefaultObjectFactory();

    private final ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    private final ReflectorFactory reflectorFactory;

    protected final InterceptorChain interceptorChain = new InterceptorChain();

    protected ProxyFactory proxyFactory = new JavassistProxyFactory();

    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    /**
     * dataSourceId -> DataSource
     */
    private final ConcurrentMap<String, DataSource> dataSourceRegistry = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<String, DatasourceOptions> datasourceOptionsRegistry = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<String, LettuceOptions> lettuceOptionsRegistry = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<String, MybatisCacheOptions> cacheOptionsRegistry = new ConcurrentHashMap<>(2);

    protected boolean useColumnLabel = true;

    protected String logPrefix;
    protected Class<? extends Logger> logImpl = Slf4JLogger.class;
    protected Class<? extends VFS> vfsImpl;

    protected JdbcType jdbcTypeForNull = JdbcType.OTHER;
    protected Integer defaultStatementTimeout;
    protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;
    protected AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior = AutoMappingUnknownColumnBehavior.NONE;

    protected Properties variables = new Properties();
    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected boolean lazyLoadingEnabled = false;
    protected ProxyFactory proxyFactory = new JavassistProxyFactory(); // #224 Using internal Javassist instead of OGNL

    protected final InterceptorChain interceptorChain = new InterceptorChain();
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry(this);
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();
    protected final Map<String, Cache> caches = new HashMap<>();
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();
    protected final Map<String, ParameterMap> parameterMaps = new HashMap<>();

    protected final Set<String> loadedResources = new HashSet<>();
    protected final Map<String, XNode> sqlFragments = new HashMap<>();

    protected final Collection<XMLStatementBuilder> incompleteStatements = new LinkedList<>();
    protected final Collection<ResultMapResolver> incompleteResultMaps = new LinkedList<>();


    public MybatisPlatformOptions(TypeHandlerRegistry typeHandlerRegistry, TypeAliasRegistry typeAliasRegistry, ReflectorFactory reflectorFactory, Properties props) {
        this.environment = System.getenv("ENV");
        this.typeHandlerRegistry = typeHandlerRegistry;
        this.typeAliasRegistry = typeAliasRegistry;
        this.reflectorFactory = reflectorFactory;
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

    public Map<String, String> getSettings() {
        return settings;
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

    public Configuration toMybatisConfiguration() {
        return null;
    }

    public boolean isUseColumnLabel() {
        return useColumnLabel;
    }

    public void setUseColumnLabel(boolean useColumnLabel) {
        this.useColumnLabel = useColumnLabel;
    }

}
