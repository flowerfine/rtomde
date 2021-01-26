package org.apache.ibatis.mapping;

import org.apache.ibatis.session.Configuration;

import java.util.*;

public class ResultMap {
    private final Configuration configuration;

    private final String id;
    private final Class<?> type;
    private final List<ResultMapping> resultMappings;
    private final Set<String> mappedColumns;
    private final Set<String> mappedProperties;
    private final Boolean autoMapping;

    public ResultMap(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings, Set<String> mappedColumns, Set<String> mappedProperties, Boolean autoMapping) {
        this.configuration = configuration;
        this.id = id;
        this.type = type;
        this.resultMappings = resultMappings;
        this.mappedColumns = mappedColumns;
        this.mappedProperties = mappedProperties;
        this.autoMapping = autoMapping;
    }

    public static Builder builder(Configuration configuration) {
        return new Builder(configuration);
    }

    public static class Builder {
        private Configuration configuration;

        private String id;
        private Class<?> type;
        private List<ResultMapping> resultMappings;
        private Set<String> mappedColumns;
        private Set<String> mappedProperties;
        private Boolean autoMapping;

        public Builder(Configuration configuration) {
            this.configuration = configuration;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder type(Class<?> type) {
            this.type = type;
            return this;
        }

        public Builder resultMappings(List<ResultMapping> resultMappings) {
            this.resultMappings = resultMappings;
            return this;
        }

        public Builder autoMapping(Boolean autoMapping) {
            this.autoMapping = autoMapping;
            return this;
        }

        public ResultMap build() {
            if (this.id == null) {
                throw new IllegalArgumentException("ResultMaps must have an id");
            }
            this.mappedColumns = new HashSet<>();
            this.mappedProperties = new HashSet<>();
            for (ResultMapping resultMapping : this.resultMappings) {
                String column = resultMapping.getColumn();
                if (column != null) {
                    this.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
                }
                String property = resultMapping.getProperty();
                if (property != null) {
                    this.mappedProperties.add(property);
                }
            }
            // lock down collections
            this.resultMappings = Collections.unmodifiableList(resultMappings);
            this.mappedColumns = Collections.unmodifiableSet(mappedColumns);
            return new ResultMap(configuration, id, type, resultMappings, mappedColumns, mappedProperties, autoMapping);
        }
    }

    public String getId() {
        return id;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }

    public Set<String> getMappedColumns() {
        return mappedColumns;
    }

    public Set<String> getMappedProperties() {
        return mappedProperties;
    }

    public Boolean getAutoMapping() {
        return autoMapping;
    }

}
