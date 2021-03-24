package org.apache.ibatis.mapping;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

public class ResultMapping {

    private final Configuration configuration;
    private final String property;
    private final Class<?> javaType;
    private final String column;
    private final JdbcType jdbcType;
    private final TypeHandler<?> typeHandler;

    private ResultMapping(Configuration configuration, String property, Class<?> javaType, String column, JdbcType jdbcType, TypeHandler<?> typeHandler) {
        this.configuration = configuration;
        this.property = property;
        this.javaType = javaType;
        this.column = column;
        this.jdbcType = jdbcType;
        this.typeHandler = typeHandler;
    }

    public static Builder builder(Configuration configuration) {
        return new Builder(configuration);
    }

    public static class Builder {

        private Configuration configuration;
        private String property;
        private Class<?> javaType;
        private String column;
        private JdbcType jdbcType;
        private TypeHandler<?> typeHandler;

        private Builder(Configuration configuration) {
            this.configuration = configuration;
        }

        public Builder property(String property) {
            this.property = property;
            return this;
        }

        public Builder javaType(Class<?> javaType) {
            this.javaType = javaType;
            return this;
        }

        public Builder column(String column) {
            this.column = column;
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

        public ResultMapping build() {
            resolveTypeHandler();
            return new ResultMapping(configuration, property, javaType, column, jdbcType, typeHandler);
        }

        private void resolveTypeHandler() {
            if (this.typeHandler == null && this.javaType != null) {
                TypeHandlerRegistry typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
                this.typeHandler = typeHandlerRegistry.getTypeHandler(this.javaType, this.jdbcType);
            }
        }
    }

    public String getProperty() {
        return property;
    }

    public String getColumn() {
        return column;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResultMapping that = (ResultMapping) o;
        return property != null && property.equals(that.property);
    }

    @Override
    public int hashCode() {
        if (property != null) {
            return property.hashCode();
        } else if (column != null) {
            return column.hashCode();
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResultMapping{");
        //sb.append("configuration=").append(configuration); // configuration doesn't have a useful .toString()
        sb.append("property='").append(property).append('\'');
        sb.append(", column='").append(column).append('\'');
        sb.append(", javaType=").append(javaType);
        sb.append(", jdbcType=").append(jdbcType);
        //sb.append(", typeHandler=").append(typeHandler); // typeHandler also doesn't have a useful .toString()
        sb.append('}');
        return sb.toString();
    }

}
