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

import java.io.Serializable;
import java.util.*;

public final class ParameterMap implements Serializable {

    private static final long serialVersionUID = 3499326686789985803L;

    private final String id;
    private final List<ParameterMapping> parameterMappings;

    private ParameterMap(String id, List<ParameterMapping> parameterMappings) {
        this.id = id;
        this.parameterMappings = parameterMappings;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private List<ParameterMapping> parameterMappings;

        public Builder() {
            this.parameterMappings = new LinkedList<>();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder parameterMapping(ParameterMapping... parameterMappings) {
            if (parameterMappings != null && parameterMappings.length > 0) {
                this.parameterMappings.addAll(Arrays.asList(parameterMappings));
            }
            return this;
        }

        public ParameterMap build() {
            return new ParameterMap(id, Collections.unmodifiableList(parameterMappings));
        }
    }

    public String getId() {
        return id;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

    public boolean containsParameter(String property) {
        return parameterMappings.stream()
                .filter(mapping -> mapping.getProperty().equals(property))
                .findAny()
                .isPresent();
    }

    public ParameterMapping getParameterMapping(String property) {
        return parameterMappings.stream()
                .filter(mapping -> mapping.getProperty().equals(property))
                .findAny()
                .get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterMap that = (ParameterMap) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(parameterMappings, that.parameterMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parameterMappings);
    }

    @Override
    public String toString() {
        return "ParameterMap{" +
                "id='" + id + '\'' +
                ", parameterMappings=" + parameterMappings +
                '}';
    }
}
