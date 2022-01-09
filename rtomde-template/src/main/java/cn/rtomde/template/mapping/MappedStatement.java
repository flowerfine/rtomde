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

import cn.rtomde.template.mapping.env.Environment;
import cn.sliew.milky.log.Logger;

import java.io.Serializable;

public final class MappedStatement implements Serializable {

    private static final long serialVersionUID = -6276490913497439407L;

    private Environment environment;

    private final String id;
    private final Integer timeout;
    private final StatementType statementType;
    private final SqlCommandType sqlCommandType;
    private final SqlTemplate sqlTemplate;
    private final ParameterMap parameterMap;
    private final ResultMap resultMap;
    private final Logger statementLog;

    private Integer fetchSize = 1000;
    private ResultSetType resultSetType = ResultSetType.FORWARD_ONLY;

    private MappedStatement(String id,
                            Integer timeout,
                            StatementType statementType,
                            SqlCommandType sqlCommandType,
                            SqlTemplate sqlTemplate,
                            ParameterMap parameterMap,
                            ResultMap resultMap,
                            Logger statementLog) {
        this.id = id;
        this.timeout = timeout;
        this.statementType = statementType;
        this.sqlCommandType = sqlCommandType;
        this.sqlTemplate = sqlTemplate;
        this.parameterMap = parameterMap;
        this.resultMap = resultMap;
        this.statementLog = statementLog;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private Integer timeout;
        private StatementType statementType;
        private SqlCommandType sqlCommandType;
        private SqlTemplate sqlTemplate;
        private ParameterMap parameterMap;
        private ResultMap resultMap;
        private Logger statementLog;

        private Builder() {

        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder statementType(StatementType statementType) {
            this.statementType = statementType;
            return this;
        }

        public Builder sqlCommandType(SqlCommandType sqlCommandType) {
            this.sqlCommandType = sqlCommandType;
            return this;
        }

        public Builder sqlTemplate(SqlTemplate sqlTemplate) {
            this.sqlTemplate = sqlTemplate;
            return this;
        }

        public Builder parameterMap(ParameterMap parameterMap) {
            this.parameterMap = parameterMap;
            return this;
        }

        public Builder resultMap(ResultMap resultMap) {
            this.resultMap = resultMap;
            return this;
        }

        public Builder statementLog(Logger statementLog) {
            this.statementLog = statementLog;
            return this;
        }

        public MappedStatement build() {
            return new MappedStatement(id, timeout, statementType, sqlCommandType, sqlTemplate, parameterMap, resultMap, statementLog);
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String getId() {
        return id;
    }

    public Integer getTimeout() {
        if (timeout == null) {
            return environment.getTimeout();
        }
        return timeout;
    }

    public Integer getFetchSize() {
        if (fetchSize == null) {
            return environment.getEtchSize();
        }
        return fetchSize;
    }

    public ResultSetType getResultSetType() {
        if (resultSetType == null) {
            return environment.getResultSetType();
        }
        return resultSetType;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }

    public ParameterMap getParameterMap() {
        return parameterMap;
    }

    public ResultMap getResultMap() {
        return resultMap;
    }

    public Logger getStatementLog() {
        return statementLog;
    }
}
