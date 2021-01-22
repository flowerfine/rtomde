package cn.sliew.rtomde.bind;

import java.util.*;

public class ResultMap {

    private final String namespace;
    private final String id;
    private final String type;
    private final List<ResultMapping> resultMappings;
    private final Boolean autoMapping;

    private Set<String> mappedColumns;
    private Set<String> mappedProperties;

    private ResultMap(String namespace, String id, String type, List<ResultMapping> resultMappings, Boolean autoMapping, Set<String> mappedColumns, Set<String> mappedProperties) {
        this.namespace = namespace;
        this.id = id;
        this.type = type;
        this.resultMappings = resultMappings;
        this.autoMapping = autoMapping;
        this.mappedColumns = mappedColumns;
        this.mappedProperties = mappedProperties;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String namespace;
        private String id;
        private String type;
        private List<ResultMapping> resultMappings;
        private Boolean autoMapping;

        private Set<String> mappedColumns;
        private Set<String> mappedProperties;

        private Builder() {

        }

        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder type(String type) {
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
            if (id == null) {
                throw new IllegalArgumentException("ResultMaps must have an id");
            }
            mappedColumns = new HashSet<>();
            mappedProperties = new HashSet<>();
            for (ResultMapping resultMapping : resultMappings) {
                String column = resultMapping.getColumn();
                if (column != null) {
                    mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
                }
                String property = resultMapping.getProperty();
                if (property != null) {
                    mappedProperties.add(property);
                }
            }
            // lock down collections
            resultMappings = Collections.unmodifiableList(resultMappings);
            mappedColumns = Collections.unmodifiableSet(mappedColumns);
            mappedProperties = Collections.unmodifiableSet(mappedProperties);
            return new ResultMap(namespace, id, type, resultMappings, autoMapping, mappedColumns, mappedProperties);
        }
    }
}
