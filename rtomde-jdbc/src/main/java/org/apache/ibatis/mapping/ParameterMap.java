package org.apache.ibatis.mapping;

import java.util.Collections;
import java.util.List;

public class ParameterMap {

    private final String id;
    private final String type;
    private final List<ParameterMapping> parameterMappings;

    private ParameterMap(String id, String type, List<ParameterMapping> parameterMappings) {
        this.id = id;
        this.type = type;
        this.parameterMappings = parameterMappings;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private String type;
        private List<ParameterMapping> parameterMappings;

        private Builder() {

        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder parameterMappings(List<ParameterMapping> parameterMappings) {
            this.parameterMappings = parameterMappings;
            return this;
        }


        public ParameterMap build() {
            //lock down collections
            this.parameterMappings = Collections.unmodifiableList(parameterMappings);
            return new ParameterMap(id, type, parameterMappings);
        }
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

}
