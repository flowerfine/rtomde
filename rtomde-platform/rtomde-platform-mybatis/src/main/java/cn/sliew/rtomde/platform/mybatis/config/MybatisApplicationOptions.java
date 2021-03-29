package cn.sliew.rtomde.platform.mybatis.config;

import cn.sliew.rtomde.config.ApplicationOptions;
import cn.sliew.rtomde.config.ConfigOptions;
import cn.sliew.rtomde.platform.mybatis.builder.IncompleteElementException;
import cn.sliew.rtomde.platform.mybatis.builder.ResultMapResolver;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLStatementBuilder;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.mapping.ParameterMap;
import cn.sliew.rtomde.platform.mybatis.mapping.ResultMap;
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
     * application properties
     */
    protected Properties props = new Properties();

    protected String logPrefix;

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

    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();
    protected final Map<String, ParameterMap> parameterMaps = new HashMap<>();

    protected final Collection<XMLStatementBuilder> incompleteStatements = new LinkedList<>();
    protected final Collection<ResultMapResolver> incompleteResultMaps = new LinkedList<>();

    public MybatisApplicationOptions(MybatisPlatformOptions platform) {
        this.platform = platform;
        this.typeAliasRegistry = new TypeAliasRegistry(platform.getTypeAliasRegistry());
        this.props.putAll(platform.getVariables());
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

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public Collection<String> getMappedStatementNames() {
        buildAllStatements();
        return mappedStatements.keySet();
    }

    public Collection<MappedStatement> getMappedStatements() {
        buildAllStatements();
        return mappedStatements.values();
    }

    /*
     * Parses all the unprocessed statement nodes in the cache. It is recommended
     * to call this method once all the mappers are added as it provides fail-fast
     * statement validation.
     */
    protected void buildAllStatements() {
        parsePendingResultMaps();
        if (!incompleteStatements.isEmpty()) {
            synchronized (incompleteStatements) {
                incompleteStatements.removeIf(x -> {
                    x.parseStatementNode();
                    return true;
                });
            }
        }
    }

    private void parsePendingResultMaps() {
        if (incompleteResultMaps.isEmpty()) {
            return;
        }
        synchronized (incompleteResultMaps) {
            boolean resolved;
            IncompleteElementException ex = null;
            do {
                resolved = false;
                Iterator<ResultMapResolver> iterator = incompleteResultMaps.iterator();
                while (iterator.hasNext()) {
                    try {
                        iterator.next().resolve();
                        iterator.remove();
                        resolved = true;
                    } catch (IncompleteElementException e) {
                        ex = e;
                    }
                }
            } while (resolved);
            if (!incompleteResultMaps.isEmpty() && ex != null) {
                // At least one result map is unresolvable.
                throw ex;
            }
        }
    }

    public void addResultMap(ResultMap rm) {
        resultMaps.put(rm.getId(), rm);
    }

    public Collection<String> getResultMapNames() {
        return resultMaps.keySet();
    }

    public Collection<ResultMap> getResultMaps() {
        return resultMaps.values();
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public boolean hasResultMap(String id) {
        return resultMaps.containsKey(id);
    }

    public void addParameterMap(ParameterMap pm) {
        parameterMaps.put(pm.getId(), pm);
    }

    public Collection<String> getParameterMapNames() {
        return parameterMaps.keySet();
    }

    public Collection<ParameterMap> getParameterMaps() {
        return parameterMaps.values();
    }

    public ParameterMap getParameterMap(String id) {
        return parameterMaps.get(id);
    }

    public boolean hasParameterMap(String id) {
        return parameterMaps.containsKey(id);
    }

    public void addDatasourceOptions(DatasourceOptions options) {
        this.datasourceOptionsRegistry.put(options.getId(), options);
    }

    public Collection<String> getDatasourceOptionsNames() {
        return this.datasourceOptionsRegistry.keySet();
    }

    public Collection<DatasourceOptions> getDatasourceOptionsMaps() {
        return this.datasourceOptionsRegistry.values();
    }

    public DatasourceOptions getDatasourceOptions(String id) {
        return this.datasourceOptionsRegistry.get(id);
    }

    public boolean hasDatasourceOptions(String id) {
        return this.datasourceOptionsRegistry.containsKey(id);
    }

    public void addLettuceOptions(LettuceOptions options) {
        this.lettuceOptionsRegistry.put(options.getId(), options);
    }

    public Collection<String> getLettuceOptionsNames() {
        return this.lettuceOptionsRegistry.keySet();
    }

    public Collection<LettuceOptions> getLettuceOptionsMaps() {
        return this.lettuceOptionsRegistry.values();
    }

    public LettuceOptions getLettuceOptions(String id) {
        return this.lettuceOptionsRegistry.get(id);
    }

    public boolean hasLettuceOptions(String id) {
        return this.lettuceOptionsRegistry.containsKey(id);
    }

    public void addCacheOptions(MybatisCacheOptions options) {
        this.cacheOptionsRegistry.put(options.getId(), options);
    }

    public Collection<String> getCacheOptionsNames() {
        return this.cacheOptionsRegistry.keySet();
    }

    public Collection<MybatisCacheOptions> getCacheOptionsMaps() {
        return this.cacheOptionsRegistry.values();
    }

    public MybatisCacheOptions getCacheOptions(String id) {
        return this.cacheOptionsRegistry.get(id);
    }

    public boolean hasCacheOptions(String id) {
        return this.cacheOptionsRegistry.containsKey(id);
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
    }
}
