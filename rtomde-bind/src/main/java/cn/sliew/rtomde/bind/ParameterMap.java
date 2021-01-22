package cn.sliew.rtomde.bind;

import java.util.Collections;
import java.util.List;

public class ParameterMap {

    private final String namespace;
    private final String id;
    private final String type;
    private final List<ParameterMapping> parameterMappings;

    private ParameterMap(String namespace, String id, String type, List<ParameterMapping> parameterMappings) {
        this.namespace = namespace;
        this.id = id;
        this.type = type;
        this.parameterMappings = parameterMappings;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String namespace;
        private String id;
        private String type;
        private List<ParameterMapping> parameterMappings;

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

        public Builder parameterMappings(List<ParameterMapping> parameterMappings) {
            this.parameterMappings = parameterMappings;
            return this;
        }

        public ParameterMap build() {
            //lock down collections
            return new ParameterMap(namespace, id, type, Collections.unmodifiableList(parameterMappings));
        }
    }

}
