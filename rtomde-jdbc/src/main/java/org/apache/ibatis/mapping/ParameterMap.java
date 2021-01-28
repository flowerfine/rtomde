package org.apache.ibatis.mapping;

import cn.sliew.rtomde.common.bytecode.BeanGenerator;
import org.apache.ibatis.session.Configuration;

import java.util.Collections;
import java.util.List;

public class ParameterMap {

    private final Configuration configuration;

    private final String id;
    private final Class<?> type;
    private final List<ParameterMapping> parameterMappings;

    private ParameterMap(Configuration configuration, String id, Class<?> type, List<ParameterMapping> parameterMappings) {
        this.configuration = configuration;
        this.id = id;
        this.type = type;
        this.parameterMappings = parameterMappings;
    }

    public static Builder builder(Configuration configuration) {
        return new Builder(configuration);
    }

    public static class Builder {

        private Configuration configuration;
        private String id;
        private String type;
        private List<ParameterMapping> parameterMappings;

        private Builder(Configuration configuration) {
            this.configuration = configuration;
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
            try (BeanGenerator paramBeanG = BeanGenerator.newInstance(this.getClass().getClassLoader())) {
                paramBeanG.className(this.type);
                for (ParameterMapping mapping : parameterMappings) {
                    paramBeanG.setgetter(mapping.getProperty(), mapping.getJavaType());
                }
                Class<?> typeClass = paramBeanG.toClass();
                configuration.getTypeAliasRegistry().registerAlias(typeClass);
                return new ParameterMap(configuration, id, typeClass, parameterMappings);
            }
        }
    }

    public String getId() {
        return id;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

}
