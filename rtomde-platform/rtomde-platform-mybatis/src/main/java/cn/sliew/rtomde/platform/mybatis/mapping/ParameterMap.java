package cn.sliew.rtomde.platform.mybatis.mapping;

import cn.sliew.rtomde.common.bytecode.BeanGenerator;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.type.TypeAliasRegistry;

import java.util.Collections;
import java.util.List;

public class ParameterMap {

    private final String id;
    private final Class<?> type;
    private final List<ParameterMapping> parameterMappings;

    private ParameterMap(String id, Class<?> type, List<ParameterMapping> parameterMappings) {
        this.id = id;
        this.type = type;
        this.parameterMappings = parameterMappings;
    }

    public static Builder builder(MybatisApplicationOptions application) {
        return new Builder(application);
    }

    public static class Builder {

        private MybatisApplicationOptions application;
        private String id;
        private String type;
        private List<ParameterMapping> parameterMappings;

        private Builder(MybatisApplicationOptions application) {
            this.application = application;
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
            TypeAliasRegistry typeAliasRegistry = application.getTypeAliasRegistry();
            if (!typeAliasRegistry.hasAlias(this.type)) {
                try (BeanGenerator paramBeanG = BeanGenerator.newInstance(this.getClass().getClassLoader())) {
                    paramBeanG.className(this.type);
                    for (ParameterMapping mapping : parameterMappings) {
                        paramBeanG.setgetter(mapping.getProperty(), mapping.getJavaType());
                    }
                    Class<?> typeClass = paramBeanG.toClass();
                    typeAliasRegistry.registerAlias(typeClass);

                }
            }
            return new ParameterMap(id, typeAliasRegistry.resolveAlias(this.type), parameterMappings);
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
