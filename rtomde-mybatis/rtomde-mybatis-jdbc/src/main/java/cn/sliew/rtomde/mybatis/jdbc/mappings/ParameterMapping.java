/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package cn.sliew.rtomde.mybatis.jdbc.mappings;

import cn.sliew.rtomde.mybatis.jdbc.type.JdbcType;
import cn.sliew.rtomde.mybatis.jdbc.type.TypeHandler;

import java.io.Serializable;
import java.util.Objects;

import static cn.sliew.milky.common.check.Ensures.checkNotNull;

public final class ParameterMapping implements Serializable {

    private static final long serialVersionUID = 178852889301910411L;

    private final String property;
    private final String jsonPath;
    private final Class<?> javaType;
    private final JdbcType jdbcType;
    private final TypeHandler<?> typeHandler;

    private ParameterMapping(String property,
                             String jsonPath,
                             Class<?> javaType,
                             JdbcType jdbcType,
                             TypeHandler<?> typeHandler) {
        checkNotNull(typeHandler, () -> String.format("Type handler was null on parameter mapping for property '%s'. " +
                "It was either not specified and/or could not be found for the javaType (%s) : jdbcType (%s) combination.",
                property, javaType.getName(), jdbcType));

        this.property = property;
        this.jsonPath = jsonPath;
        this.javaType = javaType;
        this.jdbcType = jdbcType;
        this.typeHandler = typeHandler;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String property;
        private String jsonPath;
        private Class<?> javaType;
        private JdbcType jdbcType;
        private TypeHandler<?> typeHandler;

        private Builder() {

        }

        public Builder property(String property) {
            this.property = property;
            return this;
        }

        public Builder jsonPath(String jsonPath) {
            this.jsonPath = jsonPath;
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

        public ParameterMapping build() {
            return new ParameterMapping(property, jsonPath, javaType, jdbcType, typeHandler);
        }
    }

    public String getProperty() {
        return property;
    }

    public String getJsonPath() {
        return jsonPath;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterMapping that = (ParameterMapping) o;
        return Objects.equals(property, that.property) &&
                Objects.equals(jsonPath, that.jsonPath) &&
                Objects.equals(javaType, that.javaType) &&
                jdbcType == that.jdbcType &&
                Objects.equals(typeHandler, that.typeHandler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, jsonPath, javaType, jdbcType, typeHandler);
    }

    @Override
    public String toString() {
        return "ParameterMapping{" +
                "property='" + property + '\'' +
                ", jsonPath='" + jsonPath + '\'' +
                ", javaType=" + javaType +
                ", jdbcType=" + jdbcType +
                ", typeHandler=" + typeHandler +
                '}';
    }
}
