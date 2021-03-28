package cn.sliew.rtomde.platform.mybatis.config;

import cn.sliew.rtomde.config.ApplicationOptions;
import cn.sliew.rtomde.config.ConfigOptions;
import cn.sliew.rtomde.platform.mybatis.builder.ResultMapResolver;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLStatementBuilder;
import cn.sliew.rtomde.platform.mybatis.parsing.XNode;
import cn.sliew.rtomde.platform.mybatis.type.TypeAliasRegistry;

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
     * application properties
     */
    protected Properties props = new Properties();

    /**
     * 支持应用级的类型别名
     */
    protected TypeAliasRegistry typeAliasRegistry;

    /**
     * dataSourceId -> DataSource
     */
    private final ConcurrentMap<String, DataSource> dataSourceRegistry = new ConcurrentHashMap<>(2);

    private final ConcurrentMap<String, DatasourceOptions> datasourceOptionsRegistry = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<String, LettuceOptions> lettuceOptionsRegistry = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<String, MybatisCacheOptions> cacheOptionsRegistry = new ConcurrentHashMap<>(2);

    protected final Map<String, XNode> sqlFragments = new HashMap<>();
    protected final Set<String> loadedResources = new HashSet<>();

    protected final Collection<XMLStatementBuilder> incompleteStatements = new LinkedList<>();
    protected final Collection<ResultMapResolver> incompleteResultMaps = new LinkedList<>();

    public MybatisApplicationOptions(MybatisPlatformOptions platform) {
        this.platform = platform;
        this.typeAliasRegistry = new TypeAliasRegistry(platform.getTypeAliasRegistry());
        this.props.putAll(platform.getVariables());
    }

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

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
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

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public void setTypeAliasRegistry(TypeAliasRegistry typeAliasRegistry) {
        this.typeAliasRegistry = typeAliasRegistry;
    }

    public void addDatasourceOptions(DatasourceOptions datasource) {
        this.datasourceOptionsRegistry.put(datasource.getId(), datasource);
    }

    public void addLettuceOptions(LettuceOptions lettuce) {
        this.lettuceOptionsRegistry.put(lettuce.getId(), lettuce);
    }

    public Collection<XMLStatementBuilder> getIncompleteStatements() {
        return incompleteStatements;
    }

    public void addIncompleteStatement(XMLStatementBuilder incompleteStatement) {
        incompleteStatements.add(incompleteStatement);
    }

    public Collection<ResultMapResolver> getIncompleteResultMaps() {
        return incompleteResultMaps;
    }

    public void addIncompleteResultMap(ResultMapResolver resultMapResolver) {
        incompleteResultMaps.add(resultMapResolver);
    }
}
