package cn.sliew.rtomde.mybatis.jdbc.mappings;

import cn.sliew.milky.log.Logger;

import java.io.Serializable;
import java.util.Objects;

public final class MappedStatement implements Serializable {

    private static final long serialVersionUID = -6276490913497439407L;

    private final String id;
    private final Integer timeout;
    private final StatementType statementType;
    private final SqlCommandType sqlCommandType;
    private final SqlSource sqlSource;
    private final ParameterMap parameterMap;
    private final ResultMap resultMap;
    private final Logger statementLog;
    private final LanguageDriver lang;

    private MappedStatement(String id,
                            Integer timeout,
                            StatementType statementType,
                            SqlCommandType sqlCommandType,
                            SqlSource sqlSource,
                            ParameterMap parameterMap,
                            ResultMap resultMap,
                            Logger statementLog,
                            LanguageDriver lang) {
        this.id = id;
        this.timeout = timeout;
        this.statementType = statementType;
        this.sqlCommandType = sqlCommandType;
        this.sqlSource = sqlSource;
        this.parameterMap = parameterMap;
        this.resultMap = resultMap;
        this.statementLog = statementLog;
        this.lang = lang;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private Integer timeout;
        private StatementType statementType;
        private SqlCommandType sqlCommandType;
        private SqlSource sqlSource;
        private ParameterMap parameterMap;
        private ResultMap resultMap;
        private Logger statementLog;
        private LanguageDriver lang;

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

        public Builder sqlSource(SqlSource sqlSource) {
            this.sqlSource = sqlSource;
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

        public Builder lang(LanguageDriver lang) {
            this.lang = lang;
            return this;
        }

        public MappedStatement build() {
            return new MappedStatement(id, timeout, statementType, sqlCommandType, sqlSource, parameterMap, resultMap, statementLog, lang);
        }
    }

    public String getId() {
        return id;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public SqlSource getSqlSource() {
        return sqlSource;
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

    public LanguageDriver getLang() {
        return lang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MappedStatement that = (MappedStatement) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(timeout, that.timeout) &&
                statementType == that.statementType &&
                sqlCommandType == that.sqlCommandType &&
                Objects.equals(sqlSource, that.sqlSource) &&
                Objects.equals(parameterMap, that.parameterMap) &&
                Objects.equals(resultMap, that.resultMap) &&
                Objects.equals(statementLog, that.statementLog) &&
                Objects.equals(lang, that.lang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timeout, statementType, sqlCommandType, sqlSource, parameterMap, resultMap, statementLog, lang);
    }

    @Override
    public String toString() {
        return "MappedStatement{" +
                "id='" + id + '\'' +
                ", timeout=" + timeout +
                ", statementType=" + statementType +
                ", sqlCommandType=" + sqlCommandType +
                ", sqlSource=" + sqlSource +
                ", parameterMap=" + parameterMap +
                ", resultMap=" + resultMap +
                ", statementLog=" + statementLog +
                ", lang=" + lang +
                '}';
    }
}
