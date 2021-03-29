package cn.sliew.rtomde.platform.mybatis.mapping;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.type.JdbcType;
import cn.sliew.rtomde.platform.mybatis.type.TypeHandler;
import cn.sliew.rtomde.platform.mybatis.type.TypeHandlerRegistry;

public class ParameterMapping {

    private final MybatisApplicationOptions application;

    private String property;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private TypeHandler<?> typeHandler;
    private String jdbcTypeName;
    private String expression;

    public ParameterMapping(MybatisApplicationOptions application, String property, Class<?> javaType, JdbcType jdbcType, TypeHandler<?> typeHandler, String jdbcTypeName, String expression) {
        this.application = application;
        this.property = property;
        this.javaType = javaType;
        this.jdbcType = jdbcType;
        this.typeHandler = typeHandler;
        this.jdbcTypeName = jdbcTypeName;
        this.expression = expression;
    }

    public static Builder builder(MybatisApplicationOptions application) {
        return new Builder(application);
    }

    public static class Builder {

        private MybatisApplicationOptions application;

        private String property;
        private Class<?> javaType = Object.class;
        private JdbcType jdbcType;
        private TypeHandler<?> typeHandler;
        private String jdbcTypeName;
        private String expression;

        private Builder(MybatisApplicationOptions application) {
            this.application = application;
        }

        public Builder property(String property) {
            this.property = property;
            return this;
        }

        public Builder javaType(Class<?> javaType) {
            this.javaType = javaType;
            return this;
        }

        public Builder jdbcType(JdbcType jdbcType) {
            this.jdbcType = jdbcType;
            return this;
        }

        public Builder typeHandler(TypeHandler<?> typeHandler) {
            this.typeHandler = typeHandler;
            return this;
        }

        public Builder jdbcTypeName(String jdbcTypeName) {
            this.jdbcTypeName = jdbcTypeName;
            return this;
        }

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public ParameterMapping build() {
            resolveTypeHandler();
            validate();
            return new ParameterMapping(application, property, javaType, jdbcType, typeHandler, jdbcTypeName, expression);
        }

        private void validate() {
            if (this.typeHandler == null) {
                throw new IllegalStateException("Type handler was null on parameter mapping for property '"
                        + this.property + "'. It was either not specified and/or could not be found for the javaType ("
                        + this.javaType.getName() + ") : jdbcType (" + this.jdbcType + ") combination.");
            }
        }

        private void resolveTypeHandler() {
            if (this.typeHandler == null && this.javaType != null) {
                MybatisPlatformOptions platform = (MybatisPlatformOptions) this.application.getPlatform();
                TypeHandlerRegistry typeHandlerRegistry = platform.getTypeHandlerRegistry();
                this.typeHandler = typeHandlerRegistry.getTypeHandler(this.javaType, this.jdbcType);
            }
        }

    }

    public String getProperty() {
        return property;
    }

    /**
     * Used for handling output of callable statements.
     *
     * @return the java type
     */
    public Class<?> getJavaType() {
        return javaType;
    }

    /**
     * Used in the UnknownTypeHandler in case there is no handler for the property type.
     *
     * @return the jdbc type
     */
    public JdbcType getJdbcType() {
        return jdbcType;
    }

    /**
     * Used when setting parameters to the PreparedStatement.
     *
     * @return the type handler
     */
    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    /**
     * Used for handling output of callable statements.
     *
     * @return the jdbc type name
     */
    public String getJdbcTypeName() {
        return jdbcTypeName;
    }

    /**
     * Expression 'Not used'.
     *
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParameterMapping{");
        //sb.append("configuration=").append(configuration); // configuration doesn't have a useful .toString()
        sb.append("property='").append(property).append('\'');
        sb.append(", javaType=").append(javaType);
        sb.append(", jdbcType=").append(jdbcType);
        //sb.append(", typeHandler=").append(typeHandler); // typeHandler also doesn't have a useful .toString()
        sb.append(", jdbcTypeName='").append(jdbcTypeName).append('\'');
        sb.append(", expression='").append(expression).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
