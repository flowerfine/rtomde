package cn.sliew.rtomde.platform.mybatis.config;

import cn.sliew.milky.cache.Cache;
import cn.sliew.milky.common.log.Logger;
import cn.sliew.milky.common.log.Slf4JLogger;
import cn.sliew.rtomde.config.ApplicationOptions;
import cn.sliew.rtomde.config.ConfigOptions;
import cn.sliew.rtomde.platform.mybatis.builder.ResultMapResolver;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLStatementBuilder;
import cn.sliew.rtomde.platform.mybatis.executor.loader.ProxyFactory;
import cn.sliew.rtomde.platform.mybatis.executor.loader.javassist.JavassistProxyFactory;
import cn.sliew.rtomde.platform.mybatis.io.VFS;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.mapping.ParameterMap;
import cn.sliew.rtomde.platform.mybatis.mapping.ResultMap;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
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
 * 后面直接接配置中心apollo后就不需要在自定义数据源的配置，
 * 直接可以走配置中心{@link ConfigOptions}，由配置中心
 * 拉取推送数据库配置
 */
public class MybatisApplicationOptions extends ApplicationOptions {

    private static final long serialVersionUID = 29825088458293280L;

    /**
     * application datasource
     */
    private DatasourceOptions datasource;

    /**
     * application lettuce
     */
    private LettuceOptions lettuce;

    /**
     * dataSourceId -> DataSource
     */
    private final ConcurrentMap<String, DataSource> dataSourceRegistry = new ConcurrentHashMap<>(2);

    private final ConcurrentMap<String, DatasourceOptions> datasourceOptionsRegistry = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<String, LettuceOptions> lettuceOptionsRegistry = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<String, MybatisCacheOptions> cacheOptionsRegistry = new ConcurrentHashMap<>(2);

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


    public DatasourceOptions getDatasource() {
        return datasource;
    }

    public void setDatasource(DatasourceOptions datasource) {
        this.datasource = datasource;
    }

    public LettuceOptions getLettuce() {
        return lettuce;
    }

    public void setLettuce(LettuceOptions lettuce) {
        this.lettuce = lettuce;
    }

    public Map<String, XNode> getSqlFragments() {
        return sqlFragments;
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }
}
