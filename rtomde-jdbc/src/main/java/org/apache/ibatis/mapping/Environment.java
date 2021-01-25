package org.apache.ibatis.mapping;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Environment {
    private final String id;
    /**
     * dataSourceId -> DataSource
     */
    private final ConcurrentMap<String, DataSource> dataSourceRegistry = new ConcurrentHashMap<>(2);

    private Environment(String id, Map<String, DataSource> dataSources) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' must not be null");
        }
        this.id = id;
        if (dataSources == null) {
            throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
        }
        this.dataSourceRegistry.putAll(dataSources);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Map<String, DataSource> dataSources;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder dataSource(String id, DataSource dataSource) {
            this.dataSources.put(id, dataSource);
            return this;
        }

        public Environment build() {
            return new Environment(this.id, this.dataSources);
        }

    }

    public String getId() {
        return this.id;
    }

    public DataSource getDataSource(String id) {
        return this.dataSourceRegistry.get(id);
    }

}
