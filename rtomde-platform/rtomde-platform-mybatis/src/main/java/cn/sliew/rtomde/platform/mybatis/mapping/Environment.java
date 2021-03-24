package cn.sliew.rtomde.platform.mybatis.mapping;

import cn.sliew.rtomde.platform.mybatis.config.DatasourceOptions;
import cn.sliew.rtomde.platform.mybatis.config.LettuceOptions;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Environment {
    private final String id;
    /**
     * dataSourceId -> DataSource
     */
    private final ConcurrentMap<String, DataSource> dataSourceRegistry = new ConcurrentHashMap<>(2);

    private final ConcurrentMap<String, DatasourceOptions> datasourceOptionsRegistry = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<String, LettuceOptions> lettuceOptionsRegistry = new ConcurrentHashMap<>(2);

    private Environment(String id, Map<String, DatasourceOptions> dataSources, Map<String, LettuceOptions> lettuces) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' must not be null");
        }
        this.id = id;
        if (dataSources == null) {
            throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
        }
        this.datasourceOptionsRegistry.putAll(dataSources);
        this.lettuceOptionsRegistry.putAll(lettuces);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private ConcurrentMap<String, DatasourceOptions> datasources = new ConcurrentHashMap<>(2);
        private ConcurrentMap<String, LettuceOptions> lettuces = new ConcurrentHashMap<>(2);

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder datasource(String id, DatasourceOptions dataSource) {
            this.datasources.put(id, dataSource);
            return this;
        }

        public Builder lettuce(String id, LettuceOptions lettuce) {
            this.lettuces.put(id, lettuce);
            return this;
        }

        public Environment build() {
            return new Environment(this.id, this.datasources, this.lettuces);
        }

    }

    public String getId() {
        return this.id;
    }

    public DataSource getDataSource(String id) {
        return this.dataSourceRegistry.get(id);
    }

    public Map<String, DataSource> getDataSources() {
        return Collections.unmodifiableMap(dataSourceRegistry);
    }

    public Map<String, DatasourceOptions> getDatasourceOptions() {
        return Collections.unmodifiableMap(datasourceOptionsRegistry);
    }


    public Map<String, LettuceOptions> getLettuceOptions() {
        return Collections.unmodifiableMap(lettuceOptionsRegistry);
    }

}
