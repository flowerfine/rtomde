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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class ResultMap implements Serializable {

    private static final long serialVersionUID = 6609679022627643229L;

    private final String id;
    private final List<ResultMapping> resultMappings;

    public ResultMap(String id, List<ResultMapping> resultMappings) {
        this.id = id;
        this.resultMappings = resultMappings;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private List<ResultMapping> resultMappings;

        private Builder() {
            this.resultMappings = new LinkedList<>();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder resultMappings(ResultMapping... resultMappings) {
            this.resultMappings.addAll(Arrays.asList(resultMappings));
            return this;
        }

        public ResultMap build() {
            return new ResultMap(id, resultMappings);
        }
    }

    public String getId() {
        return id;
    }

    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultMap resultMap = (ResultMap) o;
        return Objects.equals(id, resultMap.id) &&
                Objects.equals(resultMappings, resultMap.resultMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, resultMappings);
    }

    @Override
    public String toString() {
        return "ResultMap{" +
                "id='" + id + '\'' +
                ", resultMappings=" + resultMappings +
                '}';
    }
}
