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
package cn.rtomde.template.mapping;

import cn.rtomde.template.type.JdbcType;
import cn.rtomde.template.type.TypeHandler;

import java.io.Serializable;
import java.util.Objects;

public final class ResultMapping implements Serializable {

    private static final long serialVersionUID = 3790840168722844746L;

    private final String property;
    private final Class<?> javaType;
    private final String column;
    private final JdbcType jdbcType;
    private final TypeHandler<?> typeHandler;

    private ResultMapping(String property,
                          Class<?> javaType,
                          String column,
                          JdbcType jdbcType,
                          TypeHandler<?> typeHandler) {
        this.property = property;
        this.javaType = javaType;
        this.column = column;
        this.jdbcType = jdbcType;
        this.typeHandler = typeHandler;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String property;
        private Class<?> javaType;
        private String column;
        private JdbcType jdbcType;
        private TypeHandler<?> typeHandler;

        private Builder() {

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
            return new ResultMapping(property, javaType, column, jdbcType, typeHandler);
        }
    }

    public String getProperty() {
        return property;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public String getColumn() {
        return column;
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
        ResultMapping that = (ResultMapping) o;
        return Objects.equals(property, that.property) &&
                Objects.equals(javaType, that.javaType) &&
                Objects.equals(column, that.column) &&
                jdbcType == that.jdbcType &&
                Objects.equals(typeHandler, that.typeHandler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, javaType, column, jdbcType, typeHandler);
    }

    @Override
    public String toString() {
        return "ResultMapping{" +
                "property='" + property + '\'' +
                ", javaType=" + javaType +
                ", column='" + column + '\'' +
                ", jdbcType=" + jdbcType +
                ", typeHandler=" + typeHandler +
                '}';
    }
}
